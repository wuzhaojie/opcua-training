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

import org.opcfoundation.ua.core.MessageSecurityMode;

/**
 * Binding of {@link SecurityPolicy} and {@link MessageSecurityMode}.
 * <p>
 * Security Policy determines which algorithms to use during asymmetric and symmetric
 * encryption.   
 * <p>
 * MessageSecurityMode determines whether to use encryption and whether to use signing
 * during _symmetric_ encryption, which is after hand-shake. 
 */
public final class SecurityMode {
		
	// Secure Security Modes
	public final static SecurityMode BASIC128RSA15_SIGN_ENCRYPT = new SecurityMode(SecurityPolicy.BASIC128RSA15, MessageSecurityMode.SignAndEncrypt);
	public final static SecurityMode BASIC128RSA15_SIGN = new SecurityMode(SecurityPolicy.BASIC128RSA15, MessageSecurityMode.Sign);
	public final static SecurityMode BASIC256_SIGN_ENCRYPT = new SecurityMode(SecurityPolicy.BASIC256, MessageSecurityMode.SignAndEncrypt);
	public final static SecurityMode BASIC256_SIGN = new SecurityMode(SecurityPolicy.BASIC256, MessageSecurityMode.Sign);
	public final static SecurityMode BASIC256SHA256_SIGN_ENCRYPT = new SecurityMode(SecurityPolicy.BASIC256SHA256, MessageSecurityMode.SignAndEncrypt);
	public final static SecurityMode BASIC256SHA256_SIGN = new SecurityMode(SecurityPolicy.BASIC256SHA256, MessageSecurityMode.Sign);

	// Unsecure Security Mode
	public final static SecurityMode NONE = new SecurityMode(SecurityPolicy.NONE, MessageSecurityMode.None);
	
	// Security Mode Sets
	// The 101-modes are the default for the time being, until all stacks add support for BASIC256SHA256
	public final static SecurityMode[] ALL_102 = new SecurityMode[] {NONE, BASIC128RSA15_SIGN, BASIC128RSA15_SIGN_ENCRYPT, BASIC256_SIGN, BASIC256_SIGN_ENCRYPT, BASIC256SHA256_SIGN, BASIC256SHA256_SIGN_ENCRYPT}; 
	public final static SecurityMode[] ALL_101 = new SecurityMode[] {NONE, BASIC128RSA15_SIGN, BASIC128RSA15_SIGN_ENCRYPT, BASIC256_SIGN, BASIC256_SIGN_ENCRYPT}; 
	public final static SecurityMode[] ALL = ALL_101; 
	public final static SecurityMode[] SECURE_102 = new SecurityMode[] {BASIC128RSA15_SIGN, BASIC128RSA15_SIGN_ENCRYPT, BASIC256_SIGN, BASIC256_SIGN_ENCRYPT, BASIC256SHA256_SIGN, BASIC256SHA256_SIGN_ENCRYPT}; 
	public final static SecurityMode[] SECURE_101 = new SecurityMode[] {BASIC128RSA15_SIGN, BASIC128RSA15_SIGN_ENCRYPT, BASIC256_SIGN, BASIC256_SIGN_ENCRYPT}; 
	public final static SecurityMode[] SECURE = SECURE_101; 
	public final static SecurityMode[] NON_SECURE = new SecurityMode[] {NONE}; 	

	private final SecurityPolicy securityPolicy;
	private final MessageSecurityMode messageSecurityMode;
	
	/**
	 * Create all permutations of security policies and message security modes.
	 * 
	 * @param securityPolicies
	 * @param messageSecurityModes
	 * @return all permutations
	 */
	public static SecurityMode[] create(SecurityPolicy[] securityPolicies, MessageSecurityMode[] messageSecurityModes)
	{
		SecurityMode[] result = new SecurityMode[ securityPolicies.length * messageSecurityModes.length ];
		for (int i=0; i<securityPolicies.length; i++) {
			for (int j=0; j<messageSecurityModes.length; j++) {
				int x = i*messageSecurityModes.length + j;
				result[x] = new SecurityMode(securityPolicies[i], messageSecurityModes[j]);
			}
		}
		return result;
	}
		
	public SecurityMode(SecurityPolicy securityPolicy, MessageSecurityMode messageSecurityMode) {
		if (securityPolicy==null || messageSecurityMode==null) 
			throw new IllegalArgumentException("null arg");
		this.securityPolicy = securityPolicy;
		this.messageSecurityMode = messageSecurityMode;
	}
	
	public SecurityPolicy getSecurityPolicy() {
		return securityPolicy;
	}
	
	public MessageSecurityMode getMessageSecurityMode() {
		return messageSecurityMode;
	}
	
	@Override
	public int hashCode() {
		return securityPolicy.hashCode() ^ messageSecurityMode.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SecurityMode)) return false;
		SecurityMode other = (SecurityMode) obj;
		return other.securityPolicy == securityPolicy && other.messageSecurityMode == messageSecurityMode;
	}

	@Override
	public String toString() {
		return "["+securityPolicy.getPolicyUri()+","+messageSecurityMode+"]";
	}
	
	public static SecurityMode[] join(SecurityMode[] a, SecurityMode[] b) {
		SecurityMode[] result = new SecurityMode[a.length + b.length];
		for ( int i=0; i<a.length; i++) result[i] = a[i];
		for ( int j=0; j<b.length; j++) result[j+a.length] = b[j];
		return result;
	}
	
}
