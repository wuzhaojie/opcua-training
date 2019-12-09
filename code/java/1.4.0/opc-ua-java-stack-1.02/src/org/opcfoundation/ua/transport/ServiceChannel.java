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
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

/**
 * This utility class envelopes Securechannel with client service methods.
 * See {@link ChannelService}.
 */
public class ServiceChannel extends ChannelService implements SecureChannel {

	SecureChannel channel;
	
	public ServiceChannel(SecureChannel channel) {
		super(channel);
		this.channel = channel;
	}
	
	@Override
	public void close() {
		channel.close();
	}

	@Override
	public AsyncResult<SecureChannel> closeAsync() {
		return channel.closeAsync();
	}

	@Override
	public void dispose() {
		channel.dispose();		
	}

	@Override
	public String getConnectURL() {
		return channel.getConnectURL();
	}

	@Override
	public ServerConnection getConnection() {
		return channel.getConnection();
	}

	@Override
	public EndpointConfiguration getEndpointConfiguration() {
		return channel.getEndpointConfiguration();
	}

	@Override
	public EndpointDescription getEndpointDescription() {
		return channel.getEndpointDescription();
	}

	@Override
	public EncoderContext getMessageContext() {
		return channel.getMessageContext();
	}

	@Override
	public MessageSecurityMode getMessageSecurityMode() {
		return channel.getMessageSecurityMode();
	}

	@Override
	public int getOperationTimeout() {
		return channel.getOperationTimeout();
	}

	@Override
	public int getSecureChannelId() {
		return channel.getSecureChannelId();
	}

	@Override
	public SecurityPolicy getSecurityPolicy() {
		return channel.getSecurityPolicy();
	}

	@Override
	public void initialize(String url, TransportChannelSettings settings, EncoderContext ctx)
			throws ServiceResultException {
		channel.initialize(url, settings, ctx);
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void open() throws ServiceResultException {
		channel.open();
	}

	@Override
	public AsyncResult<SecureChannel> openAsync() {
		return channel.openAsync();
	}

	@Override
	public ServiceResponse serviceRequest(ServiceRequest request)
			throws ServiceResultException {
		return channel.serviceRequest(request);
	}

	@Override
	public AsyncResult<ServiceResponse> serviceRequestAsync(ServiceRequest request) {
		return channel.serviceRequestAsync(request);
	}

	@Override
	public void setOperationTimeout(int timeout) {
		channel.setOperationTimeout(timeout);
	}

	@Override
	public void initialize(TransportChannelSettings settings, EncoderContext ctx)
			throws ServiceResultException {
		channel.initialize(settings, ctx);
	}

}
