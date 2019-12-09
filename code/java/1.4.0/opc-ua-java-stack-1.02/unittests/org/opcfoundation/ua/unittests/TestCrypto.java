package org.opcfoundation.ua.unittests;

import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.transport.security.BcCryptoProvider;
import org.opcfoundation.ua.transport.security.BcJceCryptoProvider;
import org.opcfoundation.ua.transport.security.CryptoProvider;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.ScCryptoProvider;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCrypto extends TestCase {
	
	static Logger logger = LoggerFactory.getLogger(TestCrypto.class);

	SecurityConfiguration clientProfile;
	SecurityConfiguration serverProfile;
	
	CryptoProvider clientCryptoProvider;
	CryptoProvider serverCryptoProvider;
	
	public void testEncryptDecryptRsa15() throws Exception {
		try {
			clientCryptoProvider = new BcJceCryptoProvider();
			serverCryptoProvider = new BcCryptoProvider();
			_setupTest(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT, 2048);
			encryptDecryptAsymm();
		} finally {
			_tearDown();
		}
	}

	public void testEncryptDecryptRsaOaep() throws Exception {
		try {
			clientCryptoProvider = new BcJceCryptoProvider();
			serverCryptoProvider = new BcCryptoProvider();
			_setupTest(SecurityMode.BASIC256_SIGN_ENCRYPT, 2048);
			encryptDecryptAsymm();
		} finally {
			_tearDown();
		}
	}
	
	public void testEncryptDecryptRsa15WithSc() throws Exception {
		try {
			clientCryptoProvider = new ScCryptoProvider();
			serverCryptoProvider = new BcCryptoProvider();
			_setupTest(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT, 2048);
			encryptDecryptAsymm();
		} finally {
			_tearDown();
		}
	}

	public void testEncryptDecryptRsaOaepWithSc() throws Exception {
		try {
			clientCryptoProvider = new BcJceCryptoProvider();
			serverCryptoProvider = new ScCryptoProvider();
			_setupTest(SecurityMode.BASIC256_SIGN_ENCRYPT, 2048);
			encryptDecryptAsymm();
		} finally {
			_tearDown();
		}
	}

	public void encryptDecryptAsymm() throws ServiceResultException {
		
		Certificate serverCert = serverProfile.getLocalCertificate();
		RSAPrivateKey serverPrivateKey = serverProfile.getLocalPrivateKey();
		
		SecurityAlgorithm algorithm = clientProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm();
		
		int inputBlockSize = CryptoUtil.getPlainTextBlockSize(algorithm,
				serverProfile.getRemoteCertificate().getPublicKey());
		int outputBlockSize = CryptoUtil.getCipherBlockSize(algorithm,
				serverProfile.getRemoteCertificate().getPublicKey());
		logger.info("encryptAsymm: inputBlockSize={}, outputBlockSize={}",
				inputBlockSize, outputBlockSize);
		
		// Encrypt.
		byte[] dataToEncrypt = new byte[inputBlockSize];
		byte[] encrypted = new byte[outputBlockSize];
		byte[] origData = "Hello, world".getBytes();
		
		System.arraycopy(origData, 0, dataToEncrypt, 0, origData.length);
		clientCryptoProvider.encryptAsymm(serverCert.getPublicKey(), clientProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(), dataToEncrypt, encrypted, 0);
		
		// Decrypt.
		byte[] dataToDecrypt = new byte[outputBlockSize];
		byte[] decrypted = new byte[outputBlockSize];
		System.arraycopy(encrypted, 0, dataToDecrypt, 0, encrypted.length);
		
		serverCryptoProvider.decryptAsymm(serverPrivateKey, serverProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(), dataToDecrypt, decrypted, 0);
		
		for (int i = 0; i < dataToEncrypt.length; i++) {
			if (dataToEncrypt[i] != decrypted[i]) {
				Assert.fail("Encrypted and decrypted data do not match.");
			}
		}
		
	}
	
	public void _setupTest(SecurityMode mode, int keySize) throws ServiceResultException {
				
		KeyPair clientKeyPair = UnitTestKeys.getKeyPair("client", keySize);
		KeyPair serverKeyPair = UnitTestKeys.getKeyPair("server", keySize);
		
		clientProfile = new SecurityConfiguration(mode, clientKeyPair, serverKeyPair.getCertificate());
		serverProfile = new SecurityConfiguration(mode, serverKeyPair, clientKeyPair.getCertificate());
		
		logger.info("\nAlgorithm: \nclient={}, \nserver={}",
				clientProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(),
				serverProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm());
		
	}
	
	private void _tearDown() {
		// TODO Auto-generated method stub
		
	}
	
}
