/**
 * Prosys OPC UA Java SDK
 *
 * Copyright (c) 2009-2012 Prosys PMS Ltd., <http://www.prosysopc.com>. 
 * All rights reserved.
 */
//package com.prosysopc.ua.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowsePathTarget;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.ElementOperand;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.EventFilter;
import org.opcfoundation.ua.core.FilterOperator;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.LiteralOperand;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.NotificationData;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.RelativePathElement;
import org.opcfoundation.ua.core.ServerState;
import org.opcfoundation.ua.core.ServerStatusDataType;
import org.opcfoundation.ua.core.SimpleAttributeOperand;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.AttributesUtil;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.ContentFilterBuilder;
import com.prosysopc.ua.MethodCallStatusException;
import com.prosysopc.ua.MonitoredItemBase;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.client.MonitoredEventItem;
import com.prosysopc.ua.client.MonitoredEventItemListener;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.ServerList;
import com.prosysopc.ua.client.ServerListException;
import com.prosysopc.ua.client.ServerStatusListener;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.SubscriptionAliveListener;
import com.prosysopc.ua.client.SubscriptionNotificationListener;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.MethodArgumentException;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaInstance;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;

/**
 * A sample OPC UA client, running from the console.
 */
public class SampleConsoleClient {

	// Action codes for readAction, etc.
	protected static final int ACTION_ALL = -4;
	protected static final int ACTION_BACK = -2;
	protected static final int ACTION_RETURN = -1;
	protected static final int ACTION_ROOT = -3;
	protected static final int ACTION_TRANSLATE = -6;
	protected static final int ACTION_UP = -5;

	/**
	 * The name of the application.
	 */
	protected final static String APP_NAME = "SampleConsoleClient";

	public static void main(String[] args) throws URISyntaxException,
			SecureIdentityException, IOException, SessionActivationException,
			ServerListException {
		// Load Log4j configurations from external file
		PropertyConfigurator.configureAndWatch(SampleConsoleClient.class
				.getResource("log.properties").getFile(), 5000);
		SampleConsoleClient sampleConsoleClient = new SampleConsoleClient();
		try {
			if (!sampleConsoleClient.parseCmdLineArgs(args)) {
				usage();
				return;
			}
		} catch (IllegalArgumentException e) {
			println("Invalid cmd line argument: " + e.getMessage());
			usage();
			return;
		}

		sampleConsoleClient.initialize(args);
		// Show the menu, which is the main loop of the client application
		sampleConsoleClient.mainMenu();

		println(APP_NAME + ": Closed");

	}

	/**
	 * @param string
	 */
	protected static void println(String string) {
		System.out.println(string);
	}

	/**
	 * 
	 */
	protected static void usage() {
		println("Usage: " + APP_NAME + " [-d] [-t] [-n] [-?] [serverUri]");
		println("   -d         Connect to a discovery server");
		println("   -n nodeId  Define the NodeId to select after connect (requires serverUri)");
		println("   -s n|s|e[bits]   Define the security mode (n=none/s=sign/e=signAndEncrypt). Default is none.");
		println("                    Oprionally, define the bit strength (128 or 256) you want to use for encryption. Default is 128");
		println("   -m nodeId  Subscribe to the given node at start up");
		println("   -t         Output stack trace for errors");
		println("   -?         Show this help text");
		println("   serverUri  The address of the server to connect to. If you do not specify it, you will be prompted for it.");
		println("");
		println(" Examples of valid arguments:");
		println("   opc.tcp://localhost:4841                            (UA Demo Server)");
		println("   opc.tcp://localhost:52520/OPCUA/SampleConsoleServer (Prosys Sample Server)");
		println("   opc.tcp://localhost:51210/UA/SampleServer           (OPC Foundation Sample Server)");
		println("   -d opc.tcp://localhost:4840/UADiscovery             (OPC Foundation Discovery Server)");
	}

	protected UaClient client;

	protected final List<String> cmdSequence = new ArrayList<String>();

	protected boolean connectToDiscoveryServer = false;

	/**
	 * A sampler listener for monitored data changes.
	 */
	protected MonitoredDataItemListener dataChangeListener = new MonitoredDataItemListener() {
		@Override
		public void onDataChange(MonitoredDataItem sender, DataValue prevValue,
				DataValue value) {
			println(dataValueToString(sender.getNodeId(),
					sender.getAttributeId(), value));
		}
	};

	/*
	 * Main loop for user selecting OPC UA calls
	 */

	/**
	 * A sampler listener for monitored event notifications.
	 */
	protected final MonitoredEventItemListener eventListener = new MonitoredEventItemListener() {
		@Override
		public void onEvent(MonitoredEventItem sender, Variant[] eventFields) {
			println(eventToString(sender.getNodeId(), eventFields));
		}
	};

	protected final List<String> initialMonitoredItems = new ArrayList<String>();
	protected String passWord;
	// requested fields for event subscriptions
	protected final QualifiedName[] requestedEventFields = {
			new QualifiedName("EventType"), new QualifiedName("Message"),
			new QualifiedName("SourceName"), new QualifiedName("Time"),
			new QualifiedName("Severity"), new QualifiedName("ActiveState/Id") };

	protected SecurityMode securityMode = SecurityMode.NONE;

	/**
	 * A sampler listener for server status changes.
	 */
	protected ServerStatusListener serverStatusListener = new ServerStatusListener() {
		@Override
		public void onShutdown(UaClient uaClient, long secondsTillShutdown,
				LocalizedText shutdownReason) {
			// Called when the server state changes to Shutdown
			printf("Server shutdown in %d seconds. Reason: %s\n",
					secondsTillShutdown, shutdownReason.getText());
		}

		@Override
		public void onStateChange(UaClient uaClient, ServerState oldState,
				ServerState newState) {
			// Called whenever the server state changes
			printf("ServerState changed from %s to %s\n", oldState, newState);
			if (newState.equals(ServerState.Unknown))
				println("ServerStatusError: " + uaClient.getServerStatusError());
		}

		@Override
		public void onStatusChange(UaClient uaClient,
				ServerStatusDataType status) {
			// Called whenever the server status changes, typically every
			// StatusCheckInterval defined in the UaClient.
			// println("ServerStatus: " + status);
		}
	};
	protected String serverUri = "opc.tcp://localhost:52520/OPCUA/SampleConsoleServer";
	protected int sessionCount = 0;
	protected final boolean showReadValueDataType = true;
	protected boolean stackTraceOnException = false;
	protected Subscription subscription;

	/**
	 * A sampler listener for subscription alive events.
	 */
	protected SubscriptionAliveListener subscriptionAliveListener = new SubscriptionAliveListener() {

		@Override
		public void onAlive(Subscription s) {
			// the client acknowledged that the
			// connection is alive,
			// although there were no changes to send
			println(String.format(
					"%tc Subscription alive: ID=%d lastAlive=%tc",
					Calendar.getInstance(), s.getSubscriptionId().getValue(),
					s.getLastAlive()));
		}

		@Override
		public void onTimeout(Subscription s) {
			// the client did not acknowledged that the
			// connection is alive,
			// and the maxKeepAliveCount has been
			// exceeded
			println(String.format(
					"%tc Subscription timeout: ID=%d lastAlive=%tc",
					Calendar.getInstance(), s.getSubscriptionId().getValue(),
					s.getLastAlive()));

		}

	};

