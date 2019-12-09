/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.transport.endpoint;

import com.prosysopc.ua.application.Server;
import com.prosysopc.ua.builtintypes.DateTime;
import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.builtintypes.ServiceResponse;
import com.prosysopc.ua.builtintypes.ServiceResult;
import com.prosysopc.ua.core.ServiceFault;
import com.prosysopc.ua.transport.AsyncWrite;
import com.prosysopc.ua.transport.Endpoint;
import com.prosysopc.ua.transport.ServerSecureChannel;
import com.prosysopc.ua.transport.security.SecurityMode;

/**
 * Asynchronous message exchange.
 *
 * A service request to be processed by a server (as opposed to service request queried by a client).
 * <p>
 * To send service error use sendResponse(new ServiceFault());
 */
public abstract class EndpointServiceRequest<Request extends ServiceRequest, Response extends ServiceResponse> {

	protected Server server;
	protected Endpoint endpoint;
	protected Request request;
	protected DateTime receiveTimestamp;

	/**
	 * <p>Constructor for EndpointServiceRequest.</p>
	 *
	 * @param request a Request object.
	 * @param server a {@link Server} object.
	 * @param endpoint a {@link Endpoint} object.
	 */
	public EndpointServiceRequest(Request request, Server server, Endpoint endpoint) {
		this.request = request;
		this.server = server;
		this.endpoint = endpoint;

		//Best-effort solution, should be quite close.
		this.receiveTimestamp = DateTime.currentTime();
	}
	
	/**
	 * Server-side timestamp when the {@link #getRequest()} was obtained. 
	 */
	public DateTime getReceiveTimestamp() {
		return receiveTimestamp;
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
	 * A SecurityMode. Only relevant when as part of the CreateSessionRequest/Response.
	 */
	public abstract SecurityMode getSecurityMode();
	
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
	
	/**
	 * <p>getChannel.</p>
	 *
	 * @return a {@link ServerSecureChannel} object.
	 */
	public abstract ServerSecureChannel getChannel();
	
}
