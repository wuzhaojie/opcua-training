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

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.StatusCodes;

public class CertificateValidatorImpl implements CertificateValidator {

	/** List of explicitely trusted certificates */
	Set<Cert> trustedCertificates = new HashSet<Cert>();
	
	/** List of public keys of explicitely trusted signers */
	Set<PublicKey> trustedPublicKeys = new HashSet<PublicKey>();
	
	public CertificateValidatorImpl()
	{		
	}
	
	public CertificateValidatorImpl(Cert ... trustedCertificates)
	{		
		for (Cert c : trustedCertificates)
			addTrustedCertificate(c);
	}
	
	public void addTrustedCertificate(Cert certificate)
	{
		trustedCertificates.add(certificate);
	}
	
	public void addTrustedSigner(Cert signer)
	{
		addTrustedSignerPublicKey(signer.getCertificate().getPublicKey());
	}
	
	public void addTrustedSignerPublicKey(PublicKey signerPublicKey)
	{
		trustedPublicKeys.add(signerPublicKey);
	}
	
	@Override
	public StatusCode validateCertificate(Cert c) {
		for (Cert certs : trustedCertificates)
			if (certs.equals(c)) return null;
		for (PublicKey key : trustedPublicKeys)
		{	
			try {
				c.getCertificate().verify(key);
				return null;
			} catch (GeneralSecurityException e) {
				continue;
			}
		}
		return new StatusCode(StatusCodes.Bad_SecurityChecksFailed);
	}

}