	/**
	 * A sampler listener for subscription notifications.
	 */
	protected SubscriptionNotificationListener subscriptionListener = new SubscriptionNotificationListener() {

		@Override
		public void onBufferOverflow(Subscription subscription,
				UnsignedInteger sequenceNumber,
				ExtensionObject[] notificationData) {
			println("*** SUBCRIPTION BUFFER OVERFLOW ***");
		}

		@Override
		public void onDataChange(Subscription subscription,
				MonitoredDataItem item, DataValue newValue) {
			// Called for each data change notification
		}

		@Override
		public void onError(Subscription subscription, Object notification,
				Exception exception) {
			// Called if the parsing of the notification data fails,
			// notification is either a MonitoredItemNotification or
			// an EventList
			printException(exception);
		}

		@Override
		public void onEvent(Subscription subscription, MonitoredEventItem item,
				Variant[] eventFields) {
			// Called for each event notification
		}

		@Override
		public long onMissingData(UnsignedInteger lastSequenceNumber,
				long sequenceNumber, long newSequenceNumber,
				StatusCode serviceResult) {
			// Called if a data packet is missed due to communication errors and
			// failing Republish
			println("Data missed: lastSequenceNumber=" + lastSequenceNumber
					+ " newSequenceNumber=" + newSequenceNumber);
			return newSequenceNumber; // Accept the default
		}

		@Override
		public void onNotificationData(Subscription subscription,
				NotificationData notification) {
			// Called after a complete notification data package is
			// handled
			// if (notification instanceof DataChangeNotification) {
			// DataChangeNotification d = (DataChangeNotification) notification;
			// println("onNotificationData: " + d.getMonitoredItems().length);
			// }

		}

		@Override
		public void onStatusChange(Subscription subscription,
				StatusCode oldStatus, StatusCode newStatus,
				DiagnosticInfo diagnosticInfo) {
			// Called when the subscription status has changed in
			// the server
		}

	};
	protected String userName;

	/**
	 * A sampler listener for certificate validation results.
	 */
	protected CertificateValidationListener validationListener = new CertificateValidationListener() {

		@Override
		public ValidationResult onValidate(Cert certificate,
				ApplicationDescription applicationDescription,
				EnumSet<CertificateCheck> passedChecks) {
			// Called whenever the PkiFileBasedCertificateValidator has
			// validated a certificate
			println("");
			println("*** The Server Certificate : ");
			println("");
			println("Subject   : "
					+ certificate.getCertificate().getSubjectX500Principal()
							.toString());
			println("Issued by : "
					+ certificate.getCertificate().getIssuerX500Principal()
							.toString());
			println("Valid from: "
					+ certificate.getCertificate().getNotBefore().toString());
			println("        to: "
					+ certificate.getCertificate().getNotAfter().toString());
			println("");
			if (!passedChecks.contains(CertificateCheck.Signature))
				println("* The Certificate is NOT SIGNED BY A TRUSTED SIGNER!");
			if (!passedChecks.contains(CertificateCheck.Validity)) {
				Date today = new Date();
				final boolean isYoung = certificate.getCertificate()
						.getNotBefore().compareTo(today) > 0;
				final boolean isOld = certificate.getCertificate()
						.getNotAfter().compareTo(today) < 0;
				final String oldOrYoung = isOld ? "(anymore)"
						: (isYoung ? "(yet)" : "");

				println("* The Certificate time interval IS NOT VALID "
						+ oldOrYoung + "!");
			}
			if (!passedChecks.contains(CertificateCheck.Uri)) {
				println("* The Certificate URI DOES NOT MATCH the server URI!");
				println("  serverURI="
						+ applicationDescription.getApplicationUri());
			}
			if (passedChecks.contains(CertificateCheck.SelfSigned))
				println("* The Certificate is self-signed.");
			println("");
			// If the certificate is trusted, valid and verified, accept it
			if (passedChecks.containsAll(CertificateCheck.COMPULSORY))
				return ValidationResult.AcceptPermanently;
			do {
				println("Note: If the certificate is not OK,");
				println("you will be prompted again, even if you answer 'Always' here.");
				println("");
				println("Do you want to accept this certificate?\n"
						+ " (A=Always, Y=Yes, this time, N=No)\n"
						+ " (D=Show Details of the Certificate)");
				String input = readInput(false).toLowerCase();
				if (input.equals("a"))
					// if the certificate is not valid anymore or the signature
					// is not verified, you will be prompted again, even if you
					// select always here
					return ValidationResult.AcceptPermanently;

				if (input.equals("y"))
					return ValidationResult.AcceptOnce;
				if (input.equals("n"))
					return ValidationResult.Reject;
				if (input.equals("d"))
					println("Certificate Details:"
							+ certificate.getCertificate().toString());
			} while (true);
		}

	};

	NodeId nodeId = null;

	public SampleConsoleClient() {

	}

	/**
	 * @param qualifiedName
	 * @return
	 */
	protected QualifiedName[] createBrowsePath(QualifiedName qualifiedName) {
		if (!qualifiedName.getName().contains("/"))
			return new QualifiedName[] { qualifiedName };
		String[] names = qualifiedName.getName().split("/");
		QualifiedName[] result = new QualifiedName[names.length];
		for (int i = 0; i < names.length; i++)
			result[i] = new QualifiedName(names[i]);
		return result;
	}

	/**
	 * @param nodeId
	 * @param attributeId
	 * @return
	 */
	protected MonitoredDataItem createMonitoredDataItem(NodeId nodeId,
			UnsignedInteger attributeId) {
		MonitoredDataItem dataItem = new MonitoredDataItem(nodeId, attributeId,
				MonitoringMode.Reporting);
		dataItem.addChangeListener(dataChangeListener);
		DataChangeFilter filter = new DataChangeFilter();
		filter.setDeadbandValue(1.00);
		filter.setTrigger(DataChangeTrigger.StatusValue);
		filter.setDeadbandType(UnsignedInteger.valueOf(DeadbandType.Percent
				.getValue()));
		return dataItem;
	}

