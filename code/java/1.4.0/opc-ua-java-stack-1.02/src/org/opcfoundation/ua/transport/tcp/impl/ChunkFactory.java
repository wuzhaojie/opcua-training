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
import java.nio.ByteOrder;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.common.RuntimeServiceResultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.bytebuffer.ByteBufferFactory;

/**
 * Chunk factory constructs byte buffers to be used for writing.
 * The byte buffer will be backed by an array that can fit the chunk.
 * The writable portion of the byte buffer (position -> limit) reflects 
 * to writable plaintext region.
 * <p>
 * Padding and the size of the message is pre-written to the chunk. 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 * @author Mikko Salonen
 */
public class ChunkFactory extends ByteBufferFactory {

	public int maxChunkSize;
	public int maxPlaintextSize;
	public int messageHeaderSize;
	public int securityHeader;
	public int sequenceHeader;
	public int cipherBlockSize;
	public int signatureSize;
	public MessageSecurityMode securityMode = MessageSecurityMode.Invalid;

	private boolean useExtraPaddingByte;
	
	/** Logger */
	static Logger logger = LoggerFactory.getLogger(ChunkFactory.class);
	
	public ChunkFactory(
		int maxChunkSize, 
		int messageHeaderSize,
		int securityHeaderSize, 
		int sequenceHeaderSize,
		int signatureSize,
		int cipherBlockSize,
		MessageSecurityMode securityMode,
		int keySize)
	{
		logger.trace("ChunkFactory: class={}", this.getClass());
		this.maxChunkSize = maxChunkSize;
		this.messageHeaderSize = messageHeaderSize;
		this.securityHeader = securityHeaderSize;
		this.sequenceHeader = sequenceHeaderSize;
		this.cipherBlockSize = cipherBlockSize;
		this.signatureSize = signatureSize;
		this.securityMode = securityMode;
		this.useExtraPaddingByte = keySize > 2048;
		
		if (securityMode==MessageSecurityMode.None || securityMode==MessageSecurityMode.Invalid)
		{
			maxPlaintextSize = maxChunkSize - messageHeaderSize - securityHeaderSize - sequenceHeader;
			assert(signatureSize==0);
			assert(cipherBlockSize==1);
		} 
		else
		if (securityMode==MessageSecurityMode.Sign)
		{
			maxPlaintextSize = maxChunkSize - messageHeaderSize - securityHeaderSize - sequenceHeader - signatureSize;
		} 
		if (securityMode==MessageSecurityMode.SignAndEncrypt)
		{			
			int minPaddingSize = getMinimumPadding();
			
			// Calculate max encrypt block size
			int maxEncryptBlockSize = maxChunkSize - messageHeaderSize - securityHeaderSize - minPaddingSize;
			maxEncryptBlockSize -= maxEncryptBlockSize % cipherBlockSize;						
			maxPlaintextSize = maxEncryptBlockSize - sequenceHeaderSize - signatureSize - minPaddingSize; 
		} 
	}


	/**
	 * 
	 */
	protected int getMinimumPadding(){
		return useExtraPaddingByte ? 2 : 1;
	}
	
	
	/**
	 * Allocate chunk for a message with a given body size.
	 * 
	 * @param bodySize 
	 * @return plaintext writeable byte buffer backed by byte[] size of the chunk. 
	 */
	public ByteBuffer allocate(int bodySize) {
		bodySize = Math.min(bodySize, maxPlaintextSize);
		int padding = 0;
		// calculate Padding
		if (securityMode == MessageSecurityMode.SignAndEncrypt) {
			
			int plaintextSizeExcludingPadding = bodySize + sequenceHeader + signatureSize;
			
			// Minimum encrypted block size
			padding = getMinimumPadding();
			int modulo = (padding + plaintextSizeExcludingPadding ) % cipherBlockSize; 
			if ( modulo != 0 ) padding += cipherBlockSize-modulo;
			
			logger.trace("allocate: padding={}", padding);
		}
		
		int chunkSize = bodySize + messageHeaderSize + securityHeader + sequenceHeader + signatureSize + padding;
		
		logger.trace("allocate: chunkSize={}", chunkSize);
		
		assert(chunkSize<=maxChunkSize);
		
		ByteBuffer result = ByteBuffer.allocate(chunkSize);
		result.order(ByteOrder.LITTLE_ENDIAN);
		
		// Write chunk size at position 4
		result.position(4);
		result.putInt(chunkSize);
		
		// Write padding
		if (securityMode == MessageSecurityMode.SignAndEncrypt) {
			writePadding(messageHeaderSize + securityHeader + sequenceHeader + bodySize, padding, result);
		}
		
		// Change limit and offset
		result.position(messageHeaderSize + securityHeader + sequenceHeader);
		result = result.slice(); // Slice forgets byte order
		result.order(ByteOrder.LITTLE_ENDIAN);
		result.limit(bodySize);				
		return result;
	}

