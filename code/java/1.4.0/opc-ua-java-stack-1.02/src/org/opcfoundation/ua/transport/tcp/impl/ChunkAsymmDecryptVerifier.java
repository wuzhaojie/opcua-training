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

package org.opcfoundation.ua.transport.tcp.impl;


import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

import org.opcfoundation.ua.common.RuntimeServiceResultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decrypts & Verifies Chunk secured with asymmetric encryption
 * 
 */
public class ChunkAsymmDecryptVerifier implements Runnable {

	/**
	 * Log4J Error logger. 
	 * Security failures are logged with INFO level.
	 * Security info are printed with DEBUG level.
	 * Unexpected errors are logged with ERROR level. 
	 */
	static Logger logger = LoggerFactory.getLogger(ChunkAsymmDecryptVerifier.class);
	
	ByteBuffer chunk;
	SecurityConfiguration securityProfile;
	String securityPolicyUri;
	byte[] senderCertificate;
	byte[] receiverCertificateThumbPrint;
	
	
	public ChunkAsymmDecryptVerifier(ByteBuffer chunk, SecurityConfiguration securityProfile)
	{
		this.chunk = chunk;
		this.securityProfile = securityProfile;
	}
	
	@Override
	public void run() throws RuntimeServiceResultException {
		try {
			SecurityPolicy policy = securityProfile.getSecurityPolicy();
			MessageSecurityMode msm = securityProfile.getMessageSecurityMode();
			if ( msm == MessageSecurityMode.Sign ) msm = MessageSecurityMode.SignAndEncrypt;

			// securityPolicyURI
			chunk.position(12);
			securityPolicyUri = ChunkUtils.getString(chunk);
			
			logger.debug("SecurityPolicy in use: {}", securityPolicyUri);
			logger.debug("SecurityMode in use: {}", securityProfile.getMessageSecurityMode());
						
			if (logger.isTraceEnabled()) {
				logger.trace("Chunk: {}", CryptoUtil.toHex(chunk.array(), 64));
			}
			
			// SenderCertificate
			senderCertificate = ChunkUtils.getByteString(chunk);
			// ReceiverCertificateThumbPrint
			receiverCertificateThumbPrint = ChunkUtils.getByteString(chunk);
			
			int headersEnd = chunk.position();
			int plaintextStarts = chunk.position() + 8;
			int plaintextEnds = chunk.limit();
			int decryptedBytes = plaintextEnds - headersEnd;
			
			// Decrypt
			if (msm == MessageSecurityMode.SignAndEncrypt) {
				// Get data to decrypt
				byte[] dataToDecrypt = new byte[decryptedBytes];
				// Set position to the starting point of the decrypted bytes (= starting point of the sequence header)
				chunk.position(headersEnd);
				// Get the bytes to decrypt
				chunk.get(dataToDecrypt, 0, dataToDecrypt.length);
	
				// Run decrypt algorithm
				decryptedBytes = decrypt(dataToDecrypt, securityProfile.getLocalPrivateKey(), chunk.array(), headersEnd + chunk.arrayOffset());
				
				if (logger.isTraceEnabled()) {
					logger.trace("Chunk decrypted: {}", CryptoUtil.toHex(chunk.array(), 64));
				}
				
			}
			
			// Verify
			int signatureSize = 0;
			if ( msm.hasSigning() ) {
				
				SecurityAlgorithm signatureAlgorithm = policy.getAsymmetricSignatureAlgorithm();
				
				logger.debug("signatureAlgorithm={}", signatureAlgorithm);
								
				PublicKey remotePublicKey = securityProfile.getRemoteCertificate().getPublicKey();
				signatureSize = CryptoUtil.getSignatureSize(signatureAlgorithm, remotePublicKey);
				
				// verify signature
				logger.debug("signatureSize={}", signatureSize);
				
				// We want to verify headers and all the decrypted bytes without signature
				// The signature belongs to decrypted bytes, so we must reduce signatureSize of bytes from decryptedBytes
				byte[] dataToVerify = new byte[headersEnd + decryptedBytes - signatureSize];
				
				chunk.position(0);
				chunk.get(dataToVerify, 0, dataToVerify.length);
	
				// Extract the signature from the message
				chunk.position(headersEnd + decryptedBytes - signatureSize);
				byte[] signature = new byte[signatureSize];
				chunk.get(signature, 0, signatureSize);
	
				Certificate signersCertificate = securityProfile.getRemoteCertificate();
				
				// Run verify algorithm
				if (!verify(dataToVerify, signersCertificate, signature)) {
					logger.error("Signature verification fails.");
					throw new ServiceResultException(StatusCodes.Bad_SecurityChecksFailed, "Signature could not be VERIFIED");
				}
			}
			
			// Assert padding is ok
			int paddingSize = 0;
			
			// There is padding with SignAndEncrypt
			if (msm == MessageSecurityMode.SignAndEncrypt) {
				int paddingByte; 
				int keySize = securityProfile.getLocalCertificate2().getCertificate().getKeySize();
				int paddingEnd = headersEnd + decryptedBytes - signatureSize - 1;
				boolean largeKey = keySize > 2048; 
				if ( largeKey ) {
					paddingByte = chunk.get( paddingEnd-1 );
					int extraPaddingByte = chunk.get( paddingEnd );
					paddingSize = ( ( paddingByte & 0xff ) | ( (extraPaddingByte & 0xff) << 8 ) ) + 2;
				} else {
					paddingByte = chunk.get( paddingEnd );
					paddingSize = ( paddingByte & 0xff ) + 1;					
				}
				
				logger.debug("paddingEnd={} paddingSize={}", paddingEnd, paddingSize);

				// Assert every value equals the low byte of size
				{
					int len = largeKey ? paddingSize-1 : paddingSize;
					int start = largeKey ? paddingEnd-len : paddingEnd-len+1;
					for (int i=0; i<len; i++) {
						int p = chunk.get(start+i);
						if (p != paddingByte) {
							logger.error(String.format("Padding does not match: %x <> %x", p, paddingByte));
							throw new ServiceResultException(StatusCodes.Bad_SecurityChecksFailed, "Could not verify the padding in the message");
						}
					}
				}
				
			}

			// Modify the chunk so that the plaintext is between position and
			// limit of the byte buffer
			chunk.position(plaintextStarts);
			chunk.limit(chunk.position() + decryptedBytes - 8 - paddingSize
					- signatureSize);

		} catch (ServiceResultException e) {
			throw new RuntimeServiceResultException(e);
		}
	}

