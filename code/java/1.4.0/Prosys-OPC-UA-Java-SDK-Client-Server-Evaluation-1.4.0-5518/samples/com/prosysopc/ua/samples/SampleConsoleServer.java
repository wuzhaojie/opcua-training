/**
 * Prosys OPC UA Java SDK
 *
 * Copyright (c) 2009-2012 Prosys PMS Ltd., <http://www.prosysopc.com>. 
 * All rights reserved.
 */
package com.prosysopc.ua.samples;
/*
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedLong;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.AggregateConfiguration;
import org.opcfoundation.ua.core.AggregateFilterResult;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.EUInformation;
import org.opcfoundation.ua.core.EventFilter;
import org.opcfoundation.ua.core.EventFilterResult;
import org.opcfoundation.ua.core.HistoryData;
import org.opcfoundation.ua.core.HistoryEvent;
import org.opcfoundation.ua.core.HistoryModifiedData;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.MonitoringFilter;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.NodeAttributes;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.PerformUpdateType;
import org.opcfoundation.ua.core.Range;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.core.UserTokenType;
import org.opcfoundation.ua.core.ViewDescription;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.NumericRange;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.WriteAccess;
import com.prosysopc.ua.nodes.DataChangeListener;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaNodeFactoryException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaObjectType;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.EventManagerListener;
import com.prosysopc.ua.server.HistoryManagerListener;
import com.prosysopc.ua.server.IoManagerListener;
import com.prosysopc.ua.server.MethodManager;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.MonitoredDataItem;
import com.prosysopc.ua.server.MonitoredEventItem;
import com.prosysopc.ua.server.NodeManagerListener;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.Session;
import com.prosysopc.ua.server.Subscription;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.server.UserValidator;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.server.nodes.PlainVariable;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaObjectTypeNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;
import com.prosysopc.ua.server.nodes.opcua.AcknowledgeableConditionType;
import com.prosysopc.ua.server.nodes.opcua.AlarmConditionType;
import com.prosysopc.ua.server.nodes.opcua.AnalogItemType;
import com.prosysopc.ua.server.nodes.opcua.BaseEventType;
import com.prosysopc.ua.server.nodes.opcua.BuildInfoType;
import com.prosysopc.ua.server.nodes.opcua.ConditionType;
import com.prosysopc.ua.server.nodes.opcua.DataItemType;
import com.prosysopc.ua.server.nodes.opcua.EventType;
import com.prosysopc.ua.server.nodes.opcua.ExclusiveLevelAlarmType;
import com.prosysopc.ua.server.nodes.opcua.FolderType;
import com.prosysopc.ua.server.nodes.opcua.ShelvedStateMachineType; */

/**
 * A sample OPC UA server application.
 */
public class SampleConsoleServer {
	public class MyLevelAlarmType extends ExclusiveLevelAlarmType {
		private final DataChangeListener listener = new DataChangeListener() {

			@Override
			public void onDataChange(UaNode uaNode, DataValue prevValue,
					DataValue value) {
				Variant varValue = value == null ? Variant.NULL : value
						.getValue();
				DateTime activeTime = value == null ? null : value
						.getSourceTimestamp();
				if (varValue.isEmpty())
					inactivateAlarm(activeTime);
				else
					checkAlarm(varValue.floatValue(), activeTime);
			}
		};

		public MyLevelAlarmType(NodeManagerUaNode nodeManager, NodeId nodeId,
				QualifiedName browseName, LocalizedText displayName) {
			super(nodeManager, nodeId, browseName, displayName);
		}

		public MyLevelAlarmType(NodeManagerUaNode nodeManager, NodeId nodeId,
				String name, Locale locale) {
			super(nodeManager, nodeId, name, locale);
		}

		@Override
		public void setInput(UaVariable node) {
			if (getInput() instanceof UaVariableNode)
				((UaVariableNode) getInput())
						.removeDataChangeListener(listener);
			super.setInput(node);
			if (node instanceof UaVariableNode)
				((UaVariableNode) node).addDataChangeListener(listener);
		}

		private void triggerAlarm(DateTime activeTime) {
			// Trigger event
			byte[] myEventId = getNextUserEventId();
			triggerEvent(DateTime.currentTime(), activeTime, myEventId);
		}

		/**
		 * Creates an alarm, if it is not active
		 * 
		 * @param activeTime
		 */
		protected void activateAlarm(int severity, DateTime activeTime) {
			// Note: UaServer does not yet send any event notifications!
			if (isEnabled()
					&& (!isActive() || (getSeverity().getValue() != severity))) {
				println("activateAlarm: severity=" + severity);
				setActive(true);
				setRetain(true);
				setAcked(false); // Also sets confirmed to false
				setSeverity(severity);

				triggerAlarm(activeTime);

			}
		}

		protected void checkAlarm(float nextValue, DateTime activeTime) {
			if (nextValue > getHighHighLimit())
				activateAlarm(700, activeTime);
			else if (nextValue > getHighLimit())
				activateAlarm(500, activeTime);
			else if (nextValue < getLowLowLimit())
				activateAlarm(700, activeTime);
			else if (nextValue < getLowLimit())
				activateAlarm(500, activeTime);
			else
				inactivateAlarm(activeTime);
		}

		protected void inactivateAlarm(DateTime activeTime) {
			if (isEnabled() && isActive()) {
				println("inactivateAlarm");
				setActive(false);
				setRetain(!isAcked());
				triggerAlarm(activeTime);
			}
		}

	}

	enum Action {
		ADD_NODE('a', "add a new node"), CLOSE('x', "close the server"), DELETE_NODE(
				'd', "delete a node"), ENABLE_DIAGNOSTICS('D',
				"enable/disable server diagnostics"), SEND_EVENT('e',
				"send an event");

		static Map<Character, Action> actionMap = new TreeMap<Character, Action>();
		static {
			for (Action a : Action.values())
				actionMap.put(a.getKey(), a);
		}

		public static Action parseAction(Character s) {
			return actionMap.get(s);
		}

		private final String description;
		private final Character key;

		Action(Character key, String description) {
			this.key = key;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		/**
		 * @return the key
		 */
		public Character getKey() {
			return key;
		}
	}

	/**
	 * A sample class for keeping a history of a variable node.
	 */
	static class ValueHistory {
		private int capacity = 10000;
		private final DataChangeListener listener = new DataChangeListener() {

			@Override
			public void onDataChange(UaNode uaNode, DataValue prevValue,
					DataValue value) {
				values.add(value);
				while (values.size() > capacity)
					values.remove(0);
			}
		};
		private final List<DataValue> values = new CopyOnWriteArrayList<DataValue>();
		private final UaVariable variable;

		public ValueHistory(UaVariableNode variable) {
			super();
			this.variable = variable;
			variable.addDataChangeListener(listener);
		}

		public int getCapacity() {
			return capacity;
		}

		/**
		 * Get the values from the history that are between startTime and
		 * endTime.
		 * 
		 * @param startTime
		 * @param endTime
		 * @param returnBounds
		 * @param maxValues
		 * @return
		 */
		public DataValue[] getHistory(DateTime startTime, DateTime endTime,
				int maxValues, boolean returnBounds) {
			List<DataValue> history = new ArrayList<DataValue>();
			for (DataValue value : values) {
				DateTime t = value.getSourceTimestamp();
				if (t == null)
					t = value.getServerTimestamp();
				final int compareToEnd = t.compareTo(endTime);
				if ((compareToEnd > 0)
						|| (!returnBounds && (compareToEnd == 0)))
					break;
				else {
					final int compareToStart = t.compareTo(startTime);
					if ((compareToStart > 0)
							|| (returnBounds && (compareToStart == 0))) {
						history.add(value);
						if (history.size() == maxValues)
							break;
					}
				}
			}
			return history.toArray(new DataValue[history.size()]);
		}

		/**
		 * @param reqTimes
		 * @return
		 */
		public DataValue[] getHistory(DateTime[] reqTimes) {
			if (reqTimes == null)
				return null;
			// TODO: Interpolated values
			DataValue[] values = new DataValue[reqTimes.length];
			for (int i = 0; i < reqTimes.length; i++) {
				DateTime t = reqTimes[i];
				DataValue v = getValue(t);
				values[i] = new DataValue(v == null ? null : v.getValue(),
						v == null ? new StatusCode(StatusCodes.Bad_NoData) : v
								.getStatusCode(), t, UnsignedShort.ZERO,
						null, null);
			}
			return values;

		}

		/**
		 * @return the variable
		 */
		public UaVariable getVariable() {
			return variable;
		}

		/**
		 * @param capacity
		 *            the capacity to set
		 */
		public void setCapacity(int capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException(
						"capacity must be a positive value");
			this.capacity = capacity;
		}