	/**
	 * 
	 * @param paddingPosition
	 * @param padding the size of the whole padding; padding, padding fill, extra padding byte
	 * @param result
	 */
	protected void writePadding(int paddingPosition, int padding, ByteBuffer result) {
		int minimumPadding = getMinimumPadding();
		
		logger.trace("writePadding: result.position={}", result.position());
		logger.trace("writePadding: minimumPadding={}", minimumPadding);
		logger.trace("writePadding: padding={}", padding);
		
		if ( minimumPadding == 1) {
			result.position(paddingPosition);
			byte b = (byte) ( (padding-1) & 0xff);
			for (int i=0; i<padding; i++) result.put(b);
		}
		
		if ( minimumPadding == 2 ) {
			result.position(paddingPosition);
			byte b = (byte) ( (padding-2) & 0xff);
			for (int i=0; i<padding-1; i++) result.put(b);
			result.put( (byte) ( (padding-2) >> 8 ) );
		}
		
		logger.trace("writePadding: result={}", CryptoUtil.toHex(result.array(), 64));
	}

	protected void writePaddingSize(int paddingPosition, int paddingSize, ByteBuffer result) {
		int minimumPadding = getMinimumPadding();
		
		result.position(paddingPosition);
		if ( minimumPadding == 1) {
			result.put( (byte) ( (paddingSize-1) & 0xff) );
		}
		
		if ( minimumPadding == 2 ) {
			result.put( (byte) ( (paddingSize-2) & 0xff) );
			result.put( (byte) ( (paddingSize-2) >> 8 ) );
		}
		
		logger.trace("writePadding: result={}", CryptoUtil.toHex(result.array(), 64));
	}
	
	public void signChunk(ByteBuffer chunk)
	{
		
	}
	
	public void encryptChunk(ByteBuffer chunk)
	{
		
	}
	
	/**
	 * Expand allocated bytebuffer to complete chunk.
	 * 
	 * ByteBuffer allocated with allocate() returns a buffer that 
	 * has only plaintext as writable portion. This method expands the 
	 * ByteBuffer to include header and footer.
	 * 
	 * The result is rewound. 
	 *  
	 * @param plaintext
	 * @return chunk
	 */
	public ByteBuffer expandToCompleteChunk(ByteBuffer plaintext)
	{ 
		return ByteBuffer.wrap(plaintext.array()).order(ByteOrder.LITTLE_ENDIAN);
	}

	public ByteBuffer[] expandToCompleteChunk(ByteBuffer[] plaintexts)
	{ 
		ByteBuffer[] chunks = new ByteBuffer[plaintexts.length];
		for (int i=0; i<chunks.length; i++)
			chunks[i] = expandToCompleteChunk(plaintexts[i]); 
		return chunks;
	}
	
	public static class HelloChunkFactory extends ChunkFactory {
		public HelloChunkFactory() {
			super(8192, 8, 0, 0, 0, 0, MessageSecurityMode.Invalid, 0);
			maxChunkSize = 8192;
			messageHeaderSize = 8;
			maxPlaintextSize = maxChunkSize - 8; 
		}
		
	}
	
	public static class AcknowledgeChunkFactory extends ChunkFactory {
		public AcknowledgeChunkFactory() {
			super(8192, 8, 0, 0, 0, 1, MessageSecurityMode.Invalid, 0);
			maxChunkSize = 8192;
			messageHeaderSize = 8;
			maxPlaintextSize = maxChunkSize - 8; 
		}		
	}
	
	public static class ErrorMessageChunkFactory extends ChunkFactory {
		public ErrorMessageChunkFactory() {
			super(4096+4, 8, 0, 0, 0, 1, MessageSecurityMode.Invalid, 0);
			maxChunkSize = 4096+4+8;
			messageHeaderSize = 8;
			maxPlaintextSize = 4096+4 - 8; 
		}		
	}
	/*
	public static class SecureChannelChunkFactory extends ChunkFactory {
		SecurityToken token;
		int requestId;
		public SecureChannelChunkFactory(int maxChunkSize, SecurityToken token)
		{
			super(maxChunkSize, 8, 8, 8, token.getSignatureSize(), token.getCipherBlockSize(), token.getMessageSecurityMode());
			this.token = token;
		}
		public SecurityToken getToken() {
			return token;
		}
		public int getRequestId() {
			return requestId;
		}
		public void setRequestId(int requestId) {
			this.requestId = requestId;
		}
	}

	//TODO ADD ALLOCATE
*/	
	

