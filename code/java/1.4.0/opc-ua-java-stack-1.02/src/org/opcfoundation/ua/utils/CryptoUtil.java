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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.security.BcCryptoProvider;
import org.opcfoundation.ua.transport.security.CryptoProvider;
import org.opcfoundation.ua.transport.security.ScCryptoProvider;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm.AlgorithmType;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.security.SunJceCryptoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is class contains Cryptographic utilities
 * 
 * http://www.ietf.org/rfc/rfc2437.txt
 * 
 */
public class CryptoUtil {

	/**
	 * Log4J Error logger. Security failures are logged with INFO level.
	 * Security info are printed with DEBUG level. Unexpected errors are logged
	 * with ERROR level.
	 */
	static Logger LOGGER = LoggerFactory.getLogger(CryptoUtil.class);
	private final static SecureRandom random;

	static {
		// Load Bouncy Castle
		try {
			// Initialize the random number generator
			// If not done, it will self-seed, which in some Linux systems can
			// take a long while
			LOGGER.debug("CryptoUtil init");
			random = SecureRandom.getInstance("SHA1PRNG");
			LOGGER.debug("CryptoUtil init: random={}", random);
			random.setSeed(System.currentTimeMillis());
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	/** Hex chars for makeHexString-method **/
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	private static CryptoProvider cryptoProvider;

	private static String securityProviderName;

	/**
	 * Convenience method for {@link CryptoProvider#encryptAsymm}. Deprecated: Use
	 * {@link #encryptAsymm} instead.
	 * 
	 * @param input
	 * @param key
	 * @param algorithm
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws ServiceResultException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	@Deprecated
	public static byte[] asymmEncrypt(byte[] input, Key key,
			SecurityAlgorithm algorithm) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			ServiceResultException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		return encryptAsymm(input, (PublicKey) key, algorithm);
	}

	public static byte[] base64Decode(String string) {
		return getCryptoProvider().base64Decode(string);
	}

	public static String base64Encode(byte[] bytes) {
		return getCryptoProvider().base64Encode(bytes);
	}

	/**
	 * Create Message Authentication Code (MAC)
	 * 
	 * @param algorithm
	 *            encryption algorithm
	 * @param secret
	 * @return MAC
	 * @throws ServiceResultException
	 *             Bad_SecurityPolicyRejected algorithm not supported
	 */
	public static Mac createMac(SecurityAlgorithm algorithm, byte[] secret)
			throws ServiceResultException {
		return getCryptoProvider().createMac(algorithm, secret);
	}

	/**
	 * Create a non-repeatable set of bytes.
	 * 
	 * @param bytes
	 *            number of byte
	 * @return nonce
	 */
	public static byte[] createNonce(int bytes) {
		LOGGER.debug("createNonce: bytes={}", bytes);
		byte[] nonce = new byte[bytes];
		random.nextBytes(nonce);
		// LOGGER.debug("createNonce: nonce=" + nonce);
		return nonce;
	}

	public static byte[] createNonce(SecurityAlgorithm algorithm)
			throws ServiceResultException {
		return createNonce(getNonceLength(algorithm));
	}

	/**
	 * Convenience method for
	 * {@link CryptoProvider#decryptAsymm(PrivateKey, SecurityAlgorithm, byte[], byte[], int)}
	 * Possible to use only SecurityConfiguration instead of specifying
	 * SecurityAlgorithm explicitly.
	 * 
	 * @param decryptingKey
	 * @param profile
	 * @param dataToDecrypt
	 * @param output
	 * @param outputOffset
	 * @throws ServiceResultException
	 */
	public static void decryptAsymm(PrivateKey decryptingKey,
			SecurityConfiguration profile, byte[] dataToDecrypt, byte[] output,
			int outputOffset) throws ServiceResultException {
		
		CryptoUtil.getCryptoProvider().decryptAsymm(decryptingKey,
				profile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(),
				dataToDecrypt, output, outputOffset);
	}

	/**
	 * Convenience method for {@link CryptoProvider#encryptAsymm}.
	 * 
	 * @param input
	 * @param key
	 * @param algorithm
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws ServiceResultException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] encryptAsymm(byte[] input, PublicKey key,
			SecurityAlgorithm algorithm) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			ServiceResultException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		int outputBlockSize = getCipherBlockSize(algorithm, key);
		byte[] output = new byte[outputBlockSize];
		CryptoUtil.getCryptoProvider().encryptAsymm(key, algorithm, input,
				output, 0);
		return output;
	}

	/**
	 * Convenience method for
	 * {@link CryptoProvider#encryptAsymm(PublicKey, SecurityAlgorithm, byte[], byte[], int)}
	 * Possible to use only Certificate and SecurityConfiguration instead of
	 * specifying PublicKey and SecurityAlgorithm explicitly.
	 * 
	 * @param encryptingCertificate
	 *            Certificate which public key will be used during encryption.
	 * @param profile
	 *            Asymmetric encryption algorithm will be taken from this
	 *            SecurityConfiguration
	 * @param dataToEncrypt
	 *            Data to encrypt
	 * @param output
	 * @param outputOffset
	 * @throws ServiceResultException
	 */
	public static void encryptAsymm(Certificate encryptingCertificate,
			SecurityConfiguration profile, byte[] dataToEncrypt, byte[] output,
			int outputOffset) throws ServiceResultException {
		LOGGER.info("encryptAsymm called.");
		CryptoUtil.getCryptoProvider().encryptAsymm(
				encryptingCertificate.getPublicKey(),
				profile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(),
				dataToEncrypt, output, outputOffset);
	}

	public static String[] filterCipherSuiteList(String[] cipherSuiteSet,
			String[] cipherSuitePatterns) {
		List<String> result = new ArrayList<String>(cipherSuiteSet.length);
		Pattern[] patterns = new Pattern[cipherSuitePatterns.length];

		int c = cipherSuitePatterns.length;
		for (int i = 0; i < c; i++)
			patterns[i] = Pattern.compile(cipherSuitePatterns[i]);

		nextCipherSuite: for (String cipherSuite : cipherSuiteSet) {
			for (Pattern p : patterns) {
				Matcher m = p.matcher(cipherSuite);
				if (m.matches()) {
					result.add(cipherSuite);
					continue nextCipherSuite;
				}
			}
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Create signer instance using an algorithm uri.
	 * http://www.ietf.org/rfc/rfc2437.txt Ciphers are defined in PKCS #1: RSA
	 * Cryptography Specifications
	 * 
	 * @param algorithm
	 *            UA Specified algorithm
	 * @return Cipher
	 * @throws ServiceResultException
	 *             if algorithm is not supported by the stack
	 */
	public static Cipher getAsymmetricCipher(SecurityAlgorithm algorithm)
			throws ServiceResultException {
		if (algorithm == null)
			throw new IllegalArgumentException();

		try {
			// http://www.w3.org/2001/04/xmlenc#rsa-1_5
			if (algorithm.equals(SecurityAlgorithm.Rsa15))
				return Cipher.getInstance("RSA");

			// http://www.w3.org/2001/04/xmlenc#rsa-oaep
			if (algorithm.equals(SecurityAlgorithm.RsaOaep))
				// return
				// Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
				return Cipher.getInstance(
						"RSA/NONE/OAEPWithSHA1AndMGF1Padding",
						CryptoUtil.getSecurityProviderName());

		} catch (NoSuchAlgorithmException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		} catch (NoSuchPaddingException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		} catch (NoSuchProviderException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}
		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected,
				"Unsupported asymmetric signature algorithm: " + algorithm);
	}

	public static int getAsymmInputBlockSize(SecurityAlgorithm algorithm)
			throws ServiceResultException {
		// http://www.w3.org/2001/04/xmlenc#rsa-1_5
		if (algorithm.equals(SecurityAlgorithm.Rsa15))
			return 117;

		// http://www.w3.org/2001/04/xmlenc#rsa-oaep
		if (algorithm.equals(SecurityAlgorithm.RsaOaep))
			return 86;

		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected,
				"Unsupported asymmetric signature algorithm: {0}, " + algorithm);
	}

	/**
	 * Get cipher block (=output) size in bytes
	 * 
	 * @param algorithm
	 *            algorithm
	 * @param key
	 *            Optional, required for asymmetric encryption algorithms
	 * @return cipher block size
	 * @throws ServiceResultException
	 *             Bad_SecurityPolicyRejected algorithm not supported
	 */
	public static int getCipherBlockSize(SecurityAlgorithm algorithm, Key key)
			throws ServiceResultException {
		// No security
		if (algorithm == null)
			return 1;

		// Symmetric encryption
		AlgorithmType type = algorithm.getType();
		if (type.equals(SecurityAlgorithm.AlgorithmType.SymmetricEncryption))
			return 16; // 128 bits fixed for AES independent of the key size
		if (type.equals(SecurityAlgorithm.AlgorithmType.AsymmetricSignature))
			return algorithm.getKeySize() / 8;

		// Asymmetric encryption
		if (type.equals(SecurityAlgorithm.AlgorithmType.AsymmetricEncryption)) {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8;

			if (key instanceof RSAPrivateKey)
				return ((RSAPrivateKey) key).getModulus().bitLength() / 8;
		}

		// Asymmetric signature
		// if (algorithm.equals(SecurityAlgorithm.RsaSha1)) {
		// return 160/8;
		// }
		// if (algorithm.equals(SecurityAlgorithm.RsaSha256)) {
		// return 256/8;
		// }

		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected, algorithm.getUri());
	}

	/**
	 * Create an intersection of two lists of cipher suite lists
	 * 
	 * @param cipherSuiteSet1
	 *            enabled cipher suites
	 * @param cipherSuiteSet2
	 *            filter list
	 * @param omitProtocol
	 *            if true the first 3 characters are ignored in compare
	 * @return an intersection of suites
	 */
	public static String[] getCipherSuiteIntersection(String[] cipherSuiteSet1,
			String[] cipherSuiteSet2, boolean omitProtocol) {
		List<String> result = new ArrayList<String>(Math.max(
				cipherSuiteSet1.length, cipherSuiteSet2.length));
		Set<String> set = new TreeSet<String>();
		for (String cs : cipherSuiteSet2)
			set.add(omitProtocol ? cs.substring(3) : cs);
		for (String cs : cipherSuiteSet1) {
			if (set.contains(omitProtocol ? cs.substring(3) : cs))
				result.add(cs);
		}
		return result.toArray(new String[result.size()]);
	}

	public static CryptoProvider getCryptoProvider() {
		if (cryptoProvider == null) {
			if ("SC".equals(getSecurityProviderName())) {
				cryptoProvider = new ScCryptoProvider();
			} else if ("BC".equals(getSecurityProviderName())) {
				cryptoProvider = new BcCryptoProvider();
			} else if ("SunJCE".equals(getSecurityProviderName())) {
				cryptoProvider = new SunJceCryptoProvider();
			} else {
				throw new RuntimeException("NO CRYPTO PROVIDER AVAILABLE!");
			}
		}
		return cryptoProvider;
	}

	/**
	 * Returns the length of the nonce to be used with an asymmetric or
	 * symmetric encryption algorithm.
	 * <p>
	 * For symmetric algorithms, returns the algorithm key size (in bytes). For
	 * asymmetric algorithms, returns 32.
	 * 
	 * @param algorithm
	 *            encryption algorithm or null (=no encryption)
	 * @return the length of the nonce in bytes
	 * @throws ServiceResultException
	 *             Bad_SecurityPolicyRejected, if the algorithm is not supported
	 */
	public static int getNonceLength(SecurityAlgorithm algorithm)
			throws ServiceResultException {
		if (algorithm == null)
			return 0;
		if (algorithm.equals(SecurityAlgorithm.Rsa15))
			return 32;
		if (algorithm.equals(SecurityAlgorithm.RsaOaep))
			return 32;
		if (SecurityAlgorithm.AlgorithmType.SymmetricEncryption == algorithm
				.getType())
			return algorithm.getKeySize() / 8;
		LOGGER.error("getNonceLength: Unsupported algorithm={}", algorithm);
		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected, algorithm.getUri());
	}