		/**
		 * Find the value at the given time from the history.
		 * 
		 * @param requestedTime
		 *            the requested time for the value
		 * @return the last value with a smaller or equal timestamp than the
		 *         requestedTime
		 */
		private DataValue getValue(DateTime requestedTime) {
			// a "brute" find starting from the end
			int i = values.size() - 1;
			while ((i >= 0)
					&& (values.get(i).getSourceTimestamp()
							.compareTo(requestedTime) > 0))
				i--;
			return i < 0 ? null : values.get(i);
		}
	}

	private static final String APP_NAME = "SampleConsoleServer";

	/**
	 * Number of nodes to create for the Big Node Manager. This can be modified
	 * from the command line.
	 */
	private static int bigAddressSpaceNodes = 1000;
	private static Logger logger = Logger.getLogger(SampleConsoleServer.class);

	private static boolean stackTraceOnException = false;

	/**
	 * @param args
	 *            command line arguments for the application
	 * @throws StatusException
	 *             if the server address space creation fails
	 * @throws UaServerException
	 *             if the server initialization parameters are invalid
	 * @throws CertificateException
	 *             if the application certificate or private key, cannot be
	 *             loaded from the files due to certificate errors
	 */
	public static void main(String[] args) throws IOException,
			SecureIdentityException, URISyntaxException,
			ServiceResultException, StatusException, UaServerException {
		// Initialize log4j logging
		PropertyConfigurator.configureAndWatch(SampleConsoleServer.class
				.getResource("log.properties").getFile(), 5000);

		try {
			if (!parseCmdLineArgs(args)) {
				usage();
				return;
			}
		} catch (IllegalArgumentException e) {
			println("Invalid cmd line argument: " + e.getMessage());
			usage();
			return;
		}

		// *** Initialization and Start Up
		SampleConsoleServer sampleConsoleServer = new SampleConsoleServer();

		// Initialize the server
		sampleConsoleServer.initialize();

		// Create the address space
		sampleConsoleServer.createAddressSpace();

		// TCP Buffer size parameters - this may help with high traffic
		// situations.
		// See http://fasterdata.es.net/host-tuning/background/ for some hints
		// how to use it
		// UATcpServer.setReceiveBufferSize(700000);

		// Start the server, when you have finished your own initializations
		// This will allow connections from the clients
		// Start up the server (enabling or disabling diagnostics according to
		// the cmd line args)
		sampleConsoleServer.run(getUseDiags(args));
	}

	/**
	 * Parse Command line arguments. Expected options:
	 * <UL>
	 * <LI>-d connect to a discovery server instead of a normal server
	 * <LI>-t show stack trace with exceptions
	 * <LI>-n do not prompt for the server URI, if it is not specified
	 * </UL>
	 * 
	 * Also expects to get the serverUri - if not, it is prompted (unless -n
	 * given)
	 * 
	 * @param args
	 *            the arguments
	 * @return
	 */
	private static boolean parseCmdLineArgs(String[] args)
			throws IllegalArgumentException {
		int i = 0;
		while ((args.length > i)
				&& ((args[i].startsWith("-") || args[i].startsWith("/")))) {
			if (args[i].equals("-t"))
				stackTraceOnException = true;
			else if (args[i].equals("-b"))
				bigAddressSpaceNodes = Integer.parseInt(args[++i]);
			else if (args[i].equals("-?"))
				return false;
			else
				throw new IllegalArgumentException(args[i]);
			i++;
		}
		return true;
	}

	/**
	 * @param e
	 */
	private static void printException(Exception e) {
		if (stackTraceOnException)
			e.printStackTrace();
		else {
			println(e.toString());
			if (e.getCause() != null)
				println("Caused by: " + e.getCause());
		}
	}

	/**
	 * @param string
	 */
	private static void println(String string) {
		System.out.println(string);
	}

	/**
	 * @return
	 */
	private static Action readAction() {
		return Action.parseAction(readInput().charAt(0));
	}

	/**
	 * @return
	 */
	private static String readInput() {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		String s = null;
		do
			try {
				s = stdin.readLine();
			} catch (IOException e) {
				printException(e);
			}
		while ((s == null) || (s.length() == 0));
		return s;
	}

	/**
	 * 
	 */
	private static void usage() {
		println("Usage: " + APP_NAME + " [-b] [-t] [serverUri]");
		println("   -b n       Define number of nodes to create in the BigNodeManager (default=1000)");
		println("   -t         Output stack trace for errors");
		println("   -?         Show this help text");
		println("");
	}

	/**
	 * Check if diagnostics is enabled from the command line
	 * 
	 * @param args
	 * @return
	 */
	protected static boolean getUseDiags(String[] args) {
		for (String arg : args)
			if (arg.equals("-enablesessiondiags"))
				return true;
		return false;
	}

	static void printMenu() {
		println("");
		println("");
		println("");
		System.out
				.println("-------------------------------------------------------");
		for (Entry<Character, Action> a : Action.actionMap.entrySet())
			println("- Enter " + a.getKey() + " to "
					+ a.getValue().getDescription());
	}

	private FolderType analogItemArrayFolder;

	private FolderType analogItemFolder;
	private int complianceNamespaceIndex;

	private NodeManagerUaNode complianceNodeManager;

	private FolderType dataItemFolder;

	private FolderType deepFolder;

	private int eventId = 0;
	private ExclusiveLevelAlarmType myAlarm;
	private MyBigNodeManager myBigNodeManager;
	private UaObject myDevice;

	private final EventManagerListener myEventManagerListener = new EventManagerListener() {

		@Override
		public boolean onAcknowledge(ServiceContext serviceContext,
				AcknowledgeableConditionType condition, byte[] eventId,
				LocalizedText comment) throws StatusException {
			// Handle acknowledge request to a condition event
			println("Acknowledge: Condition=" + condition + "; EventId="
					+ eventIdToString(eventId) + "; Comment=" + comment);
			// If the acknowledged event is no longer active, return an error
			if (!Arrays.equals(eventId, condition.getEventId()))
				throw new StatusException(StatusCodes.Bad_EventIdUnknown);
			if (condition.isAcked())
				throw new StatusException(
						StatusCodes.Bad_ConditionBranchAlreadyAcked);
			// If the condition is no longer active, set retain to false, i.e.
			// remove it from the visible alarms
			if (!(condition instanceof AlarmConditionType)
					|| !((AlarmConditionType) condition).isActive())
				condition.setRetain(false);

			final DateTime now = DateTime.currentTime();
			condition.setAcked(true, now);
			final byte[] userEventId = getNextUserEventId();
			// addComment triggers a new event
			condition.addComment(eventId, comment, now, userEventId);
			return true; // Handled here
			// NOTE: If you do not handle acknowledge here, and return false,
			// the EventManager (or MethodManager) will call
			// condition.acknowledge, which performs the same actions as this
			// handler, except for setting Retain
		}

		@Override
		public boolean onAddComment(ServiceContext serviceContext,
				ConditionType condition, byte[] eventId, LocalizedText comment)
				throws StatusException {
			// Handle add command request to a condition event
			println("AddComment: Condition=" + condition + "; Event="
					+ eventIdToString(eventId) + "; Comment=" + comment);
			// Only the current eventId can get comments
			if (!Arrays.equals(eventId, condition.getEventId()))
				throw new StatusException(StatusCodes.Bad_EventIdUnknown);
			// triggers a new event
			final byte[] userEventId = getNextUserEventId();
			condition.addComment(eventId, comment, DateTime.currentTime(),
					userEventId);
			return true; // Handled here
			// NOTE: If you do not handle addComment here, and return false,
			// the EventManager (or MethodManager) will call
			// condition.addComment automatically
		}

		@Override
		public void onAfterCreateMonitoredEventItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredEventItem item) {
			//
		}

		@Override
		public void onAfterDeleteMonitoredEventItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredEventItem item) {
			//
		}

