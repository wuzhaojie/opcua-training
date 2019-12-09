/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.application;

import com.prosysopc.ua.common.ServiceFaultException;
import com.prosysopc.ua.core.FindServersRequest;
import com.prosysopc.ua.core.FindServersResponse;
import com.prosysopc.ua.core.RegisterServerRequest;
import com.prosysopc.ua.core.RegisterServerResponse;
import com.prosysopc.ua.transport.endpoint.EndpointServiceRequest;

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

    /**
     * <p>onFindServers.</p>
     *
     * @param req a {@link EndpointServiceRequest} object.
     * @throws ServiceFaultException if any.
     */
    public void onFindServers(EndpointServiceRequest<FindServersRequest, FindServersResponse> req) 
    throws ServiceFaultException 
    {
    	
    }

    void onRegisterServer(EndpointServiceRequest<RegisterServerRequest, RegisterServerResponse> req) 
    throws ServiceFaultException
    {
    	
    }
    
}
