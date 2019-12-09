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

import com.prosysopc.ua.builtintypes.ByteString;
import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.builtintypes.UnsignedByte;
import com.prosysopc.ua.core.ApplicationDescription;
import com.prosysopc.ua.core.AttributeServiceSetHandler;
import com.prosysopc.ua.core.DiscoveryServiceSetHandler;
import com.prosysopc.ua.core.MethodServiceSetHandler;
import com.prosysopc.ua.core.MonitoredItemServiceSetHandler;
import com.prosysopc.ua.core.NodeManagementServiceSetHandler;
import com.prosysopc.ua.core.ServiceFault;
import com.prosysopc.ua.core.SessionServiceSetHandler;
import com.prosysopc.ua.core.SubscriptionServiceSetHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.core.ApplicationType;
import com.prosysopc.ua.core.EndpointDescription;
import com.prosysopc.ua.core.MessageSecurityMode;
import com.prosysopc.ua.core.StatusCodes;
import com.prosysopc.ua.core.UserTokenPolicy;
import com.prosysopc.ua.encoding.EncoderContext;
import com.prosysopc.ua.encoding.IEncodeable;
import com.prosysopc.ua.transport.Endpoint;
import com.prosysopc.ua.transport.EndpointBinding;
import com.prosysopc.ua.transport.EndpointServer;
import com.prosysopc.ua.transport.EndpointServer.EndpointHandle;
import com.prosysopc.ua.transport.UriUtil;
import com.prosysopc.ua.transport.endpoint.EndpointBindingCollection;
import com.prosysopc.ua.transport.https.HttpsServer;
import com.prosysopc.ua.transport.https.HttpsServer.HttpsEndpointHandle;
import com.prosysopc.ua.transport.security.CertificateValidator;
import com.prosysopc.ua.transport.security.KeyPair;
import com.prosysopc.ua.transport.security.SecurityMode;
import com.prosysopc.ua.transport.security.SecurityPolicy;
import com.prosysopc.ua.transport.tcp.nio.OpcTcpServer;
import com.prosysopc.ua.transport.tcp.nio.OpcTcpServer.OpcTcpEndpointHandle;
import com.prosysopc.ua.transport.tcp.nio.OpcTcpServer.SocketHandle;
import com.prosysopc.ua.utils.EndpointUtil;

/**
 * This object represents a service server. It is an application that responds
 * to {@link ServiceRequest}s queries.
 * <p>
 * Server is assigned with at least one application instance certificate.
 * <p>
 * The initial server contains {@link EndpointDiscoveryService} by default.
 *
 * @see Application
 * @see ServiceHandler service handler
 */
public class Server {

	/** Constant <code>SOAP_XML_TRANSPORT_PROFILE_URI="http://opcfoundation.org/UA-Profile/Tra"{trunked}</code> */
	public final static String SOAP_XML_TRANSPORT_PROFILE_URI = "http://opcfoundation.org/UA-Profile/Transport/soaphttp-wssc-uaxml-uabinary";
    /** Constant <code>UATCP_BINARY_TRANSPORT_PROFILE_URI="http://opcfoundation.org/UA-Profile/Tra"{trunked}</code> */
    public final static String UATCP_BINARY_TRANSPORT_PROFILE_URI = "http://opcfoundation.org/UA-Profile/Transport/uatcp-uasc-uabinary";
    /** Constant <code>HTTPS_BINARY_TRANSPORT_PROFILE_URI="http://opcfoundation.org/UA-Profile/Tra"{trunked}</code> */
    public final static String HTTPS_BINARY_TRANSPORT_PROFILE_URI = "http://opcfoundation.org/UA-Profile/Transport/https-uabinary";

	/** Logger */
	static Logger logger = LoggerFactory.getLogger(Server.class);
	
	/** Service Handler */
	protected ServiceHandlerComposition serviceHandlers = new ServiceHandlerComposition();
	/** User Token Policies */
	protected List<UserTokenPolicy> userTokenPolicies = new CopyOnWriteArrayList<UserTokenPolicy>();
	/** Endpoints */
	protected EndpointBindingCollection endpointBindings;
	/** Endpoint discovery service */
	protected EndpointDiscoveryService endpointDiscoveryService;
	/** The application */
	protected Application application;
	/** Bound handles */
	protected List<EndpointHandle> boundHandles = new CopyOnWriteArrayList<EndpointHandle>();

