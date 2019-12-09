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

package org.opcfoundation.ua.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.transport.security.BcCertificateProvider;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.CertificateProvider;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.transport.security.ScCertificateProvider;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SunJceCertificateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class for generating self-signed certificates for UA clients and
 * servers and for using them.
 */
public class CertificateUtils {

	private static final int NAME_URI = 6;
	private static Logger logger = LoggerFactory.getLogger(CertificateUtils.class);

	/**
	 * Sign data
	 * 
	 * @param signerKey
	 * @param algorithm
	 *            asymmetric signer algorithm, See {@link SecurityAlgorithm}
	 * @param dataToSign
	 * @return signature data
	 * 
	 * @deprecated Use {@link CryptoUtil#signAsymm(PrivateKey, SecurityAlgorithm, byte[])} instead.
	 */
	public static SignatureData sign(PrivateKey signerKey,
			SecurityAlgorithm algorithm, byte[] dataToSign)
			throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

		try {
			return CryptoUtil.signAsymm(signerKey, algorithm, dataToSign);
		} catch (ServiceResultException e) {
			throw new SignatureException(e);
		}
	}

	/**
	 * Verify a signature. 
	 * 
	 * @param certificate
	 * @param algorithm
	 *            asymmetric signer algorithm, See {@link SecurityAlgorithm}
	 * @param data
	 * @param signature
	 * @return true if verified
	 *
	 * @deprecated Use {@link CryptoUtil#verifyAsymm(X509Certificate, SecurityAlgorithm, byte[], byte[])} instead.
	 */
	public static boolean verify(X509Certificate certificate,
			SecurityAlgorithm algorithm, byte[] data, byte[] signature)
					throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {

		try {
			return CryptoUtil.verifyAsymm(certificate, algorithm, data, signature);
		} catch (ServiceResultException e) {
			throw new SignatureException(e);
		}
	}

	/**
	 * Load X.509 Certificate from an url
	 * 
	 * @param url
	 * @return Certificate
	 * @throws IOException
	 * @throws CertificateException
	 *             In case the certificate is not valid
	 */
	public static X509Certificate readX509Certificate(URL url)
			throws IOException, CertificateException {
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		try {
			CertificateFactory servercf = CertificateFactory
					.getInstance("X.509");
			return (X509Certificate) servercf.generateCertificate(is);
		} finally {
			is.close();
		}
	}

	/**
	 * Load X.509 Certificate from a file
	 * 
	 * @param file
	 * @return Certificate
	 * @throws IOException
	 * @throws CertificateException
	 *             In case the certificate is not valid
	 */
	public static X509Certificate readX509Certificate(File file)
			throws IOException, CertificateException {
		return readX509Certificate(file.toURI().toURL());
	}

	/**
	 * Create SHA-1 Thumbprint
	 * 
	 * @param data
	 * @return thumbprint
	 */
	public static byte[] createThumbprint(byte[] data) {
		try {
			MessageDigest shadigest = MessageDigest.getInstance("SHA1");
			return shadigest.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	/**
	 * Decode X509 Certificate
	 * 
	 * @param encodedCertificate
	 * @return X509 certificate
	 * @throws CertificateException
	 */
	public static X509Certificate decodeX509Certificate(
			byte[] encodedCertificate) throws CertificateException {
		try {
			if (encodedCertificate == null)
				throw new IllegalArgumentException("null arg");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream bais = new ByteArrayInputStream(
					encodedCertificate);
			X509Certificate result = (X509Certificate) cf
					.generateCertificate(bais);
			bais.close();
			return result;
		} catch (IOException e) {
			// ByteArrayInputStream will not throw this
			throw new RuntimeException(e);
		}
	}

	/**
	 * Load private key from a key store
	 * 
	 * @param keystoreUrl
	 *            url to key store
	 * @param password
	 *            password to key store
	 * @return private key
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 */
	public static RSAPrivateKey loadFromKeyStore(URL keystoreUrl,
			String password) throws IOException, NoSuchAlgorithmException,
			CertificateException, KeyStoreException, UnrecoverableKeyException {
		logger.debug("loadFromKeyStore: keystoreUrl={}", keystoreUrl);
		// Open pfx-certificate
		URLConnection connection = keystoreUrl.openConnection();
		InputStream is = connection.getInputStream();
		try {
			// Open key store and load the key
			if (logger.isDebugEnabled())
				logger.debug("getproviders={}", Arrays.toString(Security.getProviders()));
			KeyStore keyStore;
			try {
				try {
					// Prefer the Sun KeyStore implementation!
					// TODO Check if the new BC works better nowadays
					keyStore = KeyStore.getInstance("PKCS12", "SunJSSE");
				} catch (NoSuchProviderException e) {
					keyStore = KeyStore.getInstance("PKCS12", CryptoUtil.getSecurityProviderName(KeyStore.class));
				}
			} catch (NoSuchProviderException e) {
				keyStore = KeyStore.getInstance("PKCS12");
			}
			logger.debug("loadFromKeyStore: keyStore Provider={}", keyStore.getProvider());
			keyStore.load(is, password == null ? null : password.toCharArray());
			Enumeration<String> aliases = keyStore.aliases();

			Key key = null;
			while (aliases.hasMoreElements()) {
				String a = (String) aliases.nextElement();
				key = keyStore.getKey(a, password == null ? null : password.toCharArray());
			}

			return (RSAPrivateKey) key;
		} finally {
			is.close();
		}
	}

	/**
	 * Save the KeyPair to a Java Key Store.
	 * 
	 * @param keyPairToSave
	 * @param storeLocation
	 * @param alias
	 * @param storePW
	 * @param privatePW
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	public static boolean saveKeyPairToProtectedStore(
			org.opcfoundation.ua.transport.security.KeyPair keyPairToSave,
			String storeLocation, String alias, String storePW, String privatePW)
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException {
		KeyStore store = null;

		// initialize and open keystore
		store = KeyStore.getInstance("JKS");
		File keystoreFile = new File(storeLocation);
		FileInputStream in;
		try {
			in = new FileInputStream(keystoreFile);
			try {
				store.load(in, storePW.toCharArray());
			} finally {
				in.close();
			}
		} catch (FileNotFoundException e) {
			store.load(null, null);
		}

		// create certificate chain containing only 1 certificate
		Certificate[] chain = new Certificate[1];
		chain[0] = keyPairToSave.certificate.getCertificate();
		store.setKeyEntry(alias, keyPairToSave.privateKey.getPrivateKey(),
				privatePW.toCharArray(), chain);

		FileOutputStream fOut = new FileOutputStream(storeLocation);
		try {
			store.store(fOut, storePW.toCharArray());
		} finally {
			fOut.close();
		}
		return true;
	}

	/**
	 * Load a KeyPair from a Java Key Store.
	 * 
	 * @param storeLocation
	 * @param alias
	 * @param storePW
	 * @param privatePW
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair loadKeyPairFromProtectedStore(
			String storeLocation, String alias, String storePW, String privatePW)
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException {

		KeyStore store = null;

		// initialize and open keystore
		store = KeyStore.getInstance("JKS");
		File keystoreFile = new File(storeLocation);
		FileInputStream in = new FileInputStream(keystoreFile);
		store.load(in, storePW.toCharArray());
		in.close();

		// try to load certificate from store
		X509Certificate cert = (X509Certificate) store.getCertificate(alias);

		// Try to load private key from keystore
		RSAPrivateKey key = (RSAPrivateKey) store.getKey(alias,
				privatePW.toCharArray());

		return new org.opcfoundation.ua.transport.security.KeyPair(new Cert(
				cert), new PrivKey(key));
	}

	/**
	 * Renew a certificate KeyPair using the old keys.
	 * 
	 * @param commonName
	 *            - Common Name (CN) for generated certificate
	 * @param organisation
	 *            - Organisation (O) for generated certificate
	 * @param applicationUri
	 *            - Alternative name (one of x509 extensiontype) for generated
	 *            certificate. Must not be null
	 * @param validityTime
	 *            - the time that the certificate is valid (in days)
	 * @param oldKeys
	 *            the old keys to renew
	 * @param issuerKeys
	 *            the optional issuer certificate and private key to use for
	 *            signing the certificate
	 * @param hostNames
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws GeneralSecurityException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair renewApplicationInstanceCertificate(
			String commonName, String organisation, String applicationUri,
			int validityTime,
			org.opcfoundation.ua.transport.security.KeyPair oldKeys,
			org.opcfoundation.ua.transport.security.KeyPair issuerKeys, String... hostNames) throws IOException, IllegalStateException,
			GeneralSecurityException {
		if (applicationUri == null)
			throw new NullPointerException("applicationUri must not be null");

		// use the same keypair for the new certificate
		PublicKey certPubKey = oldKeys.getCertificate().getCertificate()
				.getPublicKey();
		RSAPrivateKey certPrivKey = oldKeys.getPrivateKey().getPrivateKey();

		X509Certificate cert = generateCertificate(
				"CN=" + commonName
						+ (organisation == null ? "" : ", O=" + organisation), // +", C="+System.getProperty("user.country"),
				validityTime, applicationUri, new KeyPair(certPubKey, certPrivKey),
				issuerKeys, hostNames);


		// Encapsulate Certificate and private key to CertificateKeyPair
		Cert certificate = new Cert(cert);
		org.opcfoundation.ua.transport.security.PrivKey UAkey = new org.opcfoundation.ua.transport.security.PrivKey(
				(RSAPrivateKey) certPrivKey);
		return new org.opcfoundation.ua.transport.security.KeyPair(certificate,
				UAkey);
	}

	/**
	 * Renew a certificate KeyPair. Sign with the own key.
	 * 
	 * @param commonName
	 *            - Common Name (CN) for generated certificate
	 * @param organisation
	 *            - Organisation (O) for generated certificate
	 * @param applicationUri
	 *            - Alternative name (one of x509 extensiontype) for generated
	 *            certificate. Must not be null
	 * @param validityTime
	 *            - the time that the certificate is valid (in days)
	 * @param oldKeys
	 *            the old keys to renew
	 * @param hostNames
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws GeneralSecurityException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair renewApplicationInstanceCertificate(
			String commonName, String organisation, String applicationUri,
			int validityTime,
			org.opcfoundation.ua.transport.security.KeyPair oldKeys,
			String... hostNames) throws IOException, IllegalStateException,
			GeneralSecurityException {
		return renewApplicationInstanceCertificate(commonName, organisation, applicationUri, validityTime, oldKeys, null, hostNames);
	}

	private static int keySize = 2048;
	private static String certificateSignatureAlgorithm = "SHA256WithRSA";

	/**
	 * Define the algorithm to use for certificate signatures.
	 * <p>
	 * The OPC UA specification defines that the algorithm should be (at least)
	 * "SHA1WithRSA" for application instance certificates used for security
	 * policies Basic128Rsa15 and Basic256. For Basic256Sha256 it should be
	 * "SHA256WithRSA".
	 * <p>
	 * Default: "SHA256WithRSA"
	 * 
	 * @param certificateSignatureAlgorithm
	 *            the certificateSignatureAlgorithm to set
	 */
	public static void setCertificateSignatureAlgorithm(
			String certificateSignatureAlgorithm) {
		CertificateUtils.certificateSignatureAlgorithm = certificateSignatureAlgorithm;
	}

	/**
	 * @return the key size for new certificates
	 */
	public static int getKeySize() {
		return keySize;
	}

	/**
	 * Define the key size for the certificates.
	 * 
	 * Default: 2048
	 * 
	 * @param keySize
	 *            size of the certificates. Good values are multiples of 1024,2048(,3072) and 4096
	 * @throws IllegalArgumentException
	 *             if the value is not accepted
	 */
	public static void setKeySize(int keySize) {
		if (keySize < 1024 || keySize % 1024 != 0 || keySize > 4096)
			throw new IllegalArgumentException(
					"KeySize must be 1024, 2048, 3072 or 4096");
		CertificateUtils.keySize = keySize;
	}

	/**
	 * Create a X.509 V3 Certificate.
	 * 
	 * @param dn
	 *            the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param days
	 *            how many days from now the Certificate is valid for
	 * @param applicationUri
	 *            the application URI as defined in the ApplicationDescription
	 * @param keys
	 *            the public and private key to sign
	 * @param issuerKeys
	 *            the optional issuer certificate and private key to use for
	 *            signing the certificate. If null a self-signed certificate is generated
	 * @param hostNames
	 *            host names to add to the Subject Alternative Names field
	 * @param algorithm
	 *            the signing algorithm, eg "SHA1withRSA"
	 * @return the generated certificate
	 * @throws IOException
	 * 
	 * @throws GeneralSecurityException
	 */
	private static X509Certificate generateCertificate(String dn, int days,
			String applicationUri, KeyPair keys,
			org.opcfoundation.ua.transport.security.KeyPair issuerKeys, String... hostNames) throws GeneralSecurityException, IOException {

		PrivateKey privkey = keys.getPrivate();
		PublicKey publicKey = keys.getPublic();

		return generateCertificate(dn, days, applicationUri, publicKey, privkey,
				issuerKeys, hostNames);
	}

	/**
	 * Create a X.509 Certificate signed with the provided private key.
	 * @param dn
	 *            the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param days
	 *            how many days from now the Certificate is valid for
	 * @param applicationUri
	 *            the application URI as defined in the ApplicationDescription
	 * @param publicKey
	 * @param privateKey
	 * @param issuerKeys
	 *            the optional issuer certificate and private key to use for
	 *            signing the certificate. If null a self-signed certificate is generated
	 * @param hostNames
	 *            host names to add to the Subject Alternative Names field
	 * @return the generated certificate
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private static X509Certificate generateCertificate(String dn, int days,
			String applicationUri, PublicKey publicKey, PrivateKey privateKey,
			org.opcfoundation.ua.transport.security.KeyPair issuerKeys, String... hostNames) throws IOException,
			GeneralSecurityException {
		Date startDate = getCertificateStartDate();
		Date endDate = getCertificateEndDate(days);
		BigInteger sn = BigInteger.valueOf(System.currentTimeMillis());

		return getCertificateProvider().generateCertificate(dn, publicKey, privateKey, issuerKeys,
				startDate, endDate, sn, applicationUri, hostNames);
	}

	private static CertificateProvider certificateProvider;
	
	public static CertificateProvider getCertificateProvider() {
		if (certificateProvider == null) {
			if ("SC".equals(CryptoUtil.getSecurityProviderName())) {
				certificateProvider = new ScCertificateProvider();
			} else if ("BC".equals(CryptoUtil.getSecurityProviderName())) {
				certificateProvider = new BcCertificateProvider();
			} else if ("SunJCE".equals(CryptoUtil.getSecurityProviderName())) {
				certificateProvider = new SunJceCertificateProvider();
			} else {
				throw new RuntimeException("NO CRYPTO PROVIDER AVAILABLE!");
			}
		}
		return certificateProvider;
	}
	
	/**
	 * Define the preferred CertificateProvider. Usually this is determined
	 * automatically, but you may define the provider that you wish to use
	 * yourself.
	 * 
	 * @param certificateProvider
	 *            the certificateProvider to set
	 */
	public static void setCertificateProvider(CertificateProvider certificateProvider) {
		CertificateUtils.certificateProvider = certificateProvider;
	}

	private static Date getCertificateStartDate() {
		return new Date(System.currentTimeMillis() - 1000 * 60 * 60);
	}

	private static Date getCertificateEndDate(int days) {
		Calendar expiryTime = Calendar.getInstance();
		expiryTime.add(Calendar.DAY_OF_YEAR, days);
		return expiryTime.getTime();
	}


	/**
	 * 
	 * @param commonName
	 *            - Common Name (CN) for generated certificate
	 * @param organisation
	 *            - Organisation (O) for generated certificate
	 * @param applicationUri
	 *            - Alternative name (one of x509 extensiontype) for generated
	 *            certificate. Must not be null
	 * @param validityTime
	 *            - the time that the certificate is valid (in days)
	 * @param hostNames
	 *            - alternate host names or IP addresses to add to
	 *            SubjectAlternativeNames
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair createApplicationInstanceCertificate(
			String commonName, String organisation, String applicationUri,
			int validityTime, String... hostNames) throws IOException,
			GeneralSecurityException {
		return createApplicationInstanceCertificate(commonName, organisation,
				applicationUri, validityTime, null, hostNames);
	}
	
	/**
	 * 
	 * @param commonName
	 *            - Common Name (CN) for the generated certificate
	 * @param organisation
	 *            - Organisation (O) for the generated certificate
	 * @param applicationUri
	 *            - Alternative name (one of x509 extensiontype) for generated
	 *            certificate. Must not be null
	 * @param validityTime
	 *            - the time that the certificate is valid (in days)
	 * @param issuerKeys
	 *            the optional issuer certificate and private key to use for
	 *            signing the certificate. If null a self-signed certificate is generated
	 * @param hostNames
	 *            - alternate host names or IP addresses to add to
	 *            SubjectAlternativeNames
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair createApplicationInstanceCertificate(
			String commonName, String organisation, String applicationUri,
			int validityTime, org.opcfoundation.ua.transport.security.KeyPair issuerKeys, String... hostNames) throws IOException,
			GeneralSecurityException {
		if (applicationUri == null)
			throw new NullPointerException("applicationUri must not be null");
		// Add provider for generator
		if (logger.isDebugEnabled())
			logger.debug("createApplicationInstanceCertificate: getProviders={}", Arrays.toString(Security.getProviders()));

		KeyPair keyPair = generateKeyPair();
		
		// The fields appear in reverse order in the final certificate!
		String name = 
				"CN=" + commonName
				+ (organisation == null ? "" : ", O=" + organisation)
				+ ((hostNames == null || hostNames.length == 0) ? "" : ", DC=" + hostNames[0]); 

		X509Certificate cert;
		cert = generateCertificate(
					name,
					validityTime, applicationUri, keyPair, issuerKeys,
					hostNames);

		
		// Encapsulate Certificate and private key to CertificateKeyPair
		return toKeyPair(cert, keyPair.getPrivate());
	}

	public static org.opcfoundation.ua.transport.security.KeyPair toKeyPair(
			X509Certificate cert, PrivateKey privateKey)
			throws CertificateEncodingException {
		Cert certificate = new Cert(cert);
		org.opcfoundation.ua.transport.security.PrivKey UAkey = new org.opcfoundation.ua.transport.security.PrivKey(
				(RSAPrivateKey) privateKey);
		return new org.opcfoundation.ua.transport.security.KeyPair(certificate,
				UAkey);
	}

	/**
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(getKeySize());
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		return keyPair;
	}

	/**
	 * Create a new issuer certificate that can be used to issue certificates built
	 * with
	 * {@link #createApplicationInstanceCertificate(String, String, String, int, String...)}
	 * or
	 * {@link #createHttpsCertificate(String, String, int, org.opcfoundation.ua.transport.security.KeyPair)}
	 * 
	 * @param commonName
	 *            The common name to use for the Subject of the certificate (the
	 *            name will be prepended with "CN=" if it does not start with it
	 *            already)
	 * @param days
	 *            - the time that the certificate is valid (in days)
	 * @param issuerCert
	 *            - The certificate of the issuer that should sign the
	 *            certificate. If null, a self-signed certificate is created
	 * @return the new certificate and private key
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair createIssuerCertificate(
			String commonName, int days,
			org.opcfoundation.ua.transport.security.KeyPair issuerCert) throws GeneralSecurityException, IOException {
		KeyPair keyPair = generateKeyPair();
		Date startDate = getCertificateStartDate();
		Date endDate = getCertificateEndDate(days);
		BigInteger serialNr = BigInteger.valueOf(System.currentTimeMillis());
		if (!commonName.startsWith("CN="))
			commonName = "CN="+commonName;
		X509Certificate cert = getCertificateProvider().generateIssuerCert(keyPair.getPublic(), keyPair.getPrivate(),
				issuerCert, commonName, serialNr, startDate, endDate);
		return toKeyPair(cert, keyPair.getPrivate());
	}
	
	/**
	 * Create a new certificate that can be used with the HTTPS protocol. The
	 * certificate should be issued with a CA certificate, especially for the
	 * server applications, to ensure interoperability with other client
	 * applications.
	 * 
	 * @param hostName
	 *            - HostName of the computer in which the application is
	 *            running: used to initialize the Subject field of the
	 *            certificate. The client applications may validate this field
	 *            of the server certificate, so it should match the hostName
	 *            used in the ApplicationUri of the application.
	 * @param applicationUri
	 *            - The ApplicationUri corresponding to the respective field of
	 *            the ApplicationDescription of the application for which the
	 *            certificate is created
	 * @param days
	 *            - the time that the certificate is valid (in days)
	 * @param issuerCert
	 *            - The certificate of the issuer that should sign the
	 *            certificate. If null, a self-signed certificate is created
	 * @return the new certificate and private key
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static org.opcfoundation.ua.transport.security.KeyPair createHttpsCertificate(
			String hostName, String applicationUri,
			int days,  org.opcfoundation.ua.transport.security.KeyPair issuerCert) throws IOException,
			GeneralSecurityException {
		if (applicationUri == null)
			throw new NullPointerException("applicationUri must not be null");
		// Add provider for generator
		if (logger.isDebugEnabled())
			logger.debug("createApplicationInstanceCertificate: getProviders={}", Arrays.toString(Security.getProviders()));

		KeyPair keyPair = generateKeyPair();
		
		X509Certificate cert;
		cert = generateCertificate("CN="+hostName, days, applicationUri,
				keyPair.getPublic(), keyPair.getPrivate(),
				issuerCert);
		
		return toKeyPair(cert, keyPair.getPrivate());
	}

	public static void writeToPem(X509Certificate key, File file)
			throws IOException {
		getCertificateProvider().writeToPem(key, file, null, null);
	}

	/**
	 * Save the private key to a jks or pfx (PKCS12)-keystore.
	 * 
	 * @param storeLocation
	 *            save location of the keystore
	 * @param alias
	 *            alias used for the keypair
	 * @param privateKeyPassword
	 *            password to secure the private key, cannot be null for
	 *            keyStoreType "JKS"
	 * @param keyStorePassword
	 *            password to secure the key store
	 * @param keyStoreType
	 *            type of the key store, "JKS" and "PKCS12" supported
	 * @param privateKey
	 * 
	 * @return true on success
	 * 
	 * @throws IOException
	 *             if storeLocation is not available
	 * @throws NoSuchProviderException
	 *             The required security Provider not found
	 * @throws KeyStoreException
	 *             keystore failed
	 * @throws CertificateException
	 *             certificate problem
	 * @throws NoSuchAlgorithmException
	 *             cryptographic algorithm not found
	 */
	public static void saveToProtectedStore(PrivateKey privateKey,
			Certificate certificate, File storeLocation, String alias,
			String privateKeyPassword, String keyStorePassword,
			String keyStoreType) throws IOException, KeyStoreException,
			NoSuchProviderException, NoSuchAlgorithmException,
			CertificateException {
		KeyStore store = null;
		// initialize and open keystore

		if (keyStoreType.equals("PKCS12")) {
			// For some reason, this does not work with the SunJSSE provider
			store = KeyStore.getInstance(keyStoreType,
					CryptoUtil.getSecurityProviderName(KeyStore.class));
		} else
			store = KeyStore.getInstance(keyStoreType);

		store.load(null, null);

		// create certificate chain containing only 1 certificate
		Certificate[] chain = new Certificate[1];
		chain[0] = certificate;
		if (privateKeyPassword != null)
			store.setKeyEntry(alias, privateKey,
					privateKeyPassword.toCharArray(), chain);
		else
			store.setKeyEntry(alias, privateKey, null, chain);

		FileOutputStream fOut = new FileOutputStream(storeLocation);
		try {
			store.store(fOut, keyStorePassword == null ? null
					: keyStorePassword.toCharArray());
		} finally {
			fOut.close();
		}
	}

	public static String getCertificateSignatureAlgorithm() {
		return certificateSignatureAlgorithm;
	}
    
	/*
	 * This code is copied from the openJDK, which checks the AltNames correctly
	 * and raises exceptions. We try to enable reading the UriName field without
	 * raising exceptions, even if the format is invalid.
	 */
	protected static Collection<List<?>> getSubjectAlternativeNames(
			X509Certificate cert) throws CertificateParsingException {
		return getCertificateProvider().getSubjectAlternativeNames(cert);
	}

	
	/**
	 * @param applicationDescription
	 * @param certificate
	 * @return
	 * @throws CertificateParsingException
	 */
	public static String getApplicationUriOfCertificate(
			final X509Certificate certificate)
			throws CertificateParsingException {
		final Collection<List<?>> subjectAlternativeNames = getSubjectAlternativeNames(certificate);
		if (subjectAlternativeNames != null)
			for (List<?> altNames : subjectAlternativeNames) {
				int tagNo = (Integer) altNames.get(0);
				String name = (String) altNames.get(1);
				// System.out.println(tagNo + ": " + name);
				if (tagNo == NAME_URI)
					return name;
			}
		return "";
	}
	
	/**
	 * @param certificate
	 * @return
	 * @throws CertificateParsingException
	 */
	public static String getApplicationUriOfCertificate(Cert certificate)
			throws CertificateParsingException {
		return getApplicationUriOfCertificate(certificate.getCertificate());
	}	


}
