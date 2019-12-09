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

import org.opcfoundation.ua.core.TestServiceSetHandler;
import org.opcfoundation.ua.stacktest.*;
import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;

/**
 * Base class for client side test case executors.
 * 
 * The class is used as the default executor for all tests. If the test cases
 * require special execution logic you can inherit sub classes for those tests.
 * 
 * The executors to use for different test casesare defined in
 * TestClient.initExecutorMap
 * 
 * @author jouni.aro@prosys.fi
 * 
 */
public class TestCaseExecutor extends
		org.opcfoundation.ua.stacktest.TestCaseExecutor {

	private TestServiceSetHandler server;

	/**
	 * Initializes the TestCaseExecutor
	 */
	public TestCaseExecutor(TestCase testCase, PseudoRandom random,
			Logger logger) {
		super(testCase, random, logger);
	}

	/**
	 * Prepares a new test iteration for the testCase.
	 * 
	 * @param iteration
	 */
	protected void beginIteration(Integer iteration) {
		super.beginIteration(iteration);
	}

	/**
	 * Completes a test iteration for testCase.
	 * 
	 * @param iteration
	 */
	protected void endIteration(Integer iteration) {
		super.endIteration(iteration);
	}

	/**
	 * Executes one complete test iteration.
	 * 
	 * @param iteration
	 * @param input
	 * @return
	 * @throws TestCaseException
	 */
	public Object execute(Integer iteration, Object input)
			throws TestCaseException {
		beginIteration(iteration);
		try {
			initStack(iteration);

			return executeIteration(iteration, input);
		} finally {
			endIteration(iteration);
		}
	}

	/**
	 * Executes the test for the iteration between beginIteration &
	 * endIteration.
	 * 
	 * @param iteration
	 */
	protected Object executeIteration(int iteration, Object input)
			throws TestCaseException {
		if (TestCase.isBeginOrEndMarker(iteration))
			return null;

		debugDataObject("input", iteration, input);
		// Compare the received input to expected input
		Object expectedInput = getRandomInput(iteration);
		debugDataObject("expectedInput", iteration, expectedInput);

		if (!input.equals(expectedInput)) {
			String inStr = String.valueOf(input);
			
			throw new TestCaseException(String.format(
					"Input '%s'%sis not equal to Expected Input '%s'.", 
					inStr,
					inStr.length() > 80 ? "\n" : " ", // Break long message to two lines
					expectedInput
					));
		}
		final Object output = getRandomOutput(iteration);
		debugDataObject("output", iteration, output);
		return output;
	}

	public TestServiceSetHandler getServer() {
		return server;
	}

	/**
	 * @param iteration
	 */
	@Override
	protected void initStack(int iteration) {
		// TODO: Stack initialization
		/*
		 * IStackTest stack = GetStack();
		 * 
		 * if (isSetupStep && stack != null) { if (iteration ==
		 * TestCases.TestCaseInitIteration) {
		 * stack.SetEventFilter(StackEventFilter.None); // Discard remaining
		 * stack events stack.DequeueEvents(); } else if (iteration ==
		 * TestCases.TestCaseEndIteration) { // Nothing to do here } }
		 */}

	public void setServer(TestServiceSetHandler server) {
		this.server = server;
	}
}
