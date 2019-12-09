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

import static org.opcfoundation.ua.core.StatusCodes.Bad_ApplicationSignatureInvalid;
import static org.opcfoundation.ua.utils.EndpointUtil.select;

import java.security.cert.CertificateParsingException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.CreateSessionRequest;
import org.opcfoundation.ua.core.CreateSessionResponse;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.FindServersRequest;
import org.opcfoundation.ua.core.FindServersResponse;
import org.opcfoundation.ua.core.GetEndpointsRequest;
import org.opcfoundation.ua.core.GetEndpointsResponse;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.ChannelService;
import org.opcfoundation.ua.transport.SecureChannel;
import org.opcfoundation.ua.transport.ServiceChannel;
import org.opcfoundation.ua.transport.TransportChannelSettings;
import org.opcfoundation.ua.transport.UriUtil;
import org.opcfoundation.ua.transport.UriUtil.MessageFormat;
import org.opcfoundation.ua.transport.https.HttpsClient;
import org.opcfoundation.ua.transport.https.HttpsClientSecureChannel;
import org.opcfoundation.ua.transport.https.HttpsSettings;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityAlgorithm;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.transport.tcp.io.OpcTcpSettings;
import org.opcfoundation.ua.transport.tcp.io.SecureChannelTcp;
import org.opcfoundation.ua.utils.CertificateUtils;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.bytebuffer.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OPC UA Client application
 */
public class Client {
	Logger logger = LoggerFactory.getLogger(Client.class);
	
	Application application;
	EndpointConfiguration endpointConfiguration = EndpointConfiguration.defaults(); 
	
	public static Client createClientApplication(KeyPair cert) {
		Application application = new Application();
		Client client = new Client(application);
		if (cert != null) {
			try {
				String certApplicationUri = CertificateUtils.getApplicationUriOfCertificate( cert.certificate );
				application.setApplicationUri( certApplicationUri );
			} catch (CertificateParsingException e) {
				//ignore here
			}
			
			application.addApplicationInstanceCertificate(cert);
			application.getHttpsSettings().setKeyPair(cert);
		}
		return client;
	}
	
	/**
	 * Construct a new client application
	 * <p>
	 * Note: Client needs an application instance certificate to create secure
	 * channels. See {@link Application#addApplicationInstanceCertificate(KeyPair)}
	 * 
	 * @param cert
	 */
	public Client(Application application) {
		this.application = application;
	}
	
	public ApplicationDescription createApplicationDescription() {
		ApplicationDescription result = application.applicationDescription.clone();
		result.setApplicationType( ApplicationType.Client );
		return result;
	}	

	public Application getApplication() {
		return application;
	}
	
	public HttpsSettings getApplicationHttpsSettings() {
		return application.getHttpsSettings();
	}
	
	public OpcTcpSettings getApplicatioOpcTcpSettings() {
		return application.getOpctcpSettings();
	}

	/**
	 * Create a new session on a server
	 * 
	 * @param channel
	 *            open channel
	 * @throws ServiceResultException
	 */
	public Session createSession(SecureChannel channel) throws ServiceResultException 
	{
		return createSession(channel, null, null, null);
	}


	/**
	 * Create a new session on a server
	 * 
	 * @param channel
	 *            open channel to use
	 * @param maxResponseMessageSize
	 *            max size of response messages - if null, use 4194304
	 * @param requestedSessionTimeout
	 *            requested session time out (in ms) - if null, use 3600000 (one hour)
	 * @param sessionName
	 *            session name - if null a random GUID is used to generate the
	 *            name
	 * @return the session
	 * @throws IllegalArgumentException
	 * @throws ServiceResultException
	 */
	public Session createSession(SecureChannel channel,	UnsignedInteger maxResponseMessageSize,
			Double requestedSessionTimeout, String sessionName)
			throws IllegalArgumentException, ServiceResultException {
		return createSession(channel, maxResponseMessageSize, requestedSessionTimeout, sessionName, null);
		
	}
		
