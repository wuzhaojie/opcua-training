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

import java.util.*;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;

/**
 * The sequence of tests to run in the stack test.
 * 
 * The test sequence is a list of TestCase:s.
 * 
 * The sequence is read in from TestCases.xml using 
 * org.opcfoundation.ua.stacktest.io.TestSequenceReader
 * 
 * After reading in it, the reader will convert the 
 * contained TestCase objects to the actual sub classes
 * using convertToDerivedTestCases.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestSequence {
	private boolean haltOnError;

	private int logDetailLevel = 0xffffffff;

	private int randomDataStepSize = 7;

	private List<TestCase> testCases;

	public TestSequence() {
		super();
		testCases = new ArrayList<TestCase>();
	}

	/**
	 * Converts the TestCase objects to the derived classes. The derived classes
	 * are searched using the name TestCase.name + "Test", e.g.
	 * name=ScalarValues -> ScalarValuesTest
	 */
	public TestSequence convertToDerivedTestCases() {
		TestSequence newSequence = new TestSequence();
		newSequence.haltOnError = this.haltOnError;
		newSequence.logDetailLevel = this.logDetailLevel;
		newSequence.randomDataStepSize = this.randomDataStepSize;
		
		// List<TestCase> newTestCases = new ArrayList<TestCase>();
		int testId = 1;
		for (TestCase testCase : testCases) {
			if (!testCase.isSkipTest())
				try {
					String testCaseClassName = TestCase.class
							.getCanonicalName().replace(
									".TestCase",
									".testcases." + testCase.getName()
											+ "TestCase");
					Class testCaseClass = Class.forName(testCaseClassName);
					TestCase newTestCase = (TestCase) testCaseClass
							.newInstance();
					newTestCase.copy(testCase);
					if (testCase.getTestId().intValue() == 0)
						newTestCase.setTestId(new UnsignedInteger(testId++));
					// newTestCases.add(newTestCase);
					newSequence.addTestCase(newTestCase);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		// testCases = newTestCases;
		return newSequence;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void addTestCase(TestCase value) {
		this.testCases.add(value);
	}

	public boolean isHaltOnError() {
		return haltOnError;
	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	public long getLogDetailLevel() {
		return logDetailLevel;
	}

	public void setLogDetailLevel(int logDetailLevel) {
		this.logDetailLevel = logDetailLevel;
	}

	public int getRandomDataStepSize() {
		return randomDataStepSize;
	}

	public void setRandomDataStepSize(int randomDataStepSize) {
		this.randomDataStepSize = randomDataStepSize;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		Formatter formatter = new Formatter(s, Locale.US);
		formatter.format("TestSequence: HaltOnError=%b, RandomDataStepSize=%d, LogDetailLevel=0x%x%n",
				haltOnError, randomDataStepSize, logDetailLevel);
		ListIterator i = testCases.listIterator();
		while (i.hasNext()) {
			formatter.format("%s%n", i.next());
		}
		return s.toString();
	}
}
