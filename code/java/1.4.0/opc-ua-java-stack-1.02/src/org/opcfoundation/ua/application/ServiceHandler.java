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

import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;

/**
 * Service Handler reads {@link ServiceRequest} from client, processes it, and returns
 * a {@link ServiceResponse}. 
 * 
 * @see ServiceHandlerComposition
 * @see AbstractServiceHandler
 */
public interface ServiceHandler {

	/**
	 * Serve a service request. 
	 * <p>
	 * The implementation is allowed to may submit the response 
	 * later and from another thread.
	 * 
	 * @param request
	 * @throws ServiceResultException 
	 */
	void serve(EndpointServiceRequest<?, ?> request) throws ServiceResultException;

	/**
	 * Queries whether this handler supports a given request class.
	 *  
	 * @param requestMessageClass class
	 * @return true if this service handler can handle given class
	 */
	boolean supportsService(Class<? extends IEncodeable> requestMessageClass);
	
	/**
	 * Get supported services. Result will be filled with the request class of 
	 * the supported services. 
	 * 
	 * @param result to be filled with request classes of supported services. 
	 */
	void getSupportedServices(Collection<Class<? extends IEncodeable>> result);
	
	
}