	/**
	 * <p>createServerApplication.</p>
	 *
	 * @return a {@link Server} object.
	 */
	public static Server createServerApplication() {
		Application application = new Application();
		Server server = new Server(application);
		application.getOpctcpSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		application.getHttpsSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		//application.getHttpsSettings().setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
		return server;
	}
	
	/**
	 * <p>Constructor for Server.</p>
	 *
	 * @param application a {@link Application} object.
	 */
	public Server(Application application) {
		this.application = application;
		endpointBindings = new EndpointBindingCollection();
		endpointDiscoveryService = new EndpointDiscoveryService( endpointBindings );
		addServiceHandler( endpointDiscoveryService );		
	}
	
	/**
	 * <p>Getter for the field <code>application</code>.</p>
	 *
	 * @return a {@link Application} object.
	 */
	public Application getApplication() {
		return application;
	}
	
	/**
	 * <p>createApplicationDescription.</p>
	 *
	 * @return a {@link ApplicationDescription} object.
	 */
	public ApplicationDescription createApplicationDescription() {
		ApplicationDescription result = application.applicationDescription.clone();
		result.setApplicationType( ApplicationType.Server );
		return result;
	}

	/**
	 * <p>Setter for the field <code>endpointBindings</code>.</p>
	 *
	 * @param newBindings a {@link EndpointBindingCollection} object.
	 */
	public synchronized void setEndpointBindings( EndpointBindingCollection newBindings ) {
		this.endpointBindings = newBindings;
		endpointDiscoveryService.endpointBindings = newBindings;
	}
	
	/**
	 * <p>Getter for the field <code>endpointBindings</code>.</p>
	 *
	 * @return a {@link EndpointBindingCollection} object.
	 */
	public EndpointBindingCollection getEndpointBindings() {
		return endpointBindings;
	}

	/**
	 * <p>getEncoderContext.</p>
	 *
	 * @return a {@link EncoderContext} object.
	 */
	public EncoderContext getEncoderContext() {
		return application.getEncoderContext();
	}
	
	/**
	 * Add Service Handler. Service handler handles one or more service methods.
	 * Note, the server may not have more than one service handler for each method.
	 * <p>
	 * The <tt>serviceHandler</tt> is either:
	 *  (a) an implementation of {@link ServiceHandler}
	 *  (b) an object that contains methods that implement service requests.
	 *      These methods are discovered using Java Reflection.
	 * <p>
	 * The following list contains service methods grouped by service sets:
	 *
	 * @see AttributeServiceSetHandler
	 * @see DiscoveryServiceSetHandler
	 * @see MethodServiceSetHandler
	 * @see MonitoredItemServiceSetHandler
	 * @see NodeManagementServiceSetHandler
	 * @see SessionServiceSetHandler
	 * @see SubscriptionServiceSetHandler
	 * @see org.opcfoundation.ua.core.TestServiceSetHandler
	 * <P>
	 * The <tt>serviceHandler</tt> may implement one or more methods.
	 * In typical case service handler implements one service set, e.g.
	 * {@link SessionServiceSetHandler}.
	 * <p>
	 * A {@link ServiceFault} is returned to the client in case the server doesn't
	 * the requested service method.
	 * <p>
	 * Example:
	 *   addServiceHandler( new TestServiceSetHandler() {
	 *      void onTestStack(EndpointServiceRequest&lt;TestStackRequest, TestStackResponse&gt; req) {
	 *         req.sendResponse( new ServiceFault() );
	 *      }
	 *      void onTestStackEx(EndpointServiceRequest&lt;TestStackExRequest, TestStackExResponse&gt; req) {
	 *         req.sendFault(new ServiceFault());
	 *      }
	 *   } );
	 * @param serviceHandler instanceof {@link ServiceHandler} or Object implementing service requests
	 */
	public void addServiceHandler(Object serviceHandler) 
	{
		if (serviceHandler==null) throw new IllegalArgumentException("null arg");
		logger.debug("addServiceHandler: {}", serviceHandler);
		if (logger.isTraceEnabled())
			logger.trace("addServiceHandler: from {}", Thread.currentThread().getStackTrace()[2]);
		serviceHandlers.add(serviceHandler);
	}
	
	/**
	 * <p>Getter for the field <code>serviceHandlers</code>.</p>
	 *
	 * @return an array of {@link ServiceHandler} objects.
	 */
	public ServiceHandler[] getServiceHandlers() {
		return serviceHandlers.getServiceHandlers();
	}
	
