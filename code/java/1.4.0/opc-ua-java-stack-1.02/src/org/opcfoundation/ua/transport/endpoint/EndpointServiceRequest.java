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

package org.opcfoundation.ua.transport.endpoint;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.builtintypes.ServiceResult;
import org.opcfoundation.ua.core.ServiceFault;
import org.opcfoundation.ua.transport.AsyncWrite;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.ServerSecureChannel;

/**
 * Asynchronous message exchange. 
 * 
 * A service request to be processed by a server (as opposed to service request queried by a client).
 * <p>
 * To send service error use sendResponse(new ServiceFault());
 * 
 */
public abstract class EndpointServiceRequest<Request extends ServiceRequest, Response extends ServiceResponse> {

	protected Server server;
	protected Endpoint endpoint;
	protected Request request;

	public EndpointServiceRequest(Request request, Server server, Endpoint endpoint)
	{
		this.request = request;
		this.server = server;
		this.endpoint = endpoint;
	}
	
	/**
	 * Get Request. The request is in Complete state.
	 *  
	 * @return read request
	 */
	public Request getRequest() {
		return request;
	}
	
	/**
	 * Get server
	 * 
	 * @return server
	 */
	public Server getServer() {
		return server;
	}
	
	/**
	 * Get endpoint
	 * 
	 * @return endpoint
	 */
	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	/**
	 * Send response.
	 *  
	 * @param response async write wrapping response or {@link ServiceFault}
	 */
	public abstract void sendResponse(AsyncWrite response);

	/**
	 * Send a response.
	 *
	 * @param response to send, either {@link ServiceFault} or {@link ServiceResult} 
	 * @return monitor for write status 
	 */
	public abstract AsyncWrite sendResponse(Response response);

	/**
	 * Send a service fault
	 * 
	 * @param fault error
	 */
	public void sendFault(ServiceFault fault) {
		sendResponse( new AsyncWrite( fault ) );
	}	
	
	/**
	 * Convert Throwable into an Service fault and send that to the client.
	 * NOTE! This is a convenience method that exposes stack trace to the client. 
	 * Use with care! 
	 * 
	 * @param e ServiceResultException or other
	 */
	public void sendException(Throwable e) {
		sendResponse( new AsyncWrite( ServiceFault.toServiceFault(e) ) );
	}
	
	public abstract ServerSecureChannel getChannel();
	
}
