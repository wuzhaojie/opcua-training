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
package org.opcfoundation.ua.transport.tcp.nio;

import static org.opcfoundation.ua.core.StatusCodes.Bad_SecureChannelClosed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ChannelSecurityToken;
import org.opcfoundation.ua.core.CloseSecureChannelRequest;
import org.opcfoundation.ua.core.CloseSecureChannelResponse;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.OpenSecureChannelRequest;
import org.opcfoundation.ua.core.OpenSecureChannelResponse;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.AsyncWrite;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.endpoint.AbstractServerSecureChannel;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.transport.tcp.impl.SecurityToken;
import org.opcfoundation.ua.transport.tcp.impl.TcpMessageType;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.EndpointUtil;
import org.opcfoundation.ua.utils.StackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpcTcpServerSecureChannel extends AbstractServerSecureChannel {
	
	public KeyPair getLocalCertificate() {
		return securityConfiguration.getLocalCertificate2();
	}
	
	public Cert getRemoteCertificate() {
		return securityConfiguration.getRemoteCertificate2();
	}

	/** Logger */
	static Logger logger = LoggerFactory.getLogger(OpcTcpServerSecureChannel.class);
	/** Security profile for this security channel */
	public SecurityConfiguration securityConfiguration;			
	/** Secure channel counter */
	AtomicInteger tokenIdCounter = new AtomicInteger();			
	/** The tcp connection that hosts this secure channel */
	OpcTcpServerConnection connection;
	/** Sequence number counter of outbound messages */
	public final AtomicInteger		sendSequenceNumber = new AtomicInteger( new Random().nextInt(1024) );
	/** Sequence number counter of inbound messages */
	public final AtomicInteger		recvSequenceNumber = new AtomicInteger();
	
	public OpcTcpServerSecureChannel(OpcTcpServerConnection connection, int secureChannelId)
	{
		super(secureChannelId);
		this.connection = connection;
	}
	
	@Override
	public String getConnectURL() {
		return connection.ctx.endpointUrl;
	}

	@Override
	public ServerConnection getConnection() {
		return connection;
	}
	
	@Override
	public Endpoint getEndpoint() {
		return connection.binding.endpointAddress;
	}
	
	public Server getServer() {
		return connection.binding.serviceServer;
	}

	@Override
	public void getPendingServiceRequests(Collection<EndpointServiceRequest<?, ?>> result) {
		result.addAll( connection.pendingRequests.values() );
	}
	
	protected void handleSecureMessage(InputMessage mb, IEncodeable msg) throws ServiceResultException {
		logger.debug("onSecureMessage: server={}", getServer());
		logger.debug("onSecureMessage: endpoint={}", getEndpoint());
		int requestId = mb.getRequestId();
		PendingRequest req = new PendingRequest(this, getEndpoint(), getServer(), mb.getRequestId(), (ServiceRequest) msg); 
		connection.pendingRequests.put(requestId, req);
		getServer().getServiceHandlerComposition().serve(req);
	}

	private SecurityToken createToken(OpenSecureChannelRequest req, InputMessage mb) throws ServiceResultException
	{
		byte[] clientNonce					= req.getClientNonce();
		int tokenId							= tokenIdCounter.incrementAndGet();				

		SecurityAlgorithm algo = securityConfiguration.getSecurityPolicy().getSymmetricEncryptionAlgorithm();
		byte[] serverNonce = CryptoUtil.createNonce( algo );
		
		final UnsignedInteger tokenLifetime = 
			req.getRequestedLifetime() != null && req.getRequestedLifetime().intValue() > 0 
				? req.getRequestedLifetime() 
				: StackUtils.SERVER_GIVEN_TOKEN_LIFETIME;
		logger.debug("tokenLifetime: {}", tokenLifetime);
		SecurityToken token = new SecurityToken(
				securityConfiguration, 
				getSecureChannelId(),
				tokenId,
				System.currentTimeMillis(),
				tokenLifetime.longValue(),
				serverNonce,
				clientNonce
				);
		tokens.put(tokenId, token);

		return token;
	}

	private void sendOpenChannelResponse(InputMessage mb,
			SecurityToken token, SecurityConfiguration securityConfiguration) throws ServiceResultException {
		ChannelSecurityToken chanToken		= new ChannelSecurityToken();
		chanToken.setChannelId( UnsignedInteger.valueOf(getSecureChannelId()) );
		chanToken.setCreatedAt( new DateTime() );
		chanToken.setRevisedLifetime(UnsignedInteger.valueOf(token.getLifeTime()));
		chanToken.setTokenId(UnsignedInteger.valueOf(token.getTokenId()));
		
		setState(CloseableObjectState.Open);	
		connection.secureChannels.put(getSecureChannelId(), this);

		OpenSecureChannelRequest req = (OpenSecureChannelRequest) mb.getMessage();
		OpenSecureChannelResponse res		= new OpenSecureChannelResponse();
		res.setSecurityToken(chanToken);
		res.setServerNonce(token.getLocalNonce());
		res.setServerProtocolVersion( UnsignedInteger.valueOf(connection.agreedProtocolVersion) );

		UnsignedInteger reqHandle = req.getRequestHeader() == null ? null : req.getRequestHeader().getRequestHandle();
		ResponseHeader header = new ResponseHeader();
		res.setResponseHeader( header );
		header.setRequestHandle( reqHandle );
		
		AsyncWrite msgToWrite = new AsyncWrite(res);
		boolean isAsync = (mb.getMessageType() == TcpMessageType.OPEN) || (mb.getMessageType() == TcpMessageType.CLOSE); 
		if (isAsync) {
			connection.sendAsymmSecureMessage(msgToWrite, securityConfiguration, token.getSecureChannelId(), mb.getRequestId(), sendSequenceNumber);
		} else {
			connection.sendSecureMessage(msgToWrite, activeToken, mb.getRequestId(), TcpMessageType.MESSAGE, sendSequenceNumber);
		}
		
	}

	protected void handleOpenChannel(InputMessage mb, OpenSecureChannelRequest req) throws ServiceResultException {

		SecurityConfiguration sc			= (SecurityConfiguration) mb.getToken();
		SecurityPolicy securityPolicy		= sc.getSecurityPolicy();
		MessageSecurityMode messageMode		= req.getSecurityMode();
		SecurityMode securityMode			= new SecurityMode(securityPolicy, messageMode);
//		if (!getEndpoint().supportsSecurityMode(securityMode)) {
//			logger.info("The requested MessageSecurityMode("+messageMode+") is not supported by the endpoint");
//			throw new ServiceResultException("The requested MessageSecurityMode("+messageMode+") is not supported by the endpoint");
//		}
		KeyPair localCertificate = null;
		Cert remoteCertificate = sc.getRemoteCertificate2();
		if (messageMode.hasSigning() || EndpointUtil.containsSecureUserTokenPolicy(getServer().getUserTokenPolicies()))
			localCertificate = sc.getLocalCertificate2();
		else if (remoteCertificate != null) 
			logger.debug("Client defines a certificate although SecurityPolicy.NONE is defined");

		securityConfiguration = 
			new SecurityConfiguration(
				securityMode,
				localCertificate,
				remoteCertificate);
		
		SecurityToken token = createToken(req, mb);

		// Set the receive sequence number to the size of the list
		recvSequenceNumber.set( mb.getSequenceNumbers().get(mb.getSequenceNumbers().size()-1)+1 );
		
		setState(CloseableObjectState.Opening);
		setActiveSecurityToken(token);				
		
		sendOpenChannelResponse(mb, token, securityConfiguration);

		logger.info("SecureChannel opened; {}", getActiveSecurityToken());
	}

	protected void handleRenewSecureChannelRequest(InputMessage mb, OpenSecureChannelRequest req) throws ServiceResultException {
		/*// Untested code, therefore commented out //
		if ( !getState().isOpen() ) {
			msgExchange.sendException( new ServiceResultException( StatusCodes.Bad_SecureChannelClosed, "Failed to renew token, secure channel has already been closed." ) );
			return;
		}
		*/
		
		SecurityToken token = createToken(req, mb);
		sendOpenChannelResponse(mb, token, (SecurityConfiguration) mb.getToken());
		logger.info("SecureChannel renewed; {}", token);
	}
	
	protected void handleCloseSecureChannelRequest(InputMessage mb, CloseSecureChannelRequest req) {
		close();	
		CloseSecureChannelResponse res = new CloseSecureChannelResponse();
		UnsignedInteger reqHandle = req.getRequestHeader() == null ? null : req.getRequestHeader().getRequestHandle();
		ResponseHeader header = new ResponseHeader();
		res.setResponseHeader( header );
		header.setRequestHandle( reqHandle );
		AsyncWrite msg = new AsyncWrite(res);
		connection.sendSecureMessage(msg, getActiveSecurityToken(), mb.getRequestId(), TcpMessageType.CLOSE, sendSequenceNumber);		
	}
	
	// Propagate channel closed/error to pending requests
	@Override
	protected synchronized void onStateTransition(CloseableObjectState oldState, CloseableObjectState newState) 
	{			
		super.onStateTransition(oldState, newState);
		
		if (newState==CloseableObjectState.Closed) {	
			logger.info("Secure Channel closed, token={}", activeToken);
			connection.secureChannels.remove( getSecureChannelId() ); //COMPLIANCE
			//deadChannels.add(getSecureChannelId(), DateTime.currentTime());
			connection.fireSecureChannelDetached( this );
			// Cancel pending requests			
			ServiceResultException se = new ServiceResultException(Bad_SecureChannelClosed);
			for (PendingRequest pr : getPendingRequests2())
			{
				AsyncWrite w = pr.write;
				if (w!=null) w.attemptSetError(se);
			}
		}
	}
	
	protected Collection<PendingRequest> getPendingRequests2() {
		ArrayList<PendingRequest> result = new ArrayList<PendingRequest>();
		for (PendingRequest pr : connection.pendingRequests.values()) {
			if (pr.channel == this)
				result.add(pr);
		}
		return result;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean needsCertificate() {
		return getMessageSecurityMode().hasSigning() || EndpointUtil.containsSecureUserTokenPolicy(getServer().getUserTokenPolicies());
	}

}
