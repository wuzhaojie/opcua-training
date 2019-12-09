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
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.stacktest.exception.*;
import org.opcfoundation.ua.stacktest.random.PseudoRandom;


/**
 * TestCase class holds the configuration of one test case for the Stack Test client and server.
 * 
 * The TestCase:s are read into a TestSequence from the TestCases.xml
 * 
 * TestCase is the base class for all tests. The actual test case types are defined by  
 * the defined in org.opcfoundation.ua.stacktest.testcases
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestCase {

	/// <summary>
	/// Basic message exchange with array values.
	/// </summary>
	public static final String ArrayValues = "ArrayValues";

	/// <summary>
	/// Protocol test: AutoReconnect LongRequests
	/// </summary>
	public static final String AutoReconnectLongRequests = "AutoReconnectLongRequests";

	/// <summary>
	/// Protocol test: AutoReconnect ShortRequests
	/// </summary>
	public static final String AutoReconnectShortRequests = "AutoReconnectShortRequests";

	/// <summary>
	/// Autorecoonection test case test invocation interval
	/// </summary>
	public static final String AutoReconnectTestInterval = "AutoReconnectTestInterval";

	/// <summary>
	/// Basic message exchange with all Built-In types.
	/// </summary>
	public static final String BuiltInTypes = "BuiltInTypes";

	/// <summary>
	/// Number of channels for the multiple channel test case.
	/// </summary>
	public static final String ChannelsPerServer = "ChannelsPerServer";

	/// <summary>
	/// Basic message exchange with Extension Object values.
	/// </summary>
	public static final String ExtensionObjectValues = "ExtensionObjectValues";

	/// <summary>
	/// Auto reconnection long requests delay time
	/// </summary>
	public static final String LongRequestsDealy = "LongRequestsDealy";

	/// <summary>
	/// Maximum depth of the nesting
	/// </summary>
	public static final String MaxDepth = "MaxDepth";

	/// <summary>
	/// Max response delay
	/// </summary>
	public static final String MaxResponseDelay = "MaxResponseDelay";

	/// <summary>
	/// Maximum length of the a String
	/// </summary>
	public static final String MaxStringLength = "MaxStringLength";

	/// <summary>
	/// Maximum timeout value
	/// </summary>
	public static final String MaxTimeout = "MaxTimeout";

	/// <summary>
	/// Maximum service execution
	/// </summary>
	public static final String MaxTransportDelay = "MaxServiceExecutionTime";

	/// <summary>
	/// Minimum timeout value
	/// </summary>
	public static final String MinTimeout = "MinTimeout";

	/// <summary>
	/// Protocol test: Multiple Channels Test
	/// </summary>
	public static final String MultipleChannels = "MultipleChannels";

	/// <summary>
	/// Protocol test: Out of order processing
	/// </summary>
	public static final String OutOfOrderProcessing = "OutOfOrderProcessing";

	/// <summary>
	/// Protocol test: Small buffer size - Extension Object
	/// </summary>
	public static final String ProtocolExtensionObject = "ProtocolExtensionObject";

	/// <summary>
	/// Protocol test: Small buffer size - Extension Object - No precalculated size
	/// </summary>
	public static final String ProtocolExtensionObjectNoSize = "ProtocolExtensionObjectNoSize";

	/// <summary>
	/// Protocol test: Small buffer size - Simple data
	/// </summary>
	public static final String ProtocolSimpleData = "ProtocolSimpleData";

	/// <summary>
	/// Response delay interval
	/// </summary>
	public static final String ResponseDelayInterval = "ResponseDelayInterval";

	/// <summary>
	/// Basic message exchange with scalar values.
	/// </summary>
	public static final String ScalarValues = "ScalarValues";

	/// <summary>
	/// Server details for the multiple channel test case.
	/// </summary>
	public static final String ServerDetails = "ServerDetails";

	/// <summary>
	/// Basic message exchange with Server Fault.
	/// </summary>
	public static final String ServerFault = "ServerFault";

	/// <summary>
	/// Basic message exchange with Server Fault.
	/// </summary>
	public static final String ServerTimeout = "ServerTimeout";

	/// <summary>
	/// Frequency of stack actions
	/// </summary>
	public static final String StackActionFrequency = "StackActionFrequency";

	/// <summary>
	/// Iteration that is used to windup a test case
	/// </summary>
	public static final int TestCaseEndIteration = Integer.MAX_VALUE;

	/// <summary>
	/// Iteration that is used to initialize a test case
	/// </summary>
	public static final int TestCaseInitIteration = -1;

	public static final boolean isBeginMarker(int iteration) {
		return (iteration == TestCaseInitIteration);
	}

	public static final boolean isBeginOrEndMarker(int iteration) {
		return (isBeginMarker(iteration) || isEndMarker(iteration));
	}
	
	public static final boolean isEndMarker(int iteration) {
		return (iteration == TestCaseEndIteration);
	}

	private int count; 
	private String name;
	private List<TestParameter> parameters = new ArrayList<TestParameter>();
	private int responseSeed;
	private int seed;
	private boolean skipTest;
	private int start;
	private UnsignedInteger testId = new UnsignedInteger(0);

	public TestCase() {
		super();
	}

	public void addParameter(TestParameter value) {
		parameters.add(value);
	}

	public void copy(TestCase testCase) {
		name = testCase.name;
		count = testCase.count;
		responseSeed = testCase.responseSeed;
		seed = testCase.seed;
		skipTest = testCase.skipTest;
		start = testCase.start;
		testId = testCase.testId;
		parameters = testCase.parameters;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TestCase other = (TestCase) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public int getCount() {
		return count;
	}
	
	public String getName() {
		return name;
	}

	public TestParameter getParam(String paramName) {
		for (TestParameter i: parameters) {
			if (i.getName().equals(paramName))
				return i;
		}
		return new TestParameter();
	}

	public List<TestParameter> getParameters() {
		return parameters;
	}

	public int getResponseSeed() {
		return responseSeed;
	}

	public int getSeed() {
		return seed;
	}

	public int getStart() {
		return start;
	}

	public UnsignedInteger getTestId() {
		return testId;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean isSkipTest() {
		return skipTest;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setResponseSeed(int responseSeed) {
		this.responseSeed = responseSeed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public void setSkipTest(boolean skipTest) {
		this.skipTest = skipTest;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setTestId(UnsignedInteger testId) {
		this.testId = testId;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("TestCase[ID=" + testId.toString() + "]: Name=" + name + ",");
		s.append("Count=" + count + ",");
		s.append("Seed=" + seed + ",");
		s.append("ResponseSeed=" + responseSeed + ",");
		s.append("Start=" + start + ",");
		s.append("Parameters:");
		ListIterator<TestParameter> i = parameters.listIterator();
		while (i.hasNext()) {
			s.append(i.next() + ";");
		}
		return s.toString();
	}

	/**
	 * Validates the test case parameters.
	 * @param iteration The iteration 
	 * @throws InvalidTestCaseException
	 */
	public void validate(int iteration) throws InvalidTestCaseException{
        if (name.length() == 0)
            throw new InvalidTestCaseException("Invalid test case name");
        if (start < 0)
            throw new InvalidTestCaseException("Start value is less than 0 for test case.");
        if (count < 0)
            throw new InvalidTestCaseException("Iteration value is less than 0 for test case.");
        if (iteration < start)
            throw new InvalidTestCaseException("Iteration is less than start for test case.");
        if (iteration >= start+count)
            throw new InvalidTestCaseException("Iteration is greater than count for test case.");
	}

	/**
	 * Generate a random input or output for the iteration and received input.
	 * 
	 * Input type determines the type of (expected) input to generate.
	 * 
	 * @param iteration Test iteration
	 * @param input Received input
	 * @return Randomly generated AnyValue or CompositeType
	 * @throws TestCaseException 
	 */
	public Object getRandomData(int iteration, PseudoRandom random) {
		return new Variant(null);
	}
}
