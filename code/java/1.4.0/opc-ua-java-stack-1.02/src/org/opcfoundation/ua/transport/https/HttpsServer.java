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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.NHttpConnectionBase;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerResolver;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.CloseableObject;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.Endpoint;
import org.opcfoundation.ua.transport.EndpointBinding;
import org.opcfoundation.ua.transport.EndpointServer;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.UriUtil;
import org.opcfoundation.ua.transport.endpoint.EndpointBindingCollection;
import org.opcfoundation.ua.transport.impl.ConnectionCollection;
import org.opcfoundation.ua.transport.security.CertValidatorTrustManager;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.HttpsSecurityPolicy;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.AbstractState;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.asyncsocket.AsyncServerSocket;

/**
 * Host for an https endpoint
 *
 */
public class HttpsServer extends AbstractState<CloseableObjectState, ServiceResultException> implements EndpointServer {
	
	/** Logger */
	static Logger log = LoggerFactory.getLogger(HttpsServer.class);
	
    public static final HttpParams DEFAULT_HTTPPARAMS = new SyncBasicHttpParams()
    	.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0)
    	.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024) // or 64?
    	.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "OpcUA/1.1")
    	.setParameter(CoreProtocolPNames.USER_AGENT, "OpcUA/1.1");

	public static TrustManager[] makeTrustManager(CertificateValidator...validators) {
		TrustManager[] result = new TrustManager[ validators.length ];
		for ( int i=0; i<result.length; i++ ) {
			result[i] = new CertValidatorTrustManager( validators[i] );
		}
		return result;
	}

	/** Application */
	Application application;
	/** Enabled Cipher Suites */
	String[] enabledCipherSuites;
	/** Last cipher suite patterns */
	String[] cipherSuitePatterns;
	/** Last selection of cipher suites */
	String[] cipherSuites;
	/** Secure channel counter */
	AtomicInteger secureChannelCounter = new AtomicInteger();
	/** Server Socket */
	AsyncServerSocket socket;
	/** Endpoint bindings */
	EndpointBindingCollection endpointBindings = new EndpointBindingCollection();
	/** Connection listeners */
	ConnectionCollection connections = new ConnectionCollection(this);		
	/** Reactor thread */
	Thread sslReactorThread, plainReactorThread;
	/** Thread semaphores */
	Semaphore sslThreadSemaphore, plainThreadSemaphore;
	/** Protocol handler */
	HttpAsyncService protocolHandler;
	/** Connection re-use strategy */ 
	ConnectionReuseStrategy connectionReuseStrategy;
    /** Request handler registry */
	RequestResolver registry;
    /** Connection factory, either Default or SSL */
    NHttpConnectionFactory<DefaultNHttpServerConnection> plainConnFactory, sslConnFactory;
    /** SSL Engine */
    SSLEngine sslEngine;
    /** New SSL Connect Setup Handler (Set's Cipher Suite) */
    SSLSetupHandler sslSetupHandler;
	/** IO Event Dispatch */
	IOEventDispatch plainIoEventDispatch, sslIoEventDispatch;
	/** IO Reactor */
	ListeningIOReactor ioReactor;	
	/** Worker count */
	IOReactorConfig ioConfig;
	/** Security Policies */
	HttpsSecurityPolicy[] securityPolicies;
	/** Endpoint handles */
	Map<SocketAddress, SocketHandle> socketHandles = new HashMap<SocketAddress, SocketHandle>();

	/** Service server used when client connects with "" url for endpoint discovery */
	Server discoveryServer;
	/** Discovery endpoint handler uri="" */
	HttpsServerEndpointHandler discoveryHandler;

	public HttpsServer(Application application) throws ServiceResultException {
		super(CloseableObjectState.Closed, CloseableObjectState.Closed);

		this.application = application;
		this.ioConfig = new IOReactorConfig();
		this.securityPolicies = application.getHttpsSettings().getHttpsSecurityPolicies();
		
		// Disable Nagle's
		ioConfig.setTcpNoDelay(false);
		
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                // Use standard server-side protocol interceptors
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
        });
        // Create request handler registry
        registry = new RequestResolver();
        // Register the default handler for all URIs
        final Map<NHttpServerConnection, HttpsServerConnection> connMap = Collections.synchronizedMap( new HashMap<NHttpServerConnection, HttpsServerConnection>() );
        // Create connection re-use strategy
        connectionReuseStrategy = new DefaultConnectionReuseStrategy();
	        
        // Create server-side HTTP protocol handler
        protocolHandler = new HttpAsyncService(httpproc, connectionReuseStrategy, registry, getHttpParams()) {
	        	
	        @Override
	        public void connected(final NHttpServerConnection conn) {
	          	NHttpConnectionBase conn2 = (NHttpConnectionBase) conn;
				log.info("connected: {} {}<-> {} context={} socketTimeout={}", 
						HttpsServer.this.getBoundSocketAddresses(),
						conn2.getLocalAddress(), 
						conn2.getRemoteAddress(), 
						conn2.getContext(),
						conn2.getSocketTimeout());
	           	HttpsServerConnection httpsConnection = new HttpsServerConnection(HttpsServer.this, conn);
	           	connMap.put(conn, httpsConnection);
	           	connections.addConnection( httpsConnection );
	            super.connected(conn);
	        }

	        @Override
	        public void closed(final NHttpServerConnection conn) {
	          	NHttpConnectionBase conn2 = (NHttpConnectionBase) conn;
				log.info("closed: {} {}<-> {} context={} socketTimeout={}", HttpsServer.this.getBoundSocketAddresses(), conn2.getLocalAddress(), conn2.getRemoteAddress(), conn2.getContext(), conn2.getSocketTimeout());
	            HttpsServerConnection conn3 = connMap.remove(conn);
	            connections.removeConnection( conn3 );
	            super.closed(conn);
	        }
	
	    };
	    
		// Create a service server for connections that query endpoints (url = "")
		discoveryServer = new Server( application );
		discoveryServer.setEndpointBindings( endpointBindings );		
		EndpointBinding discoveryBinding = new EndpointBinding( this, discoveryEndpoint, discoveryServer );
		discoveryHandler = new HttpsServerEndpointHandler( discoveryBinding );
	}
	
	Set<SecurityPolicy> calcSecurityPolicies() {
		Set<SecurityPolicy> result = new HashSet<SecurityPolicy>();
		for ( EndpointBinding ep : endpointBindings.getAll() ) {
			for ( SecurityMode mode : ep.endpointAddress.getSecurityModes() ) {
				result.add( mode.getSecurityPolicy() );
			}
		}
		return result;
	}
	
	String[] calcCipherSuitePatterns() throws ServiceResultException
	{
		Collection<HttpsSecurityPolicy> securityPolicies = getSupportedSecurityPolicies();
		// Create an array of cipher suites
		List<String> cipherSuitePatternList = new ArrayList<String>();
		for ( HttpsSecurityPolicy securityPolicy : securityPolicies ) {
//			if ( securityPolicy != HttpsSecurityPolicy.TLS_1_0 &&
//					securityPolicy != HttpsSecurityPolicy.TLS_1_1 &&
//					securityPolicy != HttpsSecurityPolicy.TLS_1_2 )
//					throw new ServiceResultException( StatusCodes.Bad_SecurityChecksFailed, "Https Server doesn't support "+securityPolicy );
			
			String[] cps = securityPolicy.getCipherSuites();
			if ( cps == null ) continue;
			for ( String cipherSuite : cps ) {
				if ( !cipherSuitePatternList.contains( cipherSuite ) ) {
					cipherSuitePatternList.add(cipherSuite);
				}
			}
		}
		return cipherSuitePatternList.toArray( new String[ cipherSuitePatternList.size() ] );
	}
	
	public Collection<HttpsSecurityPolicy> getSupportedSecurityPolicies() {
		if (securityPolicies == null)
			return HttpsSecurityPolicy.getAvailablePolicies().values();
		return Arrays.asList(securityPolicies);
	}

	HttpParams getHttpParams() {
		return application.getHttpsSettings().getHttpParams() == null ? DEFAULT_HTTPPARAMS : application.getHttpsSettings().getHttpParams();
	}

	/**
	 * Set worker thread count. Defines how many worker treads are initialized to handle incoming HTTPS requests. Note that this does not limit the number of UA sessions, since all sessions will share these threads. Set this value before calling binding the first socket address.
	 * 
	 * @param workerThreadCount
	 */
	public void setWorkerThreadCount(int workerThreadCount) {
		if ( ioReactor!=null ) 
			throw new RuntimeException("Set workercount before binding the first socket address");
		ioConfig.setIoThreadCount( workerThreadCount );
	}
	
	/**
	 * @return the current workerThreadCount
	 * @see #setWorkerThreadCount(int)
	 */
	public int getWorkerThreadCount() {
		return ioConfig.getIoThreadCount();
	}
	
	protected void shutdownReactor() {
		for ( SocketHandle sh : socketHandleSnapshot() ) {
			ListenerEndpoint le = sh.listenerEndpoint;
			if ( le != null ) le.close();
			sh.listenerEndpoint = null;
		}
		if ( ioReactor != null ) {
			try {
				ioReactor.shutdown();
			} catch (IOException e) {
				log.error("Failed to shutdown ioReactor", e);
			}
			ioReactor = null;
		}
		if ( sslReactorThread != null ) {
			sslReactorThread.interrupt();
			try {
				sslThreadSemaphore.acquire();
			} catch (InterruptedException e) {
			}
			sslThreadSemaphore = null;
			sslReactorThread = null;
		}
		if (plainReactorThread != null ) {
			plainReactorThread.interrupt();
			try {
				plainThreadSemaphore.acquire();
			} catch (InterruptedException e) {
			}
			plainThreadSemaphore = null;
			plainReactorThread = null;
		}
	}
	
	protected void initReactor() throws ServiceResultException {
		boolean https = false, http = false;
		for ( SocketHandle sh : socketHandles.values() ) {
			https |= sh.scheme.equals( UriUtil.SCHEME_HTTPS );
			http |= sh.scheme.equals( UriUtil.SCHEME_HTTP );
		}
		
		try {
			if ( https && sslSetupHandler == null ) {
			    SSLContext sslcontext = SSLContext.getInstance("TLS");
			    sslcontext.init( application.getHttpsSettings().getKeyManagers(), application.getHttpsSettings().getTrustManagers(), null );
	
			    // SSL Setup Handler
			    sslSetupHandler = new SSLSetupHandler() {
					public void verify(IOSession iosession, SSLSession sslsession) throws SSLException {
					}
					public void initalize(SSLEngine sslengine) throws SSLException {
						//sslengine.setEnabledCipherSuites( calcCipherSuites() );
					}
			    };
			    
			    // Create HTTP connection factory
			    sslConnFactory = new SSLNHttpServerConnectionFactory(sslcontext, sslSetupHandler, getHttpParams());
				
			    // Create server-side I/O event dispatch
			    sslIoEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, sslConnFactory);
			    
			    // Create ssl engine
				sslEngine = sslcontext.createSSLEngine();
				log.info( "Enabled protocols in SSL Engine are {}",Arrays.toString( sslEngine.getEnabledProtocols()));
				enabledCipherSuites = sslEngine.getEnabledCipherSuites();
				log.info( "Enabled CipherSuites in SSL Engine are {}", Arrays.toString( enabledCipherSuites ) );
			}
			
			if ( https ) {
			    // Create list of cipher suites
				String[] oldCipherSuiteSelection = cipherSuites;
				cipherSuitePatterns = calcCipherSuitePatterns();
				//securityPolicies = calcSecurityPolicies().toArray( new SecurityPolicy[0] );
				cipherSuites = CryptoUtil.filterCipherSuiteList(enabledCipherSuites, cipherSuitePatterns);
				sslEngine.setEnabledCipherSuites(cipherSuites);
				
				if (oldCipherSuiteSelection==null || !Arrays.equals(oldCipherSuiteSelection, cipherSuites)) {
					log.info( "CipherSuites for policies ({}) are {}", Arrays.toString( securityPolicies ), Arrays.toString( cipherSuites ) );
				}
			}
				
			if ( http && plainConnFactory==null ) {
				plainConnFactory  = new DefaultNHttpServerConnectionFactory( getHttpParams() );
				
			    // Create server-side I/O event dispatch
			    plainIoEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, plainConnFactory);			
			}
			
			if ( ioReactor == null ) {
			    // Create server-side I/O reactor
				ioReactor = new DefaultListeningIOReactor(ioConfig, null);
			}
		} catch (KeyManagementException e1) {
			throw new ServiceResultException(e1);
		} catch (NoSuchAlgorithmException e1) {
			throw new ServiceResultException(e1);
		} catch (IOReactorException e1) {
			throw new ServiceResultException(e1);
		}
	}

	public HttpsSettings getHttpsSettings() {
		return application.getHttpsSettings();
	}
	
	synchronized SocketHandle getOrCreateSocketHandle(SocketAddress socketAddress, String scheme)
	throws ServiceResultException
	{
		SocketHandle handle = socketHandles.get(socketAddress);
		if ( handle == null ) {
			handle = new SocketHandle(socketAddress, scheme);
			socketHandles.put(socketAddress, handle);
		} else {
			if ( !scheme.equals(handle.scheme) ) 
				throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "Socket port="+handle.getPort()+" cannot be bound as http and https.");
		}
		return handle;
	}
	
	@Override
	public EndpointHandle bind(SocketAddress socketAddress, EndpointBinding endpointBinding) throws ServiceResultException {
		if ( endpointBinding == null || socketAddress == null || endpointBinding.endpointServer!=this )
			throw new IllegalArgumentException();
		String url = endpointBinding.endpointAddress.getEndpointUrl();
		
		// Start endpoint handler
		{
			String endpointId = url;
			endpointId = UriUtil.getEndpointName(url);		
			if ( endpointId == null ) endpointId = ""; 
//			else endpointId = "*"+endpointId;
			HttpAsyncRequestHandler<?> oldEndpointHandler = registry.lookup( endpointId );
			if ( oldEndpointHandler == null ) {
				HttpsServerEndpointHandler endpointHandler = new HttpsServerEndpointHandler( endpointBinding );
				registry.register(endpointId, endpointHandler);			
				registry.register("", discoveryHandler);
			} else {
				HttpsServerEndpointHandler oldEndpointHander2 = (HttpsServerEndpointHandler) oldEndpointHandler;
				if ( oldEndpointHander2.endpointServer != endpointBinding.endpointServer ) {
					throw new ServiceResultException(
							StatusCodes.Bad_UnexpectedError, 
							"Cannot bind endpoint " + url + 
							" and " + oldEndpointHander2.endpointBinding.endpointAddress.getEndpointUrl() + 
							" with two different sets of service.");
				}
			}
		}
		
		// Make socket handle and endpoint handle
		String scheme = UriUtil.getTransportProtocol( endpointBinding.endpointAddress.getEndpointUrl() );
		SocketHandle socketHandle = getOrCreateSocketHandle(socketAddress, scheme);
		
		HttpsEndpointHandle endpointHandle = socketHandle.getOrCreate(endpointBinding);
		
		try {
			// Shutdown reactor
			shutdownReactor();
			// Create reactor
			initReactor();
		
			// Bind to listen the given ports
			for ( SocketHandle sh : socketHandleSnapshot() ) {
				if ( sh.listenerEndpoint == null ) {
					sh.listenerEndpoint = ioReactor.listen( sh.getSocketAddress() );
				}
			}
						        
			// Start reactor threads
		    if ( UriUtil.SCHEME_HTTPS.equals( scheme ) ) {
			    if ( sslReactorThread==null || !sslReactorThread.isAlive() ) {
			    	final IOReactor r = ioReactor;
			    	final Semaphore s = sslThreadSemaphore = new Semaphore(0);
			    	sslReactorThread = new Thread() {
			    		public void run() {
			    			try {
			    				setState(CloseableObjectState.Open);
		    					r.execute(sslIoEventDispatch);
			    			} catch (IOException e) {
			    				HttpsServer.this.setError( new ServiceResultException(e) );
			    			} finally {
			    				s.release(9999);
			    			}
			    		};
			    	};
				    if ( !getState().isOpen() ) setState(CloseableObjectState.Opening);
				    sslReactorThread.start();
			    }
		    }
		        
		    if ( UriUtil.SCHEME_HTTP.equals( scheme ) ) {
			    if ( plainReactorThread==null || !plainReactorThread.isAlive() ) {
			    	final IOReactor r = ioReactor;
			    	final Semaphore s = plainThreadSemaphore = new Semaphore(0);
			    	plainReactorThread = new Thread() {
				       	public void run() {
				            try {
				          		setState(CloseableObjectState.Open);
								r.execute(plainIoEventDispatch);
							} catch (IOException e) {
								HttpsServer.this.setError( new ServiceResultException(e) );
							} finally {
								s.release(9999);
							}
			        	};
			        };
			        if ( !getState().isOpen() ) setState(CloseableObjectState.Opening);
			        plainReactorThread.start();
		        }
	        }

		} catch (ServiceResultException e) {
			endpointHandle.close();
			throw e;
		}
		log.info("Endpoint bound to {}", url);		
		return endpointHandle;
	}
	
	@Override
	public void addConnectionListener(org.opcfoundation.ua.transport.ConnectionMonitor.ConnectListener l) {
		connections.addConnectionListener(l);
	}

	@Override
	public void getConnections(Collection<ServerConnection> result) {
		connections.getConnections(result);
	}

	@Override
	public void removeConnectionListener(org.opcfoundation.ua.transport.ConnectionMonitor.ConnectListener l) {
		connections.removeConnectionListener(l);
	}	

	@Override
	public List<SocketAddress> getBoundSocketAddresses() {
		ArrayList<SocketAddress> result = new ArrayList<SocketAddress>();
		for (SocketHandle sh : socketHandleSnapshot()) result.add( sh.getSocketAddress() );
		return result;
	}

	@Override
	public EncoderContext getEncoderContext() {
		return application.getEncoderContext();
	}

	@Override
	public EndpointBindingCollection getEndpointBindings() {
		return endpointBindings;
	}
	
	public synchronized CloseableObject close() {
		for (EndpointBinding eb : endpointBindings.getAll()) {
			eb.endpointServer.getEndpointBindings().remove(eb);
		}
		endpointBindings.clear();
		try {
			setState(CloseableObjectState.Closing);
			for (SocketHandle sh : socketHandleSnapshot())
				sh.close();
		} finally {
			try {				
				if ( ioReactor!=null ) {
					ioReactor.shutdown();
				}
				
				if (sslReactorThread!=null) {
					sslReactorThread.interrupt();
					sslReactorThread = null;
					//?
				}
				
				if (plainReactorThread!=null) {
					plainReactorThread.interrupt();
					plainReactorThread = null;
					//?
				}
				
			} catch (IOException e) {
			}
			setState(CloseableObjectState.Closed);
		} 
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HttpServer");
		sb.append("(");
		for (SocketHandle sh : socketHandleSnapshot()) sb.append( sh.toString() );
		sb.append(")");
		return sb.toString();
	}

	public SocketHandle[] socketHandleSnapshot() {
		return socketHandles.values().toArray( new SocketHandle[ socketHandles.size()] );		
	}

	public class SocketHandle {
		private SocketAddress socketAddress;
		ListenerEndpoint listenerEndpoint;
		String scheme;
		Map<Endpoint, HttpsEndpointHandle> endpoints = new HashMap<Endpoint, HttpsEndpointHandle>();
		SocketHandle(SocketAddress socketAddress, String scheme)
		{
			this.setSocketAddress(socketAddress);
			this.scheme = scheme;
		}
		
		public synchronized HttpsEndpointHandle[] endpointHandleSnapshot() {
			return endpoints.values().toArray( new HttpsEndpointHandle[ endpoints.size()] );
		}
		
		synchronized void endpointHandleSnapshot(Collection<HttpsEndpointHandle> handles) {
			handles.addAll( endpoints.values() );
		}
		
		synchronized HttpsEndpointHandle getOrCreate(EndpointBinding endpointBinding) throws ServiceResultException {
			HttpsEndpointHandle handle = endpoints.get(endpointBinding.endpointAddress);
			if ( handle == null ) {
				handle = new HttpsEndpointHandle(this, endpointBinding);
				// Add endpointBinding to SocketHandle
				endpoints.put(endpointBinding.endpointAddress, handle);
				// Add endpointBinding to EndpointServer
				endpointBindings.add(endpointBinding);
				// Add endpointBinding to Server
				endpointBinding.serviceServer.getEndpointBindings().add(endpointBinding);
			} else {
				if ( !handle.endpointBinding.equals( endpointBinding ) ) {
					throw new ServiceResultException( StatusCodes.Bad_UnexpectedError, "Cannot bind an endpoint address to two different servers." );
				}
			}
			return handle;
		}
		int getPort() {
			return ((InetSocketAddress)getSocketAddress()).getPort();
		}
		void close() {
			for ( HttpsEndpointHandle eph : endpoints.values() ) eph.close__();
			socketHandles.remove(getSocketAddress());
			if (listenerEndpoint != null)
				listenerEndpoint.close();
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append( scheme+"("+getSocketAddress()+", ");
			for (HttpsEndpointHandle ep : endpoints.values()) {
				sb.append( ep.toString() );
			}
			sb.append( ")");
			return sb.toString();
		}

		/**
		 * @return the socketAddress
		 */
		public SocketAddress getSocketAddress() {
			return socketAddress;
		}

		/**
		 * @param socketAddress the socketAddress to set
		 */
		void setSocketAddress(SocketAddress socketAddress) {
			this.socketAddress = socketAddress;
		}
	}
	
	public class HttpsEndpointHandle implements EndpointHandle {
		EndpointBinding endpointBinding;
		SocketHandle socketHandle;
		HttpsEndpointHandle(SocketHandle socketHandle, EndpointBinding endpointBinding) {
			this.socketHandle = socketHandle;
			this.endpointBinding = endpointBinding;
		}
		@Override
		public SocketAddress socketAddress() {
			return socketHandle.getSocketAddress();
		}
		@Override
		public EndpointBinding endpointBinding() {
			return endpointBinding;
		}
		@Override
		public void close() {
			close_();
			close__();
		}
		void close_() {
			// Remove endpointBinding from SocketHandle
			socketHandle.endpoints.remove(endpointBinding.endpointAddress);
			// Close socket if this was the last
			if ( socketHandle.endpoints.isEmpty() ) {
				socketHandle.close();
			}
		}
		void close__() {
			// Close endpointHandler if this was the last
			int count = countEndpoints(endpointBinding.endpointAddress);
			if ( count == 0 ) {
				// Remove handler
				String uri = endpointBinding.endpointAddress.getEndpointUrl();
				registry.unregister(uri);
				registry.unregister(""); // Discovery uri
				// Remove endpointBinding from EndpointServer
				endpointBindings.remove(endpointBinding);
				// Remove endpointBinding from Server
				endpointBinding.serviceServer.getEndpointBindings().remove(endpointBinding);
			}
		}
		@Override
		public String toString() {
			return "("+endpointBinding.endpointAddress.toString()+")";
		}
	}

	List<HttpsEndpointHandle> findEndpoints(String forUri) {
		List<HttpsEndpointHandle> result = new ArrayList<HttpsEndpointHandle>();
		for (SocketHandle sh : socketHandleSnapshot() ) sh.endpointHandleSnapshot(result);
		return result;
	}

	int countEndpoints(Endpoint endpointAddress) {
		int count = 0;
		for (SocketHandle sh : socketHandleSnapshot() ) {
			for (HttpsEndpointHandle eh : sh.endpointHandleSnapshot()) {
				if (eh.endpointBinding.endpointAddress.equals(endpointAddress)) count++;
			}
		}
		return count;
	}
	
	class RequestResolver implements HttpAsyncRequestHandlerResolver {

	    Map<String, HttpsServerEndpointHandler> map;

	    public RequestResolver() {
	        map = new HashMap<String, HttpsServerEndpointHandler>();
	    }

	    public void register(String pattern, HttpsServerEndpointHandler handler) {
	        map.put(pattern, handler);
	    }

	    public void unregister(String pattern) {
	        map.remove(pattern);
	    }

	    public void setHandlers(Map<String, HttpAsyncRequestHandler<?>> otherMap) {
	        map.clear();
	        for ( Entry<String, HttpAsyncRequestHandler<?>> e : otherMap.entrySet())
	        {
	        	map.put(e.getKey(), (HttpsServerEndpointHandler)e.getValue());
	        }
	    }

	    public Map<String, HttpAsyncRequestHandler<?>> getHandlers() {
	        return new HashMap<String, HttpAsyncRequestHandler<?>>( map );
	    }

	    public HttpAsyncRequestHandler<?> lookup(String requestURI) {
	    	HttpAsyncRequestHandler<?> result = map.get(requestURI);
	    	if ( result == null && ( requestURI.equals("") || requestURI.equals("/") ) ) return discoveryHandler;
	        return result;
	    }

		
	}
	
}
