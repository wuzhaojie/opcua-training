/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.stacktest.client;

import static org.opcfoundation.ua.utils.EndpointUtil.selectByMessageSecurityMode;
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;
import static org.opcfoundation.ua.utils.EndpointUtil.selectBySecurityPolicy;
import static org.opcfoundation.ua.utils.EndpointUtil.sortBySecurityLevel;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.stacktest.TestCase;
import org.opcfoundation.ua.stacktest.TestCaseExecutorFactory;
import org.opcfoundation.ua.stacktest.TestConfiguration;
import org.opcfoundation.ua.stacktest.TestSequence;
import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.io.TestSequenceReader;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;
import org.opcfoundation.ua.stacktest.testcases.ArrayValuesTestCase;
import org.opcfoundation.ua.stacktest.testcases.ExtensionObjectValuesTestCase;
import org.opcfoundation.ua.stacktest.testcases.ScalarValuesTestCase;
import org.opcfoundation.ua.transport.SecureChannel;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

import sun.security.krb5.internal.crypto.Nonce;

/**
 * Stack Test Client.
 * 
 * The client executes the stack tests on the client side.
 * 
 * @author Jouni.Aro@prosys.fi
 */
public class TestClient {
	// Object of type TestConfiguration.        
	private TestConfiguration configuration=null;
	private TestSequence testSequence=null;
	private TestCaseExecutorFactory executorFactory=null;
	private Logger logger=null;
	private SecureChannel channel = null;
	private PseudoRandom random = null;
	
	/**
	 * The Stack Test Client application code.
	 */
	public static void main(String[] args) 
	throws Throwable {

		// Create Logger
		org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(TestClient.class);

		// Connection URLs for different servers, uncomment the one to be used
		String url = args.length>0 ? args[0] : "opc.tcp://localhost:6002/UAExample"; //Java TestServer
		//String url = args.length>0 ? args[0] : "opc.tcp://localhost:51210/UA/SampleServer"; //UA Sample Server
		//String url = args.length>0 ? args[0] : "opc.tcp://localhost:11000/StackTestServer/DotNet/1024"; //C# Stack test server
//		String url = args.length>0 ? args[0] : "opc.tcp://localhost:9001/UA/TestServer";
		String testCases = args.length>1 ? args[1] : null;
		if (testCases != null)
			myLogger.info("Loading test cases from: " + testCases);
		myLogger.info("Connecting to: " + url);
	    TestClient testClient = new TestClient(testCases);
	    try {
			testClient.connect(url);
			testClient.executeTestSequence();	    	
	    } finally {
	    	testClient.disconnect();
	    }
	}

	public TestClient(String testCases) throws UnrecoverableKeyException,
			CertificateException, NoSuchAlgorithmException, KeyStoreException,
			IOException {
	initExecutorMap();
		loadConfiguration(testCases);
	}
	
	public void connect(String url) throws TestCaseException{

		if (isConnected())
			throw new TestCaseException("Test client is already connected.");
		try {
			// Create Client
			Client myClient = Client.createClientApplication( configuration.ClientApplicationInstanceCertificate );
			// Get endpoint for desired security level
			EndpointDescription[] endpoints = myClient.discoverEndpoints( url );
			System.out.println("All endpoints: " + endpoints.length);
			// Filter out all but opc.tcp protocol endpoints
			endpoints = selectByProtocol(endpoints, "opc.tcp");
			System.out.println("opc.tcp endpoints: " + endpoints.length);
			// Filter out all but Signed & Encrypted endpoints
			endpoints = selectByMessageSecurityMode(endpoints, MessageSecurityMode.None);
			System.out.println("MessageSecurityMode endpoints: " + endpoints.length);
			endpoints = selectBySecurityPolicy(endpoints, SecurityPolicy.NONE);
			System.out.println("SecurityPolicy endpoints: " + endpoints.length);
			// Sort endpoints by security level. The lowest level at the beginning, the highest at the end of the array
			endpoints = sortBySecurityLevel(endpoints); 
			// Choose one endpoint (the most secure one)	
			EndpointDescription endpoint = endpoints[endpoints.length-1];
			// Create channel
			channel = myClient.createSecureChannel(endpoint);	
			logger.open(0 /* TODO: secureChannelId */, configuration.getTestFilePath(), configuration.getRandomFilePath());

		} catch (Exception e) {
			e.printStackTrace();
			throw new TestCaseException("Connect failed: " + e.getMessage(), e);
		}
	}

