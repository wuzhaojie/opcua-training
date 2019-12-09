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

import java.net.InetSocketAddress;

import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.IConnectionListener;
import org.opcfoundation.ua.transport.TransportChannelSettings;

public interface IConnection {	
	
	/**
	 * Set connection configuration parameters. 
	 * 
	 * @param addr
	 * @param settings
	 * @throws ServiceResultException
	 */
	public void initialize(InetSocketAddress addr, TransportChannelSettings settings, EncoderContext ctx)
	throws ServiceResultException; 

	/**
	 * Send request to the connection.
	 * 
	 * 
	 * If the connection is closed Bad_NotConnected is thrown
	 * 
	 * @param request
	 * @param secureChannelId
	 * @param requestId
	 * @throws ServiceResultException varies. Bad_NotConnected if connection is not established
	 */
	public void sendRequest(ServiceRequest request, int secureChannelId, int requestId)
	throws ServiceResultException; 

	/**
	 * Add response listener 
	 * 
	 * @param listener
	 */
	public void addMessageListener(IMessageListener listener);
	
	/**
	 * Add response listener
	 * 
	 * @param listener
	 */
	public void removeMessageListener(IMessageListener listener);	

	/**
	 * Message listener interface
	 */
	public interface IMessageListener {
		/**
		 * New message arrived to the connection.
		 *  
		 * Incoming message listeners. All incoming messages are notified to all listeners.
		 * It is up to the listener to find the interesting messages.
		 * 
		 * @param requestId
		 * @param secureChannelId
		 * @param message
		 */
		void onMessage(int requestId, int secureChannelId, IEncodeable message);
	}

	/**
	 * Add response listener 
	 * 
	 * @param listener
	 */
	public void addConnectionListener(IConnectionListener listener);
	
	/**
	 * Add response listener
	 * 
	 * @param listener
	 */
	public void removeConnectionListener(IConnectionListener listener);	
	
	/**
	 * Attempt to open the connection. 
	 * There is no error if the connection is already open.
	 * 
	 * @throws ServiceResultException 
	 */
	public void open() throws ServiceResultException;
	
	/**
	 * Close the connection.
	 * There is no error if the connection is already closed.
	 * 
	 */
	public void close();	

	/**
	 * Open if the connection is not open.
	 * 
	 * @throws ServiceResultException
	 */
	public void reconnect() throws ServiceResultException; 
	
	/**
	 * Dispose the object making it unusable.
	 */
	public void dispose();		
	
	
}