	public static class AsymmMsgChunkFactory extends ChunkFactory {
//		private static final Charset UTF8 = Charset.forName("utf-8");
		SecurityConfiguration profile;
		 
		public AsymmMsgChunkFactory(int maxChunkSize,
				SecurityConfiguration profile) throws ServiceResultException {
			super(
					maxChunkSize,
					12,
					12 + profile.getSecurityPolicy().getEncodedPolicyUri().length
					   + (profile.getEncodedLocalCertificate() != null ? 
						  profile.getEncodedLocalCertificate().length : 
						  0)
					   + (profile.getEncodedRemoteCertificateThumbprint() != null ? 
						  profile.getEncodedRemoteCertificateThumbprint().length :
						  0),
					8,

					// Asymm Signature Size
					profile.getMessageSecurityMode().hasSigning() ? 
							CryptoUtil.getSignatureSize(
									profile.getSecurityPolicy().getAsymmetricSignatureAlgorithm(), 
									profile.getLocalPrivateKey() ) : 
							0,

					// Cipher block size
					profile.getMessageSecurityMode() != MessageSecurityMode.None ? 
							CryptoUtil.getCipherBlockSize(
									profile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(),
									profile.getRemoteCertificate().getPublicKey() ) :
							1,
											
					profile.getMessageSecurityMode(),

					// KeySize used to calc paddingSize
					profile.getLocalCertificate() == null ? 0 :
							((RSAPublicKey) profile.getRemoteCertificate().getPublicKey()).getModulus().bitLength());
		
			this.profile = profile;
		}
		
		@Override
		public ByteBuffer allocate(int bodySize) {
			MessageSecurityMode msm = securityMode;
			if ( msm == MessageSecurityMode.Sign ) msm = MessageSecurityMode.SignAndEncrypt;
			bodySize = Math.min(bodySize, maxPlaintextSize);
			int encryptedBlocks = -1; //initialize blocksize and ciphertext size
			int cipherTextSize = -1;
			int encryptSize = bodySize + sequenceHeader;
			
			// There is always padding: paddingSize, padding fill bytes [0..n], extra padding byte
			int padding = 0;
			int minimumPadding = getMinimumPadding();
			int paddingFillBytes = 0;
			
			int chunkSize = -1;
							
			if ( msm == MessageSecurityMode.SignAndEncrypt ) {
				
				int blockSize = 1;
				try {
					Key key = profile.getReceiverCertificate().getPublicKey();
					blockSize = CryptoUtil.getPlainTextBlockSize( profile.getSecurityPolicy().getAsymmetricEncryptionAlgorithm(), key);					
				} catch (ServiceResultException e) {
					throw new RuntimeServiceResultException(e);
				}
				
				// Sign and encrypt...so add the signatureSize to plainttext size
				encryptSize += signatureSize;
				encryptSize += minimumPadding;
				if (encryptSize % blockSize != 0) {
					paddingFillBytes = blockSize - (encryptSize % blockSize);
					encryptSize += paddingFillBytes;
				}
	            //update plaintextsize that needs to be encrypted
				padding = minimumPadding + paddingFillBytes;
	            
	            encryptedBlocks = encryptSize/blockSize; //TODO Check that PlainTextBlockSize is not null
				cipherTextSize = encryptedBlocks*cipherBlockSize;
				chunkSize = messageHeaderSize + securityHeader +  cipherTextSize;
				
			} else if(msm == MessageSecurityMode.Sign) {
				
				chunkSize = messageHeaderSize + securityHeader +  encryptSize + signatureSize;
				
			} else if(msm == MessageSecurityMode.None) {
				
				chunkSize = messageHeaderSize + securityHeader +  encryptSize;
			}
						
			logger.trace("AsymmMSGChunkFactory.allocate: chunkSize={}", chunkSize);
			
			ByteBuffer result = ByteBuffer.allocate(chunkSize);
			result.order(ByteOrder.LITTLE_ENDIAN);

			// Write padding
			if ( msm == MessageSecurityMode.SignAndEncrypt ) {
				writePadding(messageHeaderSize + securityHeader + sequenceHeader + bodySize, padding, result);
				writePaddingSize(chunkSize - minimumPadding, padding, result);
			}
			
			// Write chunk size at position 4
			result.position(4);
			result.putInt(chunkSize);
			
			// Change limit and offset
			result.position(messageHeaderSize + securityHeader + sequenceHeader);
			result = result.slice(); // Slice forgets byte order
			result.order(ByteOrder.LITTLE_ENDIAN);
			result.limit(bodySize);				
			return result;
		}
	}

}
