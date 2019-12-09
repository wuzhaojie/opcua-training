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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndpointServerCollection {

	List<EndpointServer> servers = Collections.synchronizedList( new ArrayList<EndpointServer>() );

	public void add(EndpointServer endpointServer)
	{
		servers.add( endpointServer );
	}
	
	public void remove(EndpointServer endpointServer)
	{
		servers.remove( endpointServer );
	}
	
	/**
	 * Get a snapshot of the endpoint server list 
	 * 
	 * @return a copy of the endpoint server list
	 */
	public List<EndpointServer> getList() {
		return new ArrayList<EndpointServer>( servers );
	}
	
	/**
	 * Get all endpoint bindings
	 * @return all endpoint bindings of all endpoint servers in this list
	 */
	public List<EndpointBinding> getEndpointBindings() {
		List<EndpointBinding> result = new ArrayList<EndpointBinding>();
		synchronized(servers) {
			for (EndpointServer es : servers) {
				result.addAll( es.getEndpointBindings().getAll() );
			}
		}
		return result;
	}
	
	/**
	 * Close all endpoint servers.
	 */
	public void closeAll()
	{
		for (EndpointServer server : getList() ) {
			server.close();
		}
	}
	
	/**
	 * Get the endpoint server that is bound at the given socket address.
	 * 
	 * @param socketAddress
	 * @return EndpointServer or null
	 */
	public EndpointServer getEndpointServer( SocketAddress socketAddress ) {
		for ( EndpointServer endpointServer : servers ) {
			for ( SocketAddress sa : endpointServer.getBoundSocketAddresses() ) {
				if ( sa.equals( socketAddress )) return endpointServer;
			}
		}
		return null;
	}
	
}