		@Override
		public void onAfterModifyMonitoredEventItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredEventItem item) {
			//
		}

		@Override
		public void onConditionRefresh(ServiceContext serviceContext,
				Subscription subscription) throws StatusException {
			//
		}

		@Override
		public boolean onConfirm(ServiceContext serviceContext,
				AcknowledgeableConditionType condition, byte[] eventId,
				LocalizedText comment) throws StatusException {
			// Handle confirm request to a condition event
			println("Confirm: Condition=" + condition + "; EventId="
					+ eventIdToString(eventId) + "; Comment=" + comment);
			// If the confirmed event is no longer active, return an error
			if (!Arrays.equals(eventId, condition.getEventId()))
				throw new StatusException(StatusCodes.Bad_EventIdUnknown);
			if (condition.isConfirmed())
				throw new StatusException(
						StatusCodes.Bad_ConditionBranchAlreadyConfirmed);
			if (!condition.isAcked())
				throw new StatusException(
						"Condition can only be confirmed when it is acknowledged.",
						StatusCodes.Bad_InvalidState);
			final DateTime now = DateTime.currentTime();
			condition.setConfirmed(true, now);
			final byte[] userEventId = getNextUserEventId();
			// addComment triggers a new event
			condition.addComment(eventId, comment, now, userEventId);
			return true; // Handled here
			// NOTE: If you do not handle Confirm here, and return false,
			// the EventManager (or MethodManager) will call
			// condition.confirm, which performs the same actions as this
			// handler
		}

		@Override
		public void onCreateMonitoredEventItem(ServiceContext serviceContext,
				NodeId nodeId, EventFilter eventFilter,
				EventFilterResult filterResult) throws StatusException {
			// Item created
		}

		@Override
		public void onDeleteMonitoredEventItem(ServiceContext serviceContext,
				Subscription subscription, MonitoredEventItem monitoredItem) {
			// Stop monitoring the item?
		}

		@Override
		public boolean onDisable(ServiceContext serviceContext,
				ConditionType condition) throws StatusException {
			// Handle disable request to a condition
			println("Disable: Condition=" + condition);
			if (condition.isEnabled()) {
				DateTime now = DateTime.currentTime();
				// Setting enabled to false, also sets retain to false
				condition.setEnabled(false, now);
				// notify the clients of the change
				condition.triggerEvent(now, null, getNextUserEventId());
			}
			return true; // Handled here
			// NOTE: If you do not handle disable here, and return false,
			// the EventManager (or MethodManager) will request the
			// condition to handle the call, and it will unset the enabled
			// state, and triggers a new notification event, as here
		}

		@Override
		public boolean onEnable(ServiceContext serviceContext,
				ConditionType condition) throws StatusException {
			// Handle enable request to a condition
			println("Enable: Condition=" + condition);
			if (!condition.isEnabled()) {
				DateTime now = DateTime.currentTime();
				condition.setEnabled(true, now);
				// You should evaluate the condition now, set Retain to true,
				// if necessary and in that case also call triggerEvent
				// condition.setRetain(true);
				// condition.triggerEvent(now, null, getNextUserEventId());
			}
			return true; // Handled here
			// NOTE: If you do not handle enable here, and return false,
			// the EventManager (or MethodManager) will request the
			// condition to handle the call, and it will set the enabled
			// state.

			// You should however set the status of the condition yourself
			// and trigger a new event if necessary
		}

		@Override
		public void onModifyMonitoredEventItem(ServiceContext serviceContext,
				Subscription subscription, MonitoredEventItem monitoredItem,
				EventFilter eventFilter, EventFilterResult filterResult)
				throws StatusException {
			// Modify event monitoring, when the client modifies a monitored
			// item
		}

		@Override
		public boolean onOneshotShelve(ServiceContext serviceContext,
				AlarmConditionType condition,
				ShelvedStateMachineType stateMachine) throws StatusException {
			return false;
		}

		@Override
		public boolean onTimedShelve(ServiceContext serviceContext,
				AlarmConditionType condition,
				ShelvedStateMachineType stateMachine, double shelvingTime)
				throws StatusException {
			return false;
		}

		@Override
		public boolean onUnshelve(ServiceContext serviceContext,
				AlarmConditionType condition,
				ShelvedStateMachineType stateMachine) throws StatusException {
			return false;
		}

		private String eventIdToString(byte[] eventId) {
			return eventId == null ? "(null)" : Arrays.toString(eventId);
		}
	};

	private final HistoryManagerListener myHistoryManagerListener = new HistoryManagerListener() {

		@Override
		public void onDeleteAtTimes(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, DateTime[] reqTimes,
				StatusCode[] operationResults,
				DiagnosticInfo[] operationDiagnostics) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public void onDeleteEvents(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, byte[][] eventIds,
				StatusCode[] operationResults,
				DiagnosticInfo[] operationDiagnostics) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public void onDeleteModified(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, DateTime startTime, DateTime endTime)
				throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public void onDeleteRaw(ServiceContext serviceContext, NodeId nodeId,
				UaNode node, DateTime startTime, DateTime endTime)
				throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public Object onReadAtTimes(ServiceContext serviceContext,
				TimestampsToReturn timestampsToReturn, NodeId nodeId,
				UaNode node, Object continuationPoint, DateTime[] reqTimes,
				NumericRange indexRange, HistoryData historyData)
				throws StatusException {
			if (node == myLevel)
				historyData.setDataValues(myLevelHistory.getHistory(reqTimes));
			return null;
		}

		@Override
		public Object onReadEvents(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, Object continuationPoint,
				DateTime startTime, DateTime endTime,
				UnsignedInteger numValuesPerNode, EventFilter filter,
				HistoryEvent historyEvent) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public Object onReadModified(ServiceContext serviceContext,
				TimestampsToReturn timestampsToReturn, NodeId nodeId,
				UaNode node, Object continuationPoint, DateTime startTime,
				DateTime endTime, UnsignedInteger numValuesPerNode,
				NumericRange indexRange, HistoryModifiedData historyData)
				throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public Object onReadProcessed(ServiceContext serviceContext,
				TimestampsToReturn timestampsToReturn, NodeId nodeId,
				UaNode node, Object continuationPoint, DateTime startTime,
				DateTime endTime, Double resampleInterval,
				NodeId aggregateType,
				AggregateConfiguration aggregateConfiguration,
				NumericRange indexRange, HistoryData historyData)
				throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public Object onReadRaw(ServiceContext serviceContext,
				TimestampsToReturn timestampsToReturn, NodeId nodeId,
				UaNode node, Object continuationPoint, DateTime startTime,
				DateTime endTime, UnsignedInteger numValuesPerNode,
				Boolean returnBounds, NumericRange indexRange,
				HistoryData historyData) throws StatusException {
			if (node == myLevel)
				historyData.setDataValues(myLevelHistory.getHistory(startTime,
						endTime, numValuesPerNode.intValue(), returnBounds));
			return null;
		}

		@Override
		public void onUpdateData(ServiceContext serviceContext, NodeId nodeId,
				UaNode node, DataValue[] updateValues,
				PerformUpdateType performInsertReplace,
				StatusCode[] operationResults,
				DiagnosticInfo[] operationDiagnostics) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public void onUpdateEvent(ServiceContext serviceContext, NodeId nodeId,
				UaNode node, Variant[] eventFields, EventFilter filter,
				PerformUpdateType performInsertReplace,
				StatusCode[] operationResults,
				DiagnosticInfo[] operationDiagnostics) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

		@Override
		public void onUpdateStructureData(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, DataValue[] updateValues,
				PerformUpdateType performUpdateType,
				StatusCode[] operationResults,
				DiagnosticInfo[] operationDiagnostics) throws StatusException {
			throw new StatusException(
					StatusCodes.Bad_HistoryOperationUnsupported);
		}

	};

	private final IoManagerListener myIoManagerListener = new IoManagerListener() {
		@Override
		public EnumSet<AccessLevel> onGetUserAccessLevel(
				ServiceContext serviceContext, NodeId nodeId, UaVariable node) {
			// The AccessLevel defines the accessibility of the Variable.Value
			// attribute
			return EnumSet.of(AccessLevel.CurrentRead,
					AccessLevel.CurrentWrite, AccessLevel.HistoryRead);
		}

		@Override
		public boolean onGetUserExecutable(ServiceContext serviceContext,
				NodeId nodeId, UaMethod node) {
			// Enable execution of all methods that are allowed by default
			return true;
		}

		@Override
		public EnumSet<WriteAccess> onGetUserWriteMask(
				ServiceContext serviceContext, NodeId nodeId, UaNode node) {
			// Enable writing to everything that is allowed by default
			// The WriteMask defines the writable attributes, except for Value,
			// which is controlled by UserAccessLevel (above)

			// The following would deny write access for anonymous users:
			// if
			// (serviceContext.getSession().getUserIdentity().getType().equals(
			// UserTokenType.Anonymous))
			// return EnumSet.noneOf(WriteAccess.class);

			return EnumSet.allOf(WriteAccess.class);
		}

		@Override
		public void onReadNonValue(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, UnsignedInteger attributeId,
				DataValue dataValue) throws StatusException {
			// OK
		}

		@Override
		public void onReadValue(ServiceContext serviceContext, NodeId nodeId,
				UaVariable node, NumericRange indexRange,
				TimestampsToReturn timestampsToReturn, DateTime minTimestamp,
				DataValue dataValue) throws StatusException {
			// OK
		}

		@Override
		public boolean onWriteNonValue(ServiceContext serviceContext,
				NodeId nodeId, UaNode node, UnsignedInteger attributeId,
				DataValue dataValue) throws StatusException {
			return false;
		}

		@Override
		public boolean onWriteValue(ServiceContext serviceContext,
				NodeId nodeId, UaVariable node, NumericRange indexRange,
				DataValue dataValue) throws StatusException {
			return false;
		}
	};

	private UaVariableNode myLevel;

	private ValueHistory myLevelHistory;

	private PlainMethod myMethod;

	private final CallableListener myMethodManagerListener = new CallableListener() {

		@Override
		public boolean onCall(ServiceContext serviceContext, NodeId objectId,
				UaNode object, NodeId methodId, UaMethod method,
				final Variant[] inputArguments,
				final StatusCode[] inputArgumentResults,
				final DiagnosticInfo[] inputArgumentDiagnosticInfos,
				final Variant[] outputs) throws StatusException {
			// Handle method calls
			// Note that the outputs is already allocated
			if (methodId.equals(myMethod.getNodeId())) {
				logger.info("myMethod: " + Arrays.toString(inputArguments));
				MethodManager.checkInputArguments(new Class[] { String.class,
						Double.class }, inputArguments, inputArgumentResults,
						inputArgumentDiagnosticInfos, false);
				String operation;
				try {
					operation = (String) inputArguments[0].getValue();
				} catch (ClassCastException e) {
					throw inputError(0, e.getMessage(), inputArgumentResults,
							inputArgumentDiagnosticInfos);
				}
				double input;
				try {
					input = inputArguments[1].intValue();
				} catch (ClassCastException e) {
					throw inputError(1, e.getMessage(), inputArgumentResults,
							inputArgumentDiagnosticInfos);
				}

				operation = operation.toLowerCase();
				double result;
				if (operation.equals("sin"))
					result = Math.sin(Math.toRadians(input));
				else if (operation.equals("cos"))
					result = Math.cos(Math.toRadians(input));
				else if (operation.equals("tan"))
					result = Math.tan(Math.toRadians(input));
				else if (operation.equals("pow"))
					result = input * input;
				else
					throw inputError(0, "Unknown function '" + operation // IOP
																			// wrong
																			// index,
																			// failed
																			// function
																			// is
																			// 0
							+ "': valid functions are sin, cos, tan, pow",
							inputArgumentResults, inputArgumentDiagnosticInfos);
				outputs[0] = new Variant(result);
				return true; // Handled here
			} else
				return false;
		}

		/**
		 * Handle an error in method inputs.
		 * 
		 * @param index
		 *            index of the failing input
		 * @param message
		 *            error message
		 * @param inputArgumentResults
		 *            the results array to fill in
		 * @param inputArgumentDiagnosticInfos
		 *            the diagnostics array to fill in
		 * @return StatusException that can be thrown to break further method
		 *         handling
		 */
		private StatusException inputError(final int index,
				final String message, StatusCode[] inputArgumentResults,
				DiagnosticInfo[] inputArgumentDiagnosticInfos) {
			logger.info("inputError: #" + index + " message=" + message);
			inputArgumentResults[index] = new StatusCode(
					StatusCodes.Bad_InvalidArgument);
			final DiagnosticInfo di = new DiagnosticInfo();
			di.setAdditionalInfo(message);
			inputArgumentDiagnosticInfos[index] = di;
			return new StatusException(StatusCodes.Bad_InvalidArgument);
		}

	};

	private NodeManagerUaNode myNodeManager;

	private final NodeManagerListener myNodeManagerListener = new NodeManagerListener() {

		@Override
		public void onAddNode(ServiceContext serviceContext,
				NodeId parentNodeId, UaNode parent, NodeId nodeId, UaNode node,
				NodeClass nodeClass, QualifiedName browseName,
				NodeAttributes attributes, UaReferenceType referenceType,
				ExpandedNodeId typeDefinitionId, UaNode typeDefinition)
				throws StatusException {
			// Notification of a node addition request
			checkUserIdentity(serviceContext);
		}

		@Override
		public void onAddReference(ServiceContext serviceContext,
				NodeId sourceNodeId, UaNode sourceNode,
				ExpandedNodeId targetNodeId, UaNode targetNode,
				NodeId referenceTypeId, UaReferenceType referenceType,
				boolean isForward) throws StatusException {
			// Notification of a reference addition request
			checkUserIdentity(serviceContext);
		}

		@Override
		public void onAfterCreateMonitoredDataItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredDataItem item) {
			//
		}

		@Override
		public void onAfterDeleteMonitoredDataItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredDataItem item) {
			//
		}

		@Override
		public void onAfterModifyMonitoredDataItem(
				ServiceContext serviceContext, Subscription subscription,
				MonitoredDataItem item) {
			//
		}

		@Override
		public boolean onBrowseNode(ServiceContext serviceContext,
				ViewDescription view, NodeId nodeId, UaNode node,
				UaReference reference) {
			// Perform custom filtering, for example based on the user
			// doing the browse
			// Default is to return all references for everyone
			return true;
		}

		@Override
		public void onCreateMonitoredDataItem(ServiceContext serviceContext,
				Subscription subscription, UaNode node,
				UnsignedInteger attributeId, String indexRange,
				MonitoringParameters params, MonitoringFilter filter,
				AggregateFilterResult filterResult) throws StatusException {
			// Notification of a monitored item creation request

			// You may, for example start to monitor the node from a physical
			// device, only once you get a request for it from a client
		}

		@Override
		public void onDeleteMonitoredDataItem(ServiceContext serviceContext,
				Subscription subscription, MonitoredDataItem monitoredItem) {
			// Notification of a monitored item delete request
		}

		@Override
		public void onDeleteNode(ServiceContext serviceContext, NodeId nodeId,
				UaNode node, boolean deleteTargetReferences)
				throws StatusException {
			// Notification of a node deletion request
			checkUserIdentity(serviceContext);
		}

		@Override
		public void onDeleteReference(ServiceContext serviceContext,
				UaNode sourceNode, ExpandedNodeId targetNodeId,
				UaReferenceType referenceType, boolean isForward,
				boolean deleteBidirectional) throws StatusException {
			// Notification of a reference deletion request
			checkUserIdentity(serviceContext);
		}

		@Override
		public void onModifyMonitoredDataItem(ServiceContext serviceContext,
				Subscription subscription, MonitoredDataItem item, UaNode node,
				MonitoringParameters params, MonitoringFilter filter,
				AggregateFilterResult filterResult) {
			// Notification of a monitored item modification request
		}

		private void checkUserIdentity(ServiceContext serviceContext)
				throws StatusException {
			// Do not allow for anonymous users
			if (serviceContext.getSession().getUserIdentity().getType()
					.equals(UserTokenType.Anonymous))
				throw new StatusException(StatusCodes.Bad_UserAccessDenied);
		}
	};

	private FolderType myObjectsFolder;

	private PlainVariable<Boolean> mySwitch;

	private final Runnable simulationTask = new Runnable() {
		double dx = 1;

		@Override
		public void run() {
			if (server.isRunning()) {
				final DataValue v = myLevel.getValue();
				Double nextValue = v.isNull() ? 0 : v.getValue().doubleValue()
						+ dx;
				if (nextValue <= 0)
					dx = 1;
				else if (nextValue >= 100)
					dx = -1;
				try {
					((CacheVariable) myLevel).updateValue(nextValue);
					if (nextValue > myAlarm.getHighHighLimit())
						activateAlarm(700);
					else if (nextValue > myAlarm.getHighLimit())
						activateAlarm(500);
					else if (nextValue < myAlarm.getLowLowLimit())
						activateAlarm(700);
					else if (nextValue < myAlarm.getLowLimit())
						activateAlarm(500);
					else
						inactivateAlarm();
				} catch (Exception e) {
					printException(e);
					throw new RuntimeException(e); // End the task
				}
			}
			myBigNodeManager.simulate();
		}
	};

	private final ScheduledExecutorService simulator = Executors
			.newScheduledThreadPool(10);

	private FolderType staticArrayVariableFolder;

	private FolderType staticVariableFolder;

	private final UserValidator userValidator = new UserValidator() {

		@Override
		public boolean onValidate(Session session, UserIdentity userIdentity)
				throws StatusException {
			// Return true, if the user is allowed access to the server
			// Note that the UserIdentity can be of different actual types,
			// depending on the selected authentication mode (by the client).
			println("onValidate: userIdentity=" + userIdentity);
			if (userIdentity.getType().equals(UserTokenType.UserName))
				if (userIdentity.getName().equals("opcua")
						&& userIdentity.getPassword().equals("opcua"))
					return true;
				else
					return false;
			if (userIdentity.getType().equals(UserTokenType.Certificate))
				// Implement your strategy here, for example using the
				// PkiFileBasedCertificateValidator
				return true;
			return true;
		}
	};

	private final CertificateValidationListener validationListener = new CertificateValidationListener() {

		@Override
		public ValidationResult onValidate(Cert certificate,
				ApplicationDescription applicationDescription,
				EnumSet<CertificateCheck> passedChecks) {
			// Do not mind about URI...
			if (passedChecks.containsAll(EnumSet.of(CertificateCheck.Trusted,
					CertificateCheck.Validity, CertificateCheck.Signature))) {
				if (!passedChecks.contains(CertificateCheck.Uri))
					try {
						println("Client's ApplicationURI ("
								+ applicationDescription.getApplicationUri()
								+ ") does not match the one in certificate: "
								+ PkiFileBasedCertificateValidator
										.getApplicationUriOfCertificate(certificate));
					} catch (CertificateParsingException e) {
						throw new RuntimeException(e);
					}
				return ValidationResult.AcceptPermanently;
			}
			return ValidationResult.Reject;
		}
	};
	protected Variant[] eventFieldValues;

	protected Object eventSender;

	protected UaServer server;

	/**
	 * Creates an alarm, if it is not active
	 */
	private void activateAlarm(int severity) {
		if (myAlarm.isEnabled()
				&& (!myAlarm.isActive() || (myAlarm.getSeverity().getValue() != severity))) {
			println("activateAlarm: severity=" + severity);
			myAlarm.setActive(true);
			myAlarm.setRetain(true);
			myAlarm.setAcked(false); // Also sets confirmed to false
			myAlarm.setSeverity(severity);

			triggerEvent(myAlarm);

			// If you wish to check whether any clients are monitoring your
			// alarm, you can use the following

			// logger.info("myAlarm is monitored=" +
			// myAlarm.isMonitoredForEvents());
		}
	}

	private void addDeepObject(UaNode parent, int depth, int maxDepth) {
		if (depth <= maxDepth) {
			final String name = String.format("DeepObject%02d", depth);
			UaObjectNode newObject = new UaObjectNode(complianceNodeManager,
					new NodeId(complianceNamespaceIndex, name), name,
					Locale.ENGLISH);
			try {
				complianceNodeManager.addNodeAndReference(parent, newObject,
						Identifiers.Organizes);
			} catch (StatusException e) {
			}
			addDeepObject(newObject, depth + 1, maxDepth);
		}
	}

	private void addNode(String name) {
		// Initialize NodeVersion property, to enable ModelChangeEvents
		myObjectsFolder.initNodeVersion();

		server.getNodeManagerRoot().beginModelChange();
		try {
			NodeId nodeId = new NodeId(myNodeManager.getNamespaceIndex(),
					UUID.randomUUID());

			UaNode node = myNodeManager.getNodeFactory().createNode(
					NodeClass.Variable, nodeId, name, Locale.ENGLISH,
					Identifiers.PropertyType);
			myObjectsFolder.addComponent(node);
		} catch (UaNodeFactoryException e) {
			logger.error(e);
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} finally {
			server.getNodeManagerRoot().endModelChange();
		}
	}

	/**
	 * Create a sample alarm node structure.
	 * 
	 * @param ns
	 *            the namespaceIndex for the nodes
	 * @throws StatusException
	 */
	private void createAlarmNode(int ns) throws StatusException {

		// Level Alarm from the LevelMeasurement

		// See the Spec. Part 9. Appendix B.2 for a similar example

		final NodeId myAlarmId = new NodeId(ns, "MyLevel.Alarm");
		myAlarm = new ExclusiveLevelAlarmType(myNodeManager, myAlarmId,
				"MyLevelAlarm", Locale.ENGLISH);
		// ConditionSource is the node which has this condition
		myAlarm.setSource(myLevel);
		// Input is the node which has the measurement that generates the alarm
		myAlarm.setInput(myLevel);

		myAlarm.setMessage("Level exceeded"); // Default locale
		myAlarm.setMessage("Füllständalarm!", Locale.GERMAN);
		myAlarm.setSeverity(500); // Medium level warning
		myAlarm.setHighHighLimit(90);
		myAlarm.setHighLimit(70);
		myAlarm.setLowLowLimit(10);
		myAlarm.setLowLimit(30);
		myAlarm.setEnabled(true);
		myDevice.addComponent(myAlarm); // addReference(...Identifiers.HasComponent...)

		// + HasCondition, the SourceNode of the reference should normally
		// correspond to the Source set above
		myLevel.addReference(myAlarm, Identifiers.HasCondition, false);

		// + EventSource, the target of the EventSource is normally the
		// source of the HasCondition reference
		myDevice.addReference(myLevel, Identifiers.HasEventSource, false);

		// + HasNotifier, these are used to link the source of the EventSource
		// up in the address space hierarchy
		myObjectsFolder.addReference(myDevice, Identifiers.HasNotifier, false);
	}

	private void createAnalogItem(String dataTypeName, NodeId dataType,
			Object initialValue) throws StatusException {
		final NodeId nodeId = new NodeId(complianceNamespaceIndex, dataTypeName
				+ "AnalogItem");
		UaType type = server.getNodeManagerRoot().getType(dataType);
		AnalogItemType node = new AnalogItemType(complianceNodeManager, nodeId,
				dataTypeName + "AnalogItem", Locale.ENGLISH);
		node.setDefinition("Test definition of type " + dataTypeName);
		node.setDataType(type);
		node.setTypeDefinition(type);
		if (dataType.equals(Identifiers.Double)) {
			node.setEngineeringUnits(new EUInformation(
					"http://www.example.com", 3, new LocalizedText("kg",
							Locale.ENGLISH), new LocalizedText("kilogram",
							Locale.ENGLISH)));
			node.setEuRange(new Range((double) 0, (double) 1000));
		}
		node.setValue(new DataValue(new Variant(initialValue), StatusCode.GOOD,
				new DateTime(), new DateTime()));
		analogItemFolder.addReference(node, Identifiers.HasComponent, false);
	}

	private void createAnalogItemArray(String dataTypeName, NodeId dataType,
			Object initialValue) throws StatusException {
		final NodeId nodeId = new NodeId(complianceNamespaceIndex, dataTypeName
				+ "AnalogItemArray");
		UaType type = server.getNodeManagerRoot().getType(dataType);
		AnalogItemType node = new AnalogItemType(complianceNodeManager, nodeId,
				dataTypeName + "AnalogItemArray", Locale.ENGLISH);
		node.setDataType(type);
		node.setTypeDefinition(type);
		node.setValueRank(ValueRanks.OneDimension);
		node.setArrayDimensions(new UnsignedInteger[] { UnsignedInteger
				.valueOf(Array.getLength(initialValue)) });

		node.setValue(new DataValue(new Variant(initialValue), StatusCode.GOOD,
				new DateTime(), new DateTime()));
		analogItemArrayFolder.addReference(node, Identifiers.HasComponent,
				false);
	}

	/**
	 * Create a sample node manager, which does not use UaNode objects. These
	 * are suitable for managing big address spaces for data that is in practice
	 * available from another existing subsystem.
	 */
	private void createBigNodeManager() {
		myBigNodeManager = new MyBigNodeManager(server,
				"http://www.prosysopc.com/OPCUA/SampleBigAddressSpace",
				bigAddressSpaceNodes);
	}

	/**
	 * 
	 */
	private void createComplianceNodes() {
		try {
			// My Node Manager
			complianceNodeManager = new NodeManagerUaNode(server,
					"http://www.prosysopc.com/OPCUA/ComplianceNodes");

			complianceNamespaceIndex = complianceNodeManager
					.getNamespaceIndex();

			// UA types and folders which we will use
			final UaObject objectsFolder = server.getNodeManagerRoot()
					.getObjectsFolder();

			final NodeId staticDataFolderId = new NodeId(
					complianceNamespaceIndex, "StaticData");
			FolderType staticDataFolder = new FolderType(complianceNodeManager,
					staticDataFolderId, "StaticData", Locale.ENGLISH);
			objectsFolder.addReference(staticDataFolder, Identifiers.Organizes,
					false);

			// Folder for static test variables
			final NodeId staticVariableFolderId = new NodeId(
					complianceNamespaceIndex, "StaticVariablesFolder");
			staticVariableFolder = new FolderType(complianceNodeManager,
					staticVariableFolderId, "StaticVariables", Locale.ENGLISH);
			complianceNodeManager.addNodeAndReference(staticDataFolder,
					staticVariableFolder, Identifiers.Organizes);

			createStaticVariable("Boolean", Identifiers.Boolean, true);
			createStaticVariable("Byte", Identifiers.Byte,
					UnsignedByte.valueOf(0));
			createStaticVariable("ByteString", Identifiers.ByteString,
					new byte[] { (byte) 0 });
			createStaticVariable("DateTime", Identifiers.DateTime,
					DateTime.currentTime());
			createStaticVariable("Double", Identifiers.Double, (double) 0);
			createStaticVariable("Float", Identifiers.Float, (float) 0);
			createStaticVariable("GUID", Identifiers.Guid, UUID.randomUUID());
			createStaticVariable("Int16", Identifiers.Int16, (short) 0);
			createStaticVariable("Int32", Identifiers.Int32, 0);
			createStaticVariable("Int64", Identifiers.Int64, (long) 0);
			createStaticVariable("SByte", Identifiers.SByte, (byte) 0);
			createStaticVariable("String", Identifiers.String, "testString");
			createStaticVariable("UInt16", Identifiers.UInt16,
					UnsignedShort.valueOf(0));
			createStaticVariable("UInt32", Identifiers.UInt32,
					UnsignedInteger.valueOf(0));
			createStaticVariable("UInt64", Identifiers.UInt64,
					UnsignedLong.valueOf(0));
			createStaticVariable("XmlElement", Identifiers.XmlElement,
					new XmlElement("<testElement />"));

			// Folder for static test array variables
			final NodeId staticArrayVariableFolderId = new NodeId(
					complianceNamespaceIndex, "StaticArrayVariablesFolder");
			staticArrayVariableFolder = new FolderType(complianceNodeManager,
					staticArrayVariableFolderId, "StaticArrayVariables",
					Locale.ENGLISH);
			staticDataFolder.addReference(staticArrayVariableFolder,
					Identifiers.Organizes, false);

			createStaticArrayVariable("BooleanArray", Identifiers.Boolean,
					new Boolean[] { true, false, true, false, false });
			createStaticArrayVariable(
					"ByteArray",
					Identifiers.Byte,
					new UnsignedByte[] { UnsignedByte.valueOf(1),
							UnsignedByte.valueOf(2), UnsignedByte.valueOf(3),
							UnsignedByte.valueOf(4), UnsignedByte.valueOf(5) });
			createStaticArrayVariable("ByteStringArray",
					Identifiers.ByteString, new byte[][] {
							new byte[] { (byte) 1, (byte) 2, (byte) 3 },
							new byte[] { (byte) 2, (byte) 3, (byte) 4 },
							new byte[] { (byte) 3, (byte) 4, (byte) 5 },
							new byte[] { (byte) 4, (byte) 5, (byte) 6 },
							new byte[] { (byte) 5, (byte) 6, (byte) 7 } });
			createStaticArrayVariable(
					"DateTimeArray",
					Identifiers.DateTime,
					new DateTime[] { DateTime.currentTime(),
							DateTime.currentTime(), DateTime.currentTime(),
							DateTime.currentTime(), DateTime.currentTime() });
			createStaticArrayVariable("DoubleArray", Identifiers.Double,
					new Double[] { (double) 1, (double) 2, (double) 3,
							(double) 4, (double) 5 });
			createStaticArrayVariable("FloatArray", Identifiers.Float,
					new Float[] { (float) 1, (float) 2, (float) 3, (float) 4,
							(float) 5 });
			createStaticArrayVariable(
					"GUIDArray",
					Identifiers.Guid,
					new UUID[] { UUID.randomUUID(), UUID.randomUUID(),
							UUID.randomUUID(), UUID.randomUUID(),
							UUID.randomUUID() });
			createStaticArrayVariable("Int16Array", Identifiers.Int16,
					new Short[] { (short) 1, (short) 2, (short) 3, (short) 4,
							(short) 5 });
			createStaticArrayVariable("Int32Array", Identifiers.Int32,
					new Integer[] { 1, 2, 3, 4, 5 });
			createStaticArrayVariable("Int64Array", Identifiers.Int64,
					new Long[] { (long) 1, (long) 2, (long) 3, (long) 4,
							(long) 5 });
			createStaticArrayVariable("SByteArray", Identifiers.SByte,
					new Byte[] { (byte) 0, (byte) 15, (byte) 255, (byte) 15,
							(byte) 0 });
			createStaticArrayVariable("StringArray", Identifiers.String,
					new String[] { "testString1", "testString2", "testString3",
							"testString4", "testString5" });
			createStaticArrayVariable(
					"UInt16Array",
					Identifiers.UInt16,
					new UnsignedShort[] { UnsignedShort.valueOf(1),
							UnsignedShort.valueOf(2), UnsignedShort.valueOf(3),
							UnsignedShort.valueOf(4), UnsignedShort.valueOf(5) });
			createStaticArrayVariable(
					"UInt32Array",
					Identifiers.UInt32,
					new UnsignedInteger[] { UnsignedInteger.valueOf(1),
							UnsignedInteger.valueOf(2),
							UnsignedInteger.valueOf(3),
							UnsignedInteger.valueOf(4),
							UnsignedInteger.valueOf(5) });
			createStaticArrayVariable(
					"UInt64Array",
					Identifiers.UInt64,
					new UnsignedLong[] { UnsignedLong.valueOf(1),
							UnsignedLong.valueOf(2), UnsignedLong.valueOf(3),
							UnsignedLong.valueOf(4), UnsignedLong.valueOf(5) });
			createStaticArrayVariable("XmlElementArray",
					Identifiers.XmlElement, new XmlElement[] {
							new XmlElement("<testElement1 />"),
							new XmlElement("<testElement2 />"),
							new XmlElement("<testElement3 />"),
							new XmlElement("<testElement4 />"),
							new XmlElement("<testElement5 />") });

			// Folder for DataItem test variables
			final NodeId dataItemFolderId = new NodeId(
					complianceNamespaceIndex, "DataItemsFolder");
			dataItemFolder = new FolderType(complianceNodeManager,
					dataItemFolderId, "DataItems", Locale.ENGLISH);
			staticDataFolder.addReference(dataItemFolder,
					Identifiers.Organizes, false);

			// createDataItem("Boolean", Identifiers.Boolean, true);
			createDataItem("Byte", Identifiers.Byte, UnsignedByte.valueOf(0));
			// createDataItem("ByteString", Identifiers.ByteString,
			// new byte[] { (byte) 0 });
			createDataItem("DateTime", Identifiers.DateTime,
					DateTime.currentTime());
			createDataItem("Double", Identifiers.Double, (double) 0);
			createDataItem("Float", Identifiers.Float, (float) 0);
			// createDataItem("GUID", Identifiers.Guid, UUID.randomUUID());
			createDataItem("Int16", Identifiers.Int16, (short) 0);
			createDataItem("Int32", Identifiers.Int32, 0);
			createDataItem("Int64", Identifiers.Int64, (long) 0);
			createDataItem("SByte", Identifiers.SByte, (byte) 0);
			createDataItem("String", Identifiers.String, "testString");
			createDataItem("UInt16", Identifiers.UInt16,
					UnsignedShort.valueOf(0));
			createDataItem("UInt32", Identifiers.UInt32,
					UnsignedInteger.valueOf(0));
			createDataItem("UInt64", Identifiers.UInt64,
					UnsignedLong.valueOf(0));

			// Folder for DataItem test variables
			final NodeId analogItemFolderId = new NodeId(
					complianceNamespaceIndex, "AnalogItemsFolder");
			analogItemFolder = new FolderType(complianceNodeManager,
					analogItemFolderId, "AnalogItems", Locale.ENGLISH);
			staticDataFolder.addReference(analogItemFolder,
					Identifiers.Organizes, false);

			createAnalogItem("Byte", Identifiers.Byte, UnsignedByte.valueOf(0));
			createAnalogItem("Double", Identifiers.Double, (double) 0);
			createAnalogItem("Float", Identifiers.Float, (float) 0);
			createAnalogItem("Int16", Identifiers.Int16, (short) 0);
			createAnalogItem("Int32", Identifiers.Int32, 0);
			createAnalogItem("Int64", Identifiers.Int64, (long) 0);
			createAnalogItem("SByte", Identifiers.SByte, (byte) 0);
			createAnalogItem("UInt16", Identifiers.UInt16,
					UnsignedShort.valueOf(0));
			createAnalogItem("UInt32", Identifiers.UInt32,
					UnsignedInteger.valueOf(0));
			createAnalogItem("UInt64", Identifiers.UInt64,
					UnsignedLong.valueOf(0));

			// Folder for static test array variables
			final NodeId analogItemArrayFolderId = new NodeId(
					complianceNamespaceIndex, "AnalogItemArrayFolder");
			analogItemArrayFolder = new FolderType(complianceNodeManager,
					analogItemArrayFolderId, "AnalogItemArrays", Locale.ENGLISH);
			staticDataFolder.addReference(analogItemArrayFolder,
					Identifiers.Organizes, false);

			createAnalogItemArray("Double", Identifiers.Double,
					new Double[] { (double) 1, (double) 2, (double) 3,
							(double) 4, (double) 5 });
			createAnalogItemArray("Float", Identifiers.Float, new Float[] {
					(float) 1, (float) 2, (float) 3, (float) 4, (float) 5 });
			createAnalogItemArray("Int16", Identifiers.Int16, new Short[] {
					(short) 1, (short) 2, (short) 3, (short) 4, (short) 5 });
			createAnalogItemArray("Int32", Identifiers.Int32, new Integer[] {
					1, 2, 3, 4, 5 });
			createAnalogItemArray(
					"UInt16",
					Identifiers.UInt16,
					new UnsignedShort[] { UnsignedShort.valueOf(1),
							UnsignedShort.valueOf(2), UnsignedShort.valueOf(3),
							UnsignedShort.valueOf(4), UnsignedShort.valueOf(5) });
			createAnalogItemArray(
					"UInt32",
					Identifiers.UInt32,
					new UnsignedInteger[] { UnsignedInteger.valueOf(1),
							UnsignedInteger.valueOf(2),
							UnsignedInteger.valueOf(3),
							UnsignedInteger.valueOf(4),
							UnsignedInteger.valueOf(5) });

			// Folder for deep object chain
			final NodeId deepFolderId = new NodeId(complianceNamespaceIndex,
					"DeepFolder");
			deepFolder = new FolderType(complianceNodeManager, deepFolderId,
					"DeepFolder", Locale.ENGLISH);
			staticDataFolder.addReference(deepFolder, Identifiers.Organizes,
					false);

			addDeepObject(deepFolder, 1, 20);

			// / COMPLIANCE TEST NODES END HERE ///

			logger.info("Compliance address space created.");
		} catch (StatusException ex) {
			logger.error("Error occurred with creating compliance nodes: "
					+ ex.toString());
		}
	}

	private void createDataItem(String dataTypeName, NodeId dataType,
			Object initialValue) throws StatusException {
		final NodeId nodeId = new NodeId(complianceNamespaceIndex, dataTypeName
				+ "DataItem");
		UaType type = server.getNodeManagerRoot().getType(dataType);
		DataItemType node = new DataItemType(complianceNodeManager, nodeId,
				dataTypeName + "DataItem", Locale.ENGLISH);
		node.setDefinition("Test definition of type " + dataTypeName);
		node.setDataType(type);
		node.setTypeDefinition(type);
		node.setValue(new DataValue(new Variant(initialValue), StatusCode.GOOD,
				new DateTime(), new DateTime()));
		dataItemFolder.addReference(node, Identifiers.HasComponent, false);
	}

	/**
	 * Create a sample method.
	 * 
	 * @param ns
	 *            the namespaceIndex for the nodes
	 * @throws StatusException
	 */
	private void createMethodNode(int ns) throws StatusException {
		final NodeId myMethodId = new NodeId(ns, "MyMethod");
		myMethod = new PlainMethod(myNodeManager, myMethodId, "MyMethod",
				Locale.ENGLISH);
		Argument[] inputs = new Argument[2];
		inputs[0] = new Argument();
		inputs[0].setName("Operation");
		inputs[0].setDataType(Identifiers.String);
		inputs[0].setValueRank(ValueRanks.Scalar);
		inputs[0].setArrayDimensions(null);
		inputs[0]
				.setDescription(new LocalizedText(
						"The operation to perform on parameter: valid functions are sin, cos, tan, pow",
						Locale.ENGLISH));
		inputs[1] = new Argument();
		inputs[1].setName("Parameter");
		inputs[1].setDataType(Identifiers.Double);
		inputs[1].setValueRank(ValueRanks.Scalar);
		inputs[1].setArrayDimensions(null);
		inputs[1].setDescription(new LocalizedText(
				"The parameter for operation", Locale.ENGLISH));
		myMethod.setInputArguments(inputs);

		Argument[] outputs = new Argument[1];
		outputs[0] = new Argument();
		outputs[0].setName("Result");
		outputs[0].setDataType(Identifiers.Double);
		outputs[0].setValueRank(ValueRanks.Scalar);
		outputs[0].setArrayDimensions(null);
		outputs[0].setDescription(new LocalizedText(
				"The result of 'operation(parameter)'", Locale.ENGLISH));
		myMethod.setOutputArguments(outputs);

		myNodeManager.addNodeAndReference(myDevice, myMethod,
				Identifiers.HasComponent);
		MethodManagerUaNode m = (MethodManagerUaNode) myNodeManager
				.getMethodManager();
		m.addCallListener(myMethodManagerListener);
	}

	private void createStaticArrayVariable(String dataTypeName,
			NodeId dataType, Object initialValue) throws StatusException {
		final NodeId nodeId = new NodeId(complianceNamespaceIndex, dataTypeName);
		UaType type = server.getNodeManagerRoot().getType(dataType);
		UaVariableNode node = new CacheVariable(complianceNodeManager, nodeId,
				dataTypeName, Locale.ENGLISH);
		node.setDataType(type);
		node.setTypeDefinition(type);
		node.setValueRank(ValueRanks.OneDimension);
		node.setArrayDimensions(new UnsignedInteger[] { UnsignedInteger
				.valueOf(Array.getLength(initialValue)) });

		node.setValue(new DataValue(new Variant(initialValue), StatusCode.GOOD,
				new DateTime(), new DateTime()));
		staticArrayVariableFolder.addReference(node, Identifiers.HasComponent,
				false);
	}

	private void deleteNode(QualifiedName nodeName) throws StatusException {
		UaNode node = myObjectsFolder.getComponent(nodeName);
		if (node != null) {
			server.getNodeManagerRoot().beginModelChange();
			try {
				myNodeManager.deleteNode(node, true, true);
			} finally {
				server.getNodeManagerRoot().endModelChange();
			}
		} else
			println("MyObjects does not contain a component with name "
					+ nodeName);
	}

	/**
	 * @return
	 * @throws RuntimeException
	 */
	private byte[] getNextUserEventId() throws RuntimeException {
		return BaseEventType.createEventId(eventId++);
	}

	/**
	 * 
	 */
	private void inactivateAlarm() {
		if (myAlarm.isEnabled() && myAlarm.isActive()) {
			println("inactivateAlarm");
			myAlarm.setActive(false);
			myAlarm.setRetain(!myAlarm.isAcked());
			triggerEvent(myAlarm);
		}
	}

	/**
	 * Initialize the information to the Server BuildInfo structure
	 */
	private void initBuildInfo() {
		// Initialize BuildInfo - using the version info from the SDK
		// You should replace this with your own build information

		final BuildInfoType buildInfo = server.getNodeManagerRoot()
				.getServerData().getServerStatus().getBuildInfo();

		// Fetch version information from the package manifest
		final Package sdkPackage = UaServer.class.getPackage();
		final String implementationVersion = sdkPackage
				.getImplementationVersion();
		if (implementationVersion != null) {
			int splitIndex = implementationVersion.lastIndexOf(".");
			final String softwareVersion = implementationVersion.substring(0,
					splitIndex);
			String buildNumber = implementationVersion
					.substring(splitIndex + 1);

			buildInfo.setManufacturerName(sdkPackage.getImplementationVendor());
			buildInfo.setSoftwareVersion(softwareVersion);
			buildInfo.setBuildNumber(buildNumber);
		}

		final URL classFile = UaServer.class
				.getResource("/com/prosysopc/ua/samples/SampleConsoleServer.class");
		if (classFile != null) {
			final File mfFile = new File(classFile.getFile());
			GregorianCalendar c = new GregorianCalendar();
			c.setTimeInMillis(mfFile.lastModified());
			buildInfo.setBuildDate(new DateTime(c));
		}
	}

	/**
	 * Initialize the attributes of history nodes
	 */
	private void initHistory() {
		myLevelHistory = new ValueHistory(myLevel);
		// History is being collected
		myLevel.setHistorizing(true);
		// History can be read
		final EnumSet<AccessLevel> READ_WRITE_HISTORYREAD = EnumSet.of(
				AccessLevel.CurrentRead, AccessLevel.CurrentWrite,
				AccessLevel.HistoryRead);
		myLevel.setAccessLevel(READ_WRITE_HISTORYREAD);
	}

	/**
	 * Starts the simulation of the level measurement.
	 */
	private void startSimulation() {
		simulator.scheduleAtFixedRate(simulationTask, 1000, 1000,
				TimeUnit.MILLISECONDS);
		logger.info("Simulation started.");
	}

	/**
	 * Ends simulation.
	 */
	private void stopSimulation() {
		simulator.shutdown();
		logger.info("Simulation stopped.");
	}

	/**
	 * Send an event notification.
	 * 
	 * @param event
	 *            The event to trigger.
	 */
	private void triggerEvent(BaseEventType event) {
		// Trigger event
		final DateTime now = DateTime.currentTime();
		byte[] myEventId = getNextUserEventId();
		event.triggerEvent(now, now, myEventId);
	}

	/**
	 * Create a sample address space with a new folder, a device object, a level
	 * variable, and an alarm condition.
	 * <p>
	 * The method demonstrates the basic means to create the nodes and
	 * references into the address space.
	 * <p>
	 * Simulation of the level measurement is defined in
	 * {@link #startSimulation()}
	 * 
	 * @throws StatusException
	 *             if the referred type nodes are not found from the address
	 *             space
	 * 
	 */
	protected void createAddressSpace() throws StatusException {
		// My Node Manager
		myNodeManager = new NodeManagerUaNode(server,
				"http://www.prosysopc.com/OPCUA/SampleAddressSpace");

		myNodeManager.addListener(myNodeManagerListener);

		// My Event Manager Listener
		myNodeManager.getEventManager().setListener(myEventManagerListener);

		// My I/O Manager Listener
		myNodeManager.getIoManager().setListener(myIoManagerListener);

		// My HistoryManager
		myNodeManager.getHistoryManager().setListener(myHistoryManagerListener);

		// +++ My nodes +++

		int ns = myNodeManager.getNamespaceIndex();

		// UA types and folders which we will use
		final UaObject objectsFolder = server.getNodeManagerRoot()
				.getObjectsFolder();
		final UaType baseObjectType = server.getNodeManagerRoot().getType(
				Identifiers.BaseObjectType);
		final UaType baseDataVariableType = server.getNodeManagerRoot()
				.getType(Identifiers.BaseDataVariableType);

		// Folder for my objects

		final NodeId myObjectsFolderId = new NodeId(ns, "MyObjectsFolder");
		myObjectsFolder = new FolderType(myNodeManager, myObjectsFolderId,
				"MyObjects", Locale.ENGLISH);
		myNodeManager.addNodeAndReference(objectsFolder, myObjectsFolder,
				Identifiers.Organizes);

		// My Device Type

		final NodeId myDeviceTypeId = new NodeId(ns, "MyDeviceType");
		UaObjectType myDeviceType = new UaObjectTypeNode(myNodeManager,
				myDeviceTypeId, "MyDeviceType", Locale.ENGLISH);
		myNodeManager.addNodeAndReference(baseObjectType, myDeviceType,
				Identifiers.HasSubtype);

		// My Device

		final NodeId myDeviceId = new NodeId(ns, "MyDevice");
		myDevice = new UaObjectNode(myNodeManager, myDeviceId, "MyDevice",
				Locale.ENGLISH);
		myDevice.setTypeDefinition(myDeviceType);
		myObjectsFolder.addReference(myDevice, Identifiers.HasComponent, false);

		// My Level Type

		final NodeId myLevelTypeId = new NodeId(ns, "MyLevelType");
		UaType myLevelType = myNodeManager.addType(myLevelTypeId,
				"MyLevelType", baseDataVariableType);

		// My Level Measurement

		final NodeId myLevelId = new NodeId(ns, "MyLevel");
		UaType doubleType = server.getNodeManagerRoot().getType(
				Identifiers.Double);
		myLevel = new CacheVariable(myNodeManager, myLevelId, "MyLevel",
				Locale.ENGLISH);
		myLevel.setDataType(doubleType);
		myLevel.setTypeDefinition(myLevelType);
		myDevice.addReference(myLevel, Identifiers.HasComponent, false);

		// My Switch
		// Use PlainVariable and addComponent() to add it to myDevice
		// Note that we use NodeIds instead of UaNodes to define the data type
		// and type definition

		NodeId mySwitchId = new NodeId(ns, "MySwitch");
		mySwitch = new PlainVariable<Boolean>(myNodeManager, mySwitchId,
				"MySwitch", Locale.ENGLISH);
		mySwitch.setDataTypeId(Identifiers.Boolean);
		mySwitch.setTypeDefinitionId(Identifiers.BaseDataVariableType);
		myDevice.addComponent(mySwitch); // addReference(...Identifiers.HasComponent...);

		// Initial value
		mySwitch.setCurrentValue(false);

		// A sample alarm node
		createAlarmNode(ns);

		// A sample method node
		createMethodNode(ns);

		// A sample node manager that can handle a big amount of UA nodes
		// without creating UaNode objects in memory
		createBigNodeManager();

		// More specific nodes to enable OPC UA compliance testing of more
		// advanced features
		createComplianceNodes();

		logger.info("Address space created.");
	}

	protected UaVariableNode createStaticVariable(String dataTypeName,
			NodeId dataType, Object initialValue) throws StatusException {
		final NodeId nodeId = new NodeId(complianceNamespaceIndex, dataTypeName);
		UaType type = server.getNodeManagerRoot().getType(dataType);
		UaVariableNode node = new CacheVariable(complianceNodeManager, nodeId,
				dataTypeName, Locale.ENGLISH);
		node.setDataType(type);
		node.setTypeDefinition(type);
		node.setValue(new DataValue(new Variant(initialValue), StatusCode.GOOD,
				new DateTime(), new DateTime()));
		staticVariableFolder
				.addReference(node, Identifiers.HasComponent, false);
		return node;
	}

	protected void initialize() throws SecureIdentityException, IOException,
			UaServerException {
		// *** Create the server
		server = new UaServer();

		// Use PKI files to keep track of the trusted and rejected client
		// certificates...
		final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
		server.setCertificateValidator(validator);
		validator.setValidationListener(validationListener);

		// *** Application Identity
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText(APP_NAME,
				Locale.ENGLISH));
		// 'localhost' (all lower case) in the URI is converted to the actual
		// host name of the computer in which the application is run
		appDescription
				.setApplicationUri("urn:localhost:UA:SampleConsoleServer");
		appDescription
				.setProductUri("urn:prosysopc.com:UA:SampleConsoleServer");

		// *** Server Endpoints
		// the port for the binary protocol
		server.setPort(52520);
		// add 'localhost' to the endpoint list
		server.setUseLocalhost(true);

		// optional server name part of the URI
		server.setServerName("OPCUA/SampleConsoleServer");
		// Add the IP address(es) of the server host to the endpoints
		server.setUseAllIpAddresses(true);

		// Define the Server application identity, including the security
		// certificate.
		// We do this after defining the endpoints to be able to use
		// getHostNames()
		final ApplicationIdentity identity = ApplicationIdentity
				.loadOrCreateCertificate(appDescription, "Sample Organisation",
				/* Private Key Password */"opcua",
				/* Key File Path */new File(validator.getBaseDir(), "private"),
				/* Enable renewing the certificate */true,
				/* Additional host names for the certificate */server
						.getHostNames());

		server.setApplicationIdentity(identity);

		// *** Security settings
		// Define the security modes to support - ALL is the default
		server.setSecurityModes(SecurityMode.ALL);

		// Define the supported user Token policies
		server.addUserTokenPolicy(UserTokenPolicy.ANONYMOUS);
		server.addUserTokenPolicy(UserTokenPolicy.SECURE_USERNAME_PASSWORD);
		server.addUserTokenPolicy(UserTokenPolicy.SECURE_CERTIFICATE);
		// Define a validator for checking the user accounts
		server.setUserValidator(userValidator);

		// Register on the local discovery server (if present)
		server.setDiscoveryServerUrl("opc.tcp://localhost:4840");

		// init() creates the default endpoints according to the above settings
		server.init();

		initBuildInfo();

		// "Safety limits" for ill-behaving clients
		server.getSessionManager().setMaxSessionCount(50);
		server.getSessionManager().setMaxSessionTimeout(3600000); // one hour
		server.getSubscriptionManager().setMaxSubscriptionCount(50);

		// You can do your own additions to server initializations here

	}

	/**
	 * Run the server.
	 * 
	 * @param enableSessionDiagnostics
	 * @throws UaServerException
	 * @throws StatusException
	 */
	protected void run(boolean enableSessionDiagnostics)
			throws UaServerException, StatusException {
		server.start();
		initHistory();
		if (enableSessionDiagnostics)
			server.getNodeManagerRoot().getServerData().getServerDiagnostics()
					.setEnabled(true);
		startSimulation();

		// *** Main Menu Loop
		mainMenu();

		// *** End
		stopSimulation();
		// Notify the clients about a shutdown, with a 5 second delay
		println("Shutting down...");
		server.shutdown(1, new LocalizedText("Closed by user", Locale.ENGLISH));
		println("Closed.");
	}

	/**
	 * Send an event
	 */
	protected void sendEvent() {
		// You can define your own event type as well: we use the
		// standard SystemEventType here
		BaseEventType newEvent = new EventType(myNodeManager,
				Identifiers.SystemEventType);
		newEvent.setMessage("New event");
		// Set the severity of the event between 1 and 1000
		newEvent.setSeverity(1);
		// By default the event is sent for the "Server" object. If you want to
		// send it for some other object, use Source (or SourceNode), e.g.
		// newEvent.setSource(myDevice);
		triggerEvent(newEvent);
	}

	/*
	 * Main loop for user selecting OPC UA calls
	 */
	void mainMenu() {

		/******************************************************************************/
		/* Wait for user command to execute next action. */
		do {
			printMenu();

			try {
				switch (readAction()) {
				case CLOSE:
					return;
				case ADD_NODE:
					println("Enter the name of the new node (enter 'x' to cancel)");
					String name = readInput();
					if (!name.equals("x"))
						addNode(name);
					break;
				case DELETE_NODE:
					println("Enter the name of the node to delete (enter 'x' to cancel)");
					String input = readInput();
					if (!input.equals("x")) {
						QualifiedName nodeName = new QualifiedName(
								myNodeManager.getNamespaceIndex(), input);
						deleteNode(nodeName);
					}
					break;
				case ENABLE_DIAGNOSTICS:
					final PlainProperty<Boolean> enabledFlag = server
							.getNodeManagerRoot().getServerData()
							.getServerDiagnostics().getEnabledFlag();
					boolean newValue = !enabledFlag.getCurrentValue();
					enabledFlag.setCurrentValue(newValue);
					println("Server Diagnostics "
							+ (newValue ? "Enabled" : "Disabled"));
					break;
				case SEND_EVENT:
					sendEvent();

				default:
					continue;
				}
			} catch (Exception e) {
				printException(e);
			}

		} while (true);
		/******************************************************************************/
	}
}
