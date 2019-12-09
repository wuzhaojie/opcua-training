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
package org.opcfoundation.ua.unittests;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import junit.framework.TestCase;

import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.transport.security.BcCryptoProvider;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.CryptoProvider;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.utils.CertificateUtils;
import org.opcfoundation.ua.utils.CryptoUtil;


public class TestCertificates extends TestCase {

	private static final String ALIAS = "alias";
	private static final String KEY_PASSWORD = "pass";
	private static final String TEST_FILE = "test.key";
	private File file;
	private KeyPair keys;

	public void setUp() throws Exception {
		file = new File(TEST_FILE);
		keys = CertificateUtils.createApplicationInstanceCertificate(
				"Test", "Test", "urn:localhost:UA:Test", 365);
	}
	
	protected void tearDown() throws Exception {
		file.delete();
	}
	
	public void testPfx() throws GeneralSecurityException, IOException  {
		CertificateUtils.saveToProtectedStore(keys.getPrivateKey()
				.getPrivateKey(), keys.getCertificate().getCertificate(), file,
				ALIAS, null, KEY_PASSWORD, "PKCS12");
		RSAPrivateKey privKey = CertificateUtils.loadFromKeyStore(file.toURI()
				.toURL(), KEY_PASSWORD);
		assertEquals(keys.getPrivateKey().getPrivateKey(), privKey);
	}

	public void testCASigned() throws IllegalStateException,
			IOException,
			GeneralSecurityException {
		KeyPair caKeys = CertificateUtils.createIssuerCertificate("TestCA", 3650, null);
		File file = new File("TestCA.der");
		caKeys.getCertificate().save(file);
		Cert caCert = Cert.load(file);
		assertEquals(caKeys.getCertificate().getCertificate(), caCert.getCertificate());
		KeyPair keys = CertificateUtils.createApplicationInstanceCertificate(
				"Test", "Test", "urn:localhost:UA:Test", 365, caKeys);
		file = new File("TestCert.der");
		keys.getCertificate().save(file);
		Cert cert = Cert.load(file);
		assertEquals(keys.getCertificate().getCertificate(), cert.getCertificate());
		file = new File("TestKey.pem");
		keys.getPrivateKey().save(file, null);
		PrivKey privKey = PrivKey.load(file, null);
		assertEquals(keys.getPrivateKey().getPrivateKey(), privKey.getPrivateKey());
	}

	public void testHttpsCert() throws IllegalStateException, IOException,
			GeneralSecurityException {
		KeyPair caKeys = CertificateUtils.createIssuerCertificate("TestCA",
				3650, null);
		File file = new File("TestCA_https.der");
		caKeys.getCertificate().save(file);
		Cert caCert = Cert.load(file);
		assertEquals(caKeys.getCertificate().getCertificate(),
				caCert.getCertificate());
		KeyPair keys = CertificateUtils.createHttpsCertificate(
				InetAddress.getLocalHost().getHostName(), "urn:localhost:UA:Test", 365, caKeys);
		file = new File("TestCert_https.der");
		keys.getCertificate().save(file);
		Cert cert = Cert.load(file);
		assertEquals(keys.getCertificate().getCertificate(),
				cert.getCertificate());
		file = new File("TestKey_https.pem");
		keys.getPrivateKey().save(file, null);
		PrivKey privKey = PrivKey.load(file, null);
		assertEquals(keys.getPrivateKey().getPrivateKey(),
				privKey.getPrivateKey());
	}
	