	/**
	 * Create a new session on a server
	 * @param channel open channel to use
	 * @param maxResponseMessageSize max size of response messages - if null, defaulting to 4194304
	 * @param requestedSessionTimeout requested session timeout (ms) - if null, defaulting to 3600000 (one hour)
	 * @param sessionName session name - if null, a random GUID is used to generate the name
	 * @param discoveredEndpoints list of previously discovered Endpoints using GetEndpoints service. 
	 * This list is checked against the Endpoint list received from CreateSessionResponse, 
	 * if it does not match, an exception is thrown, if null the check is disabled
	 * @return the session
	 */
	public Session createSession(SecureChannel channel,
			UnsignedInteger maxResponseMessageSize,
			Double requestedSessionTimeout, String sessionName, EndpointDescription[] discoveredEndpoints) 
					throws IllegalArgumentException, ServiceResultException{
		
		
		if (maxResponseMessageSize == null)
		maxResponseMessageSize = UnsignedInteger.valueOf(4 * 1024 * 1024);
		if (requestedSessionTimeout == null)
		requestedSessionTimeout = 60 * 60 * 1000.0;

		EndpointDescription endpoint = channel.getEndpointDescription();
		Client client = this;
		if (endpoint == null || channel == null)
			throw new IllegalArgumentException("null arg");

		Session session = new Session();
		if (sessionName == null)
			sessionName = UUID.randomUUID() + "-"
					+ String.format("%08X", System.identityHashCode(session));

		session.endpoint = endpoint;
		session.name = sessionName;
		final KeyPair cert = client.application.getApplicationInstanceCertificate();
		if (cert != null && channel.getEndpointDescription().needsCertificate()) {
			session.clientCertificate = cert.getCertificate();
			session.clientPrivateKey = cert.getPrivateKey();
		}
		session.clientNonce = CryptoUtil.createNonce(32);

		// 1. Create Session
		CreateSessionRequest req = new CreateSessionRequest();
		req.setClientNonce(session.clientNonce);
		req.setClientDescription(client.createApplicationDescription());
		if (session.clientCertificate != null)
			req.setClientCertificate(session.getClientCertificate().getEncoded());
		req.setEndpointUrl(endpoint.getEndpointUrl());
		req.setMaxResponseMessageSize(maxResponseMessageSize);
		if (endpoint.getServer() != null)
			req.setServerUri(endpoint.getServer().getApplicationUri());
		req.setSessionName(session.name);
		req.setRequestedSessionTimeout(requestedSessionTimeout/* 1 hour */);

		RequestHeader requestHeader = new RequestHeader();
		requestHeader.setTimestamp(DateTime.currentTime());
		req.setRequestHeader(requestHeader);
		
		CreateSessionResponse res = (CreateSessionResponse) channel.serviceRequest(req);

		byte[] serverCert = res.getServerCertificate();
		session.serverCertificate = serverCert == null || serverCert.length == 0 ? null
				: new Cert(serverCert);
		session.serverNonce = res.getServerNonce();
		session.sessionId = res.getSessionId();
		session.authenticationToken = res.getAuthenticationToken();
		session.sessionTimeout = res.getRevisedSessionTimeout();
		session.maxRequestMessageSize = res.getMaxRequestMessageSize();
		session.serverSoftwareCertificates = res
				.getServerSoftwareCertificates();

		logger.debug("MessageSecurityMode: {}", channel.getMessageSecurityMode());
		if (!MessageSecurityMode.None.equals(channel.getMessageSecurityMode())) {
			// Verify Server Signature
			SignatureData serverSignature = res.getServerSignature();
			// Client Cert + Client nonce
			byte[] dataServerSigned = ByteBufferUtils.concatenate(req
					.getClientCertificate(), session.clientNonce);
				
				String signatureAlgorithm = serverSignature == null ? null : serverSignature.getAlgorithm();
				logger.debug("Algorithm: {}", signatureAlgorithm);
				SecurityAlgorithm algorithm = SecurityAlgorithm.valueOfUri(signatureAlgorithm);

				// This proofs that the server has the private key of its
				// certificate
				boolean ok = CryptoUtil.getCryptoProvider().verifyAsymm(session.serverCertificate.getCertificate().getPublicKey(), algorithm, dataServerSigned, serverSignature.getSignature());
				if (!ok)
					throw new ServiceResultException(
							Bad_ApplicationSignatureInvalid,
							"The signature generated with the server certificate is missing or invalid.");
			
		}
		
		// Verify that server has the endpoint given in the arguments
		EndpointDescription[] endpoints = res.getServerEndpoints();
		
		// this can be null, avoiding NPE (CTT)
		// also no point in continuing with 0 length because there is no endpoints?
		if(endpoints == null || endpoints.length == 0){
			throw new ServiceResultException(StatusCodes.Bad_UnexpectedError,
					"The CreateSessionResponse's endpoint list received from the server is empty");
		}
		
		/*
		 * Check that the Endpoint list received from the response match the ones given as parameter, 
		 * i.e. Endpoint list from the GetEndpoint/Discovery service. Based on spec part 4: 5.6.2.1, 
		 * the client shall verify that EndpointDescriptions with matching transportProfileUri match.
		 * 
		 * Based on spec part 4: 5.6.2.2. (Table 11) we need to verify the following parameters:
		 * endpointUrl, securityMode, securityPolicyUri, userIdentityTokens, transportProfileUri 
		 * and securityLevel. In case the endpoints differ, the client shall close the Session 
		 * and report error.
		 */
		String uri = channel.getEndpointDescription().getTransportProfileUri();
		validateEndpoints(uri, endpoints, discoveredEndpoints);
		
		EndpointDescription[] filteredEndpoints = select(endpoints, endpoint
				.getEndpointUrl(), null, endpoint.getSecurityMode(),
				SecurityPolicy.getSecurityPolicy(endpoint
						.getSecurityPolicyUri()), endpoint
						.getServerCertificate());
		// If discoveredEndpoints is not defined, retry without the endpointUrl check 
		if (filteredEndpoints.length == 0 && discoveredEndpoints == null)
			filteredEndpoints = select(endpoints, null, null,
					endpoint.getSecurityMode(),
					SecurityPolicy.getSecurityPolicy(endpoint
							.getSecurityPolicyUri()),
					endpoint.getServerCertificate());
		if (filteredEndpoints.length == 0) {
			logger.error(
					"Requested endpoint is not found on the server: Endpoint={}",
					endpoint);
			logger.debug("endpoints={}", Arrays.asList(endpoints));
			throw new ServiceResultException(
					StatusCodes.Bad_SecurityModeRejected,
					"Requested endpoint is not found on the server");
		}
		if (filteredEndpoints.length == 1)
			session.endpoint = filteredEndpoints[0];

		return session;
	}



