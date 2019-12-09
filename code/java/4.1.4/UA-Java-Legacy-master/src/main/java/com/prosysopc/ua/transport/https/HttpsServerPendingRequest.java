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
package com.prosysopc.ua.transport.https;

import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.builtintypes.ServiceResponse;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import com.prosysopc.ua.transport.endpoint.EndpointServiceRequest;
import com.prosysopc.ua.transport.security.SecurityPolicy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.core.MessageSecurityMode;
import com.prosysopc.ua.core.StatusCodes;
import com.prosysopc.ua.encoding.DecodingException;
import com.prosysopc.ua.encoding.EncodingException;
import com.prosysopc.ua.encoding.IEncodeable;
import com.prosysopc.ua.encoding.binary.BinaryDecoder;
import com.prosysopc.ua.encoding.binary.BinaryEncoder;
import com.prosysopc.ua.transport.AsyncWrite;
import com.prosysopc.ua.transport.ServerSecureChannel;
import com.prosysopc.ua.transport.security.SecurityMode;
import com.prosysopc.ua.transport.security.SecurityPolicyUri;
import com.prosysopc.ua.transport.tcp.impl.ErrorMessage;
import com.prosysopc.ua.utils.StackUtils;

class HttpsServerPendingRequest extends EndpointServiceRequest<ServiceRequest, ServiceResponse> implements Runnable {
	/** Logger */
	private final static Logger logger = LoggerFactory.getLogger(HttpsServerPendingRequest.class);
	
	HttpsServerEndpointHandler endpoint;
	HttpAsyncExchange httpExchange;
	HttpRequest httpRequest;
	HttpEntity requestEntity;
	HttpsServerSecureChannel channel;
	
	SecurityPolicy securityPolicy;
	
	int requestId;
	AsyncWrite write;
	Thread workerThread;
	int secureChannelId = -1;

	
	/**
	 * <p>Constructor for HttpsServerPendingRequest.</p>
	 *
	 * @param endpoint a {@link HttpsServerEndpointHandler} object.
	 * @param httpExchange a {@link org.apache.http.nio.protocol.HttpAsyncExchange} object.
	 * @param httpRequest a {@link org.apache.http.HttpRequest} object.
	 * @param channel a {@link HttpsServerSecureChannel} object.
	 * @param requestId a int.
	 */
	public HttpsServerPendingRequest(
			HttpsServerEndpointHandler endpoint,
			HttpAsyncExchange httpExchange,
			HttpRequest httpRequest,
			HttpsServerSecureChannel channel,
			int requestId ) {
		super(null, endpoint.endpointBinding.serviceServer, endpoint.endpointBinding.endpointAddress);
		this.endpoint = endpoint;
		this.httpRequest = httpRequest;
		this.requestId = requestId;
		this.httpExchange = httpExchange;
		this.channel = channel;		
		
		String securityPolicyUri = null;
		if ( httpRequest instanceof HttpMessage ) {
			HttpMessage msg = (HttpMessage) httpRequest;
	        Header header = msg.getFirstHeader("OPCUA-SecurityPolicy");
	        if ( header!=null ) {
	        	securityPolicyUri = header.getValue();
	        }
	        if ( securityPolicyUri == null ) securityPolicyUri = SecurityPolicyUri.URI_BINARY_NONE; //1.04 Part 7.4.1
		}
		if(securityPolicyUri == null || securityPolicyUri.isEmpty()) {
			securityPolicy = SecurityPolicy.NONE;
		}else {
			try {
				securityPolicy = SecurityPolicy.getSecurityPolicy(securityPolicyUri);
			} catch (ServiceResultException e) {
				logger.error("Encountered unknown SecurityPolicyUri, {}", securityPolicyUri, e);
				throw new RuntimeException();
			}
		}
		
		
	}
	
	/** {@inheritDoc} */
	@Override
	public ServerSecureChannel getChannel() {
		return channel;
	}
	