	public void testJks() throws IllegalStateException,
			IOException,
			GeneralSecurityException {
		CertificateUtils.saveKeyPairToProtectedStore(keys,
				file.getAbsolutePath(), ALIAS, KEY_PASSWORD, KEY_PASSWORD);
		KeyPair keys2 = CertificateUtils.createApplicationInstanceCertificate(
				"Test2", "Test2", "urn:localhost:UA:Test2", 365);
		String ALIAS2 = "Test2";
		CertificateUtils.saveKeyPairToProtectedStore(keys2,
				file.getAbsolutePath(), ALIAS2, KEY_PASSWORD, KEY_PASSWORD);

		KeyPair keyPair = CertificateUtils.loadKeyPairFromProtectedStore(TEST_FILE, ALIAS, KEY_PASSWORD, KEY_PASSWORD);
		assertEquals(keys.getPrivateKey().getPrivateKey(), keyPair.getPrivateKey().getPrivateKey());
		assertEquals(keys.getCertificate().getCertificate(), keyPair.getCertificate().getCertificate());
		KeyPair keyPair2 = CertificateUtils.loadKeyPairFromProtectedStore(TEST_FILE, ALIAS2, KEY_PASSWORD, KEY_PASSWORD);
		assertEquals(keys2.getPrivateKey().getPrivateKey(), keyPair2.getPrivateKey().getPrivateKey());
		assertEquals(keys2.getCertificate().getCertificate(), keyPair2.getCertificate().getCertificate());
	}

	public void testDerCert() throws IOException, CertificateException  {
		keys.getCertificate().save(file);
		Cert cert = Cert.load(file);
		assertEquals(keys.getCertificate().getCertificate(), cert.getCertificate());
	}

	public void testPemPrivKey() throws IOException, CertificateEncodingException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException  {
		keys.getPrivateKey().save(file, KEY_PASSWORD);
		PrivKey privKey = PrivKey.load(file, KEY_PASSWORD);
		assertEquals(keys.getPrivateKey().getPrivateKey(), privKey.getPrivateKey());
	}
	
	public void testPemPrivKeyNoPassword() throws IOException, GeneralSecurityException  {
		keys.getPrivateKey().save(file, null);
		PrivKey privKey = PrivKey.load(file, null);
		assertTrue(Arrays.equals(keys.getPrivateKey().getEncodedPrivateKey(), privKey.getEncodedPrivateKey()));
	}
	
	public void testSignVerify() throws Exception {
		setUp();
		
		PrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		@SuppressWarnings("deprecation")
		SignatureData signedData = CertificateUtils.sign(privkey, algorithm , dataToSign);
		byte[] signature = signedData.getSignature();
		@SuppressWarnings("deprecation")
		boolean isCorrect = CertificateUtils.verify(keys.certificate.certificate, algorithm, dataToSign, signature);
		tearDown();
		
		assertEquals(true, isCorrect);
		
	}
	
	public void testSignVerifyWithExplicitCryptoProvider() throws Exception {
		CryptoProvider bcCryptoProvider = new BcCryptoProvider();
		CryptoUtil.setCryptoProvider(bcCryptoProvider);
		
		setUp();
		
		PrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		@SuppressWarnings("deprecation")
		SignatureData signedData = CertificateUtils.sign(privkey, algorithm , dataToSign);
		byte[] signature = signedData.getSignature();
		@SuppressWarnings("deprecation")
		boolean isCorrect = CertificateUtils.verify(keys.certificate.certificate, algorithm, dataToSign, signature);
		
		CryptoUtil.setCryptoProvider(null);
		tearDown();
		
		assertEquals(true, isCorrect);
		
	}
	
	
	public void testSignVerifyWithIncorrectParameters() throws Exception {
		setUp();

		PrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		@SuppressWarnings("deprecation")
		SignatureData signedData = CertificateUtils.sign(privkey, algorithm , dataToSign);
		byte[] signature = signedData.getSignature();
		
		algorithm = SecurityAlgorithm.RsaSha256;
		//now signature should not be verified
		try {
			@SuppressWarnings("deprecation")
			boolean isFalse = CertificateUtils.verify(keys.certificate.certificate, algorithm, dataToSign, signature);
			assertEquals(false, isFalse);
		} finally {
			tearDown();
		}
	}
	
