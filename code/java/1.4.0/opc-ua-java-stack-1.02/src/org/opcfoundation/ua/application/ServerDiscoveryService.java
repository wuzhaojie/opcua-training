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

package org.opcfoundation.ua.application;

import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.FindServersRequest;
import org.opcfoundation.ua.core.FindServersResponse;
import org.opcfoundation.ua.core.RegisterServerRequest;
import org.opcfoundation.ua.core.RegisterServerResponse;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;

/**
 * This class is the service implementation of a discovery Server.
 * 
 * NOTE! This class is not yet implemented.
 * 
 * The plan is to create discovery server that supports various back-ends
 * using Java Naming and Directory Interface (JNDI). JNDI is extendable and
 * out-of-box it supports LDAP. 
 * 
 * See http://java.sun.com/products/jndi/
 */
public class ServerDiscoveryService {

    public void onFindServers(EndpointServiceRequest<FindServersRequest, FindServersResponse> req) 
    throws ServiceFaultException 
    {
    	
    }

    void onRegisterServer(EndpointServiceRequest<RegisterServerRequest, RegisterServerResponse> req) 
    throws ServiceFaultException
    {
    	
    }
    
}