	/**
	 * Get plain text block (=input) size in bytes
	 * 
	 * @param securityAlgorithm
	 *            algorithm
	 * @param key
	 *            Optional, required for asymmetric encryption algorithms
	 * @return cipher block size
	 * @throws ServiceResultException
	 *             Bad_SecurityPolicyRejected algorithm not supported
	 */
	public static int getPlainTextBlockSize(
			SecurityAlgorithm securityAlgorithm, Key key)
			throws ServiceResultException {
		// No security
		if (securityAlgorithm == null)
			return 1;

		if (securityAlgorithm.equals(SecurityAlgorithm.Rsa15)) {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8 - 11;
		}

		if (securityAlgorithm.equals(SecurityAlgorithm.RsaOaep)) {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8 - 42;
		}

		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected,
				securityAlgorithm.getUri());
	}

	/**
	 * @return the random
	 */
	public static SecureRandom getRandom() {
		return random;
	}

	/**
	 * The Preferred Security Provider name. Will check if a Spongy Castle (on
	 * Android) or Bouncy Castle provider is already available or if such can be
	 * initialized from the respective class.
	 * <p>
	 * If none of these is available will default to SunJCE or the first
	 * initialized provider, if SunJCE is not available either.
	 * 
	 * @return the provider name to use for specific crypto tasks
	 * @throws RuntimeException
	 *             if none is available and none cannot be initialized.
	 */
	public static String getSecurityProviderName() {
		if (securityProviderName == null) {
			Provider provider = null;
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Providers={}",
						Arrays.toString(Security.getProviders()));
			boolean isAndroid = System.getProperty("java.runtime.name")
					.toLowerCase().contains("android");
			if (isAndroid) {
				if (Security.getProvider("SC") != null)
					securityProviderName = "SC";
				else {
					provider = hasClass("org.spongycastle.jce.provider.BouncyCastleProvider");
					if (provider != null)
						securityProviderName = "SC";
				}
			} else if (Security.getProvider("BC") != null)
				securityProviderName = "BC";
			else {
				if (provider == null) {
					provider = hasClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
					if (provider != null)
						securityProviderName = "BC";
				}
				if (provider == null) {
					provider = hasClass("com.sun.crypto.provider.SunJCE");
				}
				if (provider == null) {
					Provider[] providers = Security.getProviders();
					if (providers == null || providers.length == 0)
						throw new RuntimeException(
								"No security providers available!");
					provider = providers[0];
				}
				if (provider != null)
					securityProviderName = provider.getName();

			}
			if (securityProviderName != null)
				LOGGER.info("Using SecurityProvider {}", securityProviderName);
			else
				throw new RuntimeException("NO SECURITY PROVIDER AVAILABLE!");
		}
		return securityProviderName;
	}

	public static String getSecurityProviderName(Class<?> class1) {
		if ("SunJCE".equals(getSecurityProviderName())) {
			if (Signature.class.equals(class1))
				return "SunRsaSign";
			if (KeyStore.class.equals(class1))
				return "SunJSSE";
		}
		return getSecurityProviderName();
	}

	/**
	 * Get signature size in bytes
	 * 
	 * @param signatureAlgorithm
	 * @param key
	 * @return signature size in bytes
	 * @throws ServiceResultException
	 *             Bad_SecurityPolicyRejected algorithm not supported
	 */
	public static int getSignatureSize(SecurityAlgorithm signatureAlgorithm,
			Key key) throws ServiceResultException {
		if (signatureAlgorithm == null)
			return 0;
		if (signatureAlgorithm.getType().equals(
				SecurityAlgorithm.AlgorithmType.SymmetricSignature))
			return signatureAlgorithm.getKeySize() / 8;
		else {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8;
			if (key instanceof RSAPrivateKey)
				return ((RSAPrivateKey) key).getModulus().bitLength() / 8;
		}
		// if (signatureAlgorithm.equals(SecurityAlgorithm.HmacSha1))
		// return 160 / 8;
		// if (signatureAlgorithm.equals(SecurityAlgorithm.HmacSha256))
		// return 256/8;

		if (signatureAlgorithm.equals(SecurityAlgorithm.RsaSha1)) {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8;
			if (key instanceof RSAPrivateKey)
				return ((RSAPrivateKey) key).getModulus().bitLength() / 8;
		}

		// TODO: OK?
		if (signatureAlgorithm.equals(SecurityAlgorithm.RsaSha256)) {
			if (key instanceof RSAPublicKey)
				return ((RSAPublicKey) key).getModulus().bitLength() / 8;
			if (key instanceof RSAPrivateKey)
				return ((RSAPrivateKey) key).getModulus().bitLength() / 8;
		}

		throw new ServiceResultException(
				StatusCodes.Bad_SecurityPolicyRejected,
				signatureAlgorithm.getUri());
	}

	public static byte[] hexToBytes(String s) {
		if (s == null)
			return null;
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Define the preferred CryptoProvider. Usually this is determined
	 * automatically, but you may define the provider that you wish to use
	 * yourself.
	 * 
	 * @param cryptoProvider
	 *            the cryptoProvider to set
	 */
	public static void setCryptoProvider(CryptoProvider cryptoProvider) {
		CryptoUtil.cryptoProvider = cryptoProvider;
	}

	/**
	 * Define the preferred SecurityProvider. Usually this is determined
	 * automatically, but you may define the provider name that you wish to use
	 * yourself.
	 * 
	 * @param securityProviderName
	 *            the securityProviderName to set, e.g. "BC" for
	 *            BouncyCastleProvider
	 */
	public static void setSecurityProviderName(String securityProviderName) {
		CryptoUtil.securityProviderName = securityProviderName;
	}

	/**
	 * @param signerKey
	 *            the private key used to sign the data
	 * @param algorithm
	 *            asymmetric signer algorithm, See {@link SecurityAlgorithm}
	 * @param dataToSign
	 *            the data to sign
	 * @return SignatureData
	 * @throws ServiceResultException
	 *             if the signing fails. Read the StatusCode and cause for more
	 *             details
	 */
	public static SignatureData signAsymm(PrivateKey signerKey,
			SecurityAlgorithm algorithm, byte[] dataToSign)
			throws ServiceResultException {
		if (algorithm == null)
			return new SignatureData(null, null);
		return new SignatureData(algorithm.getUri(), getCryptoProvider()
				.signAsymm(signerKey, algorithm, dataToSign));
	}

	/**
	 * Convenience method for "displaying" a hex-string of a given byte array.
	 * Calls {@link #toHex(byte[], int)} with bytesPerRow=0 (no line breaks)
	 * 
	 * @param bytes
	 *            the byte array to "display"
	 */
	public static String toHex(byte[] bytes) {
		return toHex(bytes, (bytes != null && bytes.length > 64) ? 64 : 0);
	}

	/**
	 * Convenience method for "displaying" a hex-string of a given byte array.
	 * Breaks the string to lines, if bytesPerRow > 0.
	 * 
	 * @param bytes
	 *            the byte array to "display"
	 * @param bytesPerRow
	 *            number of bytes to include on a text row. If it is 0, no line
	 *            breaks are added.
	 * 
	 */
	public static String toHex(byte[] bytes, int bytesPerRow) {
		if (bytes == null)
			return "(null)";
		StringBuffer sb = new StringBuffer();
		sb.append("[" + bytes.length + "] 0x");
		int i = 0;
		while (i < bytes.length) {
			if (bytesPerRow > 0 && i % bytesPerRow == 0)
				sb.append("\n");
			sb.append(HEX_CHARS[(bytes[i] >> 4) & 0x0F]);
			sb.append(HEX_CHARS[bytes[i] & 0x0F]);
			i++;
		}
		return sb.toString();
	}

	/**
	 * Verify a signature.
	 * 
	 * @param certificate
	 *            the certificate used to verify the signature
	 * @param algorithm
	 *            the signature algorithm
	 * @param data
	 *            data to verify
	 * @param signature
	 *            the signature to verify
	 * @return true if the signature is valid
	 * @throws ServiceResultException
	 *             if the verification fails
	 */
	public static boolean verifyAsymm(X509Certificate certificate,
			SecurityAlgorithm algorithm, byte[] data, byte[] signature)
			throws ServiceResultException {
		return getCryptoProvider().verifyAsymm(certificate.getPublicKey(),
				algorithm, data, signature);
	}

	private static Provider hasClass(String className) {
		try {
			Class<?> providerClass = CryptoUtil.class.getClassLoader()
					.loadClass(className);
			try {
				Provider provider = (Provider) providerClass.getConstructor()
						.newInstance();
				Security.addProvider(provider);
				LOGGER.info("SecurityProvider initialized from {}",
						providerClass.getName());
				return provider;
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot add Security provider class="
								+ providerClass.getName(), e);
			}
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

}