	/**
	 * @param nodeId
	 * @return
	 * @throws StatusException
	 */
	protected MonitoredEventItem createMonitoredEventItem(NodeId nodeId)
			throws StatusException {
		// Create an EventFilter that will listen to all events
		// from the node, and requests the specific event
		// fields defined in 'requestedEventFields'

		// This defines the type in which the properties are
		// defined
		// It should be defined per browsePath, but for example
		// the Java SDK servers ignore the value at the moment
		NodeId eventTypeId = Identifiers.BaseEventType;
		UnsignedInteger eventAttributeId = Attributes.Value;
		String indexRange = null;
		SimpleAttributeOperand[] selectClauses = new SimpleAttributeOperand[requestedEventFields.length + 1];
		for (int i = 0; i < requestedEventFields.length; i++) {
			QualifiedName[] browsePath = createBrowsePath(requestedEventFields[i]);
			selectClauses[i] = new SimpleAttributeOperand(eventTypeId,
					browsePath, eventAttributeId, indexRange);
		}
		// Add a field to get the Nodeid of the event source
		selectClauses[requestedEventFields.length] = new SimpleAttributeOperand(
				eventTypeId, null, Attributes.NodeId, null);
		EventFilter filter = new EventFilter();
		// Event field selection
		filter.setSelectClauses(selectClauses);

		// Event filtering: the following sample creates a
		// "Not OfType GeneralModelChangeEventType" filter
		ContentFilterBuilder fb = new ContentFilterBuilder();
		// The element operand refers to another operand -
		// operand #1 in this case which is the next,
		// LiteralOperand
		fb.add(FilterOperator.Not,
				new ElementOperand(UnsignedInteger.valueOf(1)));
		final LiteralOperand filteredType = new LiteralOperand(new Variant(
				Identifiers.GeneralModelChangeEventType));
		fb.add(FilterOperator.OfType, filteredType);

		// // Another example:
		// // OfType(custom NodeId) And (Severity > 800)
		// // Comment the previous example out if you try this
		// // Element #0
		// fb.add(FilterOperator.And, new ElementOperand(
		// UnsignedInteger.valueOf(1)),
		// new ElementOperand(UnsignedInteger.valueOf(2)));
		// // Element #1
		// final LiteralOperand filteredType = new
		// LiteralOperand(
		// new Variant(new NodeId(3, 1101)));
		// fb.add(FilterOperator.OfType, filteredType);
		// // Element #2
		// QualifiedName[] severityPath = { new QualifiedName(
		// "Severity") };
		// fb.add(FilterOperator.GreaterThan,
		// new SimpleAttributeOperand(
		// Identifiers.ConditionType,
		// severityPath, Attributes.Value, null),
		// new LiteralOperand(new Variant(800)));

		// Apply the filter to Where-clause
		filter.setWhereClause(fb.getContentFilter());

		// Create the item
		MonitoredEventItem eventItem = new MonitoredEventItem(nodeId, filter);
		eventItem.addEventListener(eventListener);
		return eventItem;
	}

	/**
	 * @param nodeId
	 * @param attributeId
	 * @return
	 * @throws ServiceException
	 * @throws StatusException
	 */
	protected void createMonitoredItem(Subscription sub, NodeId nodeId,
			UnsignedInteger attributeId) throws ServiceException,
			StatusException {
		UnsignedInteger monitoredItemId = null;
		// Create the monitored item, if it is not already in the
		// subscription
		if (!sub.hasItem(nodeId, attributeId))
			// Event or DataChange?
			if (attributeId == Attributes.EventNotifier) {
				MonitoredEventItem eventItem = createMonitoredEventItem(nodeId);
				sub.addItem(eventItem);
				monitoredItemId = eventItem.getMonitoredItemId();

				// Refresh the current state
				// client.call(
				// Identifiers.Server,
				// Identifiers.ConditionType_ConditionRefresh,
				// new Variant[] { new Variant(subscription
				// .getSubscriptionId()) });

			} else {
				MonitoredDataItem dataItem = createMonitoredDataItem(nodeId,
						attributeId);
				// Set the filter if you want to limit data changes
				// dataItem.setDataChangeFilter(filter);
				sub.addItem(dataItem);
				monitoredItemId = dataItem.getMonitoredItemId();
			}
		println("-------------------------------------------------------");
		println("Subscription: Id=" + sub.getSubscriptionId() + " ItemId="
				+ monitoredItemId);
	}

	/**
	 * @param subscription
	 * @param nodeIds
	 * @return
	 * @throws ServiceException
	 * @throws StatusException
	 */
	protected void createMonitoredItemsForChildren(Subscription subscription,
			String nodeIds) throws ServiceException, StatusException {
		{
			String parentNodeId = nodeIds.substring(0, nodeIds.length() - 2);
			NodeId nodeId = NodeId.parseNodeId(parentNodeId);
			List<ReferenceDescription> refs = client.getAddressSpace().browse(
					nodeId, BrowseDirection.Forward, Identifiers.HasChild,
					true, NodeClass.Variable);
			List<MonitoredItem> items = new ArrayList<MonitoredItem>(
					refs.size());
			println("Subscribing to " + refs.size() + " items under node "
					+ parentNodeId);
			for (ReferenceDescription r : refs)
				try {
					items.add(createMonitoredDataItem(client
							.getNamespaceTable().toNodeId(r.getNodeId()),
							Attributes.Value));
				} catch (ServiceResultException e) {
					printException(e);
				}
			try {
				subscription.addItems(items.toArray(new MonitoredItem[items
						.size()]));
				println("done");
			} catch (Exception e) {
				printException(e);
			}
		}
	}

	/**
	 * @return
	 * @throws ServiceException
	 * @throws StatusException
	 */
	protected Subscription createSubscription() throws ServiceException,
			StatusException {
		Subscription subscription;
		// Create the subscription if it does not yet exist
		subscription = new Subscription();
		// Default PublishingInterval is 1000 ms
		// subscription.setPublishingInterval(1000);

		// LifetimeCount should be at least 3 times KeepAliveCount
		// subscription.setLifetimeCount(1000);
		// subscription.setMaxKeepAliveCount(50);

		// If you are expecting big data changes, it may be better to break the
		// notifications to smaller parts
		// subscription.setMaxNotificationsPerPublish(1000);

		// Listen to the alive and timeout events of the
		// subscription
		subscription.addAliveListener(subscriptionAliveListener);
		// Listen to notifications - the data changes and events are
		// handled using the item listeners (see below), but in many
		// occasions, it may be best to use the subscription
		// listener also to handle those notifications
		subscription.addNotificationListener(subscriptionListener);
		// Add it to the client, if it wasn't there already
		if (!client.hasSubscription(subscription.getSubscriptionId()))
			client.addSubscription(subscription);
		return subscription;
	}

	/**
	 * @param attributeId
	 * @param nodeId
	 * @param value
	 * @return
	 */
	protected String dataValueToString(NodeId nodeId,
			UnsignedInteger attributeId, DataValue value) {
		StringBuilder sb = new StringBuilder();
		sb.append("Node: ");
		sb.append(nodeId);
		sb.append(".");
		sb.append(AttributesUtil.toString(attributeId));
		sb.append(" | Status: ");
		sb.append(value.getStatusCode());
		if (value.getStatusCode().isNotBad()) {
			sb.append(" | Value: ");
			if (value.isNull())
				sb.append("NULL");
			else {
				if (showReadValueDataType)
					try {
						UaVariable variable = (UaVariable) client
								.getAddressSpace().getNode(nodeId);
						NodeId dataTypeId = variable.getDataTypeId();
						UaType dataType = variable.getDataType();
						if (dataType == null)
							dataType = client.getAddressSpace().getType(
									dataTypeId);

						Variant variant = value.getValue();
						variant.getCompositeClass();
						if (attributeId.equals(Attributes.Value))
							if (dataType != null)
								sb.append("("
										+ dataType.getDisplayName().getText()
										+ ")");
							else
								sb.append("(DataTypeId: " + dataTypeId + ")");
					} catch (ServiceException e) {
					} catch (AddressSpaceException e) {
					} catch (StatusException e) {
					}
				final Object v = value.getValue().getValue();
				if (value.getValue().isArray())
					sb.append(MultiDimensionArrayUtils.toString(v));
				else
					sb.append(v);
			}
		}
		sb.append(dateTimeToString(" | ServerTimestamp: ",
				value.getServerTimestamp(), value.getServerPicoseconds()));
		sb.append(dateTimeToString(" | SourceTimestamp: ",
				value.getSourceTimestamp(), value.getSourcePicoseconds()));
		return sb.toString();
	}

