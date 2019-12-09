import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.NotificationData;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.client.MonitoredEventItem;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.SubscriptionNotificationListener;
import com.prosysopc.ua.client.UaClient;


public class OpcUaSubscription {
	
	public OpcUaSubscription(UaClient client) throws ServiceException, StatusException
	{
		m_notificationListener = new Notificationlistener();
		
		m_dataChangeListener = new DataChangeListener();
		
		m_subscription = new Subscription();

		m_subscription.addNotificationListener(m_notificationListener);	
		
		client.addSubscription(m_subscription);
	}
	
	public void createMonitoredItem() throws ServiceException, StatusException
	{		
		String variableName = new String("Temperature.ProcessValue");
		NodeId id = new NodeId(2, variableName);
		
		MonitoredDataItem dataItem = new MonitoredDataItem(id, Attributes.Value, MonitoringMode.Reporting);
		dataItem.addChangeListener(m_dataChangeListener);
		
		m_subscription.addItem(dataItem);
	}
	
	public void startSubscription() throws ServiceException, StatusException
	{
		m_subscription.setPublishingEnabled(true);
	}
	
	public void stopSubscription() throws ServiceException, StatusException
	{
		m_subscription.setPublishingEnabled(false);
	}
	
	private Subscription m_subscription;
	
	private Notificationlistener m_notificationListener;
	
	private DataChangeListener m_dataChangeListener;
	
	private class DataChangeListener implements MonitoredDataItemListener 
	{
		public void onDataChange(MonitoredDataItem sender, DataValue prevValue,	DataValue value) {
			System.out.println(sender.getNodeId().toString() + " = " + value.getValue().floatValue());
		}
	};
	
	private class Notificationlistener implements SubscriptionNotificationListener
	{
		public void onBufferOverflow(Subscription subscription,
				UnsignedInteger sequenceNumber,
				ExtensionObject[] notificationData) {
		}

		public void onDataChange(Subscription subscription,
				MonitoredDataItem item, DataValue newValue) {

		}

		public void onError(Subscription subscription, Object notification,	Exception exception) {
		}

		public void onEvent(Subscription subscription, MonitoredEventItem item,	Variant[] eventFields) {
		}

		public long onMissingData(UnsignedInteger lastSequenceNumber,
				long sequenceNumber, long newSequenceNumber,
				StatusCode serviceResult) {
			return newSequenceNumber;
		}

		public void onNotificationData(Subscription subscription,
				NotificationData notification) {
		}

		public void onStatusChange(Subscription subscription,
				StatusCode oldStatus, StatusCode newStatus,
				DiagnosticInfo diagnosticInfo) {

		}
	}
}
