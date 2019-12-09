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
package org.opcfoundation.ua.transport.tcp.nio;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.AsyncWrite;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.ServerSecureChannel;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.tcp.impl.TcpMessageType;

public class PendingRequest extends EndpointServiceRequest<ServiceRequest, ServiceResponse> {
	
	OpcTcpServerSecureChannel channel;
	IEncodeable requestMessage;
	int requestId;
	AsyncWrite write;
	
	public PendingRequest(OpcTcpServerSecureChannel channel, Endpoint endpoint, Server server, int requestId, ServiceRequest requestMessage) {
		super(requestMessage, server, endpoint);
		this.channel = channel;
		this.requestId = requestId;
		this.requestMessage = requestMessage;
	}
	
	@Override
	public ServerSecureChannel getChannel() {
		return channel;
	}
	
	@Override
	public void sendResponse(AsyncWrite response) {
		channel.connection.pendingRequests.remove(requestId);
		channel.connection.sendSecureMessage(response, channel.getActiveSecurityToken(), requestId, TcpMessageType.MESSAGE, channel.sendSequenceNumber);
	}
	
	@Override
	public AsyncWrite sendResponse(ServiceResponse response) {
		write = new AsyncWrite(response);
		sendResponse(write);
		return write;
	}
	
}