	public String getSecurityPolicyUri() {
		return securityPolicyUri;
	}

	public byte[] getSenderCertificate() {
		return senderCertificate;
	}

	public byte[] getReceiverCertificateThumbprint() {
		return receiverCertificateThumbPrint;
	}
		
	private boolean verify(byte[] dataToVerify, Certificate signingCertificate, byte[] signature) throws ServiceResultException {
		// Check which security policy is in use
		// Default
		SecurityPolicy policy = securityProfile.getSecurityPolicy();
		logger.debug("verify: policy={}", policy);
		if (logger.isTraceEnabled()) {
			logger.trace("verify: {}", signingCertificate );
			logger.trace("verify: dataToVerify={}", CryptoUtil.toHex(dataToVerify,64));
			logger.trace("verify: signature={}", CryptoUtil.toHex(signature,64));
		}
		
		// Compute the hash of the message
		return CryptoUtil.getCryptoProvider().verifyAsymm(signingCertificate.getPublicKey(), securityProfile.getSecurityPolicy().getAsymmetricSignatureAlgorithm(), dataToVerify, signature);
	}
	
	/**
	 * Decrypt a data chunk
	 * 
	 * @param dataToDecrypt
	 * @param privateKey
	 * @param output
	 * @param outputOffest
	 * @return number of bytes decrypted
	 */
	private int decrypt(byte[] dataToDecrypt, PrivateKey privateKey, byte[] output, int outputOffset ) 
	throws ServiceResultException
	{		
		int bytesDecrypted = CryptoUtil.getCryptoProvider().decryptAsymm(privateKey, securityProfile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(), dataToDecrypt, output, outputOffset);
		
		if (logger.isTraceEnabled()) {
			logger.trace("decrypt: dataToDecrypt={}", CryptoUtil.toHex(dataToDecrypt,64));
			logger.trace("decrypt: output={}", CryptoUtil.toHex(output,64));
			logger.trace("decrypt: bytesDecrypted={}", bytesDecrypted);
		}
		
		return bytesDecrypted;
	}

}
