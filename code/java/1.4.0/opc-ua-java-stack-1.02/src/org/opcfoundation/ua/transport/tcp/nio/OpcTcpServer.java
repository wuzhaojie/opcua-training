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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.EndpointBinding;
import org.opcfoundation.ua.transport.EndpointServer;
import org.opcfoundation.ua.transport.IConnectionListener;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.UriUtil;
import org.opcfoundation.ua.transport.endpoint.EndpointBindingCollection;
import org.opcfoundation.ua.transport.impl.ConnectionCollection;
import org.opcfoundation.ua.utils.AbstractState;
import org.opcfoundation.ua.utils.StackUtils;
import org.opcfoundation.ua.utils.asyncsocket.AsyncServerSocket;
import org.opcfoundation.ua.utils.asyncsocket.AsyncSocketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EndpointOpcTcp hosts an endpoint for opc.tcp socket. 
 * 
 * Endpoint discovery is provided if endpoint url is unknown.
 *
 * @see Executors for creating executor instances
 */
public class OpcTcpServer extends AbstractState<CloseableObjectState, ServiceResultException> implements EndpointServer {

	/** Logger */
	static Logger logger = LoggerFactory.getLogger(OpcTcpServer.class);
	/** Application */
	Application application;
	/** Secure channel counter */
	AtomicInteger secureChannelCounter = new AtomicInteger();
	/** Endpoint bindings */
	EndpointBindingCollection endpointBindings = new EndpointBindingCollection(); 

	/** Service server used when client connects with "" url for endpoint discovery */
	public Server discoveryServer;
	public EndpointBinding discoveryEndpointBinding;
	
	private int receiveBufferSize = 0;

	/** Endpoint handles */
	Map<SocketAddress, SocketHandle> socketHandles = new HashMap<SocketAddress, SocketHandle>();

	public EncoderContext getEncoderContext() {
		return application.getEncoderContext();
	}

	/**
	 * @return the receiveBufferSize to use for the connection socket of the server.
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * Define the receiveBufferSize to use for the connection socket of the server.
	 * <p>
	 * Default value: 0, which omits the parameter and the default value for the
	 * socket (depending on the operating system) is used.

	 * @param receiveBufferSize the new size in bytes
	 * @throws ServiceResultException
	 * @see http://fasterdata.es.net/host-tuning/background/ 
	 */
	public void setReceiveBufferSize(int receiveBufferSize) throws ServiceResultException {
		this.receiveBufferSize = receiveBufferSize;
		if (receiveBufferSize > 0) {
			for (SocketHandle sh : socketHandleSnapshot()) {
				try {
					AsyncServerSocket ass = sh.socket;
					if ( ass!=null ) {
						ass.socket().setReceiveBufferSize(receiveBufferSize);
					}
				} catch (SocketException e) {
					throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
				}
			}
		}
	}
	
	/** AsyncServerSocket Connect listener */
	org.opcfoundation.ua.utils.asyncsocket.AsyncServerSocket.ConnectListener connectListener = new org.opcfoundation.ua.utils.asyncsocket.AsyncServerSocket.ConnectListener() {		
		public void onConnected(AsyncServerSocket sender, AsyncSocketImpl newConnection) {
			logger.info("{}: {} connected", OpcTcpServer.this, newConnection.socket().getRemoteSocketAddress());
			final OpcTcpServerConnection conn = new OpcTcpServerConnection(OpcTcpServer.this, newConnection);
			connections.addConnection(conn);
			
			conn.addConnectionListener(new IConnectionListener() {
				@Override
				public void onClosed(ServiceResultException closeError) {						
						connections.removeConnection(conn);
				}
				@Override
				public void onOpen() {
				}});
			
		}};
	ConnectionCollection connections = new ConnectionCollection(this);	
	
	public OpcTcpServer(Application application) throws ServiceResultException
	{
		super(CloseableObjectState.Closed, CloseableObjectState.Closed);
		this.application = application;
		try {
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			
			// Create a service server for connections that query endpoints (url = "")
			discoveryServer = new Server( application );
			discoveryServer.setEndpointBindings( endpointBindings );
			discoveryEndpointBinding = new EndpointBinding(this, discoveryEndpoint, discoveryServer);	
		} catch (IOException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}
	}

