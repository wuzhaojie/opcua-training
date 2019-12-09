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

package org.opcfoundation.ua.transport.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opcfoundation.ua.transport.ConnectionMonitor;
import org.opcfoundation.ua.transport.ServerConnection;

public class ConnectionCollection implements ConnectionMonitor {

	Set<ServerConnection> connections = new HashSet<ServerConnection>(); 
	CopyOnWriteArrayList<ConnectListener> listeners = new CopyOnWriteArrayList<ConnectListener>();
	Object sender;	

	public void addConnection(ServerConnection c) {
		if (!connections.add(c)) return;
		for (ConnectListener cl : listeners)
			cl.onConnect(sender, c);
	}
	
	public void removeConnection(ServerConnection c) {
		connections.remove(c);
		for (ConnectListener cl : listeners)
			cl.onClose(sender, c);
	}

	public Iterator<ConnectListener> getConnectionListeners() {
		return listeners.iterator();
	}
	
	public ConnectionCollection(Object sender) {
		this.sender = sender;
	}
		
	@Override
	public void addConnectionListener(ConnectListener l) {
		listeners.add(l);
	}

	@Override
	public void removeConnectionListener(ConnectListener l) {
		listeners.remove(l);
	}
	
	@Override
	public synchronized void getConnections(Collection<ServerConnection> result) {
		result.addAll(connections);
	}
	
}
