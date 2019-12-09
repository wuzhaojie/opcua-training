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
import java.util.concurrent.Executor;

import org.opcfoundation.ua.utils.AbstractState;

/**
 * BufferMonitor is a monitor that triggers when a specific position is reached.
 * The position to monitor in AsyncSocketInputStream is the number of bytes 
 * received (buffered, not read) and in AsyncSocketOutputStream the number
 * of bytes flushed to TCP Stack.
 * <p>
 * User can set event listeners or wait for a state change.
 * E.g.
 *    // Block until stream has buffered 1000 bytes
 *    inputStream.
 *        createAlarm(inputStream.getPosition() + 1000, null).
 *        waitForState(AlarmState.FINAL_STATES); 
 * 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public abstract class BufferMonitor extends AbstractState<BufferMonitorState, IOException> implements Comparable<BufferMonitor> {

	long triggerPos;
	Executor eventExecutor;
		
	BufferMonitor(long triggerPos, Executor eventExecutor) {
		super(BufferMonitorState.Waiting, BufferMonitorState.Error);
		this.triggerPos = triggerPos;
		this.eventExecutor = eventExecutor;
	}
		
	public long getTriggerPos()
	{
		return triggerPos;
	}

	@Override
	public int compareTo(BufferMonitor o) {
		long diff = triggerPos - o.triggerPos;			
		return diff==0 ? 0 : (diff<0 ? -1 : 1);
	}
		
	public synchronized void cancel()
	{
		if (getState() != BufferMonitorState.Waiting) return;
		setState(BufferMonitorState.Canceled, eventExecutor, null);
	}
	
	protected synchronized void setError(IOException e)
	{
		super.setError(e);
//		if (getState() != BufferMonitorState.Waiting) return;
//		this.error = e;
//		super.setState(BufferMonitorState.Error, eventExecutor, null);
	}
	
	synchronized void close()
	{
		if (getState() != BufferMonitorState.Waiting) return;
		super.setState(BufferMonitorState.Closed, eventExecutor, null);		
	}
	
	synchronized void trigger()
	{
		if (getState() != BufferMonitorState.Waiting) return;
		super.setState(BufferMonitorState.Triggered, eventExecutor, null);		
	}
	
	
}
