package org.opcfoundaiton.ua;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.common.DebugLogger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ActivateSessionRequest;
import org.opcfoundation.ua.core.ActivateSessionResponse;
import org.opcfoundation.ua.core.AttributeServiceSetHandler;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CallMethodRequest;
import org.opcfoundation.ua.core.CallMethodResult;
import org.opcfoundation.ua.core.CallRequest;
import org.opcfoundation.ua.core.CallResponse;
import org.opcfoundation.ua.core.CancelRequest;
import org.opcfoundation.ua.core.CancelResponse;
import org.opcfoundation.ua.core.CloseSessionRequest;
import org.opcfoundation.ua.core.CloseSessionResponse;
import org.opcfoundation.ua.core.CreateSessionRequest;
import org.opcfoundation.ua.core.CreateSessionResponse;
import org.opcfoundation.ua.core.EndpointConfiguration;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.HistoryReadRequest;
import org.opcfoundation.ua.core.HistoryReadResponse;
import org.opcfoundation.ua.core.HistoryUpdateRequest;
import org.opcfoundation.ua.core.HistoryUpdateResponse;
import org.opcfoundation.ua.core.MethodServiceSetHandler;
import org.opcfoundation.ua.core.ReadRequest;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.core.SessionServiceSetHandler;
import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.core.WriteRequest;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.examples.certs.ExampleKeys;
import org.opcfoundation.ua.transport.EndpointServer;
import org.opcfoundation.ua.transport.UriUtil;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.EndpointUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestXMLElementServer {

	public static void main(String[] args)
	throws Exception
	{        
		// Load Log4j configurations from external file
		//PropertyConfigurator.configure( ClientServerExample.class.getResource("log.properties") );
		// Create Logger
		Logger myLogger = Logger.getLogger( TestXMLElementServer.class ); 
		
		///// Server Application /////////////		
		Application serverApplication = new Application();
		XMLElementTestServer myServer = new XMLElementTestServer(serverApplication);
		
		// Attach listener (debug logger) to each binding
		DebugLogger debugLogger = new DebugLogger( myLogger );
		for (EndpointServer b : myServer.getEndpointBindings().getEndpointServers())
			b.addConnectionListener( debugLogger );		
		//////////////////////////////////////		


		
		//////////////  CLIENT  //////////////
		// Load Client's Application Instance Certificate from file
		KeyPair myClientApplicationInstanceCertificate = ExampleKeys.getClientCert();
		// Create Client
		Client myClient = Client.createClientApplication( myClientApplicationInstanceCertificate );
		Application myClientApplication = myClient.getApplication();
		myClientApplication.getHttpsSettings().setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
		myClientApplication.getHttpsSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		//////////////////////////////////////		
		
		// Get Endpoints like this
		String uri;
		uri = "opc.tcp://localhost:8666/UAExample";
		//uri = "https://localhost:8080/UAExample";
		
		SessionChannel myChannel = null;
		try {
			myClient.setTimeout( 10000000 );
			EndpointDescription[] endpoints = myClient.discoverEndpoints(uri, "");
		
			endpoints = EndpointUtil.selectByProtocol(endpoints, UriUtil.getTransportProtocol(uri));
			
			// Connect the application
			myChannel = myClient.createSessionChannel( endpoints[0] );
			// Activate session
			myChannel.activate();
			//////////////////////////////////////

			/////////////  EXECUTE  ////////
			
			CallRequest callRequest = new CallRequest();
			CallMethodRequest methodRequest = new CallMethodRequest();
			callRequest.setMethodsToCall( new CallMethodRequest[] {methodRequest} );
			methodRequest.setMethodId( XMLElementTestServer.TEST_XMLELEMENT );
			byte[] data = new byte[65536];
			XMLElementTestServer.fill(data);
			methodRequest.setInputArguments( new Variant[] { new Variant( new XmlElement( data ) ) } );
			CallResponse res = myChannel.Call( callRequest );
			XmlElement xe = (XmlElement) res.getResults()[0].getOutputArguments()[0].getValue();
			byte[] data2 = xe.getData();
			System.out.println("CallMethod/"+XMLElementTestServer.TEST_XMLELEMENT+" request and result matcehs = "+Arrays.equals(data, data2));
			
			//////////////////////////////////////
			
			ReadResponse res4 = myChannel.Read(
					null, 
					500.0, 
					TimestampsToReturn.Source, 
					new ReadValueId(new NodeId(6, 1710), Attributes.Value, null, null ) 
				);		
			DataValue dv[] = res4.getResults();
			Variant v = dv[0].getValue();
			XmlElement xe2 = (XmlElement) v.getValue();
			System.out.println("Got response: "+xe2.getData().length+" bytes.");
			//System.out.println(xe.getValue());
			
			
			
		} 
		
		finally {
			/////////////  SHUTDOWN  /////////////
			// Close client's channel
			if ( myChannel != null ) myChannel.close();
			// Close the server by unbinding all endpoints 
			myServer.getApplication().close();
			//////////////////////////////////////		
		}
		
	}
	
	
}

