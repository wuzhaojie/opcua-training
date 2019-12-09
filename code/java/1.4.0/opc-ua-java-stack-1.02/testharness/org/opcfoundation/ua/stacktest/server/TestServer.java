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

package org.opcfoundation.ua.stacktest.server;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.opcfoundation.ua.application.Server;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.DebugLogger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CompositeTestType;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.TestServiceSetHandler;
import org.opcfoundation.ua.core.TestStackExRequest;
import org.opcfoundation.ua.core.TestStackExResponse;
import org.opcfoundation.ua.core.TestStackRequest;
import org.opcfoundation.ua.core.TestStackResponse;
import org.opcfoundation.ua.stacktest.TestCase;
import org.opcfoundation.ua.stacktest.TestCaseExecutorFactory;
import org.opcfoundation.ua.stacktest.TestConfiguration;
import org.opcfoundation.ua.stacktest.TestSequence;
import org.opcfoundation.ua.stacktest.exception.InvalidTestCaseException;
import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.io.TestSequenceReader;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;
import org.opcfoundation.ua.stacktest.testcases.ArrayValuesTestCase;
import org.opcfoundation.ua.stacktest.testcases.ExtensionObjectValuesTestCase;
import org.opcfoundation.ua.stacktest.testcases.ScalarValuesTestCase;
import org.opcfoundation.ua.transport.EndpointServer;
import org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.transport.security.SecurityMode;

/**
 * TestServer
 * 
 * Implements the server side Stack Tests.
 * 
 * Based on the .NET counterpart
 * 
 * @author jouni.aro@prosys.fi
 */
public class TestServer {

	private static TestConfiguration configuration;
	private static TestCaseExecutorFactory executorFactory;
	private static Logger logger;
	private static PseudoRandom random;
	private static TestSequence testSequence;

	
		public static void main(String[] args) throws Exception {
			
			// Create Logger
			org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(TestServer.class);

			String testCases = args.length>0 ? args[0] : null;
			if (testCases != null)
				myLogger.info("Loading test cases from: " + testCases);

			initExecutorMap();
			loadTestConfiguration(testCases);
			
			//////////////  SERVER  //////////////
			// Create UA Server
			Server myServer = Server.createServerApplication();
			
			// Add a service to the server - TestStack echo
			TestStackServer stackTestServer = new TestStackServer();
			myServer.addServiceHandler( stackTestServer );
			
			// Load Servers's Application Instance Certificate from file
			Cert myServerCertificate = Cert.load( TestCase.class.getResource( "ServerCert.der" ) );
			PrivKey myServerPrivateKey = PrivKey.loadFromKeyStore( TestCase.class.getResource( "UAServerCert.pfx"), "Opc.Sample.Ua.Server" );
			KeyPair myServerApplicationInstanceCertificate = new KeyPair(myServerCertificate, myServerPrivateKey); 
			// Add application instance certificate		
			myServer.getApplication().addApplicationInstanceCertificate( myServerApplicationInstanceCertificate );		
			
			// Create endpoint
			// Bind my server to my endpoint. This opens the port 6002    
			myServer.bind( "opc.tcp://localhost:6002/StackTestServer", "opc.tcp://localhost:6002/StackTestServer", SecurityMode.ALL );
			logger.open(0 /* TODO: secureChannelId */, configuration.getTestFilePath(), configuration.getRandomFilePath());
			// Attach debug logger
			DebugLogger debugLogger = new DebugLogger( myLogger );
			for (EndpointServer b : myServer.getEndpointBindings().getEndpointServers())
				b.addConnectionListener( debugLogger );
			//////////////////////////////////////
			
			
			//////////////////////////////////////		
			// Press enter to shutdown
			System.out.println("Press enter to shutdown");
			System.in.read();
			//////////////////////////////////////		
			
			
			/////////////  SHUTDOWN  /////////////
			// Unbind endpoint. This also closes the socket 6001 as it has no more endpoints.
			myServer.getApplication().close();
			// Write the log to file
			logger.close();
			//////////////////////////////////////	
		}
		
		/**
		 * Implementation of the TestStack service set.
		 * 
		 * @author jouni.aro@prosys.fi
		 *
		 */
		public static class TestStackServer implements TestServiceSetHandler {

			public void onTestStack(EndpointServiceRequest<TestStackRequest, TestStackResponse> req) throws ServiceFaultException {
				TestStackRequest request = req.getRequest();

				TestCase testCase = null;
				UnsignedInteger testId = request.getTestId();
				int iteration = request.getIteration();
				Variant output = null;
				
				if (TestCase.isBeginMarker(iteration))
					beginTestCase(request.getRequestHeader());
				else if (TestCase.isEndMarker(iteration))
					endTestCase();
				else {
					try {
						validateRequest(request.getRequestHeader());
						testCase = findTestCase(testId, iteration);
						testCase.validate(iteration);
						// TestCaseContext context =
						// TestUtils.GetExecutionContext(testCase);
						//random.setSeed(testCase.getSeed());
						TestCaseExecutor executor = (TestCaseExecutor) executorFactory.createExecutor(testCase, random, logger);
						executor.setServer(this);
						output = (Variant) executor.execute(iteration, request.getInput());

					} catch (InvalidTestCaseException e) {
						//e.printStackTrace();
						logger.logNotValidatedEvent(testId, iteration, e);
						throw new ServiceFaultException(new ServiceResultException(
								StatusCodes.Bad_ConfigurationError, e.getMessage()));
					} catch (TestCaseException e) {
						error(testCase, iteration, e);
					} catch (Throwable e) {
						error(testCase, iteration, e);
					}
				}
				req.sendResponse( new TestStackResponse(null, output ) );
			}