	/** {@inheritDoc} */
	@Override
	public void sendResponse(final AsyncWrite write) {
		this.write = write;
		final int statusCode = write.getMessage() instanceof ServiceResponse ? 200 : 400;
		write.setQueued();
		if ( Thread.currentThread() == workerThread) {
			write.setWriting();
			sendResponse( statusCode, write.getMessage() );
			write.setWritten();
		} else {
			Runnable r = new Runnable() {
				public void run() {
					write.setWriting();
					sendResponse( statusCode, write.getMessage() );
					write.setWritten();
				}
			};
			StackUtils.getBlockingWorkExecutor().execute(r);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public AsyncWrite sendResponse(ServiceResponse response) {
		write = new AsyncWrite(response);
		sendResponse( write );
		return write;
	}

	/**
	 * Returns {@link SecurityPolicy} equivalent of the given String as part of the header 'OPCUA-SecurityPolicy'.
	 * {@link SecurityPolicy#NONE} is used if the header is not set (as defined in 1.04 Part 6 section 7.4.1).
	 */
	public SecurityPolicy getSecurityPolicy() {
		return securityPolicy;
	}
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		// This code is ran in a worker thread.
		workerThread = Thread.currentThread();
			
		// 0. Check content Length from the header		
        Header header = httpRequest.getFirstHeader("Content-Length");
        if ( header!=null ) {
        	String len = header.getValue().trim();
        	if (!len.isEmpty()) {
        		try {
        			long contentLength = Long.valueOf(len);
        			long maxMessageSize = endpoint.endpointConfiguration.getMaxMessageSize();
        			
        			// 1. Reject content
        			if ( maxMessageSize!=0 && contentLength > maxMessageSize ) {
        				sendError(500, StatusCodes.Bad_RequestTooLarge, "No request message");
        				return;
        			}
        		} catch (NumberFormatException nfe) {            			
        		}
        	}
        }
		
		// 1. Decode message
		try {			
			byte[] data;
	        if ( httpRequest instanceof HttpEntityEnclosingRequest ) {
	        	HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
	        	requestEntity = entityEnclosingRequest.getEntity();
	        	
	        	// Check content length from the entity ( chucked case )
	        	long contentLength = requestEntity.getContentLength();
    			long maxMessageSize = endpoint.endpointConfiguration.getMaxMessageSize();
    			
    			// 1. Reject content
    			if ( maxMessageSize!=0 && contentLength > maxMessageSize ) {
    				sendError(500, StatusCodes.Bad_RequestTooLarge, "No request message");
    				return;
    			}
	        	
	        	data = EntityUtils.toByteArray(requestEntity);
	        } else {
	        	sendError(500, StatusCodes.Bad_RequestTypeInvalid, "No request message");
	        	return;
	        }				
				
			BinaryDecoder dec = new BinaryDecoder( data );    		
			dec.setEncoderContext( endpoint.getEncoderContext() );
				
			super.request = dec.getMessage();
			logger.trace("request={}", super.request);
			logger.debug("request={}", super.request.getClass().getSimpleName());
		} catch (IllegalStateException e) {
			sendError(500, StatusCodes.Bad_UnexpectedError, e.getMessage());
			return;
		} catch (IOException e) {
			sendError(400, StatusCodes.Bad_UnexpectedError, e.getMessage());
			return;
		} catch (DecodingException e) {
			sendError(400, StatusCodes.Bad_RequestTypeInvalid, e.getMessage());
			return;
		} catch (StackOverflowError e){
			sendError(400, StatusCodes.Bad_DecodingError, e.getMessage());
			return;
		}

		// Handle request
		endpoint.handleMessage( this );		
	}
	
    void sendResponse(int statusCode, IEncodeable responseObject)
    {
    	try {
    		HttpResponse responseHandle = httpExchange.getResponse();
    		responseHandle.setHeader("Content-Type", "application/octet-stream");
    		responseHandle.setStatusCode( statusCode );
	    	
	    	if ( responseObject != null ) {
	    		try {
	    	    	logger.trace("sendResponse: requestId={} statusCode={} responseObject={}", requestId, statusCode, responseObject);
    	    		logger.debug("sendResponse: requestId={} statusCode={} responseObject={}", requestId, statusCode, responseObject.getClass().getSimpleName());
	    	    	
    	    		//Check isDebugEnabled() here for possible performance reasons.
	    	    	if (logger.isDebugEnabled() && channel.getConnection() != null) {
						NHttpServerConnection nHttpServerConnection = ((HttpsServerConnection) channel.getConnection()).getNHttpServerConnection();
						logger.debug("sendResponse: timeout={} {} context={}", httpExchange.getTimeout(), nHttpServerConnection.getSocketTimeout(), nHttpServerConnection.getContext());
					}
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					BinaryEncoder enc = new BinaryEncoder( buf );
					enc.setEncoderContext( endpoint.getEncoderContext() );
					enc.putMessage( responseObject );
					responseHandle.setEntity( new NByteArrayEntity(buf.toByteArray()) );
				} catch (EncodingException e) {
					logger.info("sendResponse: Encoding failed", e);
					// Internal Error
					if ( responseObject instanceof ErrorMessage == false ) {
						responseHandle.setStatusCode( 500 );
					}
				}
	    	}
	    	logger.debug("sendResponse: {} length={}", responseHandle, responseHandle.getEntity().getContentLength());
    		httpExchange.submitResponse(new BasicAsyncResponseProducer(responseHandle));
    	} finally {
    		endpoint.pendingRequests.remove(requestId);
    	}
    }
    
    /**
     * Send error to response object. Optional errorMessage.
     */
    void sendError(int httpStatusCode, UnsignedInteger opcuaStatusCode, String errorMessage)
    {
    	try {
	    	ErrorMessage errorMsg = null;
	    	if ( errorMessage != null ) {
	    		errorMsg = new ErrorMessage(
	    				opcuaStatusCode, 
	    				errorMessage);
	    	}
	    	sendResponse(httpStatusCode, errorMsg);
    	} finally {
    		endpoint.pendingRequests.remove(requestId);
    	}
    }

	@Override
	public SecurityMode getSecurityMode() {
		/*
		 * Note that this cannot be determined from the getChannel, as that is per
		 * 1.04 Part 6 section 7.4.1. all HTTPS is treated as a single SecureChannel
		 * and thus cannot represent the client selection.
		 */
		
		if(SecurityPolicy.NONE == securityPolicy) {
			return SecurityMode.NONE;
		}
		//1.04 part 6 section 7.4.1
		return new SecurityMode(securityPolicy, MessageSecurityMode.Sign);
	}
	
	
}