	/**
	 * Creates a secure channel and an unactivated session channel.
	 * <p>
	 * The channel needs to be activated separately.
	 * <p>
	 * To close the object, both secure channel and the session must be close
	 * separately. SessionChannel.closeSession()
	 * SessionChannel.closeSecureChannel()
	 * 
	 * @param endpointUrl endpoint identifier and socket address
	 * @return
	 * @throws ServiceResultException
	 */
	public SessionChannel createSessionChannel(String endpointUrl) throws ServiceResultException 
	{
		return createSessionChannel(endpointUrl, endpointUrl);
	}
	

	/**
	 * Creates a secure channel and an unactivated session channel.
	 * <p>
	 * The channel needs to be activated separately.
	 * <p>
	 * To close the object, both secure channel and the session must be close
	 * separately. SessionChannel.closeSession()
	 * SessionChannel.closeSecureChannel()
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpointUri endpoint identifier
	 * @return
	 * @throws ServiceResultException
	 */
	public SessionChannel createSessionChannel(String connectUrl, String endpointUri) throws ServiceResultException 
	{
		// Discover server's endpoints, and choose one
		EndpointDescription[] endpoints = discoverEndpoints(connectUrl, connectUrl);		
		// Suitable endpoint
		EndpointDescription endpoint = select(endpoints, endpointUri);
		// Crete service channel
		SecureChannel channel = createSecureChannel(connectUrl, endpoint);
		try {
			// Create session
			Session session = createSession(channel);
			// Create session channel
			SessionChannel sessionChannel = session.createSessionChannel(channel, this);
			return sessionChannel;
		} catch (ServiceResultException se) {
			channel.closeAsync();
			throw se;
		}
	}	
	
