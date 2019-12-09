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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.GetEndpointsRequest;
import org.opcfoundation.ua.core.GetEndpointsResponse;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.transport.endpoint.EndpointBindingCollection;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;

//import com.sun.net.httpserver.HttpServer;

/**
 * Service handler that serves onGetEndpoints request.
 * 
 */
public class EndpointDiscoveryService {

	EndpointBindingCollection endpointBindings;
	private static Logger logger = LoggerFactory.getLogger(EndpointDiscoveryService.class);
	
	public EndpointDiscoveryService(EndpointBindingCollection endpointBindings) {
		this.endpointBindings = endpointBindings;
	}
	
	public void setEndpointBindingCollection(EndpointBindingCollection endpointBindings)
	{
		this.endpointBindings = endpointBindings;
	}
	
	public void onGetEndpoints(EndpointServiceRequest<GetEndpointsRequest, GetEndpointsResponse> messageExchange) throws ServiceFaultException {
		//local address where the request came from
		SocketAddress requestAddress = messageExchange.getChannel().getConnection().getLocalAddress();
		logger.debug("onGetEndpoints: requestAddress={}", requestAddress);
		
		logger.trace("onGetEndpoints: Request={}", messageExchange.getRequest());
		GetEndpointsResponse res = new GetEndpointsResponse();
		String requestUrl = trimUrl(messageExchange.getRequest().getEndpointUrl());
		String requestHost = getUrlHost(requestUrl);
		String requestServerName = getUrlServerName(requestUrl);
		String[] profileUriArray = messageExchange.getRequest().getProfileUris();
		logger.debug("onGetEndpoints: requestUrl={}", requestUrl);
		logger.debug("onGetEndpoints: host={}", requestHost);
		logger.debug("onGetEndpoints: serverName={}", requestServerName);
		logger.debug("onGetEndpoints: profileUris={}", Arrays.toString(profileUriArray));
				
		Collection<String> profileUris = profileUriArray==null?new ArrayList<String>(0):Arrays.asList(profileUriArray);
		List<EndpointDescription> list = new ArrayList<EndpointDescription>();
	
		final List<Server> servers = endpointBindings.getServiceServers();
		for (Server s : servers)
		{
			//mantis issue 2495: s.getEndpointDescriptions(requestAddress) will now return only addresses that are bound to queried network interface
			final EndpointDescription[] endpointDescriptions = s.getEndpointDescriptions(requestAddress);
			for (EndpointDescription ed : endpointDescriptions)
			{

				// Add, if Uri was requested
				logger.debug("ed.TransportProfileUri={}", ed.getTransportProfileUri());
				if ((profileUris.isEmpty() || profileUris.contains(ed
						.getTransportProfileUri()))) {
					logger.debug("ed.EndpointUrl={}", ed.getEndpointUrl());
					String host = getUrlHost(ed.getEndpointUrl());
					String serverName = getUrlServerName(ed.getEndpointUrl());
					if (requestUrl.isEmpty()
							|| (host.equals(requestHost) && (requestServerName.isEmpty() || serverName.equals(requestServerName))))
						list.add(ed);
				}
			}
		}

		// If no match is found
		if (list.isEmpty()) {
			logger.debug("onGetEndpoints: no matching endpoints for requested hostname, checking all with the same serverName");
			for (Server s : servers)
			{
				final EndpointDescription[] endpointDescriptions = s.getEndpointDescriptions();
				for (EndpointDescription ed : endpointDescriptions)
				{
					if ((profileUris.isEmpty() || profileUris.contains(ed.getTransportProfileUri()))
							&& (requestServerName.isEmpty() ||requestServerName.equals(getUrlServerName(ed.getEndpointUrl()))))
					{
						// Add, if Uri was requested
						logger.debug("ed.EndpointUrl={}", ed.getEndpointUrl());
						list.add(ed);
					
					}
				}
			}
		}
		// Still empty? Add one endpoint
		if (list.isEmpty()) {
			logger.debug("onGetEndpoints: no matching endpoints ignoring host, adding all");
			for (Server s : servers)
			{
				final EndpointDescription[] endpointDescriptions = s.getEndpointDescriptions();
				for (EndpointDescription ed : endpointDescriptions)
				{
					if ((profileUris.isEmpty() || profileUris.contains(ed.getTransportProfileUri())))
					{
						// Add, if Uri was requested
						logger.debug("ed.EndpointUrl={}", ed.getEndpointUrl());
						list.add(ed);
					}
				}
			}
		}
		logger.trace("onGetEndpoints: list={}", list);
		res.setEndpoints(list.toArray(new EndpointDescription[0]));
		ResponseHeader h = new ResponseHeader(DateTime.currentTime(), messageExchange
				.getRequest().getRequestHeader().getRequestHandle(), null,
				null, null, null);
		res.setResponseHeader(h);
		messageExchange.sendResponse(res);
	}

	public EndpointBindingCollection getEndpointCollection()
	{
		return endpointBindings;
	}

	/**
	 * Trim the URL, by removing an extra '/' at the end of the URL and also make it lowercase
	 * <p>
	 * @param uri
	 * @return
	 */
	private String trimUrl(String uri) {
		if (uri == null)
			return "";
		if( uri.endsWith("/"))
			uri = uri.substring(0, uri.length()-1);
		return uri;
	}
	
	/**
	 * Find the host part of the URI
	 * <p>
	 * @param uri
	 * @return
	 */
	private String getUrlHost(String uri) {
		if (uri == null)
			return "";
		int beginIndex = uri.indexOf("://")+3;
		if (beginIndex < 3)
			return "";
		int endIndex = uri.indexOf(":", beginIndex);
		if (endIndex < beginIndex)
			endIndex = uri.indexOf("/", beginIndex);
		String host;
		if (endIndex > beginIndex)
			host = uri.substring(beginIndex, endIndex);
		else
			host = uri.substring(beginIndex);
		return host.toLowerCase();
	}

	/**
	 * Find the host part of the URI
	 * <p>
	 * @param uri
	 * @return
	 */
	private String getUrlServerName(String uri) {
		if (uri == null)
			return "";
		int beginIndex = uri.indexOf("://") + 3;
		beginIndex = uri.indexOf("/", beginIndex) + 1;
		if (beginIndex < 1)
			return "";
		return uri.substring(beginIndex);
	}
	
}
