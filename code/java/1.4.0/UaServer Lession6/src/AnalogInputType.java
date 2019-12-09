import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Identifiers;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.nodes.CacheProperty;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaObjectTypeNode;
import com.prosysopc.ua.server.nodes.opcua.BaseEventType;
import com.prosysopc.ua.server.nodes.opcua.EventType;

public class AnalogInputType extends UaObjectNode {

	public AnalogInputType(NodeManager nodeManager,String name,String displayName) throws StatusException {
		super(nodeManager, 
			  new NodeId(nodeManager.getNamespaceIndex(), name), 
			  new QualifiedName(nodeManager.getNamespaceIndex(), name),
			  new LocalizedText(displayName,Locale.CHINESE));

		m_originalValue = null;
		m_processValue  = null;
		m_multiplier    = null;
		
		createObject(nodeManager, name);		
	}
	
	public void processDataChange(UaVariable variable, DataValue dataValue, NodeManager nodeManager) throws StatusException
	{
		if (variable.getNodeId() == m_originalValue.getNodeId())
		{			
			m_originalValue.setValue(dataValue);
			
			Variant value = null;
			StatusCode status = StatusCode.BAD;
			DateTime now = DateTime.currentTime();
			
			if (dataValue.getStatusCode().isBad())
			{
				value = new Variant((float)0.0);				
			} else {
				float result = dataValue.getValue().floatValue() * m_multiplier.getValue().getValue().floatValue();
				value = new Variant(result);
				status = dataValue.getStatusCode();
			}

			float oldProcessValue = m_processValue.getValue().getValue().floatValue();
			float highThreshValue = m_highThreshold.getValue().getValue().floatValue();
			
			if ( value.floatValue() >= highThreshValue )
			{
				if (oldProcessValue < highThreshValue)
				{
					BaseEventType event = new EventType(nodeManager, Identifiers.SystemEventType);
					
					event.setSourceNode(this.getNodeId());
					event.setSourceName(this.getDisplayName().getText());
					event.setMessage("温度过高");
					event.setSeverity(1);
										
					byte[] eventId = BaseEventType.createEventId(s_eventId++);
					event.triggerEvent(now, now, eventId);
				}
			}					
			
			DataValue processDataValue = new DataValue(value, status, now, now);
			
			m_processValue.setValue(processDataValue);
		}
	}
	
	public static void createType(NodeManager nodeManager) throws StatusException
	{
		final NodeId temperatureTypeNodeId = new NodeId(nodeManager.getNamespaceIndex(), TYPE_NAME);
		
		s_typeNode = new UaObjectTypeNode(nodeManager, 
				                          temperatureTypeNodeId, 
				                          new QualifiedName(nodeManager.getNamespaceIndex(), TYPE_NAME), 
				                          new LocalizedText(TYPE_CHINESE_NAME,Locale.CHINESE));

		UaType baseObjectType = nodeManager.getType(Identifiers.BaseObjectType);
		
		nodeManager.addNodeAndReference(baseObjectType, s_typeNode, Identifiers.HasSubtype);
		
		createMember(nodeManager, s_typeNode, TYPE_NAME);		
	}
	
	public static NodeId getTypeNodeId()
	{
		return s_typeNode.getNodeId();
	}
	
	private void createObject(NodeManager nodeManager, String name) throws StatusException
	{		
		this.setTypeDefinition(s_typeNode);				
		createMember(nodeManager, this, name);
		loadMember();
		
		createEventReference(this);
	}
	
	private static void createMember(NodeManager nodeManager, UaNode rootNode, String objectName) throws StatusException
	{		
		createOriginalValue(nodeManager, rootNode, objectName);
		createProcessValue(nodeManager, rootNode, objectName);
		createMultipler(nodeManager, rootNode, objectName);
		createHighThreshold(nodeManager, rootNode, objectName);			
	}
	
	private static void createEventReference(UaObjectNode rootObject)
	{		
		rootObject.addReference(Identifiers.BaseEventType, Identifiers.GeneratesEvent, false);
	}
	
	private static void createOriginalValue(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{	
		Variant defaultValue = new Variant(new Float(0.0));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);			
		
		final NodeId originalValueVariableId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+ORIGINAL_VALUE_VARIABLE_NAME);
		CacheVariable originalValueVariable = new CacheVariable(nodeManager, 
				                                                originalValueVariableId, 
				                                                new QualifiedName(nodeManager.getNamespaceIndex(),ORIGINAL_VALUE_VARIABLE_NAME),
				                                                new LocalizedText(ORIGINAL_VALUE_VARIABLE_CHINESE_NAME,Locale.CHINESE));
	
		originalValueVariable.setDataTypeId(Identifiers.Float);
		originalValueVariable.setValue(defaultDataValue);		
		originalValueVariable.setAccessLevel(NodeManager.READ_WRITE);
		