	/**
	 * Creates a secure channel and an unactivated session channel.
	 * <p>
	 * The channel needs to be activated separately.
	 * <p>
	 * To close the object, both secure channel and the session must be close
	 * separately. SessionChannel.closeSession()
	 * SessionChannel.closeSecureChannel()
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpoint endpoint description
	 * @return session channel
	 * @throws ServiceResultException
	 *             on errors
	 */
	public SessionChannel createSessionChannel(EndpointDescription endpoint)
			throws ServiceResultException {
		return createSessionChannel(endpoint.getEndpointUrl(), endpoint);
	}	
	/**
	 * Creates a secure channel and an unactivated session channel.
	 * <p>
	 * The channel needs to be activated separately.
	 * <p>
	 * To close the object, both secure channel and the session must be close
	 * separately. SessionChannel.closeSession()
	 * SessionChannel.closeSecureChannel()
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpoint endpoint description
	 * @return session channel
	 * @throws ServiceResultException
	 *             on errors
	 */
	public SessionChannel createSessionChannel(String connectUrl, EndpointDescription endpoint) throws ServiceResultException 
	{
		// Create service channel
		SecureChannel channel = createSecureChannel(connectUrl, endpoint);
		try {
			// Create session
			Session session = createSession(channel);
			// Create session channel
			SessionChannel sessionChannel = session.createSessionChannel(channel, this);
			return sessionChannel;
		} catch (ServiceResultException se) {
			channel.closeAsync();
			throw se;
		}
	}

	/**
	 * Creates a secure channel and an unactivated session channel.
	 * <p>
	 * The channel needs to be activated separately.
	 * <p>
	 * To close the object, both secure channel and the session must be close
	 * separately. SessionChannel.closeSession()
	 * SessionChannel.closeSecureChannel()
	 * 
	 * @param applicationDescription application description
	 * @return session channel
	 * @throws ServiceResultException
	 *             on errors
	 */
	public SessionChannel createSessionChannel(ApplicationDescription applicationDescription) throws ServiceResultException 
	{
		// Create service channel
		SecureChannel channel = createSecureChannel( applicationDescription );
		try {
			// Create session
			Session session = createSession(channel);
			// Create session channel
			SessionChannel sessionChannel = session.createSessionChannel(channel, this);
			return sessionChannel;
		} catch (ServiceResultException se) {
			channel.closeAsync();
			throw se;
		}
	}

