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

import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

/**
 * Client's SecureChannel connection. <p>
 * 
 * If the connection fails, and the transport channel is stateful (TCP), and 
 * the secure channel has pending service requests, it attempts to reconnect the 
 * transport channel. If the reconnect fails there is a timeout sequence
 * of the following wait periods { 0, 1, 2, 4, 8, 16, 32, 64, 120, 120, ... }.<p>
 * 
 * If error recovery mode fails to re-establish new security token before the old
 * expires, the secure channel will be closed. 
 */
public interface SecureChannel extends RequestChannel {

	/**
	 * Initialize a secure channel with endpoint identified by the URL.
	 * 
	 * @param connectUrl connect url
	 * @param settings
	 */
	void initialize(String connectUrl, TransportChannelSettings settings, EncoderContext ctx) throws ServiceResultException;

	/**
	 * Initialize a secure channel with endpoint identified by the URL.
	 * 
	 * @param settings
	 */
	void initialize(TransportChannelSettings settings, EncoderContext ctx) throws ServiceResultException;
	
	/**
	 * Open the secure channel with the endpoint identified by the URL.
	 * Once the channel is open if will be assigned a secure channel id.
	 * 
	 * The channel must be initialzied before hand.
	 * 
	 * If the operation timeouts or the thread is interrupted a 
	 * ServiceResultException is thrown with {@link StatusCodes#Bad_Timeout}.<p>
	 * 
	 * @throws ServiceResultException
	 */
	void open() throws ServiceResultException;
	
	/**
	 * Open the secure channel asynchronously. 
	 *  
	 * @return async result 
	 */
	AsyncResult<SecureChannel> openAsync();

	/**
	 * Sends a request over the secure channel. <p>
	 *  
	 * If the operation timeouts or the thread is interrupted a 
	 * ServiceResultException is thrown with {@link StatusCodes#Bad_Timeout}.<p>
	 * 
	 * @param request
	 * @return
	 * @throws ServiceResultException
	 */
	ServiceResponse serviceRequest(ServiceRequest request) throws ServiceResultException;
	
	/**
	 * Asynchronous operation to send a request over the secure channel. 
	 * 
	 * @param request the request
	 * @return the result
	 */
	AsyncResult<ServiceResponse> serviceRequestAsync(ServiceRequest request);

	
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
	 * there is no resend attempt. The secure channel will eventually
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
	 * there is no resend attempt. The secure channel will eventually
	 * time out in the server. <p> 
	 * 
	 * All pending requests will fault with Bad_SecureChannelClosed <p>
	 * 
	 * @return asynchronous monitor object
	 */	
	AsyncResult<SecureChannel> closeAsync();
	
	/**
	 * Close and dispose. The object becomes unusuable.
	 */
	void dispose();

	EndpointDescription getEndpointDescription();
	
	EndpointConfiguration getEndpointConfiguration();

	EncoderContext getMessageContext();
	
	/**
	 * Set operation timeout
	 * 
	 * @param timeout in milliseconds
	 */
	void setOperationTimeout(int timeout);
	
	/**
	 * Get operation timeout
	 * 
	 * @return timeout in milliseconds
	 */
	int getOperationTimeout();
	
}
