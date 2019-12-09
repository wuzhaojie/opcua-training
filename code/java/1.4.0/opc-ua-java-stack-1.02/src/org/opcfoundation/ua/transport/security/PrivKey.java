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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.utils.BouncyCastleUtils;
import org.opcfoundation.ua.utils.CertificateUtils;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.FileUtil;


/**
 * Valid and encodeable private key.
 * Wrapper to {@link java.security.PrivateKey}
 */
public class PrivKey {

	private static final String END_RSA_PRIVATE_KEY = "\n-----END RSA PRIVATE KEY-----";
	private static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n";
	private static final String END_PRIVATE_KEY_REGEX = "-----END .*PRIVATE KEY-----";
	private static final String BEGIN_PRIVATE_KEY_REGEX = "-----BEGIN .*PRIVATE KEY-----";

	public final RSAPrivateKey privateKey;
	private static Logger logger = LoggerFactory.getLogger(PrivKey.class);
	/**
	 * Load private key from a PKCS12 key store
	 * 
	 * @param keystoreUrl url to key store
	 * @param password password to key store
	 * @return private key
	 * @throws IOException
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 */
	public static PrivKey loadFromKeyStore(URL keystoreUrl, String password) throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		RSAPrivateKey key = CertificateUtils.loadFromKeyStore(keystoreUrl, password);
		return new PrivKey(key);
	}	

	/**
	 * Load private key from a PEM encoded file
	 * 
	 * @param file
	 * @param password
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidParameterSpecException 
	 */
	public static PrivKey load(File file, final String password)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException  {
		if (file.length() < 3)
			throw new IllegalArgumentException("file is not a valid private key (too short file)");
		byte[] keyData = FileUtil.readFile(file);
		return load(keyData, password);
	}
		
	public static PrivKey load(InputStream is, final String password)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException  {
		byte[] keyData = FileUtil.readStream(is);
		return load(keyData, password);
	}
	
	/**
	 * Load private key from key data
	 * 
	 * @param keydata
	 * @param password
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidParameterSpecException 
	 */
	public static PrivKey load(byte[] keyBytes, final String password)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException  {

		boolean isEncrypted = false;
		String dekAlgName = "";
		byte[] ivBytes = null;
			
		// this code is ...  
		if (
		((keyBytes[0] == (byte) '-' && keyBytes[1] == (byte) '-' && keyBytes[2] == (byte) '-')) ||
		((keyBytes[3] == (byte) '-' && keyBytes[4] == (byte) '-' && keyBytes[5] == (byte) '-')) ) {
			// BASE64 encoded
			String privKeyPEM = new String(keyBytes);
			String[] dekInfo;
			Scanner scanner = new Scanner(privKeyPEM);
			StringBuilder keyString;
			boolean isBase64Encoded;
			try {
				keyString = new StringBuilder();
				String ivString = "";
				isBase64Encoded = false;
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.matches(BEGIN_PRIVATE_KEY_REGEX))
						isBase64Encoded = true;
					else if (line.matches(END_PRIVATE_KEY_REGEX))
						break;
					else if (line.startsWith("Proc-Type: 4,ENCRYPTED"))
						isEncrypted = true;
					else if (line.startsWith("DEK-Info:")) {
						dekInfo = line.substring(10).split(",");
						dekAlgName = dekInfo[0];
						ivString = dekInfo[1];
						ivBytes = CryptoUtil.hexToBytes(ivString);
					} else
						keyString.append(line.trim());
				}
			} finally {
				scanner.close();
			}
			if (isBase64Encoded)
				keyBytes = CryptoUtil.base64Decode(keyString.toString());
		}

		if (isEncrypted) {
			if (password == null || password.isEmpty())
				throw new SecurityException("Encrypted private key requires a password.");
			// Decrypt the data first
			IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			String[] dekAlgParts = dekAlgName.split("-");
			String alg = dekAlgParts[0];
			int keyBits = dekAlgParts.length > 1 ? Integer.parseInt(dekAlgParts[1]) : 128;
			if (true) {
				byte[] salt = ivBytes;
				if (salt.length > 8) {
					salt = new byte[8];
					System.arraycopy(ivBytes, 0, salt, 0, 8);
				}

			byte[] sKey = generateDerivedKey(keyBits / 8,
						password.getBytes("ASCII"), salt);
				SecretKeySpec keySpec = new SecretKeySpec(sKey, alg);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
				keyBytes = cipher.doFinal(keyBytes);
			}
		}
		return new PrivKey(keyBytes);
	}

	private static byte[] generateDerivedKey(
	        int bytesNeeded, byte[] password, byte[] salt) throws NoSuchAlgorithmException
	    {
	        MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[]  buf = new byte[digest.getDigestLength()];
	        byte[]  key = new byte[bytesNeeded];
	        int     offset = 0;
	        
	        for (;;)
	        {
	            digest.update(password, 0, password.length);
	            digest.update(salt, 0, salt.length);

	            buf = digest.digest();
	            
	            int len = (bytesNeeded > buf.length) ? buf.length : bytesNeeded;
	            System.arraycopy(buf, 0, key, offset, len);
	            offset += len;

	            // check if we need any more
	            bytesNeeded -= len;
	            if (bytesNeeded == 0)
	            {
	                break;
	            }

	            // do another round
	            digest.reset();
	            digest.update(buf, 0, buf.length);
	        }
	        
	        return key;
	    }
	
	/**
	 * Load private key from a file. 
	 * <p>
	 * @Deprecated Use {@link #load(File, String)} or {@link #loadFromKeyStore(File, String)} instead 
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@Deprecated
	public static PrivKey load(File file) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException
	{
		byte[] encoded = FileUtil.readFile(file);
		return new PrivKey(encoded);
	}	
	
	/**
	 * Load private key from a key store (PKCS12) file
	 * 
	 * @param file key store file
	 * @param password password to key store
	 * @return private key
	 * @throws IOException
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 */
	public static PrivKey loadFromKeyStore(File file, String password) throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		return loadFromKeyStore( file.toURI().toURL(), password );
	}
	
	/**
	 * Save the key in a binary file. Note that the file is not secured by a password.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void save(File file)
	throws IOException
	{
		FileUtil.writeFile(file, getEncodedPrivateKey());
	}
	
	/**
	 * Save the private key to a PEM file
	 * 
	 * @param file the file
	 * @param privateKeyPassword the password used to store the key
	 * @throws IOException
	 */
	public void save(File file, String privateKeyPassword) throws IOException
	{
		if (privateKeyPassword == null || privateKeyPassword.length() == 0) {
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(getPrivateKey()
					.getEncoded());
			FileWriter fw = new FileWriter(file);
			try {
				fw.append(BEGIN_RSA_PRIVATE_KEY);
				fw.append(CryptoUtil.base64Encode(spec.getEncoded()));
				fw.append(END_RSA_PRIVATE_KEY);
			} finally {
				fw.close();
			}
		}
		else
		{
			savePemWithBC(file, privateKeyPassword);
		}
	}

	/**
	 * @param file
	 * @param privateKeyPassword
	 * @throws IOException
	 */
	private void savePemWithBC(File file, String privateKeyPassword)
			throws IOException {
		BouncyCastleUtils.writeToPem(getPrivateKey(), file, privateKeyPassword, "AES-128-CBC");
	}

	public PrivKey(byte[] encodedPrivateKey) 
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException
	{
		if (encodedPrivateKey==null) throw new IllegalArgumentException("null arg");
		this.privateKey = decodeRSAPrivateKey(encodedPrivateKey);
	}

	private RSAPrivateKey decodeRSAPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		// Try to read the private key with the default provider first
		try
		{
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) kf.generatePrivate(spec);
		} catch (Exception e) {
			// For some reason the Sun provider cannot read all keys: try with Bouncy Castle if it fails
			try {
				KeyFactory kf = KeyFactory.getInstance("RSA", CryptoUtil.getSecurityProviderName());
				return (RSAPrivateKey) kf.generatePrivate(spec);
			} catch (NoSuchProviderException e1) {
				logger.error("Could not read private key with default Provider and Bouncy Castle not available");
				throw new RuntimeException("Could not read private key with default Provider and Bouncy Castle not available", e1); 
			}
		}
	}

	public PrivKey(RSAPrivateKey privateKey)
	{
		this.privateKey = privateKey;
	}
	
	public byte[] getEncodedPrivateKey() 
	{
		return privateKey.getEncoded();
	}
	
	public RSAPrivateKey getPrivateKey()
	{
		return privateKey;
	}

	/**
	 * Save the identity to a password protected keystore.
	 * 
	 * @param cert
	 *            the certificate used to chain the key
	 * @param file
	 *            the file used to store the key
	 * @param privateKeyPassword
	 *            the password to secure the private key, must not be null for
	 *            JKS
	 * @param keyStorePassword
	 *            the password to the key store, must not be null
	 * @param keyStoreType
	 *            key store type, either "PKCS12" or "JKS"
	 * 
	 * @throws IOException
	 *             if the file cannot be written to
	 * @throws NoSuchProviderException
	 *             Bouncy Castle Provider not found
	 * @throws KeyStoreException
	 *             keystore failed
	 * @throws CertificateException
	 *             certificate problem
	 * @throws NoSuchAlgorithmException
	 *             cryptographic algorithm not found
	 */
	public void saveToKeyStore(Cert cert, File file, 
			String privateKeyPassword, String keyStorePassword, String keyStoreType) throws IOException, KeyStoreException,
			NoSuchProviderException, NoSuchAlgorithmException,
			CertificateException {
		String alias = "key";
		CertificateUtils.saveToProtectedStore(getPrivateKey(), cert.getCertificate(),
				file, alias, privateKeyPassword, keyStorePassword,
				keyStoreType);
	
	}
	
}