	/**
	 * Create a secure channel to a UA Server This method first queries
	 * endpoints, chooses the most suitable and connects to it.
	 * 
	 * @param endpointUrl Endpoint identifier and socket address
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public SecureChannel createSecureChannel(String endpointUrl) throws ServiceResultException {
		return createSecureChannel(endpointUrl, endpointUrl);
	}
	
	/**
	 * Create a secure channel to a UA Server This method first queries
	 * endpoints, chooses the most suitable and connects to it.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpointUri EndpointUri identifier or "" for discovery
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public SecureChannel createSecureChannel(String connectUrl, String endpointUri) throws ServiceResultException 
	{
		// Discover server's endpoints, and choose one
		EndpointDescription[] endpoints = discoverEndpoints(connectUrl, "");
		
		EndpointDescription endpoint = select( endpoints, connectUrl );
		
		// Connect to the endpoint
		return createSecureChannel(connectUrl, endpoint);
	}

	/**
	 * Create a secure channel to a UA application
	 * <p>
	 * This implementation accepts only connections with opc.tcp protocol and
	 * with encryption.
	 * <p>
	 * Note this implementation is unsafe as the dialog with discover endpoint
	 * is not encrypted.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param applicationDescription
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public SecureChannel createSecureChannel(ApplicationDescription applicationDescription) throws ServiceResultException 
	{
		
		String urls[] = applicationDescription.getDiscoveryUrls();
		
		if (urls == null || urls.length == 0) throw new ServiceResultException("application description does not contain any discovery url");
		
		// Try opc.tcp
		for (String url : urls) if (url.toLowerCase().startsWith("opc.tcp")) {
			SecureChannel result = createSecureChannel(url, url);
			return result;
		}

		// Try https
		for (String url : urls) if (url.toLowerCase().startsWith("https")) {
			SecureChannel result = createSecureChannel(url, url);
			return result;
		}
		
		throw new ServiceResultException("No suitable discover url was found");
	}

	/**
	 * Create a secure channel to an endpoint
	 * 
	 * @param endpoint endpoint description
	 * @return an open service channel
	 */
	public SecureChannel createSecureChannel(EndpointDescription endpoint) throws ServiceResultException 
	{
		return createSecureChannel(endpoint.getEndpointUrl(), endpoint);
	}
	
	/**
	 * Create a secure channel to an endpoint.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpoint endpoint description
	 * @return an open service channel
	 */
	public SecureChannel createSecureChannel(String connectUrl, EndpointDescription endpoint) throws ServiceResultException 
	{

		TransportChannelSettings settings = new TransportChannelSettings();
		settings.setDescription(endpoint);

		return createSecureChannel(connectUrl, settings);
		
//		SecureChannel sc = createSecureChannelImpl( endpoint.getEndpointUrl() );
//		
//		EndpointConfiguration ec = EndpointConfiguration.defaults();
//		ec.setOperationTimeout( timeout );
//		KeyPair localApplicationInstanceCertificate = application.getApplicationInstanceCertificate();
//		
//		TransportChannelSettings settings = new TransportChannelSettings();
//		settings.setDescription(endpoint);
//		settings.setConfiguration(ec);
//		settings.getOpctcpSettings().readFrom(application.getOpctcpSettings());
//		settings.getHttpsSettings().readFrom(application.getHttpsSettings());
//								
//		if (localApplicationInstanceCertificate!=null && endpoint.needsCertificate()) {
//			settings.getOpctcpSettings().setPrivKey( localApplicationInstanceCertificate.getPrivateKey() );
//			settings.getOpctcpSettings().setClientCertificate( localApplicationInstanceCertificate.getCertificate() );
//		}			
//		
//		try {
//			sc.initialize(connectUrl, settings);
//			sc.open();
//			return sc;
//		} catch (ServiceResultException e) {
//			sc.dispose();
//			throw e;
//		}

	}

	/**
	 * Create and open a secure channel.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpointUri
	 * @param mode
	 * @param remoteCertificate
	 * @return an open secure channel
	 * @throws ServiceResultException 
	 */
	public SecureChannel createSecureChannel(String connectUrl, String endpointUri, SecurityMode mode, Cert remoteCertificate) 
			throws ServiceResultException 
	{
		EndpointDescription ed = new EndpointDescription();
		ed.setEndpointUrl(endpointUri);
		ed.setSecurityMode( mode.getMessageSecurityMode() );
		ed.setSecurityPolicyUri( mode.getSecurityPolicy().getPolicyUri() );			

		Cert _remoteCertificate = mode.getMessageSecurityMode() == MessageSecurityMode.None ? null : remoteCertificate;
		
		if (_remoteCertificate != null)
			ed.setServerCertificate( _remoteCertificate.getEncoded() );

		return createSecureChannel(connectUrl, ed);
	}
	
