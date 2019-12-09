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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public interface IStatefulObject<StateType, ErrorType extends Throwable> {

	/** 
	 * Add post-event notification listener. The prosessing thread is random. 
	 * The prosessing order is not guaranteed if the handling is not synchronized.
	 * 
	 * @param listener
	 */	
	void addStateNotifiable(StateListener<StateType> notifiable);
	void removeStateNotifiable(StateListener<StateType> notifiable);

	/** 
	 * Add on-event listener. 
	 * 
	 * @param listener
	 */	
	void addStateListener(StateListener<StateType> listener);
	void removeStateListener(StateListener<StateType> listener);
	
	StateType getState();
	
	/**
	 * Wait until state changes to one of the given states.
	 * 
	 * @param set states that ends waiting  
	 * @throws InterruptedException
	 * @return the state in the given set that broke the wait 
	 */
	StateType waitForState(Set<StateType> set) 
	throws InterruptedException, ErrorType;
	
	/**
	 * Wait until state changes to one of the given states.
	 * 
	 * @param set states that ends waiting  
	 * @return the state in the given set that broke the wait 
	 */
	StateType waitForStateUninterruptibly(Set<StateType> set) 
	throws ErrorType;	
	
	
	/**
	 * Wait until state changes to one of the given states or until
	 * time out occurs. 
	 * 
	 * @param set
	 * @param timeout
	 * @param unit
	 * @return state one in set
	 * @throws InterruptedException thread was interrupted
	 * @throws TimeoutException timeout occured
	 */
	StateType waitForState(Set<StateType> set, long timeout, TimeUnit unit) 
	throws InterruptedException, TimeoutException, ErrorType;

	/**
	 * Get error state or null
	 * @return error
	 */
	ErrorType getError();
	
}
