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

import java.util.EnumSet;

/**
 * OPC UA specific security algorithm URIs and the respective Java StandardNames
 */
public enum SecurityAlgorithm {
	// Symmetric signature	
	HmacSha1(AlgorithmType.SymmetricSignature, "http://www.w3.org/2000/09/xmldsig#hmac-sha1", "HmacSHA1", 160),
	HmacSha256(AlgorithmType.SymmetricSignature, "http://www.w3.org/2000/09/xmldsig#hmac-sha256", "HmacSHA256", 256),
	// Symmetric encryption
	Aes128(AlgorithmType.SymmetricEncryption, "http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES/CBC/NoPadding", 128),
	Aes256(AlgorithmType.SymmetricEncryption, "http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES/CBC/NoPadding", 256),
	// Asymmetric signature
	RsaSha1(AlgorithmType.AsymmetricSignature, "http://www.w3.org/2000/09/xmldsig#rsa-sha1", "SHA1withRSA", 160),
	RsaSha256(AlgorithmType.AsymmetricSignature, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA", 256),
	// Asymmetric encryption
	Rsa15(AlgorithmType.AsymmetricEncryption, "http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA/NONE/PKCS1Padding", 0),
	RsaOaep(AlgorithmType.AsymmetricEncryption, "http://www.w3.org/2001/04/xmlenc#rsa-oaep", "RSA/NONE/OAEPWithSHA1AndMGF1Padding", 0),
	// Asymmetric keywrap
	KwRsaOaep(AlgorithmType.AsymmetricKeywrap, "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p","", 0),
	KwRsa15(AlgorithmType.AsymmetricKeywrap, "http://www.w3.org/2001/04/xmlenc#rsa-1_5","", 0),
	// key derivation
	PSha1(AlgorithmType.KeyDerivation, "http://www.w3.org/2001/04/xmlenc#aes128-cbc","HmacSHA1", 0),
	PSha256(AlgorithmType.KeyDerivation, "http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/dk/p_sha256","HmacSHA256", 0);
	
	
	public enum AlgorithmType {
		SymmetricSignature,
		SymmetricEncryption,
		AsymmetricSignature,
		AsymmetricEncryption,
		AsymmetricKeywrap,
		KeyDerivation
	}

	private AlgorithmType type;
	/**
	 * @return the type
	 */
	public AlgorithmType getType() {
		return type;
	}
	private final String uri;
	private final String standardName;
	private final String transformation;
	private final int keySize;
	private final String mode;
	private final String padding;
	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}
	/**
	 * @return the padding
	 */
	public String getPadding() {
		return padding;
	}
	/**
	 * @return the keySize
	 */
	public int getKeySize() {
		return keySize;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @return the standardName
	 */
	public String getStandardName() {
		return standardName;
	}
	private SecurityAlgorithm(AlgorithmType type, String uri, String transformation, int keySize) {
		this.type = type;
		this.uri = uri;
		this.transformation = transformation;
		String[] parts = transformation.split("/");
		this.standardName = parts[0];
		this.mode = parts.length > 1 ? parts[1] : "EBC";
		this.padding = parts.length > 2 ? parts[2] : "PKCS5Padding";
		this.keySize = keySize;
		
	}
	@Override
	public String toString() {
		return "Algorithm URI=" + uri + " StandardName=" + standardName
				+ " Transformation=" + transformation;
	}
	/**
	 * Find the SecurityAlgorithm with URI.
	 * @param algorithmUri the Uri to look for
	 * @return the respective SecurityAlgorithm or null if none is found.
	 */
	public static SecurityAlgorithm valueOfUri(String algorithmUri) {
		for (SecurityAlgorithm a: EnumSet.allOf(SecurityAlgorithm.class))
			if (a.getUri().equals(algorithmUri))
				return a;
		return null;
	}
	public String getTransformation() {
		return this.transformation;
	}

}
