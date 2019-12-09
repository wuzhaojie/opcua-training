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
package org.opcfoundation.ua.transport.https;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.impl.nio.NHttpConnectionBase;
import org.apache.http.nio.NHttpServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.IConnectionListener;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.ServerSecureChannel;
import org.opcfoundation.ua.utils.AbstractState;

/**
 * This class implements HTTP TSL/SSL conversation.
 * 
 * The messages are serialized using binary scheme, the same as with tcp 
 * conversation.
 * 
 * Because HTTPS channel is already secure, a OPC secure channel is not opened.
 * All HTTPS communications via a URL shall be treated as a single 
 * SecureChannel that is shared by multiple Clients. Stack shall provide a 
 * unique identifier for the SecureChannel which allows Applications correlate 
 * a request with a SecureChannel.This means that Sessions can only be 
 * considered secure if the AuthenticationToken (see Part 4) is long (>20 bytes)
 * and HTTPS encryption is enabled.
 */
public class HttpsServerConnection extends AbstractState<CloseableObjectState, ServiceResultException> implements ServerConnection {
	private static Logger logger = LoggerFactory.getLogger(HttpsServerConnection.class);
	
	HttpsServer server;
	NHttpServerConnection conn;
	Socket socket;
	
	/** List of secure channels open in this connection */
	Map<Integer, ServerSecureChannel> secureChannels = new ConcurrentHashMap<Integer, ServerSecureChannel>();
	/** List of secure channel listener */
	CopyOnWriteArrayList<SecureChannelListener> secureChannelListeners = new CopyOnWriteArrayList<SecureChannelListener>();	
	/** Listeners that follow this connection */ 
	CopyOnWriteArrayList<IConnectionListener> connectionListeners = new CopyOnWriteArrayList<IConnectionListener>();
	
	public HttpsServerConnection(HttpsServer server, NHttpServerConnection conn) {
		super(CloseableObjectState.Closed);
		this.server = server;
		this.conn = conn;
		this.socket = ( (NHttpConnectionBase) conn ).getSocket();
		this.conn.setSocketTimeout(60000);
	}

	@Override
	public SocketAddress getLocalAddress() {
		return socket.getLocalSocketAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return socket.getRemoteSocketAddress();
	}
	
	public NHttpServerConnection getNHttpServerConnection() {
		return conn;
	}

	@Override
	public void getSecureChannels(Collection<ServerSecureChannel> list) {
		list.addAll( secureChannels.values() );
	}

	@Override
	public void addSecureChannelListener(SecureChannelListener l) {
		secureChannelListeners.add(l);
	}

	@Override
	public void removeSecureChannelListener(SecureChannelListener l) {
		secureChannelListeners.remove(l);
	}

	@Override
	public void addConnectionListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	@Override
	public void removeConnectionListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}
	
	@Override
	protected void onStateTransition(CloseableObjectState oldState,
			CloseableObjectState newState) {
		logger.debug("onStateTransition: {}->{}", oldState, newState);
		super.onStateTransition(oldState, newState);
		
		if (newState == CloseableObjectState.Open)
		{
			for (IConnectionListener l : connectionListeners)
				l.onOpen();
		}
		
		if (newState == CloseableObjectState.Closed) 
		{
			ServiceResultException sre = new ServiceResultException(StatusCodes.Bad_CommunicationError);
			for (IConnectionListener l : connectionListeners) {
				l.onClosed(sre);
			}

		}
	}	
}