		nodeManager.addNodeAndReference(rootNode, originalValueVariable, Identifiers.HasComponent);
	}	
	
	private static void createProcessValue(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{
		Variant defaultValue = new Variant(new Float(0.0));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);	
		
		final NodeId processValueVariableId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+PROCESS_VALUE_VARIABLE_NAME);
		CacheVariable processValueVariable = new CacheVariable(nodeManager, 
				                                               processValueVariableId, 
				                                               new QualifiedName(nodeManager.getNamespaceIndex(),PROCESS_VALUE_VARIABLE_NAME),
				                                               new LocalizedText(PROCESS_VALUE_VARIABLE_CHINESE_NAME,Locale.CHINESE));
	
		processValueVariable.setDataTypeId(Identifiers.Float);
		processValueVariable.setValue(defaultDataValue);

		processValueVariable.setAccessLevel(NodeManager.READ_WRITE);		
		
		nodeManager.addNodeAndReference(rootNode, processValueVariable, Identifiers.HasComponent);		


	}
	
	private static void createMultipler(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{
		Variant defaultValue = new Variant(new Float(5.0));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);	
		
		final NodeId multiplierPropertyId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+MULTIPLIER_PROPERTY_NAME);
		CacheProperty multiplierProperty = new CacheProperty(nodeManager, 
				                                             multiplierPropertyId, 
				                                             new QualifiedName(nodeManager.getNamespaceIndex(),MULTIPLIER_PROPERTY_NAME),
				                                             new LocalizedText(MULTIPLIER_PROPERTY_CHINESE_NAME,Locale.CHINESE));
			
		multiplierProperty.setDataTypeId(Identifiers.Float);
		multiplierProperty.setValue(defaultDataValue);		
		
		multiplierProperty.setAccessLevel(NodeManager.READ_ONLY);
		
		nodeManager.addNodeAndReference(rootNode, multiplierProperty, Identifiers.HasProperty);
	}
		
	private static void createHighThreshold(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{
		Variant defaultValue = new Variant(new Float(400.0));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);	
		
		final NodeId highThresholdPropertyId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+HIGH_THRESHOLD_PROPERTY_NAME);
		CacheProperty highThresholdProperty = new CacheProperty(nodeManager, 
				                                                highThresholdPropertyId, 
				                                                new QualifiedName(nodeManager.getNamespaceIndex(),HIGH_THRESHOLD_PROPERTY_NAME),
				                                                new LocalizedText(HIGH_THRESHOLD_PROPERTY_CHINESE_NAME,Locale.CHINESE));
			
		highThresholdProperty.setDataTypeId(Identifiers.Float);
		highThresholdProperty.setValue(defaultDataValue);		
		
		highThresholdProperty.setAccessLevel(NodeManager.READ_ONLY);
		
		nodeManager.addNodeAndReference(rootNode, highThresholdProperty, Identifiers.HasProperty);
	}	
	
	private void loadMember()
	{
		UaNode[] variables = this.getComponents();
	
		for (int i=0; i<variables.length; i++)
		{
			if (variables[i].getBrowseName().getName() == ORIGINAL_VALUE_VARIABLE_NAME)
			{
				m_originalValue = (CacheVariable)variables[i];
			}
			
			if (variables[i].getBrowseName().getName() == PROCESS_VALUE_VARIABLE_NAME)
			{
				m_processValue = (CacheVariable)variables[i];
			}
		}
		
		UaNode[] propertys = this.getProperties();
		
		for (int j=0; j<propertys.length; j++)
		{
			if (propertys[j].getBrowseName().getName() == MULTIPLIER_PROPERTY_NAME)
			{
				m_multiplier = (CacheProperty)propertys[j];
			}
			
			if (propertys[j].getBrowseName().getName() == HIGH_THRESHOLD_PROPERTY_NAME)
			{
				m_highThreshold = (CacheProperty)propertys[j];
			}
		}
	}
	
	CacheVariable m_originalValue;
	CacheVariable m_processValue;
	
	CacheProperty m_multiplier;
	
	CacheProperty m_highThreshold;
	
	static String TYPE_NAME                             = "AnalogInputType";	
	static String TYPE_CHINESE_NAME                     = "模拟量";	
	static String ORIGINAL_VALUE_VARIABLE_NAME          = "OriginalValue";
	static String ORIGINAL_VALUE_VARIABLE_CHINESE_NAME  = "原始值";
	static String PROCESS_VALUE_VARIABLE_NAME           = "ProcessValue";
	static String PROCESS_VALUE_VARIABLE_CHINESE_NAME   = "计算值";
	static String MULTIPLIER_PROPERTY_NAME              = "Multiplier";
	static String MULTIPLIER_PROPERTY_CHINESE_NAME 	    = "乘数";
	static String HIGH_THRESHOLD_PROPERTY_NAME          = "HighThreshold";
	static String HIGH_THRESHOLD_PROPERTY_CHINESE_NAME  = "高限值";
	
	static UaObjectTypeNode s_typeNode;
	
	static long s_eventId = 1;
}