	public void testSignVerifyWithIncorrectParametersAndExplicitCryptoProvider() throws Exception {
		setUp();
		CryptoProvider bcCryptoProvider = new BcCryptoProvider();
		CryptoUtil.setCryptoProvider(bcCryptoProvider);

		PrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		@SuppressWarnings("deprecation")
		SignatureData signedData = CertificateUtils.sign(privkey, algorithm , dataToSign);
		byte[] signature = signedData.getSignature();
		
		algorithm = SecurityAlgorithm.RsaSha256;
		//now signature should not be verified
		try {
			@SuppressWarnings("deprecation")
			boolean isFalse = CertificateUtils.verify(keys.certificate.certificate, algorithm, dataToSign, signature);
			assertEquals(false, isFalse);
		} finally {
			CryptoUtil.setCryptoProvider(null);
			tearDown();
		}
		
	}
	
	/* Not deprecated way of signing and verifying is to use CryptoProviders methods. 
	 * These are the same tests as above but using CryptoUtil.getCryptoProvider() provided methods.
	 * */
	public void testSignVerifyWithCryptoUtil() throws Exception {
		setUp();
		
		RSAPrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		SignatureData signedData = new SignatureData(algorithm.getUri(), CryptoUtil.getCryptoProvider().signAsymm(privkey, algorithm, dataToSign));
		byte[] signature = signedData.getSignature();
		boolean isCorrect = CryptoUtil.getCryptoProvider().verifyAsymm(keys.certificate.certificate.getPublicKey(), algorithm, dataToSign, signature);
		tearDown();
		
		assertEquals(true, isCorrect);
		
	}
	
	public void testSignVerifyWithExplicitCryptoProviderWithCryptoUtil() throws Exception {
		CryptoProvider bcCryptoProvider = new BcCryptoProvider();
		CryptoUtil.setCryptoProvider(bcCryptoProvider);
		
		setUp();
		
		RSAPrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		SignatureData signedData = new SignatureData(algorithm.getUri(), CryptoUtil.getCryptoProvider().signAsymm(privkey, algorithm, dataToSign));
		byte[] signature = signedData.getSignature();
		boolean isCorrect = CryptoUtil.getCryptoProvider().verifyAsymm(keys.certificate.certificate.getPublicKey(), algorithm, dataToSign, signature);
		
		CryptoUtil.setCryptoProvider(null);
		tearDown();
		
		assertEquals(true, isCorrect);
		
	}
	
	
	public void testSignVerifyWithIncorrectParametersWithCryptoUtil() throws Exception {
		setUp();

		RSAPrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		SignatureData signedData = new SignatureData(algorithm.getUri(), CryptoUtil.getCryptoProvider().signAsymm(privkey, algorithm, dataToSign));
		byte[] signature = signedData.getSignature();
		
		algorithm = SecurityAlgorithm.RsaSha256;
		//now signature should not be verified
		try {
			boolean isFalse = CryptoUtil.getCryptoProvider().verifyAsymm(keys.certificate.certificate.getPublicKey(), algorithm, dataToSign, signature); 
			assertEquals(false, isFalse);
		} finally {
			tearDown();
		}
	}
	
	public void testSignVerifyWithIncorrectParametersAndExplicitCryptoProviderWithCryptoUtil() throws Exception {
		setUp();
		CryptoProvider bcCryptoProvider = new BcCryptoProvider();
		CryptoUtil.setCryptoProvider(bcCryptoProvider);

		RSAPrivateKey privkey = keys.getPrivateKey().getPrivateKey();
		SecurityAlgorithm algorithm = SecurityAlgorithm.RsaSha1;
		byte[] dataToSign = new byte[100];
		SignatureData signedData = new SignatureData(algorithm.getUri(), CryptoUtil.getCryptoProvider().signAsymm(privkey, algorithm, dataToSign));
		byte[] signature = signedData.getSignature();
		
		algorithm = SecurityAlgorithm.RsaSha256;
		//now signature should not be verified
		try {
			boolean isFalse = CryptoUtil.getCryptoProvider().verifyAsymm(keys.certificate.certificate.getPublicKey(), algorithm, dataToSign, signature); 
			assertEquals(false, isFalse);
		} finally {
			CryptoUtil.setCryptoProvider(null);
			tearDown();
		}
		
	}
	
}
