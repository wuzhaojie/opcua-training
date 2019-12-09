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

package org.opcfoundation.ua.stacktest.io;

import java.util.ArrayList;
import java.util.EnumSet;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.stacktest.StackEvent;
import org.opcfoundation.ua.stacktest.TestCase;
import org.opcfoundation.ua.stacktest.TestEvent;
import org.opcfoundation.ua.stacktest.TestLog;

/**
 * Logger is used for creating TestLog and writing it to TestLog.xml
 * using TestLogWriter.
 * 
 * Copy of the .NET counter-part.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class Logger {
	// / <summary>
	// / Bits that indicate what level of detail to include in the logs.
	// / </summary>
	public enum TestLogDetailMasks {
		// / <summary>
		// / Log all errors.
		// / </summary>
		Errors(0x01),

		// / <summary>
		// / Log an event when starting the first iteration for a test case.
		// / </summary>
		FirstStart(0x02),

		// / <summary>
		// / Log an event when starting the all iterations for a test case.
		// / </summary>
		AllsStarts(0x04),

		// / <summary>
		// / Log an event after completing the last iteration for a test case.
		// / </summary>
		LastEnd(0x08),

		// / <summary>
		// / Log an event after completing each iterations for a test case.
		// / </summary>
		AllsEnds(0x10),

		// / <summary>
		// / Log first 24 bytes of random data used to create the
		// request/response data.
		// / </summary>
		RandomData(0x20);

		private int value;

		TestLogDetailMasks(int value) {
			this.value = value;
		}

		/**
		 * @return the value
		 */
		public int getValue() {
			return value;
		}
	}

	// File path for log.
	private String filePath;

	// Test log detail level.
	private EnumSet<TestLogDetailMasks> detailLevel = EnumSet
			.noneOf(TestLogDetailMasks.class);

	// The TestLog
	private TestLog testLog;

	// Object of type TestLogEventHandler.
	// private event TestLogEventHandler m_TestLogEvent;

	// / <summary>
	// / The default constructor.
	// / </summary>
	// / <param name="filepath">File path for the logger.</param>
	// / <param name="detailLevel">Log detail level.<see
	// cref="TestLogDetailMasks"/></param>
	public Logger(String filePath, long detailLevelMasks) {
		this.filePath = filePath;
		setDetailLevelFromLongValue(detailLevelMasks);
	}

	public void setDetailLevelFromLongValue(long detailLevelMasks) {
		detailLevel.clear();
		if ((detailLevelMasks & 0x01) != 0)
			detailLevel.add(TestLogDetailMasks.Errors);
		if ((detailLevelMasks & 0x02) != 0)
			detailLevel.add(TestLogDetailMasks.FirstStart);
		if ((detailLevelMasks & 0x04) != 0)
			detailLevel.add(TestLogDetailMasks.AllsStarts);
		if ((detailLevelMasks & 0x08) != 0)
			detailLevel.add(TestLogDetailMasks.LastEnd);
		if ((detailLevelMasks & 0x10) != 0)
			detailLevel.add(TestLogDetailMasks.AllsEnds);
		if ((detailLevelMasks & 0x20) != 0)
			detailLevel.add(TestLogDetailMasks.RandomData);
	}

	// / <summary>
	// / Opens the log file with the settings.
	// / </summary>
	// / <param name="secureChannelId">Channel Id.</param>
	// / <param name="testFilePath">Test case file path</param>
	// / <param name="randomFilePath">Random number generator file path.</param>
	public void open(int secureChannelId, String testFilePath,
			String randomFilePath) {
		if (testLog == null) {
			testLog = new TestLog();

			testLog.setSecureChannelId(secureChannelId);
			testLog.setTestCaseFile(testFilePath);
			testLog.setRandomDataFile(randomFilePath);
		}
		/*
		 * m_logger.WriteStartElement("TestLog", Namespaces.OpcUaTest);
		 * m_logger.WriteAttributeString("xmlns:xsi",
		 * "http://www.w3.org/2001/XMLSchema-instance");
		 * 
		 * m_logger.WriteElementString("ProcessName", Namespaces.OpcUaTest,
		 * Process.GetCurrentProcess().MainModule.ModuleName);
		 * m_logger.WriteElementString("SecureChannelId", Namespaces.OpcUaTest,
		 * secureChannelId); m_logger.WriteElementString("TestCaseFile",
		 * Namespaces.OpcUaTest, new FileInfo(testFilePath).FullName);
		 * m_logger.WriteElementString("RandomDataFile", Namespaces.OpcUaTest,
		 * new FileInfo(randomFilePath).FullName);
		 * m_logger.WriteElementString("CreateTime", Namespaces.OpcUaTest,
		 * XmlConvert.ToString(DateTime.UtcNow,
		 * XmlDateTimeSerializationMode.Utc));
		 */}

	// / <summary>
	// / Writes the log and closes it. The log must be open'd before it can be
	// used again.
	// / </summary>
	public void close() {
		if (testLog != null) {
			TestLogWriter logWriter = new TestLogWriter();
			logWriter.write(testLog, filePath);
			testLog = null;
		}
	}

	// / <summary>
	// / Logs an event when a test case starts.
	// / </summary>
	// / <param name="testCase">This parameter stores the test case related
	// data.</param>
	// / <param name="iteration">This parameter stores the current iteration
	// number.</param>
	public void logStartEvent(TestCase testCase, int iteration) {
		// the start event can be logged each iteration or once per test case.
		if (detailLevel.contains(TestLogDetailMasks.AllsStarts)
				&& (detailLevel.contains(TestLogDetailMasks.FirstStart) && (!TestCase
						.isBeginOrEndMarker(iteration)))) {
			TestEvent events = new TestEvent(testCase, iteration,
					TestEvent.TestEventType.Started);
			logEvent(events);
		}
	}

	// / <summary>
	// / Logs an event when a test case completes sucessfully.
	// / </summary>
	// / <param name="testCase">This parameter stores the test case related
	// data.</param>
	// / <param name="iteration">This parameter stores the current iteration
	// number.</param>
	public void logCompleteEvent(TestCase testCase, int iteration) {
		// the complete event can be logged each iteration or once per test
		// case.
		if (detailLevel.contains(TestLogDetailMasks.AllsEnds)
				&& ((detailLevel.contains(TestLogDetailMasks.LastEnd) && (!TestCase
						.isBeginOrEndMarker(iteration))))) {
			TestEvent events = new TestEvent(testCase, iteration,
					TestEvent.TestEventType.Completed);
			logEvent(events);
		}
	}

	// / <summary>
	// / Logs an event when a test case cannot be validated.
	// / </summary>
	// / <param name="testId">Test case Id.</param>
	// / <param name="iteration">This parameter stores the current iteration
	// number.</param>
	// / <param name="e">Exception to be logged.</param>
	public void logNotValidatedEvent(UnsignedInteger testId, int iteration,
			Exception e) {
		if (detailLevel.contains(TestLogDetailMasks.Errors)) {
			TestEvent events = new TestEvent(testId, iteration,
					TestEvent.TestEventType.NotValidated);
			events.setDetails(e.toString());
			logEvent(events);
		}
	}

	// / <summary>
	// / Logs an event when an error occurs during a test case.
	// / </summary>
	// / <param name="testCase">This parameter stores the test case related
	// data.</param>
	// / <param name="iteration">This parameter stores the current iteration
	// number.</param>
	// / <param name="e">Exception to be logged.</param>
	public void logErrorEvent(TestCase testCase, int iteration, Throwable e) {
		if (detailLevel.contains(TestLogDetailMasks.Errors)) {
			TestEvent events = new TestEvent(testCase, iteration,
					TestEvent.TestEventType.Failed);
			events.setDetails(e.toString());
			logEvent(events);
		}
	}

	// / <summary>
	// / Logs an event to the log.
	// / </summary>
	// / <param name="events">Test event.</param>
	public void logEvent(TestEvent events) {
		synchronized (testLog) {
			if (!detailLevel.isEmpty() && (testLog != null)) {

				testLog.addTestEvent(events);

				/*
				 * if (m_TestLogEvent != null && events.EventType !=
				 * TestEventType.StackEvents) { m_TestLogEvent(this, events); }
				 */}
		}
	}

	// / <summary>
	// / Logs stack events of an iteration
	// / </summary>
	// / <param name="stackEvents">List of stack events.</param>
	// / <param name="testCase">This parameter stores the test case related
	// data.</param>
	// / <param name="iteration">This parameter stores the current iteration
	// number.</param>
	public void logStackEvents(ArrayList<StackEvent> stackEvents,
			TestCase testCase, int iteration) {
		if ((stackEvents != null) && (!stackEvents.isEmpty())
				&& (!TestCase.isBeginOrEndMarker(iteration))) {
			TestEvent testEvent = new TestEvent(testCase, iteration,
					TestEvent.TestEventType.StackEvents);
			testEvent.setStackEvents(stackEvents);
			logEvent(testEvent);
		}
	}

	// / <summary>
	// / This event notifies to the caller that application is logging the test
	// event.
	// / </summary>
	/*
	 * public event TestLogEventHandler TestLogEvent { add { lock (m_lock) {
	 * m_TestLogEvent += value; } }
	 * 
	 * remove { lock (m_lock) { m_TestLogEvent -= value; ; } } }
	 */
}