class XMLElementTestServer extends Server implements MethodServiceSetHandler, SessionServiceSetHandler, AttributeServiceSetHandler {
	
	public static final NodeId TEST_XMLELEMENT = new NodeId(2, "TEST_XMLELEMENT");
	
	public XMLElementTestServer(Application application) throws CertificateException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, URISyntaxException, ServiceResultException
	{
		super(application);
		// Add method service set
		addServiceHandler( this );
		
		// Add Client Application Instance Certificate validator - Accept them all (for now)
		application.getOpctcpSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		application.getHttpsSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		
		// Peer verifier
		application.getHttpsSettings().setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
		
		// Load Servers's Application Instance Certificate from file
		KeyPair myServerApplicationInstanceCertificate = ExampleKeys.loadServerCert(); 
		application.addApplicationInstanceCertificate( myServerApplicationInstanceCertificate );
		application.getHttpsSettings().setKeyPair( myServerApplicationInstanceCertificate );
		
		// Add User Token Policies
		addUserTokenPolicy( UserTokenPolicy.ANONYMOUS );
		addUserTokenPolicy( UserTokenPolicy.SECURE_USERNAME_PASSWORD );
		
		// Create an endpoint for each network interface
		String hostname = EndpointUtil.getHostname();
		String bindAddress, endpointAddress;
		for (String addr : EndpointUtil.getInetAddressNames()) {			
			bindAddress = "opc.tcp://"+addr+":8666/UAExample";
			endpointAddress = "opc.tcp://"+hostname+":8666/UAExample";
			System.out.println(endpointAddress+" bound at "+bindAddress);
			bind(bindAddress, endpointAddress, SecurityMode.ALL);
			
			bindAddress = "opc.tcp://"+addr+":8080/UAExample";
			endpointAddress = "opc.tcp://"+hostname+":8080/UAExample";
			System.out.println(endpointAddress+" bound at "+bindAddress);
			bind(bindAddress, endpointAddress, SecurityMode.ALL);
		}
		
		//////////////////////////////////////
		
	}

	@Override
	public void onCall(EndpointServiceRequest<CallRequest, CallResponse> callRequest)
			throws ServiceFaultException {
		
		CallResponse response = new CallResponse();
		CallMethodRequest[] reqs = callRequest.getRequest().getMethodsToCall();
		CallMethodResult[] results = new CallMethodResult[ reqs.length ];
		response.setResults( results );
		
		// Iterate all calls
		for (int i=0; i<reqs.length; i++) {
			CallMethodRequest req = reqs[i];
			CallMethodResult result = results[i] = new CallMethodResult();
			
			NodeId methodId = req.getMethodId();
			if ( TEST_XMLELEMENT.equals(methodId) ) {
				
				Variant input = req.getInputArguments()[0];
				XmlElement xe = (XmlElement) input.getValue();
				byte[] inputdata = xe.getData();
				verify( inputdata );
				
				xe = new XmlElement( inputdata );
				Variant out = new Variant( xe );				
				result.setOutputArguments( new Variant[] { out } );
				result.setStatusCode( StatusCode.GOOD );				
			}
			
			else {
				// Unknown method
				result.setStatusCode( new StatusCode( StatusCodes.Bad_MethodInvalid ) );
			}
			
		}
		
		callRequest.sendResponse(response);

	}