	/**
	 * @throws ServiceResultException
	 */
	private void disconnect() throws ServiceResultException {
		if (channel!=null) {
			channel.closeAsync();
			channel = null;
		}
		logger.close();
	}
	
	private boolean isConnected() {
		return (channel != null) && (channel.isOpen());
	}

	/**
	 * Defines the TestCase Executors to use for different TestCases.
	 */
	protected void initExecutorMap() {
		executorFactory = new TestCaseExecutorFactory();
		executorFactory.registerExecutor(ScalarValuesTestCase.class, TestCaseExecutor.class);
		executorFactory.registerExecutor(ArrayValuesTestCase.class, TestCaseExecutor.class);
		executorFactory.registerExecutor(ExtensionObjectValuesTestCase.class, TestCaseExecutor.class);
	}
	
	/**
	 * Loads the test configuration.
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateException 
	 * @throws UnrecoverableKeyException 
	 */
	protected void loadConfiguration(String testCases) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		configuration = new TestConfiguration(testCases);
		configuration.setLogFilePath("testharness/ClientTestLog.xml");
		loadTestConfiguration();
	}

	// / <summary>
	// / This method loads the test configuration.
	// / </summary>
	// / <param name="testFilePath">Test case file path.</param>
	private void loadTestConfiguration()
	{
		// load sequence to execute.
		TestSequenceReader testSequenceReader = new TestSequenceReader();
		testSequence = testSequenceReader.read(configuration.getTestFilePath());
		System.out.println(testSequence.toString());

		// init random generator
		random = new PseudoRandom();
		random.setRandomFile(configuration.getRandomFilePath());
		random.setStep(testSequence.getRandomDataStepSize());

		// open log file.
		logger = new Logger(configuration.getLogFilePath(), testSequence.getLogDetailLevel());
	}

	public void executeTestSequence() throws Throwable {
		for (TestCase testCase: testSequence.getTestCases()) {
			//random.setSeed(testCase.getSeed());
			TestCaseExecutor executor;

			try {
				executor = (TestCaseExecutor) executorFactory.createExecutor(testCase, random, logger);
			} catch (TestCaseException e1) {
				executor = new TestCaseExecutor(testCase, random, logger);
			}
			try {
				executor.setHaltOnError(testSequence.isHaltOnError());
				executor.setChannelService(channel);
				executor.execute();
			} catch (Throwable e) {
				error(testCase, TestCase.TestCaseInitIteration, e);
			}			
		}
		
	}

	/**
	 * Generates an error event for a test case.
	 * 
	 * @param testCase
	 * @param iteration
	 * @param e
	 * @throws ServiceResultException
	 */
	private void error(TestCase testCase, int iteration, Throwable e) throws Throwable {
		logger.logErrorEvent(testCase, iteration, e);
		if (testSequence.isHaltOnError()) 
			throw e;
	}

	public static byte[] nextNonce() {
	    int nonce = Nonce.value();
	    return toBytes(nonce);
	}

	public static byte[] toBytes(int value)
	{
	    byte array[] = new byte[4];
	    array[3] = (byte) (value & 0xff);
	    array[2] = (byte) ((value >> 8) & 0xff);
	    array[1] = (byte) ((value >> 16) & 0xff);
	    array[0] = (byte) ((value >> 24) & 0xff);
	    return array;
	}

	
}