	/**
	 * Get Service Handler object by service.
	 * <p>
	 * For example, to acquire session manager:
	 *    SessionManager sessionManager = x.getServiceHandlerByService( CreateSessionRequest.class );
	 *
	 * @param <T> service handler type
	 * @param requestClass Service request class
	 * @return Service handler that serves the given request class in this server
	 */
	@SuppressWarnings("unchecked")
	public <T> T getServiceHandlerByService(Class<? extends ServiceRequest> requestClass)
	{
		return (T) serviceHandlers.getServiceHandlerByService(requestClass);
	}	
	
	/**
	 * Query whether the server can handle a service.
	 *
	 * @param requestClass request class of the service, e.g. ReadRequest
	 * @return true if server can handle the service
	 */
	public boolean handlesService(Class<? extends IEncodeable> requestClass)
	{		
		return serviceHandlers.supportsService(requestClass);
	}
	
	/**
	 * <p>getServiceHandlerComposition.</p>
	 *
	 * @return a {@link ServiceHandlerComposition} object.
	 */
	public ServiceHandlerComposition getServiceHandlerComposition() {
		return serviceHandlers;
	}
	
	/**
	 * <p>addUserTokenPolicy.</p>
	 *
	 * @param policy a {@link UserTokenPolicy} object.
	 */
	public void addUserTokenPolicy(UserTokenPolicy policy) {
		this.userTokenPolicies.add(policy);
	}
	
	/**
	 * <p>removeUserTokenPolicy.</p>
	 *
	 * @param policy a {@link UserTokenPolicy} object.
	 */
	public void removeUserTokenPolicy(UserTokenPolicy policy) {
		this.userTokenPolicies.remove( policy );
	}
	
	/**
	 * <p>Getter for the field <code>userTokenPolicies</code>.</p>
	 *
	 * @return an array of {@link UserTokenPolicy} objects.
	 */
	public UserTokenPolicy[] getUserTokenPolicies() {
		return userTokenPolicies.toArray( new UserTokenPolicy[0] );
	}

	/**
	 * Bind an endpoint to the server.
	 *
	 * bindAddress is resolved into ip-addresses. For instance, if "localhost"
	 * is used, then all local interface ips are bound. If port is not explicitly
	 * specified, the default port for the protocol is used.
	 *
	 * There must be protocol: "opc.tcp", "http", or "https".
	 *
	 * EndpointUri is the identifier of the endpoint.
	 *
	 * @param bindAddress bind address for the endpoint
	 * @param endpointAddress endpoint to bind
	 * @return list of handles
	 * @throws ServiceResultException if error
	 */
	public List<EndpointHandle> bind(String bindAddress, Endpoint endpointAddress) throws ServiceResultException
	{
		List<EndpointHandle> result = new ArrayList<EndpointHandle>();
		String scheme = UriUtil.getTransportProtocol( bindAddress );
		List<SocketAddress> socketAddresses = EndpointUtil.toSocketAddresses( bindAddress );
		if ( socketAddresses.isEmpty() ) return Collections.emptyList();
		EndpointServer endpointServer = application.getOrCreateEndpointServer(scheme);
		
		for ( SocketAddress socketAddress : socketAddresses ) {
			EndpointBinding eb = new EndpointBinding(endpointServer, endpointAddress, this);
			EndpointHandle handle = endpointServer.bind(socketAddress, eb);
			boundHandles.add( handle );
		}
		
		return result;
	}
	
