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

package org.opcfoundation.ua.transport.security;

import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.core.MessageSecurityMode;

/**
 * SecurityConfiguration is binding of local application instance certificate, 
 * local private key, remote application instance certificate, security policy 
 * and message security mode.
 * 
 * @author Mikko Salonen
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi) 
 */
public class SecurityConfiguration {
	
	public static final SecurityConfiguration NO_SECURITY;
	
	/** Logger */
	static Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
	
	/** Security mode */
	SecurityMode mode;
	/** Local Application Instance Certificate */
	KeyPair localCertificate = null;
	/** Remote Application Instance Certificate */
	Cert remoteCertificate = null;
	
	public SecurityConfiguration(
			SecurityMode mode, 
			KeyPair localApplicationInstanceCertificate,
			Cert remoteCertificate)
	{
		if (mode==null)
			throw new NullPointerException("SecurityMode mode is null");
		this.mode = mode;

		// Spec. Part 6:
		//
		// SenderCertificate
		// This field SHALL be null if the message is not signed.
		//
		// ReceiverCertificateThumbprint
		// This field SHALL be null if the message is not encrypted.

		if ( mode.getMessageSecurityMode().hasSigning() )
		{
			if (localApplicationInstanceCertificate==null)
			  throw new NullPointerException("localApplicationInstanceCertificate is null");
			this.localCertificate = localApplicationInstanceCertificate;
			
//			If the mode is Sign (and not Sign&Encrypt), remote certificate is required for 
//			open secure channel. 
			if (remoteCertificate==null)
				throw new NullPointerException("remoteCertificate is null");
			this.remoteCertificate = remoteCertificate;
		}
	}

	public Certificate getReceiverCertificate(){
		if (remoteCertificate==null) return null;
		return remoteCertificate.getCertificate();
	}

	public SecurityPolicy getSecurityPolicy() {
		return mode.getSecurityPolicy();
	}

	public byte[] getEncodedLocalPrivateKey() {
		if (localCertificate==null) return null;
		return localCertificate.getPrivateKey().getPrivateKey().getEncoded();
	}

	public byte[] getEncodedLocalCertificate() {
		if (localCertificate==null) return null;
		return localCertificate.getCertificate().getEncoded();
	}

	public byte[] getEncodedRemoteCertificateThumbprint() {
		if (remoteCertificate==null) return null;
		return remoteCertificate.getEncodedThumbprint();
	}

	public byte[] getEncodedRemoteCertificate() {
		if (remoteCertificate==null) return null;
		return remoteCertificate.getEncoded();
	}
	
	public byte[] getEncodedLocalCertificateThumbprint() {
		if (localCertificate==null) return null;
		return localCertificate.getCertificate().getEncodedThumbprint();
	}

	public RSAPrivateKey getLocalPrivateKey() {
		if (localCertificate==null) return null;
		return (RSAPrivateKey) localCertificate.getPrivateKey().getPrivateKey();
	}

	public Certificate getLocalCertificate() {
		if (localCertificate==null) return null;
		return localCertificate.getCertificate().getCertificate();
	}
		
	public Certificate getRemoteCertificate() {
		if (remoteCertificate==null) return null;
		return remoteCertificate.getCertificate();
	}

	public org.opcfoundation.ua.transport.security.Cert getRemoteCertificate2() {
		return remoteCertificate;
	}

	public KeyPair getLocalCertificate2() {
		return localCertificate;
	}
	
	public SecurityMode getSecurityMode() {
		return mode;
	}
	
	public MessageSecurityMode getMessageSecurityMode() {
		return mode.getMessageSecurityMode();
	}	
	
	static {		
		NO_SECURITY = new SecurityConfiguration(SecurityMode.NONE, null, null);
	}
	
}
