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

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.utils.AbstractState;

/**
 *
 * 
 */
public class AsyncRead extends AbstractState<ReadState, ServiceResultException> {

	IEncodeable msg;
	
	public AsyncRead() {
		super(ReadState.Waiting, ReadState.Error);
	}
	
	public IEncodeable getMessage()
	{
		return msg;
	}
	
	public synchronized void setError(ServiceResultException e) {
		assert(!getState().isFinal());
		super.setError(e);
	}
		
	public synchronized void attemptSetError(ServiceResultException e) {
		if (getState().isFinal()) return;
		super.setError(e);
	}
		
	public synchronized void setComplete(IEncodeable msg) {
		assert(!getState().isFinal());
		this.msg = msg;
		setState(ReadState.Complete);		
	}	
	
}
