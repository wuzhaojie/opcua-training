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

import java.net.URI;
import java.util.Arrays;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.ObjectUtils;

/**
 * Endpoint is a connection point to a server.
 * An endpoint is assigned to a {@link Server} using {@link BindingFactory}.
 * Endpoint doesn't contain service logic, it is merely description of a connection point. 
 * <p>
 * This class is hash-equals comparable. 
 * 
 * @see BindingFactory#bind(Endpoint, Server) to bind an endpoint to a {@link Server}
 */
public class Endpoint {

	/** Security Modes */
	SecurityMode[] modes;
	/** Endpoint Url */
	String endpointUrl;
	
	private int hash;
	
	EndpointConfiguration endpointConfiguration;
	
	/**
	 * Create a new endpoint.
	 * 
	 * @param endpointUrl endpoint address
	 * @param modes security modes
	 */
	public Endpoint(URI endpointUrl, SecurityMode...modes) {		
		if (modes==null || endpointUrl==null)
			throw new IllegalArgumentException("null arg");
		for (SecurityMode m : modes) {
			if (m==null) throw new IllegalArgumentException("null arg");
			hash = 13*hash + m.hashCode();
		}
		this.endpointUrl = endpointUrl.toString();
		this.modes = modes;
		this.endpointConfiguration = EndpointConfiguration.defaults();
		hash = 13*hash + endpointUrl.hashCode();
	}
	
	/**
	 * Create new endpoint.
	 * 
	 * @param endpointUrl endpoint address
	 * @param modes security modes
	 */
	public Endpoint(String endpointUrl, SecurityMode...modes) {		
		if (modes==null || endpointUrl==null)
			throw new IllegalArgumentException("null arg");
		for (SecurityMode m : modes) {
			if (m==null) throw new IllegalArgumentException("null arg");
			hash = 13*hash + m.hashCode();
		}
		this.endpointUrl = endpointUrl;
		this.modes = modes;
		this.endpointConfiguration = EndpointConfiguration.defaults();
		hash = 13*hash + endpointUrl.hashCode();
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public SecurityMode[] getSecurityModes() {
		return modes;
	}
	
	public boolean supportsSecurityMode(SecurityMode mode)
	{
		for (SecurityMode m : modes)
			if (m.equals(mode)) return true;
		return false;
	}
	
	public boolean supportsSecurityPolicy(SecurityPolicy policy)
	{
		for (SecurityMode m : modes)
			if (m.getSecurityPolicy().equals(policy)) return true;
		return false;
	}
	
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Endpoint)) return false;
		Endpoint other = (Endpoint) obj;
		if (!ObjectUtils.objectEquals(other.getEndpointUrl(), getEndpointUrl())) return false;
		if (!Arrays.deepEquals(modes, other.modes)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return endpointUrl+" "+Arrays.toString(modes);
	}

	public EndpointConfiguration getEndpointConfiguration() {
		return endpointConfiguration;
	}
	
	public void setEndpointConfiguration( EndpointConfiguration endpointConfiguration ) {
		this.endpointConfiguration = endpointConfiguration; 
	}
	
}
