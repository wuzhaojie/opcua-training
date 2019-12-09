/* ========================================================================
 * Copyright (c) 2005-2014 The OPC Foundation, Inc. All rights reserved.
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

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Mac;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.transport.tcp.impl.SecurityToken;

/**
 * Crypto Provider interface for encrypting and decrypting services.
 */
public interface CryptoProvider {

	public byte[] base64Decode(String string);

	public String base64Encode(byte[] bytes);

	public Mac createMac(SecurityAlgorithm algorithm, byte[] secret)
			throws ServiceResultException;

	public int decryptAsymm(PrivateKey decryptingKey,
			SecurityAlgorithm algorithm, byte[] dataToDecrypt, byte[] output,
			int outputOffset) throws ServiceResultException;

	public int decryptSymm(SecurityToken token, byte[] dataToDecrypt,
			int inputOffset, int inputLength, byte[] output, int outputOffset)
					throws ServiceResultException;

	public void encryptAsymm(PublicKey encryptingCertificate,
			SecurityAlgorithm algorithm, byte[] dataToEncrypt, byte[] output,
			int outputOffset) throws ServiceResultException;

	public int encryptSymm(SecurityToken token, byte[] dataToEncrypt,
			int inputOffset, int inputLength, byte[] output, int outputOffset)
					throws ServiceResultException;

	public byte[] signAsymm(PrivateKey senderPrivate,
			SecurityAlgorithm algorithm, byte[] dataToSign)
					throws ServiceResultException;

	public void signSymm(SecurityToken token, byte[] input, int verifyLen,
			byte[] output) throws ServiceResultException;

	public boolean verifyAsymm(PublicKey signingCertificate,
			SecurityAlgorithm algorithm, byte[] dataToVerify, byte[] signature)
					throws ServiceResultException;

	public void verifySymm(SecurityToken token, byte[] dataToVerify,
			byte[] signature) throws ServiceResultException;

}
