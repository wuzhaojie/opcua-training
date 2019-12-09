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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.PrivKey;

public class OpcTcpSettings {

	PrivKey privKey;
	Cert clientCertificate;
	CertificateValidator certificateValidator;
	EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);	
	public enum Flag {
		/**
		 * In multithread mode, depending on implementation, channels 
		 * encrypt & decrypt messages simultaneously in multiple threads.
		 * 
		 * This allows higher throughput in secured data intensive applications with 
		 * large messages.
		 */
		MultiThread
	}

	
	
	public Cert getClientCertificate() {
		return clientCertificate;
	}
	public void setClientCertificate(X509Certificate clientCertificate) throws CertificateEncodingException {
		this.clientCertificate = new Cert(clientCertificate);
	}

	public CertificateValidator getCertificateValidator() {
		return certificateValidator;
	}
	public void setCertificateValidator(CertificateValidator certificateValidator) {
		this.certificateValidator = certificateValidator;
	}

	public PrivKey getPrivKey() {
		return privKey;
	}

	public void setPrivKey(PrivKey privKey) {
		this.privKey = privKey;
	}

	public void setClientCertificate(Cert clientCertificate) {
		this.clientCertificate = clientCertificate;
	}
	
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	public void setFlags(EnumSet<Flag> flags) {
		this.flags = flags;
	}	
	
	
	public void readFrom(OpcTcpSettings tcs) {
		if (tcs.clientCertificate!=null) clientCertificate = tcs.clientCertificate;
		if (tcs.certificateValidator!=null) certificateValidator = tcs.certificateValidator;
		if (tcs.privKey!=null) privKey = tcs.privKey;
		flags = tcs.flags;
	}
	
	@Override
	public OpcTcpSettings clone() {
		OpcTcpSettings result = new OpcTcpSettings();

		result.setClientCertificate(clientCertificate);
		result.setCertificateValidator(certificateValidator);
		result.setPrivKey(privKey);
		
		result.flags = flags.clone();
		return result;
	}	


}
