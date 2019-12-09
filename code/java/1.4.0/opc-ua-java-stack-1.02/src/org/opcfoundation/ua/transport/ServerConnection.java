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
import java.util.Collection;


/**
 * UAConnection is a stateful object with four possible states {Closed,
 * Opening, Open, Closing}. A connection is transfers to open state
 * after handshake (Hello/Acknowledge). Closed state is final. 
 */
public interface ServerConnection {

	/**
	 * Get local socket address 
	 * 
	 * @return socket address 
	 */
	SocketAddress getLocalAddress();
	
	/**
	 * Get remote socket address
	 * @return remote socket address
	 */
	SocketAddress getRemoteAddress();
	
	/**
	 * Get all open and opening secure channels of this connection.
	 * 
	 * @param list list to be filled
	 */
	void getSecureChannels(Collection<ServerSecureChannel> list);
	
	static interface SecureChannelListener {
		void onSecureChannelAttached(Object sender, ServerSecureChannel channel);
		void onSecureChannelDetached(Object sender, ServerSecureChannel channel);
	}

	void addSecureChannelListener(SecureChannelListener l);
	void removeSecureChannelListener(SecureChannelListener l);

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
	
}