	@Override
	public EndpointHandle bind(SocketAddress socketAddress, EndpointBinding endpointBinding) throws ServiceResultException {
		if ( endpointBinding == null || socketAddress == null || endpointBinding.endpointServer!=this )
			throw new IllegalArgumentException();
		
		String scheme = UriUtil.getTransportProtocol( endpointBinding.endpointAddress.getEndpointUrl() );
		if ( !"opc.tcp".equals(scheme) ) throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "Cannot bind "+scheme+" to opc.tcp server");
		SocketHandle socketHandle = getOrCreateSocketHandle(socketAddress);
		
		if ( socketHandle.socket == null ) {
			try {
				socketHandle.setChannel(ServerSocketChannel.open());
				socketHandle.getChannel().configureBlocking(false);
			
				socketHandle.socket = new AsyncServerSocket(
						socketHandle.getChannel(),
						StackUtils.getNonBlockingWorkExecutor()//,
						//new AsyncSelector(Selector.open())
						,StackUtils.getSelector()
						);
				socketHandle.socket.bind(socketHandle.socketAddress, 0);
				socketHandle.socket.addListener(connectListener);
				logger.info("TCP/IP Socket bound to {}", socketAddress);
			} catch (IOException e) {
				logger.error("Failed to bind address "+ socketHandle.socketAddress, e);
				socketHandle.close();
				throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
			}
			
		}
		