	/**
	 * Create and open a secure channel.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param settings (optional) overriding settings
	 * @param mode
	 * @param remoteCertificate
	 * @return an open secure channel
	 * @throws ServiceResultException 
	 */
	public SecureChannel createSecureChannel(String connectUrl, TransportChannelSettings settings) throws ServiceResultException 
	{

		MessageFormat proto = UriUtil.getMessageFormat( connectUrl );		
		if ( proto == MessageFormat.Binary ) {			

			SecureChannel sc = createSecureChannelImpl( connectUrl );
						
			TransportChannelSettings s = new TransportChannelSettings();
			s.setConfiguration( endpointConfiguration );
			s.getHttpsSettings().readFrom(application.getHttpsSettings());
			s.getOpctcpSettings().readFrom(application.getOpctcpSettings());
			if ( settings != null ) s.readFrom(settings);
			
			KeyPair localApplicationInstanceCertificate = application.getApplicationInstanceCertificate();
			if (localApplicationInstanceCertificate!=null && s.getDescription().needsCertificate()) {
				s.getOpctcpSettings().setPrivKey( localApplicationInstanceCertificate.getPrivateKey() );
				s.getOpctcpSettings().setClientCertificate( localApplicationInstanceCertificate.getCertificate() );
			}			
			
			try {
				sc.initialize(connectUrl, s, getEncoderContext());
				sc.open();
				return sc;
			} catch (ServiceResultException e) {
				sc.dispose();
				throw e;
			}

		}
		throw new ServiceResultException("Unsupported protocol " + proto);
	}	

	/**
	 * Create and open a secure channel.
	 * 
	 * @param settings
	 * @param mode
	 * @param remoteCertificate
	 * @return an open secure channel
	 * @throws ServiceResultException 
	 */
	public SecureChannel createSecureChannel(TransportChannelSettings settings) throws ServiceResultException 
	{
		return createSecureChannel(settings.getDescription().getEndpointUrl(), settings);
	}	

	/**
	 * Create a service channel to a UA Server This method first queries
	 * endpoints, chooses the most suitable and connects to it.
	 * 
	 * @param endpointUrl endpoint idenfier and socket address
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public ServiceChannel createServiceChannel(String endpointUrl) throws ServiceResultException 
	{
		return new ServiceChannel( createSecureChannel( endpointUrl, endpointUrl ) );
	}
	
	/**
	 * Create a service channel to a UA Server This method first queries
	 * endpoints, chooses the most suitable and connects to it.
	 * 
	 * @param endpointUri endpointUri idenfier or "" for discovery service
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public ServiceChannel createServiceChannel(String connectUrl, String endpointUri) throws ServiceResultException 
	{
		return new ServiceChannel( createSecureChannel( connectUrl, endpointUri ) );
	}
	
	/**
	 * Create a service channel
	 * <p>
	 * Note this implementation is unsecure as the dialog with discover endpoint
	 * is not encrypted.
	 * 
	 * @param applicationDescription
	 * @return service channel
	 * @throws ServiceResultException
	 */
	public ServiceChannel createServiceChannel(ApplicationDescription applicationDescription) throws ServiceResultException 
	{
		return new ServiceChannel( createSecureChannel( applicationDescription ) );
	}

	/**
	 * Create a service channel to an endpoint. 
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpoint endpoint description
	 * @return an open service channel
	 */
	public ServiceChannel createServiceChannel(EndpointDescription endpoint) throws ServiceResultException {
		return new ServiceChannel( createSecureChannel( endpoint.getEndpointUrl(), endpoint ) );
	}
	