	/**
	 * Binds a "ReverseHello" pseudo-endpoint to the server. Server will connect the clients based on
	 * Part 6 ReverseHello connection Procedure. Note that this works only for opc.tcp at the moment.
	 * 
	 * @param clientEndpointUrl a client address to connect to (a client running a ReverseHello server and accepting connections)
	 * @param endpointUrlForClient the endpoint which client should connect to.
	 * @throws ServiceResultException if any error
	 */
	public void bindReverse(String clientEndpointUrl, String endpointUrlForClient) throws ServiceResultException{
		String proto = UriUtil.getTransportProtocol(clientEndpointUrl);
		if(!"opc.tcp".equals(proto)) {
			throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "ReverseHello is only supported for 'opc.tcp'");
		}
		URI uri;
		try {
			uri = new URI(clientEndpointUrl);
		} catch (URISyntaxException e) {
			throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, e, "Cannot resolve the given URI: "+clientEndpointUrl);
		}
		
		String host = uri.getHost();
		if(host == null) {
			throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "Cannot resolve host component of the given URI: "+clientEndpointUrl);
		}
		int port = uri.getPort();
		if(port < 0) {
			throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "Cannot resolve port component of the given URI: "+clientEndpointUrl);
		}
		
		EndpointServer endpointServer = application.getOrCreateEndpointServer("opc.tcp");
		endpointServer.bindReverse(new InetSocketAddress(host, port), endpointUrlForClient);
	}
	
	/**
	 * <p>bind.</p>
	 *
	 * @param bindAddress a {@link java.lang.String} object.
	 * @param endpointUri a {@link java.lang.String} object.
	 * @param modes a {@link SecurityMode} object.
	 * @return a {@link java.util.List} object.
	 * @throws ServiceResultException if any.
	 */
	public List<EndpointHandle> bind(String bindAddress, String endpointUri, SecurityMode...modes) throws ServiceResultException {
		Endpoint endpointAddress = new Endpoint(endpointUri, modes);
		return bind(bindAddress, endpointAddress);
	}
	
	public List<EndpointHandle> bind(String bindAddress, String endpointUri, Collection<SecurityMode> modes) throws ServiceResultException {
		Endpoint endpointAddress = new Endpoint(endpointUri, modes.toArray(SecurityMode.EMPTY_ARRAY));
		return bind(bindAddress, endpointAddress);
	}

	/**
	 * Close the server.
	 */
	public void close()
	{
		for (EndpointHandle handle : boundHandles) {
			logger.debug("unbind: {}", handle);
			handle.close();
		}
		boundHandles.clear();
		logger.info("Server {} closed", this);
		OpcTcpServer opctcpServer = getApplication().opctcpServer;
		HttpsServer httpsServer = getApplication().httpsServer;
		if ((opctcpServer == null || opctcpServer.getEndpointBindings().isEmpty()) && (httpsServer == null || httpsServer.getEndpointBindings().isEmpty()))
			getApplication().close();
	}
	
	/**
	 * <p>getEndpoints.</p>
	 *
	 * @return an array of {@link Endpoint} objects.
	 */
	public Endpoint[] getEndpoints() {
		List<EndpointBinding> bindings = endpointBindings.get(this);
		List<Endpoint> endpoints = EndpointBindingCollection.getEndpointAddresses(bindings);
		return endpoints.toArray(new Endpoint[endpoints.size()]);
	}
	
	/**
	 * <p>hasEndpoint.</p>
	 *
	 * @param uri a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean hasEndpoint(String uri) {
		List<EndpointBinding> bindings = endpointBindings.get(uri);
		return !bindings.isEmpty();
	}
	
	/**
	 * <p>getEndpointByUri.</p>
	 *
	 * @param uri a {@link java.lang.String} object.
	 * @return a {@link Endpoint} object.
	 */
	public Endpoint getEndpointByUri(String uri) {
		List<EndpointBinding> bindings = endpointBindings.get(uri);
		if ( bindings.isEmpty() ) return null;
		return bindings.get(0).endpointAddress;
	}
	
	/**
	 * <p>getEndpointDescriptions.</p>
	 *
	 * @return an array of {@link EndpointDescription} objects.
	 */
	public EndpointDescription[] getEndpointDescriptions() {
		return getEndpointDescriptions(null);
	}
	
	/**
	 * <p>getEndpointDescriptions.</p>
	 *
	 * @param requestAddress a {@link java.net.SocketAddress} object.
	 * @return an array of {@link EndpointDescription} objects.
	 */
	public EndpointDescription[] getEndpointDescriptions(SocketAddress requestAddress) {

		List<EndpointDescription> result = new ArrayList<EndpointDescription>( endpointBindings.size() );
		UserTokenPolicy[] userTokenPolicies = getUserTokenPolicies();

		ApplicationDescription ap = createApplicationDescription(); 
				
		/* Mantis issue 2495: In case the endpoints are bound to some addresses only, 
		 * it should be ensured that they are only returned for the endpoint requests coming from that interface.
		 * 
		 * */
		ArrayList<Endpoint> requestAddressEndpoints = new ArrayList<Endpoint>();
		if (requestAddress != null) {
			//TODO change to logging
			logger.trace("Server.getEndpointDescriptions: requestAddress=", requestAddress);
			for(EndpointServer es : endpointBindings.getEndpointServers()) {
				logger.trace("Server.getEndpointDescriptions: EndpointServer es=", es);

				if(es instanceof OpcTcpServer) {
					OpcTcpServer ots = (OpcTcpServer) es;
					SocketHandle[] sh = ots.socketHandleSnapshot();
					for(SocketHandle socketHandle : sh)  {

						if(requestAddress.equals(socketHandle.getSocketAddress())) {
							OpcTcpEndpointHandle[] endpointhandles = socketHandle.endpointHandleSnapshot();
							for(OpcTcpEndpointHandle eh : endpointhandles) {

								requestAddressEndpoints.add(eh.endpointBinding().endpointAddress);
							}
						}
					}
				}

				if(es instanceof HttpsServer) {
					HttpsServer hs = (HttpsServer) es;
					HttpsServer.SocketHandle[] sh = hs.socketHandleSnapshot();
					for(HttpsServer.SocketHandle socketHandle : sh)  {
						if(requestAddress.equals(socketHandle.getSocketAddress())) {
							HttpsEndpointHandle[] endpointhandles = socketHandle.endpointHandleSnapshot();
							for(HttpsEndpointHandle eh : endpointhandles) {
								requestAddressEndpoints.add(eh.endpointBinding().endpointAddress);
							}
						}
					}
				}
			}
		}

		for (Endpoint ep : endpointBindings.getEndpointAddresses())
		{
			if(requestAddress != null && !requestAddressEndpoints.contains(ep)) continue;
			
			String endpointUrl = ep.getEndpointUrl();
			String proto = UriUtil.getTransportProtocol( endpointUrl ).toLowerCase();
			
			for (KeyPair keypair : application.getApplicationInstanceCertificates())
			{
				SecurityMode[] securityModes = ep.getSecurityModes();

				logger.trace("getEndpointDescriptions: endpoint={}", ep);
				logger.trace("getEndpointDescriptions: keyPair={}", keypair);
				if (logger.isTraceEnabled())
					logger.trace("getEndpointDescriptions: securityModes={}", Arrays.toString(securityModes));
				
				for (SecurityMode conf : securityModes)
				{
					MessageSecurityMode msm = MessageSecurityMode.None;
					String securityPolicyUri = "";
					String transportProfileUri = UATCP_BINARY_TRANSPORT_PROFILE_URI;
					int securityLevel = 0;
					
					if ( UriUtil.SCHEME_HTTPS.equals(proto) ) {
						msm = conf.getMessageSecurityMode();
						securityLevel = msm == MessageSecurityMode.None ? 0 : msm == MessageSecurityMode.Sign ? 2 : -1;
						securityPolicyUri = conf.getSecurityPolicy().getPolicyUri();
						transportProfileUri = HTTPS_BINARY_TRANSPORT_PROFILE_URI;
					} else if ( UriUtil.SCHEME_HTTP.equals(proto) ) {
						securityLevel = 0;
						securityPolicyUri = SecurityPolicy.NONE.getPolicyUri();
						transportProfileUri = HTTPS_BINARY_TRANSPORT_PROFILE_URI;
					} else if ( UriUtil.SCHEME_OPCTCP.equals(proto) ) {
						msm = conf.getMessageSecurityMode();						
						securityLevel = msm == MessageSecurityMode.None ? 0 : msm == MessageSecurityMode.Sign ? 1 : msm == MessageSecurityMode.SignAndEncrypt ? 2 : -1;
						securityPolicyUri = conf.getSecurityPolicy().getPolicyUri();
					}					
					
					// Ensure keysize matches security policy
					if ( msm.hasEncryption() || msm.hasSigning() ) {
						if ( !conf.getSecurityPolicy().isUsableWith( keypair.certificate ) ) 
							continue;
					}
				
					EndpointDescription desc = new EndpointDescription();
					desc.setEndpointUrl( ep.getEndpointUrl() );
					desc.setSecurityMode( msm );
					desc.setSecurityLevel( UnsignedByte.valueOf(securityLevel) );
					desc.setSecurityPolicyUri( securityPolicyUri );
					desc.setServer( ap );
					desc.setServerCertificate(ByteString.valueOf(keypair.getCertificate().getEncoded() ));
					desc.setTransportProfileUri( transportProfileUri );
					desc.setUserIdentityTokens( userTokenPolicies );
					result.add(desc);
				}
			}
		}

		logger.trace("getEndpointDescriptions: result={}" + result);
		
		return result.toArray( new EndpointDescription[0] );
	}
	
	/**
	 * <p>getBindings.</p>
	 *
	 * @return an array of {@link EndpointServer} objects.
	 */
	public EndpointServer[] getBindings() {
		List<EndpointServer> result = new ArrayList<EndpointServer>();
		for ( EndpointBinding binding : endpointBindings.get(this) ) {
			if ( !result.contains( binding.endpointServer ) ) result.add( binding.endpointServer );
		}
		return result.toArray( new EndpointServer[result.size()] );
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Server "+application.getApplicationUri();
	}

}
