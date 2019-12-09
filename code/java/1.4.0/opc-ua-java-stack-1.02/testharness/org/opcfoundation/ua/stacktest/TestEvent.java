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
import org.opcfoundation.ua.stacktest.StackEvent;


/**
 * Stack test event. The events are logged into TestLog.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestEvent {
	
	public TestEvent() {
		super();
		timestamp = new Date();
	}
	public TestEvent(TestCase testCase, Integer Iteration, TestEventType EventType) {
		super();
		timestamp = new Date();
        testId = testCase.getTestId();
        iteration = Iteration;
		eventType = EventType;
	}
	public TestEvent(UnsignedInteger TestId, Integer Iteration, TestEventType EventType) {
		super();
		timestamp = new Date();
        testId = TestId;
        iteration = Iteration;
		eventType = EventType;
	}
	public enum TestEventType {
	    NotValidated, /// Generated when the test case could not be validated.
	    Started, /// Generated when the test case is validated and processing has started.
	    Failed, /// Generated when the test case failed.
	    Completed, /// Generated when the test case completes successfully.
	    StackEvents /// Stack events
	}

    private UnsignedInteger testId; /// The id of the test case that generated the event.
    private Integer iteration; /// The iteration within the test case that generated the event.
    private Date timestamp; /// The UTC time indicating when the event occurred.
    private TestEventType eventType; /// The type of event.
    private String details; /// Free form text describing the event.
    private ArrayList<TestParameter> results; ///  Additional results that defined for specific test cases.
    private ArrayList<StackEvent> stackEvents; /// Any stack events that occurred.
	
	public ArrayList<TestParameter> getResults() {
		return results;
	}
	public void addResult(TestParameter result) {
		results.add(result);
	}
	public ArrayList<StackEvent> getStackEvents() {
		return stackEvents;
	}
	public void setStackEvents(ArrayList<StackEvent> stackEvents) {
		this.stackEvents = stackEvents;
	}
	public void addStackEvent(StackEvent stackEvent) {
		stackEvents.add(stackEvent);
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public TestEventType getEventType() {
		return eventType;
	}
	public void setEventType(TestEventType eventType) {
		this.eventType = eventType;
	}
	public Integer getIteration() {
		return iteration;
	}
	public void setIteration(Integer iteration) {
		this.iteration = iteration;
	}
	public UnsignedInteger getTestId() {
		return testId;
	}
	public void setTestId(UnsignedInteger testId) {
		this.testId = testId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
