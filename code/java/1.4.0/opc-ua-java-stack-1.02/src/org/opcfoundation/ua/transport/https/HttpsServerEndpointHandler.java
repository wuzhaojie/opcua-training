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
package org.opcfoundation.ua.transport.https;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CloseSecureChannelRequest;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.core.OpenSecureChannelRequest;
import org.opcfoundation.ua.core.ServiceFault;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.EndpointBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsServerEndpointHandler implements HttpAsyncRequestHandler<HttpRequest> {

	/** Logger */
	private final static Logger logger = LoggerFactory.getLogger(HttpsServerEndpointHandler.class);		
	
	/** The endpoint binding */
	EndpointBinding endpointBinding;
	
	/** The endpoint server */
	HttpsServer endpointServer;
	
    /** Endpoint Configuration */
	EndpointConfiguration endpointConfiguration;	
	
	/** Secure channel counter */
	AtomicInteger requestIdCounter = new AtomicInteger();			
	
	/** Pending Requests */
	Map<Integer, HttpsServerPendingRequest> pendingRequests = new ConcurrentHashMap<Integer, HttpsServerPendingRequest>();
	
	HttpsServerSecureChannel singleSecureChannel;
	
	public HttpsServerEndpointHandler( EndpointBinding endpointBinding ) {
		this.endpointBinding = endpointBinding;
		this.endpointServer = (HttpsServer) endpointBinding.endpointServer;
        endpointConfiguration = endpointBinding.endpointAddress.getEndpointConfiguration();
        
		singleSecureChannel = new HttpsServerSecureChannel(this, 1);
	}

	public EncoderContext getEncoderContext() {
		return endpointServer.getEncoderContext();
	}

	@Override
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context) throws HttpException, IOException {
        // Buffer request content in memory for simplicity
        return new BasicAsyncRequestConsumer();
	}

	@Override
	public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context) throws HttpException, IOException {
		
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
                                
		HttpsServerPendingRequest req = new HttpsServerPendingRequest( 
				this, 
				httpExchange, 
				request, 
				singleSecureChannel,
				requestIdCounter.getAndIncrement() );
		pendingRequests.put(req.requestId, req);

		//Check isDebugEnabled() here for possible performance reasons.
    	//if (logger.isDebugEnabled()) {
            // Request URI is already set.
            //String requestUri = request.getRequestLine().getUri();
            //requestUri = URLDecoder.decode(requestUri, "UTF-8");            
    	NHttpServerConnection connection = (NHttpServerConnection) context.getAttribute("http.connection");
    		//logger.debug(method+" "+requestUri+"from"+connection);    		
    	logger.debug("handle: {} context={}: {}", connection, connection.getContext(), request);
    	//}

    	HttpsServerConnection currentConnection = ( HttpsServerConnection ) singleSecureChannel.getConnection();

    	if(currentConnection == null || !connection.equals(currentConnection.getNHttpServerConnection())) {
        	HttpsServerConnection httpsConnection = new HttpsServerConnection(this.endpointServer, connection);
    		singleSecureChannel.setConnection(httpsConnection);
    		logger.info("HttpsServerEndpointHandler.handle(): singleSecureChannel.setConnection({})", connection);
    	}
		// Run in worker thread.
		//StackUtils.getBlockingWorkExecutor().execute( req );
		// Run in current thread
		req.run();
    }

    /**
     * Handle incoming request. This method is called from HttpsServerPendingRequest from 
     * worker thread.
     * 
     * @param pendingMessage
     */
    void handleMessage( HttpsServerPendingRequest pendingMessage ) {
    	
    	IEncodeable msg = pendingMessage.getRequest();
    				
		// Client sent OpenSecureChannelRequest
		if ( msg instanceof OpenSecureChannelRequest ) {
			try {
				handleOpenSecureChannelRequest( (OpenSecureChannelRequest) msg, pendingMessage );
			} catch (ServiceResultException e) {
				logger.info("Channel: ", e);
				pendingMessage.sendError( 400, e.getStatusCode().getValue(), e.getMessage() ); 
			}
		} else
		
		if ( msg instanceof CloseSecureChannelRequest ) {
			try {
				handleCloseSecureChannelRequest( (CloseSecureChannelRequest) msg, pendingMessage );
			} catch (ServiceResultException e) {
				logger.info("Channel: ", e);
				pendingMessage.sendError( 400, e.getStatusCode().getValue(), e.getMessage() ); 
			}
		} else 
		
		if ( msg instanceof ServiceRequest ) {
			try {
				handleServiceRequest( (ServiceRequest) msg, pendingMessage );
			} catch (ServiceResultException e) {
				logger.info("Channel: ", e);
				pendingMessage.sendError( 400, e.getStatusCode().getValue(), e.getMessage() ); 
			}
		}
		
		else {
			String errorMessage = pendingMessage.getClass().getSimpleName()+" is not a ServiceRequest.";
			logger.info("Channel: {} {}", pendingMessage.getChannel().getSecureChannelId(), errorMessage);
			// Client sent something odd
			pendingMessage.sendError(400, StatusCodes.Bad_RequestTypeInvalid, errorMessage);
			return;
		}
    }

	void handleServiceRequest(ServiceRequest serviceRequest, HttpsServerPendingRequest msgExchange) 
			throws ServiceResultException 
	{
		// 1. Handle message & 2. Send Response
		try {			
			endpointBinding.serviceServer.getServiceHandlerComposition().serve( msgExchange );
		} catch (ServiceResultException e) {
			// Managed Error
			String errorMessage = msgExchange.getClass().getSimpleName()+" is not a ServiceRequest.";
			logger.info("Channel: {} {}", msgExchange.getChannel().getSecureChannelId(), errorMessage);
			msgExchange.sendError(200, e.getStatusCode().getValue(), errorMessage);
			return;
		}		
	}

	void handleOpenSecureChannelRequest(OpenSecureChannelRequest req, HttpsServerPendingRequest msgExchange) throws ServiceResultException {
		msgExchange.sendFault( ServiceFault.createServiceFault( StatusCodes.Bad_ServiceUnsupported ) );
	}
	
	void handleCloseSecureChannelRequest(CloseSecureChannelRequest msg, HttpsServerPendingRequest msgExchange) 
	throws ServiceResultException 
	{
		msgExchange.sendFault( ServiceFault.createServiceFault( StatusCodes.Bad_ServiceUnsupported ) );
	}
    
	///// Secure channels are disabled /////
	
	// Secure Channels 
	//Map<Integer, HttpsServerSecureChannel> secureChannels = new ConcurrentHashMap<Integer, HttpsServerSecureChannel>();
	// SecureChannel-TokenId <-> SecureChannel
	//Map<UnsignedInteger, HttpsServerSecureChannel> tokenIdMap = new ConcurrentHashMap<UnsignedInteger, HttpsServerSecureChannel>();
	// Session-AuthenticationToken <-> SecureChannelId
	//ConcurrentBijectionMap<NodeId, Integer> authenticationTokenMap = new ConcurrentBijectionMap<NodeId, Integer>();

	/*
	void handleOpenSecureChannelRequest(OpenSecureChannelRequest req, HttpsServerPendingRequest msgExchange) throws ServiceResultException {
		if (req.getRequestType() == SecurityTokenRequestType.Issue)
		{
			int secureChannelId = endpointServer.secureChannelCounter.incrementAndGet();
			HttpsServerSecureChannel channel = new HttpsServerSecureChannel( this, secureChannelId );
			secureChannels.put(secureChannelId, channel);
			logger.debug(endpointServer + " SecureChannelId=" + channel.getSecureChannelId());
			channel.handleOpenChannel(msgExchange, req);
			
		} else if (req.getRequestType() == SecurityTokenRequestType.Renew) {
			int secureChannelId = msgExchange.secureChannelId;
			if ( secureChannelId == -1 ) {
				NodeId authenticationToken = req.getRequestHeader().getAuthenticationToken();
				Integer secureChannelId_ = authenticationTokenMap.getRight( authenticationToken );
				if ( secureChannelId_ != null ) secureChannelId = secureChannelId_;
			}
			HttpsServerSecureChannel channel = (HttpsServerSecureChannel) secureChannels.get( secureChannelId );
			if ( channel == null ) throw new ServiceResultException( Bad_SecureChannelIdInvalid );
			
			if ( !ObjectUtils.objectEquals(
					req.getRequestType(), 
					SecurityTokenRequestType.Renew ) ) { 
				throw new ServiceResultException(Bad_UnexpectedError);
			}
			
			channel.handleRenewSecureChannelRequest(msgExchange, req);								
		}
		
		// Remove closed channels, should maybe be based on timestamps TODO
		Object[] channelArray = secureChannels.values().toArray();
		for (Object channel : channelArray) {
			AbstractState<CloseableObjectState, ServiceResultException> state = (AbstractState<CloseableObjectState, ServiceResultException>) channel;
			if (state.equals(CloseableObjectState.Closed)) {
				secureChannels.remove(((AbstractServerSecureChannel)channel).getSecureChannelId());
			}
		}
	}
	void handleCloseSecureChannelRequest(CloseSecureChannelRequest msg, HttpsServerPendingRequest msgExchange) 
	throws ServiceResultException 
	{
		int secureChannelId = msgExchange.secureChannelId;
		HttpsServerSecureChannel channel = (HttpsServerSecureChannel) secureChannels.get( secureChannelId );
		if ( channel == null ) throw new ServiceResultException( Bad_SecureChannelIdInvalid );
		
		channel.handleCloseChannel( msgExchange, msg );
		secureChannels.remove( secureChannelId );
		authenticationTokenMap.removeWithRight( secureChannelId );
	}
	*/
	///// Secure channels are disabled /////
	
}
