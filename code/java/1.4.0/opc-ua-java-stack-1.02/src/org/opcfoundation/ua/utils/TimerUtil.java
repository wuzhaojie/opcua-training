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

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class TimerUtil {
	
	public static WeakReference<Timer> timer;
	
	public synchronized static Timer getTimer()
	{
		Timer t = timer!=null ? timer.get() : null;
		if (t==null)
		{
			t = new Timer("UA Timer", true);
			timer = new WeakReference<Timer>(t);
		}
		return t;
	}
	
	public static TimerTask schedule(final Runnable run, final Executor executor, long systemTime)
	{
		if (run==null || executor==null)
			throw new IllegalArgumentException("null arg");
		long delay = systemTime - System.currentTimeMillis();
		if (delay<1) delay = 1;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				executor.execute(run);
			}
			
			@Override
			public boolean cancel() {
				boolean result = super.cancel();				
				purge();	
				return result;
			}

		};
		getTimer().schedule(task, delay);
		return task;
	}
	
	/**
	 * 
	 */
	protected static void purge() {
		Timer t = timer!=null ? timer.get() : null;
		if (t!=null) 
			t.purge();
	}
	
	public static TimerTask schedule(Timer timer, final Runnable run, final Executor executor, long systemTime)
	{
		if (run==null || executor==null)
			throw new IllegalArgumentException("null arg");
		long delay = systemTime - System.currentTimeMillis();
		if (delay<1) delay = 1;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				executor.execute(run);
			}
			
			@Override
			public boolean cancel() {
				boolean result = super.cancel();				
				purge();	
				return result;
			}
		};
		timer.schedule(task, delay);
		return task;
	}	
}