		OpcTcpEndpointHandle endpointHandle = socketHandle.getOrCreate(endpointBinding);
		return endpointHandle;
	}

	@Override
	public List<SocketAddress> getBoundSocketAddresses() {
		ArrayList<SocketAddress> result = new ArrayList<SocketAddress>();
		for (SocketHandle sh : socketHandleSnapshot()) result.add( sh.socketAddress );
		return result;
	}

	public SocketAddress getBoundAddress() {
		SocketHandle[] shs = socketHandleSnapshot();
		for (SocketHandle sh: shs) {
			if ( sh.socket!=null) return sh.socketAddress;
		}
		return null;
	}
	
	/**
	 * Disconnect all existing connections. 
	 */
	public void disconnectAll()
	{
		List<ServerConnection> list = new ArrayList<ServerConnection>();
		getConnections(list);
		for (ServerConnection connection : list) {
			OpcTcpServerConnection c = (OpcTcpServerConnection) connection;
			c.close();
		}
	}

	/**
	 * Closes server socket. Does not disconnect existing connections.
	 */
	@Override
	public synchronized OpcTcpServer close() {
		logger.info("{} closed", getBoundAddress());
		if (!getState().isClosed()) { 
			setState(CloseableObjectState.Closing);
		}
		
		try {
			for (SocketHandle sh : socketHandleSnapshot())
				sh.close();
		} finally {
			setState(CloseableObjectState.Closed);
		}
		return this;
	}

	@Override
	public void addConnectionListener(org.opcfoundation.ua.transport.ConnectionMonitor.ConnectListener l) {
		connections.addConnectionListener(l);
	}

	@Override
	public void getConnections(Collection<ServerConnection> result) {
		connections.getConnections(result);
	}

	@Override
	public void removeConnectionListener(org.opcfoundation.ua.transport.ConnectionMonitor.ConnectListener l) {
		connections.removeConnectionListener(l);
	}	
	
	@Override
	public EndpointBindingCollection getEndpointBindings() {
		return endpointBindings;
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OpcTcpServer");
		sb.append("(");
		for (SocketHandle sh : socketHandleSnapshot()) sb.append( sh.toString() );
		sb.append(")");
		return sb.toString();
	}

	synchronized SocketHandle getOrCreateSocketHandle(SocketAddress socketAddress)
	throws ServiceResultException
	{
		SocketHandle handle = socketHandles.get(socketAddress);
		if ( handle == null ) {
			handle = new SocketHandle(socketAddress);
			socketHandles.put(socketAddress, handle);
		}
		return handle;
	}
	
	public SocketHandle[] socketHandleSnapshot() {
		return socketHandles.values().toArray( new SocketHandle[ socketHandles.size()] );		
	}

	public class SocketHandle {
		SocketAddress socketAddress;
		/** Server Socket */
		AsyncServerSocket socket;
		private ServerSocketChannel channel;
		int port;
		Map<Endpoint, OpcTcpEndpointHandle> endpoints = new HashMap<Endpoint, OpcTcpEndpointHandle>();
		SocketHandle(SocketAddress socketAddress)
		{
			this.socketAddress = socketAddress;
		}
		
		public synchronized OpcTcpEndpointHandle[] endpointHandleSnapshot() {
			return endpoints.values().toArray( new OpcTcpEndpointHandle[ endpoints.size()] );
		}
		
		synchronized void endpointHandleSnapshot(Collection<OpcTcpEndpointHandle> handles) {
			handles.addAll( endpoints.values() );
		}
		
		synchronized OpcTcpEndpointHandle getOrCreate(EndpointBinding endpointBinding) throws ServiceResultException {
			OpcTcpEndpointHandle handle = endpoints.get(endpointBinding.endpointAddress);
			if ( handle == null ) {
				handle = new OpcTcpEndpointHandle(this, endpointBinding);
				// Add endpointBinding to SocketHandle
				endpoints.put(endpointBinding.endpointAddress, handle);
				// Add endpointBinding to EndpointServer
				endpointBindings.add(endpointBinding);
				// Add endpointBinding to Server
				endpointBinding.serviceServer.getEndpointBindings().add(endpointBinding);
			} else {
				if ( !handle.endpointBinding.equals( endpointBinding ) ) {
					throw new ServiceResultException( StatusCodes.Bad_UnexpectedError, "Cannot bind an endpoint address to two different servers." );
				}
			}
			return handle;
		}
		int getPort() {
			return ((InetSocketAddress)socketAddress).getPort();
		}

		void close() {
			for ( OpcTcpEndpointHandle eph : endpoints.values() ) eph.close__();
			socketHandles.remove(socketAddress);
			if ( socket!=null ) {
				AsyncServerSocket ass = socket;
				socket = null;
				ass.close();				
			}
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append( "opc.tcp("+socketAddress+", ");
			for (OpcTcpEndpointHandle ep : endpoints.values()) {
				sb.append( ep.toString() );
			}
			sb.append( ")");
			return sb.toString();
		}

		/**
		 * @return the channel
		 */
		public ServerSocketChannel getChannel() {
			return channel;
		}

		/**
		 * @param channel the channel to set
		 */
		public void setChannel(ServerSocketChannel channel) {
			this.channel = channel;
		}

		public SocketAddress getSocketAddress() {
			return socketAddress;
		}
	}
	
	public class OpcTcpEndpointHandle implements EndpointHandle {
		EndpointBinding endpointBinding;
		SocketHandle socketHandle;
		OpcTcpEndpointHandle(SocketHandle socketHandle, EndpointBinding endpointBinding) {
			this.socketHandle = socketHandle;
			this.endpointBinding = endpointBinding;
		}
		@Override
		public SocketAddress socketAddress() {
			return socketHandle.socketAddress;
		}
		@Override
		public EndpointBinding endpointBinding() {
			return endpointBinding;
		}
		@Override
		public void close() {
			close_();
			close__();
		}
		void close_() {
			// Remove endpointBinding from SocketHandle
			socketHandle.endpoints.remove(endpointBinding.endpointAddress);
			// Close socket if this was the last
			if ( socketHandle.endpoints.isEmpty() ) {
				socketHandle.close();
			}
		}
		void close__() {
			// Close endpointHandler if this was the last
			int count = countEndpoints(endpointBinding.endpointAddress);
			if ( count == 0 ) {
				// Remove endpointBinding from EndpointServer
				endpointBindings.remove(endpointBinding);
				// Remove endpointBinding from Server
				endpointBinding.serviceServer.getEndpointBindings().remove(endpointBinding);
			}
		}
		@Override
		public String toString() {
			return "("+endpointBinding.endpointAddress.toString()+")";
		}
	}

	List<OpcTcpEndpointHandle> findEndpoints(String forUri) {
		List<OpcTcpEndpointHandle> result = new ArrayList<OpcTcpEndpointHandle>();
		for (SocketHandle sh : socketHandleSnapshot() ) sh.endpointHandleSnapshot(result);
		return result;
	}
	int countEndpoints(Endpoint endpointAddress) {
		int count = 0;
		for (SocketHandle sh : socketHandleSnapshot() ) {
			for (OpcTcpEndpointHandle eh : sh.endpointHandleSnapshot()) {
				if (eh.endpointBinding.endpointAddress.equals(endpointAddress)) count++;
			}
		}
		return count;
	}
	
}
