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

package org.opcfoundation.ua.application;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.SessionDiagnosticsDataType;
import org.opcfoundation.ua.core.SignedSoftwareCertificate;
import org.opcfoundation.ua.transport.SecureChannel;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.PrivKey;

/**
 * Session
 */
public class Session {
	
	/** Server Endpoint */
	EndpointDescription endpoint;
	/** Session Name */
	String name;
	/** Session ID */
	NodeId sessionId;
	/** Server nonce */
	byte[] serverNonce;
	/** Client nonce */
	byte[] clientNonce;
	/** NodeId that contains SessionDiagnosticsInfo, see {@link SessionDiagnosticsDataType} */
	NodeId diagnosticsInfo;
	/** Server assigned authentication token. It is passed in every request and response */
	NodeId authenticationToken;
	/** Inactivity timeout time */
	double sessionTimeout;
	/** Max request message size */
	UnsignedInteger maxRequestMessageSize;
	/** Server software Certificates */
	SignedSoftwareCertificate[] serverSoftwareCertificates;
	/** Server certificate */
	Cert serverCertificate;
	/** Client certificate */
	Cert clientCertificate;
	/** Client private key (optional) */
	PrivKey clientPrivateKey;
	/** Server private key (optional) */
	PrivKey serverPrivateKey;

	Session() {}

	/**
	 * Create new unactivated session channel.
	 * Session Channel will be wrapped over secure channel.
	 * 
	 * @param channel securechannel secure channel to wrap session channel over
	 * @return session channel
	 */
	public SessionChannel createSessionChannel(SecureChannel channel, Client client)
	{
		return new SessionChannel(client, this, channel);
	}

	public EndpointDescription getEndpoint() {
		return endpoint;
	}

	public byte[] getServerNonce() {
		return serverNonce;
	}

	public NodeId getDiagnosticsInfo() {
		return diagnosticsInfo;
	}

	public NodeId getAuthenticationToken() {
		return authenticationToken;
	}

	public double getSessionTimeout() {
		return sessionTimeout;
	}

	public UnsignedInteger getMaxRequestMessageSize() {
		return maxRequestMessageSize;
	}

	public SignedSoftwareCertificate[] getServerSoftwareCertificates() {
		return serverSoftwareCertificates;
	}

	public Cert getServerCertificate() {
		return serverCertificate;
	}

	public String getName() {
		return name;
	}

	public NodeId getSessionId() {
		return sessionId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getClientNonce() {
		return clientNonce;
	}

	public Cert getClientCertificate() {
		return clientCertificate;
	}

	public PrivKey getClientPrivateKey() {
		return clientPrivateKey;
	}

	public PrivKey getServerPrivateKey() {
		return serverPrivateKey;
	}
	
	

}
