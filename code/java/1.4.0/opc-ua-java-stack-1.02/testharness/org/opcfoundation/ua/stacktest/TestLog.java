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

import org.opcfoundation.ua.stacktest.TestEvent;


/**
 * Log of the stack test results.
 * 
 * The log contains mainly a list of TestEvents.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestLog {
	private String processName; /// The name of the executable that created the log.
    private int secureChannelId; ///  The identifier associated with the secure channel used for the test.
    private String testCaseFile; /// The full path of the test case file used.
    private String randomDataFile; /// The full path of the random data file used.
    private Date createTime; /// When the log was created.
    private ArrayList<TestEvent> testEvents;
	public TestLog() {
		super();
		testEvents = new ArrayList<TestEvent>();
		createTime = new Date();
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public void setCreateGTime(GregorianCalendar createTime) {
		this.createTime = createTime.getTime();
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getRandomDataFile() {
		return randomDataFile;
	}
	public void setRandomDataFile(String randomDataFile) {
		this.randomDataFile = randomDataFile;
	}
	public int getSecureChannelId() {
		return secureChannelId;
	}
	public void setSecureChannelId(int secureChannelId) {
		this.secureChannelId = secureChannelId;
	}
	public String getTestCaseFile() {
		return testCaseFile;
	}
	public void setTestCaseFile(String testCaseFile) {
		this.testCaseFile = testCaseFile;
	}
	public ArrayList<TestEvent> getTestEvents() {
		return testEvents;
	}
	public void addTestEvent(TestEvent testEvent) {
		testEvents.add(testEvent);
	}
	
}
