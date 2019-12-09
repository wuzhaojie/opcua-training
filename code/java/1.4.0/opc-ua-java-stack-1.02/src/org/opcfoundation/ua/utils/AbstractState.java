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

package org.opcfoundation.ua.utils;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is a default implementation to {@link IStatefulObject}.
 * This class can be subclassed or used as it. 
 * The state type is parametrized (typically an enumeration). 
 * 
 * TODO Remove locks - use spin set and test
 *
 * @see IStatefulObject
 * @see StateListener Listener for state modifications
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public abstract class AbstractState<StateType, ErrorType extends Throwable> implements IStatefulObject<StateType, ErrorType> {

	/** Current state */
	private StateType state = null;
	/** Optional error state */
	private StateType errorState = null;
	/** Error cause */
	private ErrorType errorCause;
	
	// Optimization for 1 listener, ListenerList is heavy //
	// TODO Replace with array that provides snapshots with-out instantiating new object
	private SnapshotArray<StateListener<StateType>> listenerList = null;
	private SnapshotArray<StateListener<StateType>> notifiableList = null;
	private Object lock = new Object();
	
	public AbstractState(StateType initialState)
	{
		state = initialState;
	}
	
	/**
	 * Creates a state with a error state. The state object goes to errorState on setError(). 
	 * 
	 * @param initialState
	 * @param errorState
	 */
	public AbstractState(StateType initialState, StateType errorState)
	{
		state = initialState;
		this.errorState = errorState;
	}
	
	@Override
	public synchronized StateType getState() {
		return state;
	}
	
	/**
	 * Attempts to change the state. The state will be changed only if current
	 * state is one of the expected states. 
	 * 
	 * @param prerequisiteState expected current state
	 * @param newState
	 * @return state after attempt
	 */
	protected StateType attemptSetState(Set<StateType> prerequisiteState, StateType newState)
	{
		if (prerequisiteState==null || newState==null)
			throw new IllegalArgumentException("null arg");
		return setState(newState, null, prerequisiteState);
	}

	/** 
	 * Add post-event notification listener. The prosessing thread is random. 
	 * The prosessing order is not guaranteed if the handling is not synchronized.
	 * 
	 * @param listener
	 */	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void addStateNotifiable(StateListener<StateType> listener) 
	{
		if (listener==null)  throw new IllegalArgumentException("null arg");
		if (notifiableList==null) notifiableList = (SnapshotArray<StateListener<StateType>>) new SnapshotArray(StateListener.class);
		notifiableList.add(listener);
	}
	
	public synchronized void removeStateNotifiable(StateListener<StateType> listener)
	{
		if (notifiableList!=null) notifiableList.remove(listener);
		if (notifiableList.isEmpty()) notifiableList = null;		
	}
	
	/**
	 * Add on-event listener.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void addStateListener(StateListener<StateType> listener) {
		if (listener==null)  throw new IllegalArgumentException("null arg");
		if (listenerList==null) listenerList = (SnapshotArray<StateListener<StateType>>) new SnapshotArray(StateListener.class);
		listenerList.add(listener);
		return;
	}

	@Override
	public synchronized void removeStateListener(StateListener<StateType> listener) {
		if (listener==null) throw new IllegalArgumentException("null arg");
		if (listenerList==null) return;
		listenerList.remove(listener);
		if (listenerList.isEmpty()) listenerList = null;
		return;
	}

	protected boolean setState(StateType state)
	{
		return setState(state, null, null) == state;
	}
	
	protected void setError(ErrorType error)
	{
		this.errorCause = error;
		if (errorState==null || !setState(errorState))
		{
			// wake up sleepers
			synchronized(lock) 
			{
				lock.notifyAll();
			}
		}
	}
	
	protected void clearError()
	{
		errorCause = null;		 
	}
	
	public ErrorType getError()
	{
		return errorCause;
	}
	
	public boolean hasError()
	{
		return errorCause!=null;
	}
	
	protected void assertNoError()
	throws ErrorType
	{
		ErrorType e = errorCause;		
		if (e!=null)
			throw e;
	}
	
	/**
	 * Set state
	 * 
	 * @param state
	 * @param listenerExecutor executor for post listener handling or null for immediate
	 * @param prerequisiteStates old state prerequisite or null 
	 * @return state after attempt
	 */
	protected StateType setState(StateType state, Executor listenerExecutor, Set<StateType> prerequisiteStates)
	{		
		if (listenerExecutor!=null && listenerExecutor instanceof CurrentThreadExecutor) listenerExecutor = null; 
		StateListener<StateType>[] listeners = null;
		StateListener<StateType>[] notifiables = null;
		StateType oldState = null;
		StateType newState = null;
		synchronized (this) {
			oldState = this.state;
			newState = state;
			if (oldState==newState) return state;
			if (prerequisiteStates!=null && !prerequisiteStates.contains(this.state))
				return state;
			if (!isStateTransitionAllowed(oldState, newState))
				return state;

			this.state = newState;
			if (listenerList!=null) listeners = listenerList.getArray();
			if (notifiableList!=null) notifiables = notifiableList.getArray();
		}
		synchronized(lock) 
		{
			lock.notifyAll();
		}
		// Threads wake up here...
		
		// Handle listeners
		onStateTransition(oldState, newState);
		
		final StateType os = oldState;
		final StateType ns = newState;
		if (listeners!=null && listenerExecutor==null) {
			for (final StateListener<StateType> sl : listeners) { 
				try {
					sl.onStateTransition(AbstractState.this, os, ns);
				} catch (RuntimeException e) {
					onListenerException(e);
				}
			}			
		}
		
		if ( (listeners!=null && listenerExecutor!=null) ) {		
			for (final StateListener<StateType> sl : listeners) { 
				Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						sl.onStateTransition(AbstractState.this, os, ns);
					} catch (RuntimeException e) {
						onListenerException(e);
					}
				}};
				listenerExecutor.execute(runnable);
			}
		}
		
		if ( notifiables!=null ) {		
			for (final StateListener<StateType> sl : notifiables) { 
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							sl.onStateTransition(AbstractState.this, os, ns);
						} catch (RuntimeException e) {
							onListenerException(e);
						}
					}};
				StackUtils.BLOCKING_EXECUTOR.execute(runnable);
			}
		}
		
		return state;
	}
	
	/**
	 * Checks whether state transition is allowed.
	 * Override this
	 * 
	 * @param oldState
	 * @param newState
	 * @return true if state transition is allowed
	 */
	protected boolean isStateTransitionAllowed(StateType oldState, StateType newState)
	{
		return true;
	}
	
	/**
	 * Override this.
	 * 
	 * @param oldState
	 * @param newState
	 */
	protected void onStateTransition(StateType oldState, StateType newState)
	{		
	}

	@Override
	public StateType waitForState(Set<StateType> set) 
	throws InterruptedException, ErrorType
	{
		// This impl makes unnecessary wakeups but is memory conservative		
		synchronized(lock) {
			while (!set.contains(state))
				lock.wait();
			ErrorType e = getError();
			if (e!=null)
				throw e;
			return state;
		}
	}

	public StateType waitForStateUninterruptibly(Set<StateType> set) 
	throws ErrorType
	{
		// This impl makes unnecessary wakeups but is memory conservative		
		synchronized(lock) {
			while (!set.contains(state))
				try {
					lock.wait();
				} catch (InterruptedException qwer) {}
			ErrorType e = getError();
			if (e!=null)
				throw e;
			return state;
		}
	}

	@Override
	public StateType waitForState(
			Set<StateType> set, 
			long timeout,
			TimeUnit unit) 
	throws InterruptedException, TimeoutException, ErrorType {
		long abortTime = System.currentTimeMillis() + unit.toMillis(timeout);
		synchronized(lock) {
			while (!set.contains(state)) {
				long waitTime = System.currentTimeMillis() - abortTime;
				if (waitTime<0)
					throw new TimeoutException("timeout");
				lock.wait(waitTime);
				ErrorType e = getError();
				if (e!=null)
					throw e;
			}
			return state;
		}		
	}
	
	/**
	 * Override this.
	 * @param rte
	 */
	protected void onListenerException(RuntimeException rte)
	{
		rte.printStackTrace();
	}

}
