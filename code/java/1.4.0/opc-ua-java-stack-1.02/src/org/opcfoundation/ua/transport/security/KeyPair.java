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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Valid and encodeable certificate, including signed public key and private key
 * This class aggregates private and public keys.
 * 
 * TODO Use {@link KeyPair} Instead?
 * 
 * @author Toni Kalajainen (toni.kalajainen@iki.fi)
 * @author Mikko Salonen
 */
public final class KeyPair {

	public final Cert certificate;
	public final PrivKey privateKey;

	/** 
	 * Load Certificate & Private key pair from X.509 and keystore file
	 * 
	 * @param certificateFile
	 * @param privateKeyFile
	 * @param privateKeyPassword
	 * @return a new keypair instance
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 */
	public static KeyPair load(URL certificateFile, URL privateKeyFile, String privateKeyPassword) 
	throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		Cert cert = Cert.load(certificateFile);
		PrivKey privKey = PrivKey.loadFromKeyStore(privateKeyFile, privateKeyPassword);
		return new KeyPair(cert, privKey);
	}
	
	/** 
	 * Load Certificate & Private key pair from X.509 and keystore file
	 * 
	 * @param certificateFile
	 * @param privateKeyFile
	 * @param privateKeyPassword
	 * @return a new keypair instance
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 */
	public static KeyPair load(File certificateFile, File privateKeyFile, String privateKeyPassword) 
	throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		Cert cert = Cert.load(certificateFile);
		PrivKey privKey = PrivKey.loadFromKeyStore(privateKeyFile, privateKeyPassword);
		return new KeyPair(cert, privKey);
	}
	
	public void save(File certificateFile, File privateKeyFile) 
	throws IOException
	{
		certificate.save(certificateFile);
		privateKey.save(privateKeyFile);
	}
	
	public void save(File certificateFile, File privateKeyFile, String privateKeyPassword) 
	throws IOException
	{
		certificate.save(certificateFile);
		privateKey.save(privateKeyFile, privateKeyPassword);
	}

	public KeyPair(Cert certificate, PrivKey privateKey)
	{
		if (certificate==null || privateKey==null)
			throw new IllegalArgumentException("null arg");
		this.certificate = certificate;
		this.privateKey = privateKey;
	}

	public Cert getCertificate() {
		return certificate;
	}

	public PrivKey getPrivateKey() {
		return privateKey;
	}

	@Override
	public String toString() {
		return certificate.toString();
	}
	
}
