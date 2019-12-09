package org.opcfoundation.ua.transport.security;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.tcp.impl.SecurityToken;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.pkcs.RSAPrivateKey;
import org.spongycastle.asn1.pkcs.RSAPublicKey;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.CryptoException;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.Signer;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.encodings.OAEPEncoding;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.engines.RijndaelEngine;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.spongycastle.crypto.signers.RSADigestSigner;
import org.spongycastle.util.encoders.Base64;

public class ScCryptoProvider implements CryptoProvider {

	static Logger logger = LoggerFactory.getLogger(ScCryptoProvider.class);

	public ScCryptoProvider() {

	}

	@Override
	public byte[] base64Decode(String string) {
		return Base64.decode(string);
	}

	@Override
	public String base64Encode(byte[] bytes) {
		try {
			return new String(Base64.encode(bytes), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * private Certificate getCertificate(java.security.cert.Certificate cert)
	 * throws CertificateEncodingException { return
	 * Certificate.getInstance(cert.getEncoded()); }
	 */
	@Override
	public Mac createMac(SecurityAlgorithm algorithm, byte[] secret)
			throws ServiceResultException {
		SecretKeySpec keySpec = new SecretKeySpec(secret,
				algorithm.getStandardName());
		Mac hmac;
		try {
			hmac = Mac.getInstance(algorithm.getStandardName());
			hmac.init(keySpec);
		} catch (InvalidKeyException e) {
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityChecksFailed, e);
		} catch (GeneralSecurityException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}
		return hmac;
	}

	/*
	 * @Override public void encryptAsymm(SecurityConfiguration profile, byte[]
	 * dataToEncrypt, java.security.cert.Certificate encryptingCertificate,
	 * byte[] output, int outputOffset) throws ServiceResultException {
	 * 
	 * SecurityPolicy policy = profile.getSecurityPolicy(); SecurityAlgorithm
	 * algorithm = policy.getAsymmetricEncryptionAlgorithm();
	 * 
	 * try { encryptAsymm(algorithm, dataToEncrypt,
	 * getCertificate(encryptingCertificate), output, outputOffset); } catch
	 * (CertificateEncodingException e) { throw new
	 * ServiceResultException(StatusCodes.Bad_InternalError, e); }
	 * 
	 * }
	 */

	@Override
	public int decryptAsymm(PrivateKey decryptingKey,
			SecurityAlgorithm algorithm, byte[] dataToDecrypt, byte[] output,
			int outputOffset) throws ServiceResultException {

		java.security.interfaces.RSAPrivateCrtKey rsaPrivateKey = (java.security.interfaces.RSAPrivateCrtKey) decryptingKey;
		RSAPrivateKey privateKey = new RSAPrivateKey(
				rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent(),
				rsaPrivateKey.getPrivateExponent(), rsaPrivateKey.getPrimeP(),
				rsaPrivateKey.getPrimeQ(), rsaPrivateKey.getPrimeExponentP(),
				rsaPrivateKey.getPrimeExponentQ(),
				rsaPrivateKey.getCrtCoefficient());

		AsymmetricBlockCipher cipher = getAsymmetricCipher(algorithm,
				privateKey);

		try {

			int len = 0;
			int inputBlockSize = cipher.getInputBlockSize();
			int outputBlockSize = cipher.getOutputBlockSize();
			logger.info(
					"Decrypt: inputBlockSize={}, outputBlockSize={}, dataToDecrypt.length={}",
					inputBlockSize, outputBlockSize, dataToDecrypt.length);
			for (int i = 0; i < dataToDecrypt.length; i += inputBlockSize) {
				int size = Math.min(dataToDecrypt.length - i, inputBlockSize);
				byte[] tmp = cipher.processBlock(dataToDecrypt, i, size);
				System.arraycopy(tmp, 0, output, outputOffset + len, tmp.length);
				len += tmp.length;
			}
			return len;

		} catch (CryptoException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}

	}

	/*
	 * private RSAPublicKey getRSAPublicKey(Certificate cert) {
	 * SubjectPublicKeyInfo subjectPublicKeyInfo = cert
	 * .getSubjectPublicKeyInfo(); DERBitString publicKeyData =
	 * subjectPublicKeyInfo.getPublicKeyData(); return
	 * RSAPublicKey.getInstance(publicKeyData.getBytes()); }
	 */

	/*
	 * @Override public int decryptAsymm(SecurityConfiguration profile, byte[]
	 * dataToDecrypt, java.security.cert.Certificate decryptingCertificate,
	 * byte[] output, int outputOffset) throws ServiceResultException {
	 * 
	 * java.security.interfaces.RSAPrivateCrtKey rsaPrivateKey =
	 * (java.security.interfaces.RSAPrivateCrtKey) profile
	 * .getLocalPrivateKey(); RSAPrivateKey privateKey = new RSAPrivateKey(
	 * rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent(),
	 * rsaPrivateKey.getPrivateExponent(), rsaPrivateKey.getPrimeP(),
	 * rsaPrivateKey.getPrimeQ(), rsaPrivateKey.getPrimeExponentP(),
	 * rsaPrivateKey.getPrimeExponentQ(), rsaPrivateKey.getCrtCoefficient());
	 * return decryptAsymm(profile, dataToDecrypt, privateKey, output,
	 * outputOffset);
	 * 
	 * }
	 */

	@Override
	public int decryptSymm(SecurityToken token, byte[] dataToDecrypt,
			int inputOffset, int inputLength, byte[] output, int outputOffset)
					throws ServiceResultException {

		BufferedBlockCipher cipher = new BufferedBlockCipher(
				new CBCBlockCipher(new AESEngine()));

		cipher.init(
				false,
				new ParametersWithIV(new KeyParameter(token
						.getRemoteEncryptingKey()), token
						.getRemoteInitializationVector()));

		int decryptedBytes = cipher.processBytes(dataToDecrypt, inputOffset,
				inputLength, output, outputOffset);

		try {

			decryptedBytes += cipher.doFinal(output, outputOffset
					+ decryptedBytes);
			return decryptedBytes;

		} catch (DataLengthException e) {
			logger.error("Input data is not an even number of encryption blocks.");
			throw new ServiceResultException(
					StatusCodes.Bad_InternalError,
					"Error in symmetric decrypt: Input data is not an even number of encryption blocks.");
		} catch (CryptoException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}

	}

	public void encryptAsymm(PublicKey encryptingCertificate,
			SecurityAlgorithm algorithm, byte[] dataToEncrypt, byte[] output,
			int outputOffset) throws ServiceResultException {

		try {
			java.security.interfaces.RSAPublicKey encryptingCertificateRSA = (java.security.interfaces.RSAPublicKey) encryptingCertificate;
			RSAPublicKey publicKey = new RSAPublicKey(
					encryptingCertificateRSA.getModulus(),
					encryptingCertificateRSA.getPublicExponent());
			AsymmetricBlockCipher cipher = getAsymmetricCipher(algorithm,
					publicKey);

			int len = 0;
			int inputBlockSize = cipher.getInputBlockSize();
			int outputBlockSize = cipher.getOutputBlockSize();
			logger.info(
					"Encrypt: inputBlockSize={}, outputBlockSize={}, dataToEncrypt.length={}",
					inputBlockSize, outputBlockSize, dataToEncrypt.length);
			for (int i = 0; i < dataToEncrypt.length; i += inputBlockSize) {
				int size = Math.min(dataToEncrypt.length - i, inputBlockSize);
				byte[] tmp = cipher.processBlock(dataToEncrypt, i, size);
				System.arraycopy(tmp, 0, output, outputOffset + len, tmp.length);
				len += tmp.length;
			}

		} catch (InvalidCipherTextException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}

	}

	/*
	 * public int decryptAsymm(SecurityConfiguration profile, byte[]
	 * dataToDecrypt, RSAPrivateKey privateKey, byte[] output, int outputOffset)
	 * throws ServiceResultException {
	 * 
	 * SecurityPolicy policy = profile.getSecurityPolicy(); SecurityAlgorithm
	 * algorithm = policy.getAsymmetricEncryptionAlgorithm();
	 * AsymmetricBlockCipher cipher = getAsymmetricCipher(algorithm,
	 * privateKey);
	 * 
	 * try {
	 * 
	 * int len = 0; int inputBlockSize = cipher.getInputBlockSize(); int
	 * outputBlockSize = cipher.getOutputBlockSize(); logger.info(
	 * "Decrypt: inputBlockSize={}, outputBlockSize={}, dataToDecrypt.length={}"
	 * , inputBlockSize, outputBlockSize, dataToDecrypt.length); for (int i = 0;
	 * i < dataToDecrypt.length; i += inputBlockSize) { int size =
	 * Math.min(dataToDecrypt.length - i, inputBlockSize); byte[] tmp =
	 * cipher.processBlock(dataToDecrypt, i, size); System.arraycopy(tmp, 0,
	 * output, outputOffset + len, tmp.length); len += tmp.length; } return len;
	 * 
	 * } catch (CryptoException e) { throw new
	 * ServiceResultException(StatusCodes.Bad_InternalError, e); }
	 * 
	 * }
	 */
	@Override
	public int encryptSymm(SecurityToken token, byte[] dataToEncrypt,
			int inputOffset, int inputLength, byte[] output, int outputOffset)
					throws ServiceResultException {

		// BufferedBlockCipher cipher = new BufferedBlockCipher(new
		// CBCBlockCipher(new AESEngine()));
		BufferedBlockCipher cipher = new BufferedBlockCipher(
				new CBCBlockCipher(new RijndaelEngine()));

		cipher.init(
				true,
				new ParametersWithIV(new KeyParameter(token
						.getLocalEncryptingKey()), token
						.getLocalInitializationVector()));

		int encryptedBytes = cipher.processBytes(dataToEncrypt, inputOffset,
				inputLength, output, outputOffset);

		try {

			encryptedBytes += cipher.doFinal(output, outputOffset
					+ encryptedBytes);
			return encryptedBytes;

		} catch (DataLengthException e) {
			logger.error("Input data is not an even number of encryption blocks.");
			throw new ServiceResultException(
					StatusCodes.Bad_InternalError,
					"Error in symmetric decrypt: Input data is not an even number of encryption blocks.");
		} catch (CryptoException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}

	}

	/*
	 * @Override public byte[] signAsymm(SecurityConfiguration profile, byte[]
	 * dataToSign, java.security.interfaces.RSAPrivateKey senderPrivate) throws
	 * ServiceResultException {
	 * 
	 * java.security.interfaces.RSAPrivateCrtKey privateKey =
	 * (java.security.interfaces.RSAPrivateCrtKey) senderPrivate; RSAPrivateKey
	 * privKey = new RSAPrivateKey(privateKey.getModulus(),
	 * privateKey.getPublicExponent(), privateKey.getPrivateExponent(),
	 * privateKey.getPrimeP(), privateKey.getPrimeQ(),
	 * privateKey.getPrimeExponentP(), privateKey.getPrimeExponentQ(),
	 * privateKey.getCrtCoefficient()); return signAsymm(profile, dataToSign,
	 * privKey);
	 * 
	 * }
	 * 
	 * public byte[] signAsymm(SecurityConfiguration profile, byte[] dataToSign,
	 * RSAPrivateKey senderPrivate) throws ServiceResultException {
	 * 
	 * SecurityAlgorithm algorithm =
	 * profile.getSecurityPolicy().getAsymmetricSignatureAlgorithm();
	 * 
	 * Signer signer = getAsymmetricSigner(true, algorithm, senderPrivate);
	 * signer.update(dataToSign, 0, dataToSign.length);
	 * 
	 * try { return signer.generateSignature(); } catch (DataLengthException e)
	 * { logger.error("Input data is not an even number of encryption blocks.");
	 * throw new ServiceResultException( StatusCodes.Bad_InternalError,
	 * "Error in symmetric decrypt: Input data is not an even number of encryption blocks."
	 * ); } catch (CryptoException e) { throw new
	 * ServiceResultException(StatusCodes.Bad_InternalError, e); }
	 * 
	 * }
	 */

	@Override
	public byte[] signAsymm(PrivateKey senderPrivate,
			SecurityAlgorithm algorithm, byte[] dataToSign)
					throws ServiceResultException {
		if (algorithm == null)
			return null;

		if (dataToSign == null || senderPrivate == null)
			throw new IllegalArgumentException("null arg");

		java.security.interfaces.RSAPrivateCrtKey privateKey = (java.security.interfaces.RSAPrivateCrtKey) senderPrivate;
		RSAPrivateKey privKey = new RSAPrivateKey(privateKey.getModulus(),
				privateKey.getPublicExponent(),
				privateKey.getPrivateExponent(), privateKey.getPrimeP(),
				privateKey.getPrimeQ(), privateKey.getPrimeExponentP(),
				privateKey.getPrimeExponentQ(), privateKey.getCrtCoefficient());

		Signer signer = getAsymmetricSigner(true, algorithm, privKey);
		signer.update(dataToSign, 0, dataToSign.length);

		try {
			return signer.generateSignature();
		} catch (DataLengthException e) {
			logger.error("Input data is not an even number of encryption blocks.");
			throw new ServiceResultException(
					StatusCodes.Bad_InternalError,
					"Error in symmetric decrypt: Input data is not an even number of encryption blocks.");
		} catch (CryptoException e) {
			throw new ServiceResultException(StatusCodes.Bad_InternalError, e);
		}

	}

	/*
	 * @Override public boolean verifyAsymm(SecurityConfiguration profile,
	 * byte[] dataToVerify, java.security.cert.Certificate signingCertificate,
	 * byte[] signature) throws ServiceResultException {
	 * 
	 * try { Certificate cert = getCertificate(signingCertificate); return
	 * verifyAsymm(profile, dataToVerify, cert, signature); } catch
	 * (CertificateEncodingException e) { throw new
	 * ServiceResultException(StatusCodes.Bad_InternalError, e); }
	 * 
	 * }
	 */

	@Override
	public void signSymm(SecurityToken token, byte[] input, int verifyLen,
			byte[] output) throws ServiceResultException {

		SecurityAlgorithm algorithm = token.getSecurityPolicy()
				.getSymmetricSignatureAlgorithm();
		HMac hmac = createMac(algorithm,
				new KeyParameter(token.getLocalSigningKey()));
		hmac.update(input, 0, verifyLen);
		hmac.doFinal(output, 0);

	}

	/*
	 * public boolean verifyAsymm(SecurityConfiguration profile, byte[]
	 * dataToVerify, Certificate signingCertificate, byte[] signature) throws
	 * ServiceResultException {
	 * 
	 * RSAPublicKey publicKey = getRSAPublicKey(signingCertificate);
	 * SecurityAlgorithm algorithm =
	 * profile.getSecurityPolicy().getAsymmetricSignatureAlgorithm(); Signer
	 * signer = getAsymmetricSigner(false, algorithm, publicKey);
	 * signer.update(dataToVerify, 0, dataToVerify.length); return
	 * signer.verifySignature(signature);
	 * 
	 * }
	 */

	@Override
	public boolean verifyAsymm(PublicKey signingCertificate,
			SecurityAlgorithm algorithm, byte[] dataToVerify, byte[] signature)
					throws ServiceResultException {
		if (algorithm == null)
			return true;
		if (signingCertificate == null || dataToVerify == null
				|| signature == null)
			throw new IllegalArgumentException("null arg");

		/*
		 * Certificate cert = getCertificate((java.security.cert.Certificate)
		 * signingCertificate); RSAPublicKey publicKey =
		 * getRSAPublicKey(signingCertificate);
		 */
		java.security.interfaces.RSAPublicKey signingCertificateRSA = (java.security.interfaces.RSAPublicKey) signingCertificate;
		RSAPublicKey publicKey = new RSAPublicKey(
				signingCertificateRSA.getModulus(),
				signingCertificateRSA.getPublicExponent());
		Signer signer = getAsymmetricSigner(false, algorithm, publicKey);
		signer.update(dataToVerify, 0, dataToVerify.length);
		return signer.verifySignature(signature);

	}

	@Override
	public void verifySymm(SecurityToken token, byte[] dataToVerify,
			byte[] signature) throws ServiceResultException {

		SecurityAlgorithm algorithm = token.getSecurityPolicy()
				.getSymmetricSignatureAlgorithm();
		HMac hmac = createMac(algorithm,
				new KeyParameter(token.getRemoteSigningKey()));
		byte[] computedSignature = new byte[hmac.getMacSize()];
		hmac.update(dataToVerify, 0, dataToVerify.length);
		hmac.doFinal(computedSignature, 0);

		// Compare signatures
		// First test that sizes are the same
		if (signature.length != computedSignature.length) {
			logger.warn("Signature lengths do not match: \n"
					+ CryptoUtil.toHex(signature) + " vs. \n"
					+ CryptoUtil.toHex(computedSignature));
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityChecksFailed,
					"Invalid signature: lengths do not match");
		}
		// Compare byte by byte
		for (int index = 0; index < signature.length; index++) {
			if (signature[index] != computedSignature[index]) {
				logger.warn("Signatures do not match: \n"
						+ CryptoUtil.toHex(signature) + " vs. \n"
						+ CryptoUtil.toHex(computedSignature));
				throw new ServiceResultException(
						StatusCodes.Bad_SecurityChecksFailed,
						"Invalid signature: signatures do not match");
			}
		}
		// Everything went fine, signatures matched
	}

	private HMac createMac(SecurityAlgorithm algorithm, KeyParameter param)
			throws ServiceResultException {

		HMac hmac = null;
		if (algorithm.equals(SecurityAlgorithm.HmacSha1)) {
			hmac = new HMac(new SHA1Digest());
		} else if (algorithm.equals(SecurityAlgorithm.HmacSha256)) {
			hmac = new HMac(new SHA256Digest());
		} else {
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityPolicyRejected,
					"Unsupported symmetric signature algorithm: " + algorithm);
		}
		hmac.init(param);
		return hmac;

	}

