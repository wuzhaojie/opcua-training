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

/**
 * 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class SecurityConstants {
	
	public static final String SECURITY_POLICY_URI_BINARY_NONE = "http://opcfoundation.org/UA/SecurityPolicy#None";
	public static final String SECURITY_POLICY_URI_BINARY_BASIC128RSA15 = "http://opcfoundation.org/UA/SecurityPolicy#Basic128Rsa15";		
	public static final String SECURITY_POLICY_URI_BINARY_BASIC256 = "http://opcfoundation.org/UA/SecurityPolicy#Basic256";	

	public static final String SECURITY_POLICY_URI_XML_NONE = "http://opcfoundation.org/UA-Profile/Securitypolicy/None";
	public static final String SECURITY_POLICY_URI_XML_BASIC128RSA15 = "http://opcfoundation.org/UA-Profile/Securitypolicy/Basic128Rsa15";		
	public static final String SECURITY_POLICY_URI_XML_BASIC256 = "http://opcfoundation.org/UA-Profile/Securitypolicy/Basic256";

	// Symmetric signature	
	public static final String HmacSha1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
	public static final String HmacSha256 = "http://www.w3.org/2000/09/xmldsig#hmac-sha256";

	// Asymmetric encryption
	public static final String Rsa15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
	public static final String RsaOaep = "http://www.w3.org/2001/04/xmlenc#rsa-oaep";
	
	// Asymmetric signature
	public static final String RsaSha1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
	
	// Asymmetric keywrap
	public static final String KwRsaOaep = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
	public static final String KwRsa15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
		
	// Symmetric encryption
	public static final String Aes128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
	public static final String Aes256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
	
	// key derivation
	public static final String PSha1 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";	
	
}
