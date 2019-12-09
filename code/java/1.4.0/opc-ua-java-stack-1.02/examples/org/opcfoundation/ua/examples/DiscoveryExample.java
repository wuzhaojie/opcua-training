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
package org.opcfoundation.ua.examples;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.examples.certs.ExampleKeys;
import org.opcfoundation.ua.transport.security.KeyPair;

public class DiscoveryExample {

	public static void main(String[] args) throws ServiceResultException {
		
		//////////////  CLIENT  //////////////
		// Load Client's Application Instance Certificate from file
		KeyPair myClientApplicationInstanceCertificate = ExampleKeys.getCert("Cert");
		// Create Client
		Client myClient = Client.createClientApplication( myClientApplicationInstanceCertificate );
		KeyPair myHttpsCertificate = ExampleKeys.getHttpsCert("Client"); 
		myClient.getApplication().getHttpsSettings().setKeyPair(myHttpsCertificate);		
		//////////////////////////////////////
		
		
		////////  DISCOVER ENDPOINTS  ////////
		String uri = "opc.tcp://localhost:62541/";
		//uri = "https://localhost:62541/Quickstarts/DataAccessServer";
		//uri = "https://localhost:62542/Quickstarts/DataAccessServer"
		EndpointDescription[] endpoints = myClient.discoverEndpoints( uri, "" ); 
		for ( EndpointDescription ed : endpoints ) {
			System.out.println( ed );
		}
		//////////////////////////////////////
		
		
		/////////  DISCOVER SERVERS  /////////
		uri = "opc.tcp://localhost:62541/Quickstarts/DataAccessServer";
		// Discover server applications
		ApplicationDescription[] servers = myClient.discoverApplications( uri );
		
		// Choose one application
		ApplicationDescription server = servers[0];
		// Connect the application
		SessionChannel mySessionChannel = myClient.createSessionChannel( server );
		try {
			// Activate session
			mySessionChannel.activate();
//			mySessionChannel.activate("username", "password");
			//////////////////////////////////////

			/////////////  EXECUTE  //////////////		
			BrowseDescription browse = new BrowseDescription();
			browse.setNodeId( Identifiers.RootFolder );
			browse.setBrowseDirection( BrowseDirection.Forward );
			browse.setIncludeSubtypes( true );
			BrowseResponse res3 = mySessionChannel.Browse( null, null, null, browse );
			System.out.println(res3);
			//////////////////////////////////////
		
		
			/////////////  SHUTDOWN  /////////////
			// close the session and the channel from the server
			mySessionChannel.close();
			//////////////////////////////////////		
		
		} finally {
			mySessionChannel.dispose();
		}
		
	}
	
}
