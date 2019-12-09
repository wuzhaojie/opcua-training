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

import org.opcfoundation.ua.application.Server;

/**
 * Endpoint binding is a 3-way binding between 
 *   the endpoint address (Endpoint), 
 *   the listening socket (EndpointServer),  
 *   and the service server (Server).
 *
 */
public class EndpointBinding {

	/** The object that listens to the network socket */
	public final EndpointServer endpointServer;
	
	/** Endpoint address description */
	public final Endpoint endpointAddress;
	
	/** Service server */
	public final Server serviceServer;

	public EndpointBinding(EndpointServer endpointServer, Endpoint endpointAddress, Server serviceServer) {
		if ( endpointServer==null || endpointAddress==null || serviceServer==null ) throw new IllegalArgumentException("null arg");
		this.endpointServer = endpointServer;
		this.endpointAddress = endpointAddress;
		this.serviceServer = serviceServer;
	}
	
	@Override
	public int hashCode() {
		return endpointServer.hashCode() + 13*serviceServer.hashCode() + 7*endpointAddress.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EndpointBinding)) return false;
		EndpointBinding other = (EndpointBinding) obj;
		if (!other.endpointServer.equals(endpointServer)) return false;
		if (!other.endpointAddress.equals(endpointAddress)) return false;
		if (!other.serviceServer.equals(serviceServer)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(EndpointServer="+endpointServer+", EndpointAddress="+endpointAddress+", ServiceServer="+serviceServer+")";
	}
}
