/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.utils.asyncsocket;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opcfoundation.ua.utils.ObjectUtils;
import org.opcfoundation.ua.utils.State;

/**
 * Create asyncronous selector. Selector has one selector thread for each cpu
 * core in the system. SelectionKeys are listened with register method.
 * <p>
 * To close async selector, close its selector (getSelector().close()).
 * <p> 
 * AsyncSelector guarantees that selection event of a key is handled in one
 * thread at a time, and it the event handled accordingly, new selection events do not occur.
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class AsyncSelector implements Runnable {

	private enum SelectorState {Active, Disabled};
	private final static EnumSet<SelectorState> ENABLED_STATE = EnumSet.of(SelectorState.Active);
	private final ThreadGroup THREAD_GROUP = new ThreadGroup("Async Selector");

	/** Selector state */
	State<SelectorState>				state = new State<SelectorState>(SelectorState.Active);
	/** Selector */
	Selector							sel;
	/** Registered keys and their handlers */
	Map<SelectionKey, SelectListener>	map = new ConcurrentHashMap<SelectionKey, SelectListener>();
	Map<SelectionKey, Integer>			supposedIOP = new ConcurrentHashMap<SelectionKey, Integer>();
	/** Selector Thread */
	Thread								thread;

	/**
	 * Construct AsyncSelector with brand new selector 
	 * 
	 * @throws IOException
	 */
	public AsyncSelector() throws IOException {
		this(Selector.open());
	}
	
	/**
	 * Construct new AsyncSelector 
	 * 
	 * @param sel
	 * @throws IOException
	 */
	public AsyncSelector(Selector sel) throws IOException {
		this.sel = sel;		
		thread = new Thread(THREAD_GROUP, this, "Selector");
		thread.setDaemon(true);
		thread.start();
	}

	public Selector getSelector() {
		return sel;
	}

	/**
	 * Modify interest ops of a key. 
	 * 
	 * @param channel registered key
	 * @param interestOps new interest op set
	 */
	public void interestOps(SelectableChannel channel, int interestOps) 
			throws CancelledKeyException {
		if (channel != null) {
			SelectionKey key = channel.keyFor(sel);
			if (key == null)
				throw new IllegalArgumentException(
						"Key is not registered to channel");
			if (ObjectUtils.objectEquals(supposedIOP.put(key, interestOps),
					interestOps))
				return;
			try {
				disable();
				key.interestOps(interestOps);
			} finally {
				enable();
			}
		}
	}
	
	private void enable()
	{
		state.setState(SelectorState.Active);
	}
	
	/**
	 * Acquire select permission
	 */
	private void disable()
	{
		state.setState(SelectorState.Disabled);
		sel.wakeup();
	}

	/**
	 * Register a selection event handler to a selectable channel.
	 * <p> 
	 * selectEventListener is invoked by one thread at a time. 
	 * The rule of thumb is that the listener must not block.
	 * 
	 * Note! If channel is registered and closed, select event is invoked
	 * until the channel is unregistered. 
	 *  
	 * 
	 * @param channel
	 * @param ops initial interest ops See {@link SelectionKey}
	 * @param selectEventListener
	 * @throws ClosedChannelException
	 */
	public void register(SelectableChannel channel,
			int ops, SelectListener selectEventListener) throws ClosedChannelException {
		disable();
		try {
			// register blocks if any thread select()s
			synchronized(sel) {
				SelectionKey key = channel.register(sel, ops);
				supposedIOP.put(key, ops);
				map.put(key, selectEventListener);
			}
		} finally {
			enable();		
		}		
	}

	public void unregister(SelectableChannel channel) {
		SelectionKey key = channel.keyFor(sel);		
		if (key == null || !map.containsKey(key)) return;
		disable();
		try {
			synchronized(sel) {
				key.cancel();
				map.remove(key);
				supposedIOP.remove(key);
			}
		} finally {
			enable();		
		}		
			
	}
	
	public void close() 
	throws IOException {
		sel.close();
		try {
			state.setState(SelectorState.Active);
			sel.wakeup();
		} catch (Exception e) {}
	}
	
	public interface SelectListener {
		/**
		 * Event for selected key.
		 * 
		 * Note! InterestSet of key is set to 0, the implementor must return as
		 * a result of onSelection the new set of interest ops.
		 * 
		 * Selection of a key is handled in one thread.
		 * 
		 * @param sender
		 * @param channel channel that selected
		 * @param selectOps selected event operations (See {@link SelectionKey}) 
		 * @param interestOps previous interest ops
		 */
		public void onSelected(AsyncSelector sender, SelectableChannel channel, int selectOps, int interestOps);
	}
	
	String toStr(int i)
	{
		String res = "[";
		if ((i & SelectionKey.OP_READ) != 0)res += "read";
		if ((i & SelectionKey.OP_CONNECT) != 0)res += "connect";
		if ((i & SelectionKey.OP_WRITE) != 0)res += "write";
		if ((i & SelectionKey.OP_ACCEPT) != 0)res += "accept";
		return res+"]";
	}

	@Override
	public void run() {
		try {
			while (sel.isOpen()) {
				state.waitForStateUninterruptibly(ENABLED_STATE);
				sel.select(1000);
				
				Set<SelectionKey> selectedKeys = sel.selectedKeys();
				for (SelectionKey key : selectedKeys) 
					try {
						key.interestOps(0);
					} catch(CancelledKeyException e) {/*ignore*/}
				
				for (SelectionKey key : selectedKeys) 
					try {
						Integer iop = supposedIOP.get(key);
						if (iop==null) continue;
						int readyOps = key.readyOps() /*| (iop & SelectionKey.OP_WRITE)*/;
						SelectListener l = map.get(key);
						if (l != null) l.onSelected(AsyncSelector.this, key.channel(), readyOps, iop);						
					} catch(CancelledKeyException e) {/*ignore*/}
					
				for (SelectionKey key : supposedIOP.keySet()) 
					try {
						Integer iop = supposedIOP.get(key);
						if (iop==null) continue;
						key.interestOps(iop);
					} catch(CancelledKeyException e) {/*ignore*/}					
					
				selectedKeys.clear();
			}
			
		} catch (ClosedSelectorException cse) {
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}

}
