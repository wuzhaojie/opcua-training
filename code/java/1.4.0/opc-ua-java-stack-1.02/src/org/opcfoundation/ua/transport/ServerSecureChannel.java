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

import java.util.Collection;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

/**
 * Server side Secure channel.
 */
public interface ServerSecureChannel {
	
	/** Get Local Application Instance Certificate 
	 * 
	 * @return Local Application Instance Certificate 
	 * */
	KeyPair getLocalCertificate();
	
	/** Get Remote Application Instance Certificate 
	 * 
	 * @return Remote Application Instance Certificate 
	 * */
	Cert getRemoteCertificate();

	/**
	 * Get secure channel ID
	 * 
	 * @return secure channel id
	 */
	int getSecureChannelId();
	
	/**
	 * Get current socket connection if applicable for the binding type.
	 * 
	 * @return connection or null
	 */
	ServerConnection getConnection();

	/**
	 * Get message security mode.
	 * 
	 * @return security mode or null
	 */
	MessageSecurityMode getMessageSecurityMode();
	
	/**
	 * Get security policy
	 *  
	 * @return security policy or null if channel has not been initialized
	 */
	SecurityPolicy getSecurityPolicy();

	/**
	 * Return the URL of the connection. 
	 * This value is only available when the channel is in
	 * Open or Closing state, if not the return value is null.
	 * 
	 * @return connect URL or null
	 */
	String getConnectURL();	
	
	/**
	 * Is the secure channel open.
	 * 
	 * @return true if the channel is open
	 */
	boolean isOpen();
	
	/**
	 * Close the secure channel. This method does nothing if the channel is 
	 * already closed or has never been opened. <p>
	 * 
	 * This method sends CloseSecureChannelRequest to the server and 
	 * closes the socket connection. If sending of the message fails and thus
	 * the servers never receives notification about closed secure channel, then
	 * there is no resend attempt, instead the secure channel will eventually
	 * time out in the server. <p> 
	 * 
	 * All pending requests will fault with Bad_SecureChannelClosed <p>
	 */
	void close();
	
	/**
	 * Close the secure channel. This method does nothing if the channel is 
	 * already closed or has never been opened. <p>
	 * 
	 * This method sends CloseSecureChannelRequest to the server and 
	 * closes the socket connection. If sending of the message fails and thus
	 * the servers never receives notification about closed secure channel, then
	 * there is no resend attempt, instead the secure channel will eventually
	 * time out in the server. <p> 
	 * 
	 * All pending requests will fault with Bad_SecureChannelClosed <p>
	 * 
	 * @return asynchronous monitor object
	 */	
	AsyncResult<ServerSecureChannel> closeAsync();
	
	/**
	 * Close and dispose. The object becomes unusuable.
	 */
	void dispose();
	
	/**
	 * Get all unanswered service requests.
	 * 
	 * @param result container to fill with unanswered service requests
	 */
	void getPendingServiceRequests(Collection<EndpointServiceRequest<?, ?>> result);
	
	/**
	 * Get endpoint
	 * @return Endpoint
	 */
	Endpoint getEndpoint();
	
	/**
	 * Get Server
	 * @return server
	 */
	Server getServer();

	boolean needsCertificate();
	
}
