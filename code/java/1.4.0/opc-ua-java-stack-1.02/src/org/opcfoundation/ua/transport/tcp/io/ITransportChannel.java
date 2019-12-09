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

package org.opcfoundation.ua.transport.tcp.io;

import java.util.EnumSet;

import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.AsyncResult;
import org.opcfoundation.ua.transport.TransportChannelSettings;

public interface ITransportChannel {
	
	/**
	 * Initialize a secure channel with endpoint identified by the URL.
	 * 
	 * @param url
	 * @param settings
	 */
	void initialize(String url, TransportChannelSettings settings, EncoderContext ctx) throws ServiceResultException;
	
	/**
	 * Send a service request over the secure channel. <p>
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
	 * Send a service request over the secure channel. <p>
	 *  
	 * If the operation timeouts or the operation is interrupted and a 
	 * ServiceResultException is thrown with {@link StatusCodes#Bad_Timeout}.<p>
	 * 
	 * @param request
	 * @param operationTimeout timeout time in milliseconds
	 * @return
	 * @throws ServiceResultException
	 */
	ServiceResponse serviceRequest(ServiceRequest request, long operationTimeout) throws ServiceResultException;
	
	/**
	 * Asynchronous operation to send a request over the secure channel. 
	 * 
	 * @param request the request
	 * @param operationTimeout timeout time
	 * @return the result
	 */
	AsyncResult<ServiceResponse> serviceRequestAsync(ServiceRequest request, long operationTimeout);
	
	/**
	 * Get a list of features supported by the channel. 
	 * 
	 * @return
	 */
	EnumSet<TransportChannelFeature> getSupportedFeatures();
	public enum TransportChannelFeature { open, openAsync, reconnect, reconnectAsync, sendRequest, sendRequestAsync, close, closeAync }	

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
	
	void dispose();
	
}
