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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.net.ssl.X509KeyManager;


/**
 * This class adapts a collection of key pair classes into a X509KeyManager.
 *
 * @author toni.kalajainen@semantum.fi
 */
public class KeyPairsKeyManager implements X509KeyManager {

	Collection<KeyPair> keypairs;
	
	public KeyPairsKeyManager(Collection<KeyPair> keypairs) {
		this.keypairs = keypairs;
	}

	@Override
	public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
		return null;
	}

	@Override
	public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(String arg0) {
		return null;
	}

	@Override
	public String[] getClientAliases(String arg0, Principal[] arg1) {
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String arg0) {
		return null;
	}

	@Override
	public String[] getServerAliases(String arg0, Principal[] arg1) {
		return null;
	}

	public Collection<KeyPair> getKeyPairs() {
		return keypairs;
	}
}
