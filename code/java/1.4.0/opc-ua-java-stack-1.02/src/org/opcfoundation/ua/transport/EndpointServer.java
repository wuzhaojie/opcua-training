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

import java.net.SocketAddress;
import java.util.List;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.endpoint.EndpointBindingCollection;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.tcp.nio.OpcTcpServer;

/**
 * EndpointServer is an object that binds to a network socket and offers
 * hosting for endpoints.  
 * 
 * @see BindingFactory Instantiates UABindings
 * @see OpcTcpServer tcp.opc implementation 
 */
public interface EndpointServer extends CloseableObject, ConnectionMonitor {

	public static final Endpoint discoveryEndpoint = new Endpoint("", SecurityMode.ALL);
	
	
	/**
	 * Get a collection that contains related endpoints bindings.
	 * 
	 * @return endpoint collection
	 */
	EndpointBindingCollection getEndpointBindings();
	
	/**
	 * Bind an endpoint to a handle. 
	 * 
	 * @param socketAddress
	 * @param endpointBinding
	 * @throws ServiceResultException
	 */
	EndpointHandle bind(SocketAddress socketAddress, EndpointBinding endpointBinding) throws ServiceResultException;
	
	List<SocketAddress> getBoundSocketAddresses();

	EncoderContext getEncoderContext();

	interface EndpointHandle {
		SocketAddress socketAddress();
		EndpointBinding endpointBinding();
		void close();
	}
	
}