	@Override
	public void onCreateSession(EndpointServiceRequest<CreateSessionRequest, CreateSessionResponse> msgExchange) throws ServiceFaultException {
		CreateSessionRequest req = msgExchange.getRequest();
		CreateSessionResponse res = new CreateSessionResponse();
		byte[] token = new byte[32];
		byte[] nonce = new byte[32];
		Random r = new Random();
		r.nextBytes( nonce );
		r.nextBytes( token );
		SignatureData signatureData = new SignatureData();
		//signatureData.setAlgorithm(Algorithm)
		res.setAuthenticationToken( new NodeId(0, token) );
		EndpointConfiguration endpointConfiguration = EndpointConfiguration.defaults();
		res.setMaxRequestMessageSize( UnsignedInteger.valueOf( Math.max( endpointConfiguration.getMaxMessageSize(), req.getMaxResponseMessageSize().longValue()) ) );
		res.setRevisedSessionTimeout( Math.max( req.getRequestedSessionTimeout() , 60*1000) );
		res.setServerCertificate( getApplication().getApplicationInstanceCertificates()[0].getCertificate().encodedCertificate );
		res.setServerEndpoints( this.getEndpointDescriptions() );
		res.setServerNonce( nonce );
		res.setServerSignature( signatureData );
		res.setServerSoftwareCertificates( getApplication().getSoftwareCertificates() );
		res.setSessionId( new NodeId(0, "Client") );
		msgExchange.sendResponse(res);
	}

	@Override
	public void onActivateSession(EndpointServiceRequest<ActivateSessionRequest, ActivateSessionResponse> msgExchange) throws ServiceFaultException {
		ActivateSessionRequest req = msgExchange.getRequest();
		ActivateSessionResponse res = new ActivateSessionResponse();
		byte[] nonce = new byte[32];
		Random r = new Random();
		r.nextBytes( nonce );
		res.setServerNonce(nonce);
		res.setResults(new StatusCode[]{ StatusCode.GOOD});
		msgExchange.sendResponse(res);
	}

	@Override
	public void onCloseSession(EndpointServiceRequest<CloseSessionRequest, CloseSessionResponse> msgExchange) throws ServiceFaultException {
		CloseSessionRequest req = msgExchange.getRequest();
		CloseSessionResponse res = new CloseSessionResponse();
		msgExchange.sendResponse(res);
		}

	@Override
	public void onCancel(EndpointServiceRequest<CancelRequest, CancelResponse> msgExchange) throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRead(EndpointServiceRequest<ReadRequest, ReadResponse> req) throws ServiceFaultException {
		ReadResponse response = new ReadResponse();

//		try {
//			Document doc1 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Feedwater_section.xml" ) );
//			Document doc2 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Turbine_section.xml" ) );
//			Document doc3 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal-3\\boiler_example.xml" ) );
//			
//			String str1 = XMLTool.printXMLDocument( doc1 );
//			String str2 = XMLTool.printXMLDocument( doc2 );
//			String str3 = XMLTool.printXMLDocument( doc3 );
//			
//			byte[] data1 = FileUtil.readFile( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Turbine_section.pdf" ) );
//			byte[] data2 = FileUtil.readFile( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Control_system.pdf" ) );
//			byte[] data3 = FileUtil.readFile( new File( "C:\\scratch\\boiler_runs.pdf" ) );
			
			byte[] data65535 = new byte[ 65535 ];
			fill( data65535 );
			
			XmlElement xe = new XmlElement( data65535 );
			Variant variant = new Variant( xe );
			DataValue value = new DataValue( variant );
			response.setResults( new DataValue[] { value } );
			ResponseHeader rh = new ResponseHeader();
			rh.setServiceResult( StatusCode.GOOD );
			response.setResponseHeader( rh );
			
			req.sendResponse(response);
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		} catch (TransformerException e) {
//			e.printStackTrace();
//		}

		
	}

	@Override
	public void onHistoryRead(EndpointServiceRequest<HistoryReadRequest, HistoryReadResponse> req) throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWrite(EndpointServiceRequest<WriteRequest, WriteResponse> req) throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHistoryUpdate(EndpointServiceRequest<HistoryUpdateRequest, HistoryUpdateResponse> req) throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	// Fill array with debug data
	public static void fill(byte[] data)
	{
		// Create random generator with fixed seed
		Random r = new Random(data.length);
		for (int i=0; i<data.length; i++)
			data[i] = (byte) (r.nextInt(256) - 128);
	}
	
	// Verify array of debug data
	public static void verify(byte[] data)
	{
		// Create random generator with the same seed
		Random r = new Random(data.length);
		for (int i=0; i<data.length; i++) {
			if ( (byte) (r.nextInt(256) - 128) != data[i] ) {
				throw new AssertionError("data mismatch");
			}
		}
		
	}		
	
}