	/**
	 * @param title
	 * @param timestamp
	 * @param picoSeconds
	 * @return
	 */
	protected String dateTimeToString(String title, DateTime timestamp,
			UnsignedShort picoSeconds) {
		if ((timestamp != null) && !timestamp.equals(DateTime.MIN_VALUE)) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy MMM dd (zzz) HH:mm:ss.SSS");
			StringBuilder sb = new StringBuilder(title);
			sb.append(format.format(timestamp
					.getCalendar(TimeZone.getDefault()).getTime()));
			if ((picoSeconds != null)
					&& !picoSeconds.equals(UnsignedShort.valueOf(0)))
				sb.append(String.format("/%d picos", picoSeconds.getValue()));
			return sb.toString();
		}
		return "";
	}

	/**
	 * @throws ServerListException
	 * @throws URISyntaxException
	 */
	protected void discover() throws ServerListException, URISyntaxException {
		ApplicationDescription serverApp;
		serverApp = discoverServer(client.getUri());
		if (serverApp != null) {
			final String[] discoveryUrls = serverApp.getDiscoveryUrls();
			if (discoveryUrls != null)
				for (String url : discoveryUrls) {
					// Select opc.tcp endpoints only
					URI uri = new URI(url);
					if (uri.getScheme().equals(Protocol.Opc.toString())) {
						client.setUri(url);
						println("Selected application " + url);
						break;
					}

				}
		}
	}

	/**
	 * @param eventFields
	 * @return
	 */
	protected String eventFieldsToString(Variant[] eventFields) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < eventFields.length; i++) {
			Object fieldValue = eventFields[i] == null ? null : eventFields[i]
					.getValue();
			// Find the BrowseName of the node corresponding to NodeId values
			try {
				UaNode node = null;
				if (fieldValue instanceof NodeId)
					node = client.getAddressSpace()
							.getNode((NodeId) fieldValue);
				else if (fieldValue instanceof ExpandedNodeId)
					node = client.getAddressSpace().getNode(
							(ExpandedNodeId) fieldValue);
				if (node != null)
					fieldValue = String.format("%s {%s}", node.getBrowseName(),
							fieldValue);
			} catch (Exception e) {
				// Node not found, just use fieldValue
			}
			if (i < requestedEventFields.length) {
				QualifiedName fieldName = requestedEventFields[i];
				sb.append(fieldName.getName() + "=" + fieldValue + "; ");
			} else
				sb.append(fieldValue + "; ");
		}
		return sb.toString();
	}

	protected String eventToString(NodeId nodeId, Variant[] eventFields) {
		return String.format("Node: %s Fields: %s", nodeId,
				eventFieldsToString(eventFields));
	}

	/**
	 * Initializes the object, must be called after constructor
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws SecureIdentityException
	 * @throws SessionActivationException
	 * @throws ServerListException
	 */
	protected void initialize(String[] args) throws URISyntaxException,
			SecureIdentityException, IOException, SessionActivationException,
			ServerListException {

		println("Connecting to " + serverUri);

		// Create the UaClient
		client = new UaClient(serverUri);

		// Use PKI files to keep track of the trusted and rejected server
		// certificates...
		final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
		client.setCertificateValidator(validator);
		// ...and react to validation results with a custom handler (to prompt
		// the user what to do, if necessary)
		validator.setValidationListener(validationListener);

		// The ApplicationDescription is sent to the server...
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText(APP_NAME,
				Locale.ENGLISH));
		// 'localhost' (all lower case) in the URI is converted to the actual
		// host name of the computer in which the application is run
		appDescription
				.setApplicationUri("urn:localhost:UA:SampleConsoleClient");
		appDescription
				.setProductUri("urn:prosysopc.com:UA:SampleConsoleClient");
		appDescription.setApplicationType(ApplicationType.Client);

		// Define the client application identity, including the security
		// certificate
		final ApplicationIdentity identity = ApplicationIdentity
				.loadOrCreateCertificate(appDescription, "Sample Organisation",
				/* protected Key Password */"opcua",
						/* Key File Path */new File(validator.getBaseDir(),
								"protected"),
						/* Enable renewing the certificate */true);

		client.setApplicationIdentity(identity);

		// Define our user locale - the default is Locale.getDefault()
		client.setLocale(Locale.ENGLISH);

		// Define the call timeout in milliseconds. Default is 0 - to
		// use the value of UaClient.getEndpointConfiguration() which is
		// 120000 (2 min) by default
		client.setTimeout(30000);

		// StatusCheckTimeout is used to detect communication
		// problems and start automatic reconnection.
		// These are the default values:
		client.setStatusCheckTimeout(10000);
		// client.setAutoReconnect(true);

		// Listen to server status changes
		client.addServerStatusListener(serverStatusListener);

		// Define the security mode
		// - Default (in UaClient) is BASIC128RSA15_SIGN_ENCRYPT
		// client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT);
		// client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN);
		// client.setSecurityMode(SecurityMode.NONE);

		// securityMode is defined from the command line
		client.setSecurityMode(securityMode);

		// If the server supports user authentication, you can set the user
		// identity.
		if (userName == null)
			// - Default is to use Anonymous authentication, like this:
			client.setUserIdentity(new UserIdentity());
		else {
			// - Use username/password authentication (note requires security,
			// above):
			if (passWord == null) {
				print("Enter password for user " + userName + ":");
				passWord = readInput(false);
			}
			client.setUserIdentity(new UserIdentity(userName, passWord));
		}
		// - Read the user certificate and protected key from files:
		// client.setUserIdentity(new UserIdentity(new java.net.URL(
		// "my_certificate.der"), new java.net.URL("my_protectedkey.pfx"),
		// "my_protectedkey_password"));

		// Session timeout 10 minutes; default is one hour
		// client.setSessionTimeout(600000);

		// Set endpoint configuration parameters
		client.getEndpointConfiguration().setMaxByteStringLength(
				Integer.MAX_VALUE);
		client.getEndpointConfiguration().setMaxArrayLength(Integer.MAX_VALUE);

		// TCP Buffer size parameters - these may help with high traffic
		// situations.
		// See http://fasterdata.es.net/host-tuning/background/ for some hints
		// how to use them
		// TcpConnection.setReceiveBufferSize(700000);
		// TcpConnection.setSendBufferSize(700000);
	}

	/**
	 * @param s
	 * @return
	 */
	protected int parseAction(String s) {
		if (s.equals("x"))
			return ACTION_RETURN;
		if (s.equals("b"))
			return ACTION_BACK;
		if (s.equals("r"))
			return ACTION_ROOT;
		if (s.equals("a"))
			return ACTION_ALL;
		if (s.equals("u"))
			return ACTION_UP;
		if (s.equals("t"))
			return ACTION_TRANSLATE;
		return Integer.parseInt(s);
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
	protected boolean parseCmdLineArgs(String[] args)
			throws IllegalArgumentException {
		int i = 0;
		while ((args.length > i)
				&& ((args[i].startsWith("-") || args[i].startsWith("/")))) {
			if (args[i].equals("-d")) {
				println("Connecting to a discovery server.");
				connectToDiscoveryServer = true;
			} else if (args[i].equals("-n"))
				nodeId = NodeId.parseNodeId(args[++i]);
			else if (args[i].equals("-s")) {
				final char secModeStr = args[++i].charAt(0);
				final int bits = ((args[i].length() == 1) || (args[i].charAt(1) == '1')) ? 128
						: 256;
				if (secModeStr == 'n')
					securityMode = SecurityMode.NONE;
				else if (secModeStr == 's')
					securityMode = (bits == 128) ? SecurityMode.BASIC128RSA15_SIGN
							: SecurityMode.BASIC256_SIGN;
				else if (secModeStr == 'e')
					securityMode = (bits == 128) ? SecurityMode.BASIC128RSA15_SIGN_ENCRYPT
							: SecurityMode.BASIC256_SIGN_ENCRYPT;
				else
					throw new IllegalArgumentException(
							"parameter for -s is invalid, expected 'n', 's' or 'e'; was '"
									+ secModeStr + "'");

			} else if (args[i].equals("-m"))
				initialMonitoredItems.add(args[++i]);
			else if (args[i].equals("-u"))
				userName = args[++i];
			else if (args[i].equals("-p"))
				passWord = args[++i];
			else if (args[i].equals("-t"))
				stackTraceOnException = true;
			else if (args[i].equals("-?"))
				return false;
			else
				throw new IllegalArgumentException(args[i]);
			i++;
		}
		if (i < args.length) {
			serverUri = args[i++];
			while ((i < args.length) && !args[i].startsWith("#"))
				cmdSequence.add(args[i++]);
		} else
			promptServerUri();
		return true;
	}

	/**
	 * @param string
	 */
	protected void print(String string) {
		System.out.print(string);

	}

	/**
	 * @param nodeId
	 */
	protected void printCurrentNode(NodeId nodeId) {
		String nodeStr = "";
		if (client.isConnected())
			try {
				// Find the node from the NodeCache
				UaNode node = client.getAddressSpace().getNode(nodeId);
				if (node instanceof UaInstance) {
					UaNode type = ((UaInstance) node).getTypeDefinition();
					nodeStr = node.getDisplayName().getText()
							+ (type == null ? "" : ": "
									+ type.getDisplayName().getText());
				}
			} catch (ServiceException e) {
				printException(e);
			} catch (AddressSpaceException e) {
				printException(e);
			} catch (StatusException e) {
				printException(e);
			}
		println(String.format("*** Current Node: %s (ID: %s)", nodeStr, nodeId));
		println("");
	}

	protected void printEndpoints(EndpointDescription[] endpoints) {
		println("Endpoints supported by the server");
		for (EndpointDescription e : endpoints)
			println(String.format("%s [%s,%s]", e.getEndpointUrl(),
					e.getSecurityPolicyUri(), e.getSecurityMode()));

	}

	/**
	 * @param e
	 */
	protected void printException(Exception e) {
		if (stackTraceOnException)
			e.printStackTrace();
		else {
			println(e.toString());
			if (e instanceof MethodCallStatusException) {
				MethodCallStatusException me = (MethodCallStatusException) e;
				final StatusCode[] results = me.getInputArgumentResults();
				if (results != null)
					for (int i = 0; i < results.length; i++) {
						StatusCode s = results[i];
						if (s.isBad()) {
							println("Status for Input #" + i + ": " + s);
							DiagnosticInfo d = me
									.getInputArgumentDiagnosticInfos()[i];
							if (d != null)
								println("  DiagnosticInfo:" + i + ": " + d);
						}
					}
			}
			if (e.getCause() != null)
				println("Caused by: " + e.getCause());
		}
	}

	/**
	 * @param format
	 * @param args
	 */
	protected void printf(String format, Object... args) {
		System.out.printf(format, args);

	}

	/**
	 * @param method
	 *            The method node
	 * @param outputs
	 *            The output values The output values
	 * @throws AddressSpaceException
	 * @throws ServiceException
	 * @throws MethodArgumentException
	 *             if the output arguments are not valid for the node
	 * @throws StatusException
	 */
	protected void printOutputArguments(UaMethod method, Variant[] outputs)
			throws ServiceException, AddressSpaceException,
			MethodArgumentException, StatusException {
		if ((outputs != null) && (outputs.length > 0)) {
			println("Output values:");
			Argument[] outputArguments = method.getOutputArguments();
			for (int i = 0; i < outputArguments.length; i++) {
				UaNode dataType = client.getAddressSpace().getType(
						outputArguments[i].getDataType());
				println(String.format("%s: %s {%s} = %s",
						outputArguments[i].getName(), dataType.getBrowseName(),
						outputArguments[i].getDescription().getText(),
						outputs[i].getValue()));
			}
		} else
			println("OK (no output)");
	}

	/**
	 * @param supportedSecurityModes
	 */
	protected void printSecurityModes(List<SecurityMode> supportedSecurityModes) {
		println("SecurityModes supported by the server:");
		for (SecurityMode m : supportedSecurityModes)
			println(m.toString());

	}

	/**
	 * @param supportedUserIdentityTokens
	 */
	protected void printUserIdentityTokens(
			UserTokenPolicy[] supportedUserIdentityTokens) {
		println("The server supports the following user tokens:");
		for (UserTokenPolicy p : supportedUserIdentityTokens)
			println(p.getTokenType().toString());

	}

	/**
	 * @throws IllegalArgumentException
	 */
	protected void promptServerUri() throws IllegalArgumentException {
		println("No server URI defined. (Run with /? to see command line usage)");
		println("Would you like to use the default server URI\n'" + serverUri
				+ "'?\n (Y=Yes, N=No, E=Enter a different URI manually)");

		String input = readInput(false).toLowerCase();
		if (input.equals("y"))
			return; // Using the default URI
		else if (input.equals("n"))
			throw new IllegalArgumentException();
		else {
			println("Enter URL:");
			serverUri = readInput(false);
		}
	}

	// protected fields
	/**
	 * @return
	 */
	protected int readAction() {
		return parseAction(readInput(true).toLowerCase());
	}

	/**
	 * @return
	 */
	protected UnsignedInteger readAttributeId() {

		println("Select the node attribute.");
		for (long i = Attributes.NodeId.getValue(); i < Attributes.UserExecutable
				.getValue(); i++)
			printf("%d - %s\n", i,
					AttributesUtil.toString(UnsignedInteger.valueOf(i)));
		int action = readAction();
		if (action < 0)
			return null;
		UnsignedInteger attributeId = UnsignedInteger.valueOf(action);
		System.out
				.println("attribute: " + AttributesUtil.toString(attributeId));
		return attributeId;
	}

	/**
	 * @return
	 */
	protected String readInput(boolean useCmdSequence) {
		// You can provide "commands" already from the command line, in which
		// case they will be kept in cmdSequence
		if (useCmdSequence && !cmdSequence.isEmpty()) {
			String cmd = cmdSequence.remove(0);
			try {
				// Negative int values are used to pause for n seconds
				int i = Integer.parseInt(cmd);
				if (i < 0) {
					try {
						TimeUnit.SECONDS.sleep(-i);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					return readInput(useCmdSequence);
				}
			} catch (NumberFormatException e) {
				// never mind
			}
			return cmd;
		}
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
	 * Read the values for each input argument from the console.
	 * 
	 * @param method
	 *            The method whose inputs are read
	 * @return Variant array of the input values
	 * @throws ServiceException
	 *             if a service call fails
	 * @throws AddressSpaceException
	 *             if the input data types cannot be determined
	 * @throws ServerConnectionException
	 *             if we are not connected to the client
	 * @throws MethodArgumentException
	 *             if the input arguments are not validly defined for the node
	 * @throws StatusException
	 */
	protected Variant[] readInputArguments(UaMethod method)
			throws ServiceException, ServerConnectionException,
			AddressSpaceException, MethodArgumentException, StatusException {
		Argument[] inputArguments = method.getInputArguments();
		if ((inputArguments == null) || (inputArguments.length == 0))
			return new Variant[0];
		Variant[] inputs = new Variant[inputArguments.length];
		println("Enter value for Inputs:");
		for (int i = 0; i < inputs.length; i++) {
			UaDataType dataType = (UaDataType) client.getAddressSpace()
					.getType(inputArguments[i].getDataType());
			println(String.format("%s: %s {%s} = ",
					inputArguments[i].getName(), dataType.getDisplayName()
							.getText(), inputArguments[i].getDescription()
							.getText()));
			while (inputs[i] == null)
				try {
					inputs[i] = client.getAddressSpace().getDataTypeConverter()
							.parseVariant(readInput(false), dataType);
				} catch (NumberFormatException e) {
					printException(e);
				}
		}
		return inputs;
	}

	/**
	 * @param nodeId
	 * @return
	 * @throws ServiceException
	 * @throws ServerConnectionException
	 * @throws StatusException
	 * @throws AddressSpaceException
	 */
	protected NodeId readMethodId(NodeId nodeId)
			throws ServerConnectionException, ServiceException,
			StatusException, AddressSpaceException {
		// A lightweight way to list the methods is to use browseMethods
		// List<ReferenceDescription> methodRefs =
		// client.getAddressSpace().browseMethods(nodeId);
		List<UaMethod> methods = client.getAddressSpace().getMethods(nodeId);
		if (methods.size() == 0) {
			println("No methods available");
			return null;
		}
		println("Select the method to execute.");
		for (int i = 0; i < methods.size(); i++)
			printf("%d - %s\n", i, methods.get(i).getDisplayName().getText());
		int action;
		do
			action = readAction();
		while (action >= methods.size());
		if (action < 0)
			return null;
		NodeId methodId = methods.get(action).getNodeId();
		System.out.println("Method: " + methodId);
		return methodId;
	}

	/**
	 * @param r
	 * @return
	 * @throws ServiceException
	 * @throws ServerConnectionException
	 * @throws StatusException
	 */
	protected String referenceToString(ReferenceDescription r)
			throws ServerConnectionException, ServiceException, StatusException {
		if (r == null)
			return "";
		String referenceTypeStr = r.getReferenceTypeId().getValue().toString();
		;
		try {
			// Find the reference type from the NodeCache
			UaReferenceType referenceType = (UaReferenceType) client
					.getAddressSpace().getType(r.getReferenceTypeId());
			if ((referenceType != null)
					&& (referenceType.getDisplayName() != null))
				if (r.getIsForward())
					referenceTypeStr = referenceType.getDisplayName().getText();
				else
					referenceTypeStr = referenceType.getInverseName().getText();
		} catch (AddressSpaceException e) {
			printException(e);
		}
		String typeStr;
		try {
			// Find the type from the NodeCache
			UaNode type = client.getAddressSpace().getNode(
					r.getTypeDefinition());
			if (type != null)
				typeStr = type.getDisplayName().getText();
			else
				typeStr = r.getTypeDefinition().getValue().toString();
		} catch (AddressSpaceException e) {
			printException(e);
			typeStr = r.getTypeDefinition().getValue().toString();
		}
		return String.format("%s%s (ReferenceType=%s, BrowseName=%s%s)", r
				.getDisplayName().getText(), ": " + typeStr, referenceTypeStr,
				r.getBrowseName(), r.getIsForward() ? "" : "[Inverse]");
	}

	/**
	 * Subscribe to items specified from command line
	 */
	protected void subscribeToInitialItems() {
		if (!initialMonitoredItems.isEmpty())
			// while (true) {
			try {
				if (subscription == null)
					subscription = createSubscription();
			} catch (ServiceException e) {
				printException(e);
				return;
			} catch (StatusException e) {
				printException(e);
				return;
			}
		for (String s : initialMonitoredItems)
			try {
				if (s.endsWith("/*"))
					createMonitoredItemsForChildren(subscription, s);
				else
					createMonitoredItem(subscription, NodeId.parseNodeId(s),
							Attributes.Value);
			} catch (IllegalArgumentException e1) {
				printException(e1);
			} catch (ServiceException e1) {
				printException(e1);
			} catch (StatusException e1) {
				printException(e1);
			}
		try {
			if (subscription != null)
				subscriptionMenu();
		} catch (ServiceException e) {
			printException(e);
		} catch (StatusException e) {
			printException(e);
		}
		// }
	}

	/**
	 * @throws ServiceException
	 * @throws StatusException
	 */
	protected void subscriptionMenu() throws ServiceException, StatusException {
		// Make sure the subscription is re-enabled, in case it was
		// paused earlier (see below). By default, new subscriptions are
		// enabled
		subscription.setPublishingEnabled(true);

		do {
			println("-------------------------------------------------------");
			println("- Enter x to end (and remove) the subcription");
			println("- Enter p to pause the subscription (e.g. to add new items)");
			println("- Enter r to remove an item from the subscription");
			println("-------------------------------------------------------");
			String input;
			input = readInput(true);
			if (input.equals("r")) {
				subscription.setPublishingEnabled(false);
				try {
					MonitoredItemBase removedItem = null;
					while (removedItem == null) {
						println("-------------------------------------------------------");
						println("Monitored Items:");
						for (MonitoredItemBase item : subscription.getItems())
							println(item.toString());
						println("- Enter the ClientHandle of the item to remove it");
						println("- Enter x to cancel.");
						println("-------------------------------------------------------");
						String handleStr = readInput(true);
						if (handleStr.equals("x"))
							break;
						try {
							UnsignedInteger handle = UnsignedInteger
									.parseUnsignedInteger(handleStr);
							removedItem = subscription.removeItem(handle);
							printf(removedItem != null ? "Item %s removed\n"
									: "No such item: %s\n", handle);
						} catch (Exception e) {
							printException(e);
						}
					}
				} finally {
					subscription.setPublishingEnabled(true);

				}
			} else if (input.equals("p")) {
				subscription.setPublishingEnabled(false);
				break;
			} else if (input.equals("x")) {
				try {
					client.removeSubscription(subscription);
				} catch (Exception e) {
					// Nevermind
				}
				subscription = null;
				break;
			}
		} while (true);
	}

	/**
	 * Browse the references for a node.
	 * 
	 * @param nodeId
	 * @param prevId
	 * @throws ServiceException
	 * @throws StatusException
	 */
	NodeId browse(NodeId nodeId, NodeId prevId) throws ServiceException,
			StatusException {
		printCurrentNode(nodeId);
		client.getAddressSpace().setMaxReferencesPerNode(1000);
		// client.getAddressSpace().setReferenceTypeId(ReferencesToReturn);
		List<ReferenceDescription> references = client.getAddressSpace()
				.browse(nodeId);
		for (int i = 0; i < references.size(); i++)
			printf("%d - %s\n", i, referenceToString(references.get(i)));
		List<ReferenceDescription> ur = client.getAddressSpace().browseUp(
				nodeId);
		ReferenceDescription upReference = ur.isEmpty() ? null : ur.get(0);
		System.out
				.println("-------------------------------------------------------");
		println("- Enter node number to browse into that");
		println("- Enter a to show/hide all references");
		if (prevId != null)
			println("- Enter b to browse back to the previous node");
		if (upReference != null)
			println("- Enter u to browse up to the 'parent' node");
		println("- Enter r to browse back to the root node");
		System.out
				.println("- Enter x to select the current node and return to previous menu");
		System.out
				.println("-------------------------------------------------------");
		do {
			int action = readAction();
			switch (action) {
			case ACTION_RETURN:
				return nodeId;
			case ACTION_BACK:
				if (prevId == null)
					continue;
				return prevId;
			case ACTION_UP:
				if ((upReference != null) && upReference.getNodeId().isLocal())
					try {
						return browse(
								client.getAddressSpace().getNamespaceTable()
										.toNodeId(upReference.getNodeId()),
								nodeId);
					} catch (ServiceResultException e1) {
						printException(e1);
					}
			case ACTION_ROOT:
				return browse(Identifiers.RootFolder, nodeId);
			case ACTION_ALL:
				if (NodeId
						.isNull(client.getAddressSpace().getReferenceTypeId())) {
					client.getAddressSpace().setReferenceTypeId(
							Identifiers.HierarchicalReferences);
					client.getAddressSpace().setBrowseDirection(
							BrowseDirection.Forward);
				} else {
					// request all types
					client.getAddressSpace().setReferenceTypeId(NodeId.NULL);
					client.getAddressSpace().setBrowseDirection(
							BrowseDirection.Both);
				}
				// if (ReferencesToReturn == null) {
				// ReferencesToReturn = Identifiers.HierarchicalReferences;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Forward);
				// } else {
				// ReferencesToReturn = null;
				// client.getAddressSpace().setBrowseDirection(
				// BrowseDirection.Both);
				// }
				return browse(nodeId, prevId);
			case ACTION_TRANSLATE:
				// This is only provided as an example. It should also prompt
				// for the namespaceIndex to make it properly usable. Now it is
				// assuming that the target is in the same namespace as the
				// current node.
				// For that reason, the shortcut key for this action is not
				// shown in the user menu.
				println("Which node do you wish to translate?");
				String s = readInput(false);
				final QualifiedName targetName = new QualifiedName(
						nodeId.getNamespaceIndex(), s);
				BrowsePathTarget pathTarget = client.getAddressSpace()
						.translateBrowsePathToNodeId(
								nodeId,
								new RelativePathElement(
										Identifiers.HierarchicalReferences,
										false, true, targetName))[0];
				println("Target: " + pathTarget.getTargetId());
				println("RemainingPathIndex: "
						+ pathTarget.getRemainingPathIndex());
				break;
			default:
				try {
					ReferenceDescription r = references.get(action);
					NodeId target;
					try {
						target = browse(client.getAddressSpace()
								.getNamespaceTable().toNodeId(r.getNodeId()),
								nodeId);
					} catch (ServiceResultException e) {
						throw new ServiceException(e);
					}
					if (target != nodeId)
						return target;
					return browse(nodeId, prevId);
				} catch (IndexOutOfBoundsException e) {
					System.out.println("No such item: " + action);
				}
			}
		} while (true);
	}

	/**
	 * @param methodId
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * @throws ServerConnectionException
	 * @throws MethodArgumentException
	 * @throws StatusException
	 * 
	 */
	void callMethod(NodeId nodeId, NodeId methodId) throws ServiceException,
			ServerConnectionException, AddressSpaceException,
			MethodArgumentException, StatusException {
		// // Example values to call "condition acknowledge" using the standard
		// // methodId:
		// methodId = Identifiers.AcknowledgeableConditionType_Acknowledge;
		// // change this to the ID of the event you are acknowledging:
		// byte[] eventId = null;
		// LocalizedText comment = new LocalizedText("Your comment",
		// Locale.ENGLISH);
		// final Variant[] inputs = new Variant[] { new Variant(eventId),
		// new Variant(comment) };

		UaMethod method = client.getAddressSpace().getMethod(methodId);
		Variant[] inputs = readInputArguments(method);
		Variant[] outputs = client.call(nodeId, methodId, inputs);
		printOutputArguments(method, outputs);

	}

	/**
	 * Connect to the server.
	 * 
	 * @throws ServerConnectionException
	 */
	void connect() throws ServerConnectionException {
		if (!client.isConnected())
			try {
				println("Using SecurityMode " + client.getSecurityMode());

				// We can define the session name that is visible in the server
				// as
				// well
				client.setSessionName(APP_NAME + " Session" + ++sessionCount);

				client.connect();
				try {
					println("ServerStatus: " + client.getServerStatus());
				} catch (StatusException ex) {
					printException(ex);
				}
			} catch (InvalidServerEndpointException e) {
				printException(e);
				try {
					// In case we have selected a wrong endpoint, print out the
					// supported ones
					printEndpoints(client.discoverEndpoints());
				} catch (Exception ex) {
					// never mind, if the endpoints are not available
				}
			} catch (ServerConnectionException e) {
				printException(e);
				try {
					// In case we have selected an unavailable security mode,
					// print out the supported ones
					printSecurityModes(client.getSupportedSecurityModes());
				} catch (ServerConnectionException e1) {
					// never mind, if the security modes are not available
				} catch (ServiceException e1) {
					// never mind, if the security modes are not available
				}
			} catch (SessionActivationException e) {
				printException(e);
				try {
					printUserIdentityTokens(client
							.getSupportedUserIdentityTokens());
				} catch (ServiceException e1) {
					// never mind, if not available
				}
				return; // No point to continue
			} catch (ServiceException e) {
				printException(e);
			}
	}

	/**
	 * Disconnect from the server.
	 */
	void disconnect() {
		client.disconnect();
	}

	/**
	 * @return
	 * @throws ServerListException
	 *             if the client list cannot be retrieved
	 * 
	 */
	ApplicationDescription discoverServer(String uri)
			throws ServerListException {
		// Discover a new server list from a discovery server at URI
		ServerList serverList = new ServerList(uri);
		if (serverList.size() == 0)
			println("No servers found");
		for (int i = 0; i < serverList.size(); i++) {
			final ApplicationDescription s = serverList.get(i);
			println(String.format("%s - %-30s - %-7s - %-30s - %s", "#",
					"Name", "Type", "Product", "Application"));
			println(String.format("%d - %-30s - %-7s - %-30s - %s", i, s
					.getApplicationName().getText(), s.getApplicationType(), s
					.getProductUri(), s.getApplicationUri()));
		}
		System.out
				.println("-------------------------------------------------------");
		println("- Enter client number to select that one");
		println("- Enter x to return to main menu");
		System.out
				.println("-------------------------------------------------------");
		do {
			int action = readAction();
			switch (action) {
			case ACTION_RETURN:
				return null;
			default:
				return serverList.get(action);
			}
		} while (true);
	}

	void mainMenu() throws ServerListException, URISyntaxException {

		if (connectToDiscoveryServer)
			discover();
		else
			// Try to connect to the client already at this point.
			connect();

		// Subscribe to items specified from command line
		subscribeToInitialItems();

		// You have one node selected all the time, and all operations
		// target that. We can initialize that to the standard ID of the
		// RootFolder (unless it was specified from command line).

		// Identifiers contains a list of all standard node IDs
		if (nodeId == null)
			nodeId = Identifiers.RootFolder;

		/******************************************************************************/
		/* Wait for user command to execute next action. */
		do {
			printMenu(nodeId);

			try {
				switch (readAction()) {
				case ACTION_RETURN:
					disconnect();
					return;
				case 0:
					discover();
					break;
				case 1:
					connect();
					break;
				case 2:
					disconnect();
					break;
				case 3:
					NodeId browseId = browse(nodeId, null);
					if (browseId != null)
						nodeId = browseId;
					break;
				case 4:
					read(nodeId);
					break;
				case 5:
					write(nodeId);
					break;
				case 6:
					registerNodes(nodeId);
					break;
				case 7:
					unregisterNodes();
					break;
				case 8:
					subscribe(nodeId);
					break;
				case 9:
					NodeId methodId = readMethodId(nodeId);
					if (methodId != null)
						callMethod(nodeId, methodId);
					break;
				case 10:
					readHistory(nodeId);
				default:
					continue;
				}
			} catch (Exception e) {
				printException(e);
			}

		} while (true);
		/******************************************************************************/
	}

	void printMenu(NodeId nodeId) {
		println("");
		println("");
		println("");
		if (client.isConnected()) {
			println("*** Connected to: " + client.getUri());
			println("");
			if (nodeId != null)
				printCurrentNode(nodeId);
		} else
			println("*** NOT connected to: " + client.getUri());

		System.out
				.println("-------------------------------------------------------");
		println("- Enter x to close client");
		System.out
				.println("-------------------------------------------------------");
		System.out
				.println("- Enter 0 to start discovery                          -");
		System.out
				.println("- Enter 1 to connect to server                        -");
		System.out
				.println("- Enter 2 to disconnect from server                   -");
		System.out
				.println("- Enter 3 to browse the server address space          -");
		System.out
				.println("- Enter 4 to read values                              -");
		System.out
				.println("- Enter 5 to write values                             -");
		System.out
				.println("- Enter 6 to register nodes                           -");
		System.out
				.println("- Enter 7 to unregister nodes                         -");
		if (subscription == null)
			System.out
					.println("- Enter 8 to create a subscription                    -");
		else
			System.out
					.println("- Enter 8 to add a new item to the subscription       -");
		System.out
				.println("- Enter 9 to call a method                            -");
		System.out
				.println("- Enter 10 to read history                            -");
		System.out
				.println("-------------------------------------------------------");
	}

	/**
	 * @param nodeId
	 * @throws StatusException
	 * @throws ServiceException
	 * 
	 */
	void read(NodeId nodeId) throws ServiceException, StatusException {
		println("read node " + nodeId);
		UnsignedInteger attributeId = readAttributeId();
		DataValue value = client.readAttribute(nodeId, attributeId);
		println(dataValueToString(nodeId, attributeId, value));
	}

	/**
	 * @param nodeId
	 * @throws StatusException
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * 
	 */
	void readHistory(NodeId nodeId) throws ServiceException, StatusException,
			AddressSpaceException {
		UaNode node = client.getAddressSpace().getNode(nodeId);

		if (node instanceof UaVariable) {

			// Validate that history is readable for the node

			UaVariable variable = (UaVariable) node;
			if (!variable.getAccessLevel().contains(AccessLevel.HistoryRead)) {
				println("The node does not contain history");
				return;
			}
			println("0 - raw data");
			println("1 - at times");
			int action = readAction();

			println("Reading history of node " + node);
			try {
				DateTime endTime = DateTime.currentTime();
				DateTime startTime = new DateTime(
						(endTime.getMilliSeconds() - (3600 * 1000)) * 10000);
				DataValue[] values = null;

				switch (action) {
				case 0:
					println("between " + startTime + " and " + endTime);

					values = client.historyReadRaw(nodeId, startTime, endTime,
							UnsignedInteger.ZERO, true, null,
							TimestampsToReturn.Source);
					break;
				case 1:
					println("at " + startTime + " and " + endTime);

					DateTime[] reqTimes = new DateTime[] { startTime, endTime };
					values = client.historyReadAtTimes(nodeId, reqTimes, null,
							TimestampsToReturn.Source);
					break;
				}

				if (values != null) {
					println("Count = " + values.length);
					for (int i = 0; i < values.length; i++)
						println("Value " + (i + 1) + " = "
								+ values[i].getValue().getValue() + " | "
								+ values[i].getSourceTimestamp());
				}
			} catch (Exception e) {
				printException(e);
			}
		} else
			println("History is only available for variable nodes. The current node is a "
					+ node.getNodeClass() + ".");
	}

	/**
	 * @param nodeId
	 * 
	 */
	void registerNodes(NodeId nodeId) {
		try {
			NodeId[] registeredNodeId = client.getAddressSpace().registerNodes(
					nodeId);
			println("Registered NodeId " + nodeId + " -> registeredNodeId is "
					+ registeredNodeId[0]);
		} catch (ServiceException e) {
			printException(e);
		}
	}

	/**
	 * @param nodeId
	 * 
	 */
	void subscribe(NodeId nodeId) {
		if (nodeId == null) {
			println("*** Select a node to subscribe first ");
			println("");
			return;
		}
		println("*** Subscribing to node: " + nodeId);
		println("");
		UnsignedInteger attributeId = readAttributeId();
		if (attributeId != null)
			try {
				// Create the subscription
				if (subscription == null)
					subscription = createSubscription();
				// Create the monitored item
				createMonitoredItem(subscription, nodeId, attributeId);
				// Show the menu and wait for action
				subscriptionMenu();

			} catch (ServiceException e) {
				printException(e);
			} catch (StatusException e) {
				printException(e);
			}

	}

	/**
	 * Unregisters all previously registered nodes.
	 */
	void unregisterNodes() {
		try {
			NodeId[] nodes = client.getAddressSpace().unregisterAllNodes();
			println("Unregistered " + nodes.length + " node(s).");
		} catch (ServiceException e) {
			printException(e);
		}
	}

	/**
	 * @param nodeId
	 * @throws StatusException
	 * @throws AddressSpaceException
	 * @throws ServiceException
	 */
	void write(NodeId nodeId) throws ServiceException, AddressSpaceException,
			StatusException {
		UnsignedInteger attributeId = readAttributeId();

		UaNode node = client.getAddressSpace().getNode(nodeId);
		println("Writing to node " + nodeId + " - "
				+ node.getDisplayName().getText());

		// Find the DataType if setting Value - for other properties you must
		// find the correct data type yourself
		UaDataType dataType = null;
		if (attributeId.equals(Attributes.Value)
				&& (node instanceof UaVariable)) {
			UaVariable v = (UaVariable) node;
			// Initialize DataType node, if it is not initialized yet
			if (v.getDataType() == null)
				v.setDataType(client.getAddressSpace().getType(
						v.getDataTypeId()));
			dataType = (UaDataType) v.getDataType();
			println("DataType: " + dataType.getDisplayName().getText());
		}

		print("Enter the value to write: ");
		String value = readInput(true);
		try {
			Object convertedValue = dataType != null ? client.getAddressSpace()
					.getDataTypeConverter().parseVariant(value, dataType)
					: value;
			boolean status = client.writeAttribute(nodeId, attributeId,
					convertedValue);
			if (status)
				println("OK");
			else
				println("OK (completes asynchronously)");
		} catch (ServiceException e) {
			printException(e);
		} catch (StatusException e) {
			printException(e);
		}

	}
}
