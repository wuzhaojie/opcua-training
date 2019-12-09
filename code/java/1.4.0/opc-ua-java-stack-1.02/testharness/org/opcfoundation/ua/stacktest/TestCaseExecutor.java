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

package org.opcfoundation.ua.stacktest;

import org.opcfoundation.ua.stacktest.exception.RandomException;
import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;

/**
 * Base class for both client and server side test execution logic.
 * 
 * The actual executer classes are defined in 
 * org.opcfoundation.ua.stacktest.server.TextCaseExecutor and
 * org.opcfoundation.ua.stacktest.client.TextCaseExecutor
 * 
 * @author jouni.aro@prosys.fi
 */
public abstract class TestCaseExecutor  {

	protected TestCase testCase;
	protected Logger logger;
	protected PseudoRandom random;
	
	/**
	 * Initializes the TestCaseExecutor
	 */
	public TestCaseExecutor(TestCase testCase, PseudoRandom random, Logger logger) {
		super();
		this.testCase = testCase;
		this.logger = logger;
		this.random = random;
	}

	/**
	 * Prepares a new test iteration for the testCase.
	 * @param iteration
	 */
	protected void beginIteration(Integer iteration) {
		if (!TestCase.isBeginOrEndMarker(iteration))
			logger.logStartEvent(testCase, iteration);
	}
	
	/**
	 * Completes a test iteration for testCase.
	 * @param iteration
	 */
	protected void endIteration(Integer iteration) {
		if (!TestCase.isBeginOrEndMarker(iteration))
			logger.logCompleteEvent(testCase, iteration);
	}
	
	/**
	 * @param iteration
	 */
	protected void initStack(int iteration) {
		//TODO: Stack initialization
/*		IStackTest stack = GetStack();

		if (isSetupStep && stack != null)
		{
			if (iteration == TestCases.TestCaseInitIteration)
			{
				stack.SetEventFilter(StackEventFilter.None);

				// Discard remaining stack events
				stack.DequeueEvents();
			}
			else if (iteration == TestCases.TestCaseEndIteration)
			{
				// Nothing to do here
			}
		}
*/	}

	public void startRandomResponse(Integer iteration) throws TestCaseException {
		try {
			random.start(iteration, testCase.getResponseSeed());
		} catch (RandomException e) {
			e.printStackTrace();
			throw new TestCaseException("Error in startRandom: " + e.getMessage());
		}
	}

	/**
	 * Generate a random input or output for the iteration and received input.
	 * 
	 * Input type determines the type of (expected) input to generate.
	 * 
	 * @param iteration Test iteration
	 * @return Randomly generated AnyValue or CompositeType
	 * @throws TestCaseException 
	 */
	protected Object getRandomOutput(int iteration) throws TestCaseException {
		startRandomResponse(iteration);
		return testCase.getRandomData(iteration, random);
	}

	/**
	 * Log the data object (input or outputm, actual or expected) on the console.
	 * @param name
	 * @param iteration
	 * @param data
	 */
	protected void debugDataObject(String name, int iteration, Object data) {
		String dataStr = "(null)";
		if (data != null)
			dataStr = data.toString();
		System.out.format("TestCase[ID=%d]: %s, iteration=%d, %s=%s%n",
				testCase.getTestId().intValue(), testCase.getName(), iteration, name, dataStr);
	}

	/**
	 * Generate a random input or output for the iteration and received input.
	 * 
	 * Input type determines the type of (expected) input to generate.
	 * 
	 * @param iteration Test iteration
	 * @return Randomly generated AnyValue or CompositeType
	 * @throws TestCaseException 
	 */
	protected Object getRandomInput(int iteration) throws TestCaseException {
		startRandom(iteration);
		return testCase.getRandomData(iteration, random);
	}

	public void startRandom(Integer iteration) throws TestCaseException {
		try {
			random.start(iteration, testCase.getSeed());
		} catch (RandomException e) {
			e.printStackTrace();
			throw new TestCaseException("Error in startRandom: " + e.getMessage());
		}
	}
}