	/**
	 * Create a service channel to an endpoint
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpoint endpoint description
	 * @return an open service channel
	 */
	public ServiceChannel createServiceChannel(String connectUrl, EndpointDescription endpoint)
			throws ServiceResultException {
		return new ServiceChannel( createSecureChannel( connectUrl, endpoint ) );
	}

	/**
	 * Create and open a service channel.
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param endpointUri endpointUri identifier of the endpoint
	 * @param mode
	 * @param remoteCertificate
	 * @return an open secure channel
	 * @throws ServiceResultException 
	 */
	public ServiceChannel createServiceChannel(String connectUrl, String endpointUri, SecurityMode mode, org.opcfoundation.ua.transport.security.Cert remoteCertificate) throws ServiceResultException {
		return new ServiceChannel( createSecureChannel( connectUrl, endpointUri, mode, remoteCertificate ) );
	}
	
	/**
	 * Create and open a secure channel and adapt to service channel.
	 * 
	 * @param settings
	 * @param mode
	 * @param remoteCertificate
	 * @return an open service channel
	 * @throws ServiceResultException 
	 */
	public ServiceChannel createServiceChannel(TransportChannelSettings settings) 
	throws ServiceResultException {
		return new ServiceChannel( createSecureChannel(settings) );
	}	

	/**
	 * Discover endpoints
	 * 
	 * @param discoveryEndpointUrl endpoint identifier or socket address 
	 * @return Endpoint Descriptions
	 * @throws ServiceResultException Error that occured while processing the operation.
	 */
	public EndpointDescription[] discoverEndpoints(String connectUrl) throws ServiceResultException
	{
		return discoverEndpoints(connectUrl,  "");
	}
	
	/**
	 * Discover endpoints
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param discoveryEndpointUri endpointUri the endpoint identifier, or "" for discovery endpoint 
	 * @return Endpoint Descriptions
	 * @throws ServiceResultException Error that occured while processing the operation.
	 */
	public EndpointDescription[] discoverEndpoints(String connectUrl, String discoveryEndpointUri)
			throws ServiceResultException {
		// Must not use encryption!
		SecureChannel channel = createSecureChannel( connectUrl, discoveryEndpointUri, SecurityMode.NONE, null );
		ChannelService chan = new ChannelService(channel);
		try {
			GetEndpointsRequest req = new GetEndpointsRequest(null, discoveryEndpointUri, new String[0], new String[0]);
			req.setRequestHeader( new RequestHeader() );
			req.getRequestHeader().setTimeoutHint( UnsignedInteger.valueOf( getTimeout() ) );
			GetEndpointsResponse res = chan.GetEndpoints(req);
			
			EndpointDescription[] result = res.getEndpoints();
			return result;
		} finally {
			channel.close();
			channel.dispose();
		}
	}

	/**
	 * Discover applications
	 * 
	 * @param discoverServerEndpointUrl endpointURI that is also the socket address
	 * @return Endpoint Application Descriptions
	 * @throws ServiceResultException
	 */
	public ApplicationDescription[] discoverApplications(String discoverServerEndpointUrl) throws ServiceResultException {
		return discoverApplications(discoverServerEndpointUrl, discoverServerEndpointUrl);
	}
	
	/**
	 * Discover applications
	 * 
	 * @param connectUrl address that contains the socket address to the endpoint
	 * @param discoverServerEndpointUri endpointURI or ""
	 * @return Endpoint Application Descriptions
	 * @throws ServiceResultException
	 */
	public ApplicationDescription[] discoverApplications(String connectUrl, String discoverServerEndpointUri) throws ServiceResultException {
		// Must not use encryption!
		SecurityMode mode = SecurityMode.NONE;
		
		SecureChannel channel = createSecureChannel(connectUrl, discoverServerEndpointUri, mode, null);
		ChannelService chan = new ChannelService(channel);
		try {
			FindServersRequest req = new FindServersRequest(null,
					discoverServerEndpointUri, new String[0],
					new String[0]);
			req.setRequestHeader( new RequestHeader() );
			req.getRequestHeader().setTimeoutHint( UnsignedInteger.valueOf( getTimeout() ) );
			FindServersResponse res = chan.FindServers(req);
			return res.getServers();
		} finally {
			channel.close();
			channel.dispose();
		}
	}

