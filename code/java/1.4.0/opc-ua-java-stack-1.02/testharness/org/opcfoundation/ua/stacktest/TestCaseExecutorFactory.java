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

import java.lang.reflect.Constructor; 
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.stacktest.exception.TestCaseException;
import org.opcfoundation.ua.stacktest.io.Logger;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;

/**
 * Creates a TestCaseExecutor instance for a given TestCase.
 * 
 * Known problem: the has table does not seem to handle classes very well, i.e.
 * it does not find correct matches using the class references. It might be best
 * to store the hash keys using the class name strings, instead of the classes.
 * 
 * @author jouni.aro@prosys.fi
 */
public class TestCaseExecutorFactory {
	protected Map<Class<? extends TestCase>, Class<? extends TestCaseExecutor>> executorMap;

	public TestCaseExecutorFactory() {
		executorMap = new HashMap<Class<? extends TestCase>, Class<? extends TestCaseExecutor>>();
	}

	public TestCaseExecutor createExecutor(TestCase testCase, PseudoRandom random, Logger logger)
			throws TestCaseException {
		if (!executorMap.containsKey(testCase.getClass())) {
			throw new TestCaseException("No executor for TestCase: "
					+ testCase.getName());
		}
		try {
			Class<? extends TestCaseExecutor> c = executorMap.get(testCase.getClass());
            Constructor<? extends TestCaseExecutor> constructor = c.getConstructor(TestCase.class, PseudoRandom.class, Logger.class);
			return (TestCaseExecutor) constructor.newInstance(testCase, random, logger);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TestCaseException(
					String.format("Failed to create executor for test case '%s'", testCase.getName()), 
					e);
		}
	}

	public void registerExecutor(Class<? extends TestCase> testCase,
			Class<? extends TestCaseExecutor> executor) {
		if (!executorMap.containsKey(testCase))
			executorMap.put(testCase, executor);
		/*
		 * else throw new TestCaseException("Executor already defined for
		 * TestCase: " + testCase.getName());
		 */
	}
}
