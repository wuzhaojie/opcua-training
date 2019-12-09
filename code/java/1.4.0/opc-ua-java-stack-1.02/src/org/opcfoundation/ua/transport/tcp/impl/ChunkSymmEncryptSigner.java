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

import org.opcfoundation.ua.common.RuntimeServiceResultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class ChunkSymmEncryptSigner implements Runnable {

	/**
	 * Log4J Error logger. 
	 * Security failures are logged with INFO level.
	 * Security settings are logged with DEBUG level.
	 * Unexpected errors are logged with ERROR level. 
	 */
	static Logger logger = LoggerFactory.getLogger(ChunkSymmEncryptSigner.class);
	
	ByteBuffer chunk, body;
	SecurityToken token;
	
	public ChunkSymmEncryptSigner(ByteBuffer chunk, ByteBuffer body, SecurityToken token)
	{
		this.chunk = chunk;
		this.body = body;
		this.token = token;
	}
	
	@Override
	public void run() throws RuntimeServiceResultException 
	{
		SecurityPolicy policy = token.getSecurityPolicy();
		MessageSecurityMode msm = token.getMessageSecurityMode();
		try {

			int chunkSize = chunk.limit();
			int bodySize = body.limit();
			int sequenceHeader = 8;
			int messageHeaderSize = 8;
			int securityHeader = 8;

			// Chunk Size
//			int count = 0;
//			count += sequenceHeader;
//			count += bodySize;
//			count += policy.getSymmetricSignatureSize();

			// Sign
			int signatureSize = policy.getSymmetricSignatureSize();
			if ( msm == MessageSecurityMode.Sign || msm == MessageSecurityMode.SignAndEncrypt ) {
					
				// Message written so far will be signed
				int verifyLen = chunkSize - signatureSize;
				byte[] signature = new byte[ signatureSize ];
				
				sign(token, chunk.array(), verifyLen, signature);
				
				chunk.position(chunkSize - signatureSize);
				chunk.put(signature, 0, signatureSize);
				
				//isTraceEnabled checked because potentially time consuming CryptoUtil method gets evaluated otherwise every time.
				if (logger.isTraceEnabled()) {
					logger.trace("signature={}", CryptoUtil.toHex(signature));
				}
				
			}
			
			// Padding
			int padding = 0;
			if ( msm == MessageSecurityMode.SignAndEncrypt ) {
				chunk.position( chunkSize - signatureSize - 1 );
				padding = chunk.get() + 1;
			}

//			count += messageHeaderSize + securityHeader;

			// Write chunk size
			chunk.position(4);
			chunk.putInt(chunkSize);

			// Encrypt
			if ( msm == MessageSecurityMode.SignAndEncrypt ) {
				
				byte[] plaintext = new byte[ sequenceHeader + bodySize + padding + signatureSize ];
				chunk.position( messageHeaderSize + securityHeader );
				chunk.get(plaintext);
				
				// Run encrypt algorithm	
				encrypt(token, plaintext, 0, plaintext.length, chunk.array(), messageHeaderSize + securityHeader);
			}
			
		} catch (ServiceResultException e) {
			throw new RuntimeServiceResultException(e);
		}
	}
	
	private int encrypt(SecurityToken token, byte[] dataToEncrypt, int inputOffset, int inputLength, byte[] output, int outputOffset) 
			throws ServiceResultException
	{
		return CryptoUtil.getCryptoProvider().encryptSymm(token, dataToEncrypt, inputOffset, inputLength, output, outputOffset);
	}
	
	private void sign(SecurityToken token, byte[] input, int verifyLen, byte[] output)
			throws ServiceResultException
	{
		CryptoUtil.getCryptoProvider().signSymm(token, input, verifyLen, output);
	}

}
