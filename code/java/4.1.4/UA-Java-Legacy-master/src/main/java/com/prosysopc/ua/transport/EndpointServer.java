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

package com.prosysopc.ua.transport;

import com.prosysopc.ua.transport.endpoint.EndpointBindingCollection;
import com.prosysopc.ua.transport.security.SecurityMode;
import com.prosysopc.ua.transport.tcp.nio.OpcTcpServer;
import java.net.SocketAddress;
import java.util.List;

import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.encoding.EncoderContext;

/**
 * EndpointServer is an object that binds to a network socket and offers
 * hosting for endpoints.
 *
 * @see OpcTcpServer tcp.opc implementation
 */
public interface EndpointServer extends CloseableObject, ConnectionMonitor {

	/** Constant <code>discoveryEndpoint</code> */
	public static final Endpoint discoveryEndpoint = new Endpoint("", SecurityMode.NONE);
	
	
	/**
	 * Get a collection that contains related endpoints bindings.
	 *
	 * @return endpoint collection
	 */
	EndpointBindingCollection getEndpointBindings();
	
	/**
	 * Bind an endpoint to a handle.
	 *
	 * @param socketAddress a {@link java.net.SocketAddress} object.
	 * @param endpointBinding a {@link EndpointBinding} object.
	 * @throws ServiceResultException if any.
	 * @return a {@link EndpointServer.EndpointHandle} object.
	 */
	EndpointHandle bind(SocketAddress socketAddress, EndpointBinding endpointBinding) throws ServiceResultException;
	
	/**
	 * Binds a ReverseHello connection point. Server shall connect to the given addresstoConnect parameter 
	 * per the specification 1.04 Part 6 7.1.3, assuming the target is a ReverseHello-aware port.
	 */
	void bindReverse(SocketAddress addressToConnect, String endpointUrl) throws ServiceResultException;
	
	/**
	 * <p>getBoundSocketAddresses.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<SocketAddress> getBoundSocketAddresses();

	/**
	 * <p>getEncoderContext.</p>
	 *
	 * @return a {@link EncoderContext} object.
	 */
	EncoderContext getEncoderContext();

	interface EndpointHandle {
		SocketAddress socketAddress();
		EndpointBinding endpointBinding();
		void close();
	}
	
}
