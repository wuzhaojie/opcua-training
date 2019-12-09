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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.EndpointBinding;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.endpoint.AbstractServerSecureChannel;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.transport.tcp.nio.PendingRequest;
import org.opcfoundation.ua.utils.StackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsServerSecureChannel extends AbstractServerSecureChannel {
	
	public KeyPair getLocalCertificate() {
		return null;
	}
	
	public Cert getRemoteCertificate() {
		return null;
	}

	/** Logger */
	private static Logger logger = LoggerFactory.getLogger(HttpsServerSecureChannel.class);
	/** Endpoint binding */
	private EndpointBinding endpointBinding;
	/** Https Endpoint Handler */
	private HttpsServerEndpointHandler httpsEndpointHandler;
	/** The connection object that delivered the last message for this secure channel */
	private volatile HttpsServerConnection lastConnection;
	
	/**
	 * List on pending requests. All reads and writes are done by synchronizing to the
	 * requests object. 
	 */
	Map<Integer, PendingRequest> requests = new ConcurrentHashMap<Integer, PendingRequest>();
	
	public HttpsServerSecureChannel(HttpsServerEndpointHandler httpsEndpointHandler, int secureChannelId) {
		super(secureChannelId);
		this.endpointBinding = httpsEndpointHandler.endpointBinding;
		this.httpsEndpointHandler = httpsEndpointHandler;
	}

	public MessageSecurityMode getMessageSecurityMode() {
		return MessageSecurityMode.None;
	}

	public SecurityPolicy getSecurityPolicy() {
		return SecurityPolicy.NONE;
	}	
		
	public synchronized void setError(ServiceResultException e) {
		super.setError( e );
	}

	@Override
	protected void onListenerException(RuntimeException rte) {
		setError( StackUtils.toServiceResultException(rte) );
	}

	@Override
	public ServerConnection getConnection() {
		return lastConnection;
	}
	public void setConnection(HttpsServerConnection connection) {
		lastConnection = connection;
	}

	public String getConnectURL() {
		return endpointBinding.endpointAddress.getEndpointUrl();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void getPendingServiceRequests(Collection<EndpointServiceRequest<?, ?>> result) {
		Map<Integer, HttpsServerPendingRequest> snapshot = new HashMap<Integer, HttpsServerPendingRequest>( httpsEndpointHandler.pendingRequests );
		for (HttpsServerPendingRequest req : snapshot.values()) {
			if ( req.secureChannelId == getSecureChannelId() )
				result.add( req );
		}
	}

	@Override
	public Endpoint getEndpoint() {
		return endpointBinding.endpointAddress;
	}

	@Override
	public Server getServer() {
		return endpointBinding.serviceServer;
	}

	@Override
	public boolean needsCertificate() {
		return true;
	}

	
	// Removed - There are no proper secure channels in https //

	/*
	void handleOpenChannel(HttpsServerPendingRequest msgExchange, OpenSecureChannelRequest req) throws ServiceResultException {
		
		setState(CloseableObjectState.Opening);
		
		securityPolicy		= SecurityPolicy.getSecurityPolicy( msgExchange.securityPolicyUri );
		MessageSecurityMode messageMode		= req.getSecurityMode();
		SecurityMode securityMode			= new SecurityMode(securityPolicy, messageMode);
		if (!getEndpoint().supportsSecurityMode(securityMode)) {
			log.warn("The requested MessageSecurityMode("+messageMode+") is not supported by the endpoint");
			throw new ServiceResultException("The requested MessageSecurityMode("+messageMode+") is not supported by the endpoint");
		}
		
		securityConfiguration				= 
				new SecurityConfiguration(
					securityMode,
					endpointBinding.serviceServer.getApplicationInstanceCertificate(),
					null);
		
		SecurityToken token = createToken(msgExchange, req);		
		
		
		ChannelSecurityToken chanToken		= new ChannelSecurityToken();
		chanToken.setChannelId( UnsignedInteger.valueOf(secureChannelId) );
		chanToken.setCreatedAt( new DateTime() );
		chanToken.setRevisedLifetime(UnsignedInteger.valueOf(token.getLifeTime()));
		chanToken.setTokenId(UnsignedInteger.valueOf(token.getTokenId()));

		setState(CloseableObjectState.Open);	
		
		OpenSecureChannelResponse res = new OpenSecureChannelResponse();
		res.setResponseHeader(new ResponseHeader());
		res.setSecurityToken(chanToken);
		res.setServerNonce(token.getLocalNonce());
		res.setServerProtocolVersion( UnsignedInteger.valueOf( 0 ) );
		tokens.put( token.getTokenId(), token );
		setActiveSecurityToken(token);				
		msgExchange.sendResponse(res);
	}
	
	void handleRenewSecureChannelRequest(HttpsServerPendingRequest msgExchange, OpenSecureChannelRequest req) throws ServiceResultException {
		if ( !getState().isOpen() ) {
			msgExchange.sendException( new ServiceResultException( StatusCodes.Bad_SecureChannelClosed, "Failed to renew token, secure channel has already been closed." ) );
			return;
		}
		
		SecurityToken token = createToken(msgExchange, req);		
		ChannelSecurityToken chanToken		= new ChannelSecurityToken();
		chanToken.setChannelId( UnsignedInteger.valueOf(secureChannelId) );
		chanToken.setCreatedAt( new DateTime() );
		chanToken.setRevisedLifetime(UnsignedInteger.valueOf(token.getLifeTime()));
		chanToken.setTokenId(UnsignedInteger.valueOf(token.getTokenId()));
		
		
		OpenSecureChannelResponse res = new OpenSecureChannelResponse();
		res.setSecurityToken(chanToken);
		res.setServerNonce(token.getLocalNonce());
		res.setServerProtocolVersion( UnsignedInteger.valueOf( 0 ) );
		
		UnsignedInteger reqHandle = req.getRequestHeader() == null ? null : req.getRequestHeader().getRequestHandle();
		ResponseHeader header = new ResponseHeader();
		res.setResponseHeader( header );
		header.setRequestHandle( reqHandle );
		
		tokens.put( token.getTokenId(), token );
		msgExchange.sendResponse(res);
	}

	void handleCloseChannel(HttpsServerPendingRequest msgExchange, CloseSecureChannelRequest req) {
		close();	
		CloseSecureChannelResponse res = new CloseSecureChannelResponse();
		ResponseHeader header = new ResponseHeader();
		header.setRequestHandle( req.getRequestHeader().getRequestHandle() );
		res.setResponseHeader( header );
		msgExchange.sendResponse( res );
	}
	Map<Integer, SecurityToken> tokens = new ConcurrentHashMap<Integer, SecurityToken>();
	SecurityToken			activeToken;
	NodeId 				authenticationToken;

	public synchronized SecurityToken getSecurityToken(int tokenId) {
		if (log.isDebugEnabled())
			log.debug("tokens("+tokens.size()+")="+tokens.values());
		return tokens.get(tokenId);
	}
	
	private void pruneInvalidTokens()
	{	
		if (log.isDebugEnabled())
			log.debug("pruneInvalidTokens: tokens("+tokens.size()+")="+tokens.values());
		for (SecurityToken t : tokens.values())
			if (!t.isValid()) {
				if (log.isDebugEnabled())
					log.debug("pruneInvalidTokens: remove Id="+t.getTokenId());
				tokens.remove(t.getTokenId());
			}
	}	
	public synchronized SecurityToken getLatestNonExpiredToken()
	{
		SecurityToken result = null;
		for (SecurityToken t : tokens.values())
		{
			if (t.isExpired()) continue;
			if (result==null) result = t;
			if (t.getCreationTime() > result.getCreationTime()) result = t;
		}
		return result;
	}
	
	
	private SecurityToken createToken(HttpsServerPendingRequest msgExchange, OpenSecureChannelRequest req) throws ServiceResultException
	{
		byte[] clientNonce					= req.getClientNonce();
		int tokenId							= tokenIdCounter.incrementAndGet();				
		
		SecurityAlgorithm algo = securityPolicy.getAsymmetricEncryptionAlgorithm();
		int nonceLength = CryptoUtil.getNonceLength( algo );
		byte[] serverNonce = CryptoUtil.createNonce( nonceLength );
		
		final UnsignedInteger tokenLifetime = 
			req.getRequestedLifetime() != null && req.getRequestedLifetime().intValue() > 0 
				? req.getRequestedLifetime() 
				: StackUtils.SERVER_GIVEN_TOKEN_LIFETIME;
		log.debug("tokenLifetime: "+tokenLifetime);
		SecurityToken token = new SecurityToken(
				securityConfiguration, 
				secureChannelId,
				tokenId,
				System.currentTimeMillis(),
				tokenLifetime.longValue(),
				serverNonce,
				clientNonce
				);
		tokens.put(tokenId, token);

		return token;
	}
	
	public SecurityToken getActiveSecurityToken() {
		return activeToken;
	}
	
	public void setActiveSecurityToken(SecurityToken token) {
		if (token==null) 
			throw new IllegalArgumentException("null");
		if (log.isDebugEnabled())
			log.debug("Switching to new security token "+token.getTokenId());
		this.activeToken = token;
		pruneInvalidTokens();
	}
	public MessageSecurityMode getMessageSecurityMode() {
		SecurityToken token = getActiveSecurityToken();
		return token==null ? null : token.getMessageSecurityMode();
	}

	public SecurityPolicy getSecurityPolicy() {
		SecurityToken token = getActiveSecurityToken();
		return token==null ? null : token.getSecurityPolicy();
	}	
	public NodeId getAuthenticationToken() {
		return authenticationToken;
	}

	*/
}
