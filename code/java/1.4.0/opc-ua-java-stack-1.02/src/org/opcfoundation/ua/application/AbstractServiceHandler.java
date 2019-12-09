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

package org.opcfoundation.ua.application;

import java.util.Collection;

import org.opcfoundation.ua.encoding.IEncodeable;

/**
 * Abstract implementation for a service handler that can server only
 * one type of service request;
 * 
 */
public abstract class AbstractServiceHandler implements ServiceHandler {

	Class<? extends IEncodeable> clazz;
	
	public AbstractServiceHandler(Class<? extends IEncodeable> clazz)
	{
		if (clazz==null)
			throw new IllegalArgumentException("null");
		this.clazz = clazz;
	}
	
	@Override
	public boolean supportsService(Class<? extends IEncodeable> clazz) {
		return clazz.equals(this.clazz);
	}

	@Override
	public void getSupportedServices(Collection<Class<? extends IEncodeable>> result) {
		result.add(clazz);
	}

}
