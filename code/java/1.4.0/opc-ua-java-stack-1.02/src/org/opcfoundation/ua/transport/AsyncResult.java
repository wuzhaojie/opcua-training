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

package org.opcfoundation.ua.transport;

import java.util.concurrent.TimeUnit;

import org.opcfoundation.ua.common.ServiceResultException;

/**
 * Asynchronous result is a multi-thread object that operates as a container
 * for a result. The result is either an error or the result object. 
 * 
 * The result can be
 *   blocked ({@link #waitForResult()}), 
 *   polled ({@link #getResult()} and {@link #getError()}), or 
 *   listened to {@link #setListener(ResultListener)}.
 * 
 * AsyncResult can be used from any thread and from multiple-thread.
 *  
 * @See AsyncResultImpl 
 */
public interface AsyncResult<T> {
	
	/**
	 * Set a listener. If the result is already available, the listener
	 * is invoked from the current thread.  
	 * 
	 * Otherwise, the notification is placed from the thread that sets
	 * the result. The listener implementation may not create any locks
	 * during the handling of the result, also it is good policy not do
	 * any long term operations in the listener implementation.  
	 *  
	 * @param listener (listener may not block) or null to remove listener
	 */
	void setListener(ResultListener<T> listener);
	
	/**
	 * Get result if available
	 * 
	 * @return result or null if not complete
	 * @throws ServiceResultException;
	 */
	T getResult()
	throws ServiceResultException;
	
	ServiceResultException getError(); 
	
	/**
	 * Get request status
	 * 
	 * @return status
	 */
	AsyncResultStatus getStatus(); 
	
	/**
	 * Wait for result until result is available. 
	 * 
	 * Typically result becomes available in a default operation time out period. (120s)
	 * 
	 * @return the result
	 * @throws ServiceResultException network error, e.g. IOException of MethodNotSupportedException
	 * @throws RuntimeInterruptedException the user interrupted the operation with {@link Thread#interrupt()}
	 */
	T waitForResult() 
	throws ServiceResultException;
	
	/**
	 * Wait for result or time out. On timeout ServiceResultException(Bad_Timeout) is thrown.
	 * 
	 * @param timeout time out value
	 * @param unit time unit
	 * @return the result
	 * @throws ServiceResultException error during invocation  
	 * @throws RuntimeInterruptedException the user interrupted the operation with {@link Thread#interrupt()}
	 */
	T waitForResult(long timeout, TimeUnit unit)
	throws ServiceResultException;

	public static enum AsyncResultStatus {Waiting, Succeed, Failed}
	
}