	private AsymmetricBlockCipher getAsymmetricCipher(boolean forEncryption,
			SecurityAlgorithm algorithm, CipherParameters params)
					throws ServiceResultException {
		AsymmetricBlockCipher cipher = null;
		if (algorithm.equals(SecurityAlgorithm.Rsa15)) {
			cipher = new PKCS1Encoding(new RSAEngine());
		} else if (algorithm.equals(SecurityAlgorithm.RsaOaep)) {
			cipher = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
		} else {
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityPolicyRejected,
					"Unsupported asymmetric encryption algorithm: " + algorithm);
		}
		cipher.init(forEncryption, params);
		return cipher;
	}

	private AsymmetricBlockCipher getAsymmetricCipher(
			SecurityAlgorithm algorithm, RSAPrivateKey privateKey)
					throws ServiceResultException {
		CipherParameters params = new RSAPrivateCrtKeyParameters(
				privateKey.getModulus(), privateKey.getPublicExponent(),
				privateKey.getPrivateExponent(), privateKey.getPrime1(),
				privateKey.getPrime2(), privateKey.getExponent1(),
				privateKey.getExponent2(), privateKey.getCoefficient());
		return getAsymmetricCipher(false, algorithm, params);
	}

	private AsymmetricBlockCipher getAsymmetricCipher(
			SecurityAlgorithm algorithm, RSAPublicKey publicKey)
					throws ServiceResultException {
		CipherParameters params = new RSAKeyParameters(false,
				publicKey.getModulus(), publicKey.getPublicExponent());
		// logger.info("Cipher: \nmodulus={}, \npublicExponent={}\n",
		// publicKey.getModulus(), publicKey.getPublicExponent());
		return getAsymmetricCipher(true, algorithm, params);
	}

	private Signer getAsymmetricSigner(boolean forSigning,
			SecurityAlgorithm algorithm, CipherParameters params)
					throws ServiceResultException {

		Signer signer = null;
		if (algorithm.equals(SecurityAlgorithm.RsaSha1)) {
			signer = new RSADigestSigner(new SHA1Digest());
		} else if (algorithm.equals(SecurityAlgorithm.RsaSha256)) {
			signer = new RSADigestSigner(new SHA256Digest());
		} else {
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityPolicyRejected,
					"Unsupported asymmetric signature algorithm: " + algorithm);
		}
		signer.init(forSigning, params);
		return signer;

	}

	private Signer getAsymmetricSigner(boolean forSigning,
			SecurityAlgorithm algorithm, RSAPrivateKey privateKey)
					throws ServiceResultException {

		CipherParameters params = new RSAPrivateCrtKeyParameters(
				privateKey.getModulus(), privateKey.getPublicExponent(),
				privateKey.getPrivateExponent(), privateKey.getPrime1(),
				privateKey.getPrime2(), privateKey.getExponent1(),
				privateKey.getExponent2(), privateKey.getCoefficient());
		return getAsymmetricSigner(forSigning, algorithm, params);

	}

	private Signer getAsymmetricSigner(boolean forSigning,
			SecurityAlgorithm algorithm, RSAPublicKey publicKey)
					throws ServiceResultException {

		CipherParameters params = new RSAKeyParameters(false,
				publicKey.getModulus(), publicKey.getPublicExponent());
		return getAsymmetricSigner(forSigning, algorithm, params);

	}

}