			public void onTestStackEx(org.opcfoundation.ua.transport.endpoint.EndpointServiceRequest<TestStackExRequest, TestStackExResponse> req) throws ServiceFaultException {
				TestStackExRequest request = req.getRequest();

				// This method is equals to TestStack, except that input and output are of
				// CompositeTestType, instead of Variant
				TestCase testCase = null;
				
				UnsignedInteger testId = request.getTestId();
				Integer iteration = request.getIteration();
				CompositeTestType output = null;
				
				if (TestCase.isBeginMarker(iteration))
					beginTestCase(request.getRequestHeader());
				else if (TestCase.isEndMarker(iteration))
					endTestCase();
				else {
					try {
						validateRequest(request.getRequestHeader());
						testCase = findTestCase(testId, iteration);
						testCase.validate(iteration);
						// TestCaseContext context =
						// TestUtils.GetExecutionContext(testCase);
						//random.setSeed(testCase.getSeed());
						TestCaseExecutor executor = (TestCaseExecutor) executorFactory.createExecutor(testCase, random, logger);
						executor.setServer(this);
						output = (CompositeTestType) executor.execute(iteration, request.getInput());
					} catch (ServiceResultException e ) {
						throw new ServiceFaultException( e );
					} catch (InvalidTestCaseException e) {
						logger.logNotValidatedEvent(testId, iteration, e);
						throw new ServiceFaultException( new ServiceResultException(
								StatusCodes.Bad_ConfigurationError, e.getMessage()) );
					} catch (TestCaseException e) {
						error(testCase, iteration, e);					
					}
				}

				req.sendResponse( new TestStackExResponse(null, output) );
			}

			/// <summary>
				/// Used by the performance test.
				/// </summary>
				//@Override
			//	 TODO
		/*
			 * 	public ReadResponse Read(ReadRequest request) 
				{
					if (request.getRequestHeader().getReturnDiagnostics().intValue() != 5000)
					{
						return super.Read(request);
					}
					diagnosticInfos = null;
			
					DataValue value = new DataValue();
			
					value.Value           = Int32.MaxValue;
					value.SourceTimestamp = DateTime.UtcNow;
			
					values = new DataValueCollection(nodesToRead.Count);
			
					foreach (ReadValueId valueId in nodesToRead)
					{
						values.Add(value);
					}
					return new ReadResponse();
				}
			*/
			
				public void validateRequest(RequestHeader requestHeader) throws ServiceResultException {
			        if (requestHeader == null)
			        {
			            throw new ServiceResultException(StatusCodes.Bad_RequestHeaderInvalid);
			        }
				}

			/**
			 * Generates an error event for a test case.
			 * 
			 * @param testCase
			 * @param iteration
			 * @param e
			 * @throws ServiceFaultException
			 */
			private void error(TestCase testCase, int iteration, Throwable e) throws ServiceFaultException {
				logger.logErrorEvent(testCase, iteration, e);
				e.printStackTrace();
				
				throw new ServiceFaultException(new ServiceResultException(StatusCodes.Bad_UnexpectedError,  
						String.format("Test %s, Iteration %d Failed: %s", testCase.getName(), iteration, e.toString())));
			}

			/*
				 * Initializes a new TestCase.
				 */
				protected void beginTestCase(RequestHeader requestHeader) {
					// The code below is from .NET stack test.
			/*		synchronized (logger) {
						logger.open(
								"", request.getRequestHeader().
								getRequestContext().Current.SecureChannelId, 
								configuration.getTestFilePath(), 
								configuration.getRandomFilePath());
			
						m_activeTestCasesCount++;
					}
			*/	}

			/*
				 * Finalizes a TestCase after it has been run through.
				 */
				protected void endTestCase() {
					// The code below is from .NET stack test.
			/*		synchronized (logger)	{
						m_activeTestCasesCount--;
						if (m_activeTestCasesCount == 0)
							logger.close();
					}
			*/	}
		}

		/// <summary>
		/// This method loads the test configuration.
		/// </summary>
		/// <param name="testFilePath">Test case file path.</param>
		private static void loadTestConfiguration(String testCases) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
		{
			// (re)load the test configuration file.
			configuration = new TestConfiguration(testCases);
		
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

		protected static void initExecutorMap() {
			executorFactory = new TestCaseExecutorFactory();
			executorFactory.registerExecutor(ScalarValuesTestCase.class, TestCaseExecutor.class);
			executorFactory.registerExecutor(ArrayValuesTestCase.class, TestCaseExecutor.class);
			executorFactory.registerExecutor(ExtensionObjectValuesTestCase.class, TestCaseExecutor.class); // added 26.5.2009
		}

		/// <summary>
		/// This method returns the test case identified by the id.
		/// </summary>       
		/// <param name="testId">Test Case Id.</param>
		/// <param name="iteration">This parameter stores the current iteration number.</param>      
		/// <returns>Testcase or exception based on the conditions</returns>
		private static TestCase findTestCase(UnsignedInteger testId, int iteration) throws InvalidTestCaseException
		{
			for (TestCase testCase: testSequence.getTestCases())
			{
				if (testCase.getTestId().equals(testId))
				{
					return testCase;
				}
			}
			throw new InvalidTestCaseException("Cannot find test case '" + testId.toString() + "' iteration=" + iteration);
		}
}
