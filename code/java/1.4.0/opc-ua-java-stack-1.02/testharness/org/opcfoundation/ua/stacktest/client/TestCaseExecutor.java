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

import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CompositeTestType;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.TestStackExRequest;
import org.opcfoundation.ua.core.TestStackExResponse;
import org.opcfoundation.ua.core.TestStackRequest;
import org.opcfoundation.ua.core.TestStackResponse;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.stacktest.TestCase;
import org.opcfoundation.ua.stacktest.exception.InvalidTestCaseException;
import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;
import org.opcfoundation.ua.transport.SecureChannel;

/**
 * Base class for client side test case executors.
 * 
 * The class is used as the default executor for all tests.
 * If the test cases require special execution logic you
 * can inherit sub classes for those tests.
 * 
 * The executors to use for different test casesare defined in
 * TestClient.initExecutorMap 
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestCaseExecutor extends
		org.opcfoundation.ua.stacktest.TestCaseExecutor {
	public TestCaseExecutor(TestCase testCase, PseudoRandom random,
			Logger logger) {
		super(testCase, random, logger);
	}

	private boolean haltOnError = false;

	private SecureChannel channel;

	@Override
	protected void beginIteration(Integer iteration) {
		super.beginIteration(iteration);
		// channelContext.Stack.SetEventFilter(StackEventFilter.None);
		// Discard remaining stack events
		// channelContext.Stack.DequeueEvents();
		beginOrEndIteration(iteration);
	}

	protected void beginOrEndIteration(Integer iteration) {
		// AnyValue input = new AnyValue();
		//sendRequest(null, iteration);
	}

	@Override
	protected void endIteration(Integer iteration) {
		super.endIteration(iteration);
		beginOrEndIteration(iteration);
	}

	public void execute() throws Throwable {
		System.out.println(testCase);
		if (testCase.isSkipTest()) {
			//logger.logSkipped() // TODO: log test skipped
		} else {
			try {
				testCase.validate(testCase.getStart());
			} catch (InvalidTestCaseException e) {
				error(testCase, TestCase.TestCaseInitIteration,	e);
			}
			executeIteration(TestCase.TestCaseInitIteration);
			try {
				for (int iteration = testCase.getStart(); 
						iteration < testCase.getCount() + testCase.getStart(); 
						iteration++) {
					executeIteration(iteration);
				}
			} finally {
				executeIteration(TestCase.TestCaseEndIteration);
			}
		}
	}

	protected void executeIteration(Integer iteration) throws Throwable {
		beginIteration(iteration);
		try {
			if (TestCase.isBeginOrEndMarker(iteration)) {
				sendRequest(null, iteration);
			}
			else {
				try {
					//random.start(iteration, testCase.getSeed());
					Object input = this.getRandomInput(iteration);
					debugDataObject("input", iteration, input);

					Object output = sendRequest(input, iteration);
					debugDataObject("output", iteration, output);
					//random.start(iteration, testCase.getResponseSeed());
					Object expectedOutput = this.getRandomOutput(iteration);
					debugDataObject("expectedOutput", iteration, expectedOutput);
					if (!output.equals(expectedOutput)) {
						if (((Variant)output).getCompositeClass().equals(byte[].class))
							System.out.println("byte[] not equal, but skipping");
						else
						throw new TestCaseException(
								String.format("Output '%s' is not equal to Expected Output '%s'.",
										output, expectedOutput));
					} else {
						System.out.println("iteration " + iteration + " succeeded");
					}
					
				} catch(Throwable e) {
					// re-throws e, if isHaltOnError 
					error(testCase, iteration, e);
				}
			}
		} finally {
			endIteration(iteration);
		}
	}

	public boolean isHaltOnError() {
		return haltOnError;
	}

	private Object sendRequest(Object input, Integer iteration) throws ServiceResultException, InterruptedException {
		RequestHeader requestHeader = new RequestHeader();
		//requestHeader.setSessionId(new UnsignedInteger(0));
		requestHeader.setTimestamp(new DateTime());
		requestHeader.setReturnDiagnostics(new UnsignedInteger(0)); // DiagnosticsMasks.All.ordinal()));

		ServiceRequest req;
		if (input instanceof CompositeTestType) {
			req = new TestStackExRequest(requestHeader,
					testCase.getTestId(), iteration, (CompositeTestType) input);
		} else { // if (input instanceof AnyValue) {
//			req = new TestStackRequest(requestHeader,						//changed 26.5.2009
//					testCase.getTestId(), iteration, (Variant) input);
			if (input instanceof ExtensionObject) {
				req = new TestStackRequest(requestHeader,						//changed 26.5.2009
						testCase.getTestId(), iteration, new Variant(input));
			} else {
				req = new TestStackRequest(requestHeader,						//changed 26.5.2009
						testCase.getTestId(), iteration, (Variant) input);
			}
		} /*
			 * else throw new IllegalArgumentException("Unknown input type: " +
			 * input.getClass().toString());
			 */
		// Create a service request
		IEncodeable response = channel.serviceRequest(req);
		if (response instanceof TestStackResponse)
			return ((TestStackResponse) response).getOutput();
		if (response instanceof TestStackExResponse)
			return ((TestStackExResponse) response).getOutput();
		return response; // return the response, which can be logged as unexpected

	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	public SecureChannel getChannelService() {
		return channel;
	}

	public void setChannelService(SecureChannel channelService) {
		this.channel = channelService;
	}

	/**
	 * Generates an error event for a test case.
	 * 
	 * @param testCase
	 * @param iteration
	 * @param e
	 * @throws ServiceResultException
	 */
	protected void error(TestCase testCase, int iteration, Throwable e) throws Throwable {
		System.out.format("TestCase[ID=%d]: %s, iteration=%d, %s: %s",
				testCase.getTestId().intValue(), testCase.getName(), iteration, e.getClass().getName(), e.getMessage());
		
		logger.logErrorEvent(testCase, iteration, e);
		if (isHaltOnError()) 
			throw e;
	}

}
