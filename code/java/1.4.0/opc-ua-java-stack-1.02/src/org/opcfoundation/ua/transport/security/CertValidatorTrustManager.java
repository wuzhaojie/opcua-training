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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import org.opcfoundation.ua.builtintypes.StatusCode;

/**
 * This class adapts cert validator to trust manager.
 * 
 * Validation is evaluated every time, validator must cache results if needed.
 *
 */
public class CertValidatorTrustManager implements X509TrustManager {
	
	CertificateValidator validator;
	
	// X509Certificate convertion to Cert
	Map<X509Certificate, Cert> certMap = new HashMap<X509Certificate, Cert>();
	
	List<Cert> acceptedCertificates = new ArrayList<Cert>();
	List<Cert> acceptedIssuers = new ArrayList<Cert>();
	X509Certificate[] acceptedIssuersArray;

	public CertValidatorTrustManager(CertificateValidator validator) {
		this.validator = validator;
	}

	/**
	 * Encode X509Certificate to binary and cache 
	 * 
	 * @param c
	 * @return Cert or null if error occured
	 * @throws CertificateException 
	 */
	synchronized void validate(X509Certificate c) throws CertificateException {
		Cert cert = certMap.get( c );
		if ( cert == null ) {
			cert = new Cert(c);
			certMap.put(c, cert);
		}	
		
		// Check validity (only once)
		StatusCode code = validator.validateCertificate(cert);
		boolean valid = code == null || code.isGood();
		if ( valid ) {
			acceptedCertificates.add( cert );
		}else{
			throw new CertificateException("Certificate is not valid");
		}
		
		X500Principal issuer = c.getIssuerX500Principal();
		X500Principal subject = c.getSubjectX500Principal();
		if ( !subject.equals(issuer) && valid ) {
			acceptedIssuers.add(cert);
			acceptedIssuersArray = null;
		}
	}
	
	@Override
	public synchronized void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		for ( X509Certificate c : certs ) {
			validate(c);
		}		
	}

	@Override
	public synchronized void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException 
	{	
		for ( X509Certificate c : certs ) {
			validate(c);
		}		
	}

	@Override
	public synchronized X509Certificate[] getAcceptedIssuers() {
		if ( acceptedIssuersArray == null ) {
			int count = acceptedIssuers.size();
			acceptedIssuersArray = new X509Certificate[ count ];
			for (int i=0; i<count; i++) {
				Cert issuerCert = acceptedIssuers.get(i);
				acceptedIssuersArray[i] = issuerCert.getCertificate();
			}
		}
		return acceptedIssuersArray;
	}
	
}
