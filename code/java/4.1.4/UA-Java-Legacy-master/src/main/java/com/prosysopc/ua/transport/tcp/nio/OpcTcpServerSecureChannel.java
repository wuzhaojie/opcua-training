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
package com.prosysopc.ua.transport.tcp.nio;

import static com.prosysopc.ua.core.StatusCodes.Bad_SecureChannelClosed;

import com.prosysopc.ua.application.Server;
import com.prosysopc.ua.builtintypes.ByteString;
import com.prosysopc.ua.builtintypes.DateTime;
import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import com.prosysopc.ua.core.CloseSecureChannelResponse;
import com.prosysopc.ua.transport.endpoint.AbstractServerSecureChannel;
import com.prosysopc.ua.transport.endpoint.EndpointServiceRequest;
import com.prosysopc.ua.transport.security.Cert;
import com.prosysopc.ua.transport.security.KeyPair;
import com.prosysopc.ua.transport.security.SecurityConfiguration;
import com.prosysopc.ua.transport.security.SecurityMode;
import com.prosysopc.ua.transport.security.SecurityPolicy;
import com.prosysopc.ua.transport.tcp.impl.SecurityToken;
import com.prosysopc.ua.transport.tcp.impl.TcpMessageType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.core.ChannelSecurityToken;
import com.prosysopc.ua.core.CloseSecureChannelRequest;
import com.prosysopc.ua.core.MessageSecurityMode;
import com.prosysopc.ua.core.OpenSecureChannelRequest;
import com.prosysopc.ua.core.OpenSecureChannelResponse;
import com.prosysopc.ua.core.ResponseHeader;
import com.prosysopc.ua.encoding.IEncodeable;
import com.prosysopc.ua.transport.AsyncWrite;
import com.prosysopc.ua.transport.CloseableObjectState;
import com.prosysopc.ua.transport.Endpoint;
import com.prosysopc.ua.transport.ServerConnection;
import com.prosysopc.ua.utils.CryptoUtil;
import com.prosysopc.ua.utils.EndpointUtil;
import com.prosysopc.ua.utils.StackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>OpcTcpServerSecureChannel class.</p>
 *
 */
public class OpcTcpServerSecureChannel extends AbstractServerSecureChannel {
	
	/**
	 * <p>getLocalCertificate.</p>
	 *
	 * @return a {@link KeyPair} object.
	 */
	public KeyPair getLocalCertificate() {
		return securityConfiguration.getLocalCertificate2();
	}
	
	/**
	 * <p>getRemoteCertificate.</p>
	 *
	 * @return a {@link Cert} object.
	 */
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
	
	/**
	 * <p>Constructor for OpcTcpServerSecureChannel.</p>
	 *
	 * @param connection a {@link OpcTcpServerConnection} object.
	 * @param secureChannelId a int.
	 */
	public OpcTcpServerSecureChannel(OpcTcpServerConnection connection, int secureChannelId)
	{
		super(secureChannelId);
		this.connection = connection;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getConnectURL() {
		return connection.ctx.endpointUrl;
	}

	/** {@inheritDoc} */
	@Override
	public ServerConnection getConnection() {
		return connection;
	}
	
	/** {@inheritDoc} */
	@Override
	public Endpoint getEndpoint() {
		return connection.binding.endpointAddress;
	}
	
	/**
	 * <p>getServer.</p>
	 *
	 * @return a {@link Server} object.
	 */
	public Server getServer() {
		return connection.binding.serviceServer;
	}

	/** {@inheritDoc} */
	@Override
	public void getPendingServiceRequests(Collection<EndpointServiceRequest<?, ?>> result) {
		result.addAll( connection.pendingRequests.values() );
	}
	
	/**
	 * <p>handleSecureMessage.</p>
	 *
	 * @param mb a {@link InputMessage} object.
	 * @param msg a {@link IEncodeable} object.
	 * @throws ServiceResultException if any.
	 */
	protected void handleSecureMessage(InputMessage mb, IEncodeable msg) throws ServiceResultException {
		logger.debug("onSecureMessage: server={}", getServer());
		logger.debug("onSecureMessage: endpoint={}", getEndpoint());
		int requestId = mb.getRequestId();
		PendingRequest req = new PendingRequest(this, getEndpoint(), getServer(), mb.getRequestId(), (ServiceRequest) msg);
		connection.pendingRequests.put(requestId, req);
		getServer().getServiceHandlerComposition().serve(req);
	}

	private SecurityToken createToken(OpenSecureChannelRequest req, InputMessage mb) throws ServiceResultException 	{
		ByteString clientNonce = req.getClientNonce();
		int tokenId = tokenIdCounter.incrementAndGet();

		ByteString serverNonce = CryptoUtil.createNonce(securityConfiguration.getSecurityPolicy().getSecureChannelNonceLength());
		
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

	/**
	 * <p>handleOpenChannel.</p>
	 *
	 * @param mb a {@link InputMessage} object.
	 * @param req a {@link OpenSecureChannelRequest} object.
	 * @throws ServiceResultException if any.
	 */
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

	/**
	 * <p>handleRenewSecureChannelRequest.</p>
	 *
	 * @param mb a {@link InputMessage} object.
	 * @param req a {@link OpenSecureChannelRequest} object.
	 * @throws ServiceResultException if any.
	 */
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
	
	/**
	 * <p>handleCloseSecureChannelRequest.</p>
	 *
	 * @param mb a {@link InputMessage} object.
	 * @param req a {@link CloseSecureChannelRequest} object.
	 */
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
	/** {@inheritDoc} */
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
	
	/**
	 * <p>getPendingRequests2.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	protected Collection<PendingRequest> getPendingRequests2() {
		ArrayList<PendingRequest> result = new ArrayList<PendingRequest>();
		for (PendingRequest pr : connection.pendingRequests.values()) {
			if (pr.channel == this)
				result.add(pr);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean needsCertificate() {
		return getMessageSecurityMode().hasSigning() || EndpointUtil.containsSecureUserTokenPolicy(getServer().getUserTokenPolicies());
	}

}