	public EncoderContext getEncoderContext() {
		return application.getEncoderContext();
	}

	SecureChannel createSecureChannelImpl(String uri) throws ServiceResultException {
		String proto = UriUtil.getTransportProtocol( uri );
		
		if ( proto.equals( UriUtil.SCHEME_OPCTCP )) {						
			SecureChannelTcp channel = new SecureChannelTcp();
			return channel;
		} else
		if ( proto.equals( UriUtil.SCHEME_HTTPS )) {
			HttpsClient client = new HttpsClient( proto );
			// ?? Should this be more strict ??
			HttpsClientSecureChannel secureChannel = new HttpsClientSecureChannel( client );
			return secureChannel;
		} else
		if ( proto.equals( UriUtil.SCHEME_HTTP )) {
			HttpsClient client = new HttpsClient( proto );
			HttpsClientSecureChannel secureChannel = new HttpsClientSecureChannel( client );
			return secureChannel;
		} else throw new ServiceResultException("Unsupported protocol: "+proto);		
	}

	public void setTimeout(int timeout) {
		endpointConfiguration.setOperationTimeout( timeout );		
	}

	public int getTimeout() {
		return endpointConfiguration.getOperationTimeout();
	}
	
	public void setMaxMessageSize(int maxMessagSize) {
		endpointConfiguration.setMaxMessageSize( maxMessagSize );
	}
	
	public int getMaxMessageSize() {
		return endpointConfiguration.getMaxMessageSize();
	}
	
	public EndpointConfiguration getEndpointConfiguration() {
		return endpointConfiguration;
	}
	
	public void setEndpointConfiguration( EndpointConfiguration endpointConfiguration ) {
		this.endpointConfiguration = endpointConfiguration; 
	}
	
	private void validateEndpoints(String uri, EndpointDescription[] endpoints,
			EndpointDescription[] discoveredEndpoints) throws ServiceResultException {
		// if discoveredEndpoints parameter is null, skip the test
		if(discoveredEndpoints == null){
			return;
		}
		
		/*
		  * We need to verify the following parameters:
		  * endpointUrl, securityMode, securityPolicyUri, userIdentityTokens, transportProfileUri 
		  * and securityLevel. Since .equals does compare all these, lets use it. It also 
		  * compares server ApplicationDescription and Certificate, so we clone and set those to null
		  */		
		List<EndpointDescription> dedl = new ArrayList<EndpointDescription>();
		for(EndpointDescription e : discoveredEndpoints){
			EndpointDescription ne = e.clone();
			ne.setServer(null);
			ne.setServerCertificate(null);
			dedl.add(ne);
		}		
		List<EndpointDescription> edl = new ArrayList<EndpointDescription>();
		for(EndpointDescription e : endpoints){
			EndpointDescription ne = e.clone();
			ne.setServer(null);
			ne.setServerCertificate(null);
			edl.add(ne);
		}
		
		//each discovered Endpoint must appear in the endpoint list
		for(EndpointDescription e : dedl){
			if(!edl.contains(e)){
				logger.error("The endpoint received from GetEndpoints is not in the endpoints of CreateSessionResponse. Endpoint={}",e);
				logger.debug("GetEndpoints returned endpoints={}", dedl);
				logger.debug("CreateSessionResponse endpoints={}", edl);
				
				throw new ServiceResultException(StatusCodes.Bad_UnexpectedError,
						"The endpoint received from GetEndpoints is not in the endpoints of CreateSessionResponse");
			}
		}

		
	}
	
}
