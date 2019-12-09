import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Argument;
import org.opcfoundation.ua.core.Identifiers;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaObjectTypeNode;
import com.prosysopc.ua.server.nodes.UaReferenceTypeNode;
import com.prosysopc.ua.server.MethodManager;

public class ValveType extends UaObjectNode {

	public ValveType(NodeManager nodeManager,String name,String displayName) throws StatusException {
		super(nodeManager, 
			  new NodeId(nodeManager.getNamespaceIndex(), name), 
			  new QualifiedName(nodeManager.getNamespaceIndex(), name),
			  new LocalizedText(displayName,Locale.CHINESE));

		m_statusValue = null;
		m_controlSignal = null;	
		
		m_method = null;	
		
		createObject(nodeManager, name);		
	}
	
	public boolean processMethodCall(UaMethod method, 	
                                     Variant[] inputArguments,
                                     StatusCode[] inputArgumentResults,
                                     DiagnosticInfo[] inputArgumentDiagnosticInfos,
                                     Variant[] outputs) throws StatusException
	{
		MethodManager.checkInputArguments(new Class[] { Boolean.class }, 
				                          inputArguments, 
				                          inputArgumentResults,
				                          inputArgumentDiagnosticInfos,
				                          false);
		
		if (method.getNodeId() != m_method.getNodeId()) return false;

		boolean operationOpen = inputArguments[0].booleanValue();
		boolean statusValue = m_statusValue.getValue().getValue().booleanValue();
		
		
		if (statusValue == operationOpen) 
		{
			outputs[0] = new Variant(new String("Invalid operation"));
		}
			
		if (statusValue != operationOpen) 
		{
			DateTime now = DateTime.currentTime();
				
			m_controlSignal.setValue(new DataValue(new Variant(new Boolean(operationOpen)), StatusCode.GOOD, now, now));
		
			m_statusValue.setValue(new DataValue(new Variant(new Boolean(operationOpen)), StatusCode.GOOD, now, now));
			
			outputs[0] = new Variant(new String("Operation complete"));
		}		
		
		return true;
	}
	
	public static NodeId getTypeNodeId()
	{
		return s_typeNode.getNodeId();
	}
	
	public static void createType(NodeManager nodeManager) throws StatusException
	{
		final NodeId valveTypeNodeId = new NodeId(nodeManager.getNamespaceIndex(), TYPE_NAME);
		
		s_typeNode = new UaObjectTypeNode(nodeManager, 
				                          valveTypeNodeId, 
				                          new QualifiedName(nodeManager.getNamespaceIndex(), TYPE_NAME), 
				                          new LocalizedText(TYPE_CHINESE_NAME,Locale.CHINESE));

		UaType baseObjectType = nodeManager.getType(Identifiers.BaseObjectType);
		
		nodeManager.addNodeAndReference(baseObjectType, s_typeNode, Identifiers.HasSubtype);
		
		createMember(nodeManager, s_typeNode, TYPE_NAME);		
	}
	
	
	private void createObject(NodeManager nodeManager, String name) throws StatusException
	{		
		this.setTypeDefinition(s_typeNode);				
		createMember(nodeManager, this, name);
		loadMember();
		
		NodeId refNodeId = new NodeId(nodeManager.getNamespaceIndex(), "Connects");
		UaReferenceTypeNode reNode = new UaReferenceTypeNode(nodeManager, 
				refNodeId,
                new QualifiedName(nodeManager.getNamespaceIndex(), "Connects"), 
                new LocalizedText("测试连接引用",Locale.CHINESE));
		reNode.setInverseName(new LocalizedText("ConnectsOf",Locale.ENGLISH));
		reNode.setSymmetric(false);
		NodeId hRefNodeId = new NodeId(0,33);
		
		nodeManager.addReference(hRefNodeId, refNodeId, Identifiers.HasSubtype, true);
		
		NodeId svrNodeId = new NodeId(0,2253);
		nodeManager.addReference(svrNodeId, this.getNodeId(), refNodeId, true);
		
	}
	
	private void loadMember()
	{
		UaNode[] variables = this.getComponents();
	
		for (int i=0; i<variables.length; i++)
		{
			if (variables[i].getBrowseName().getName() == STATUS_VALUE_VARIABLE_NAME)
			{
				m_statusValue = (CacheVariable)variables[i];
			}
			
			if (variables[i].getBrowseName().getName() == CONTROL_SIGNAL_VARIABLE_NAME)
			{
				m_controlSignal = (CacheVariable)variables[i];
			}
			
			if (variables[i].getBrowseName().getName() == OPEN_CLOSE_METHOD_NAME)
			{
				m_method = (PlainMethod)variables[i];
			}			
		}
	}
	
	
	private static void createMember(NodeManager nodeManager, UaNode rootNode, String objectName) throws StatusException
	{
		createStatusValue(nodeManager, rootNode, objectName);
		createControlSignal(nodeManager, rootNode, objectName);
		createMethod(nodeManager, rootNode, objectName);
	}
	
	private static void createStatusValue(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{	
		Variant defaultValue = new Variant(new Boolean(false));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);			
		
		final NodeId statusValueVariableId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+STATUS_VALUE_VARIABLE_NAME);
		CacheVariable statusValueVariable = new CacheVariable(nodeManager, 
				                                              statusValueVariableId, 
				                                              new QualifiedName(nodeManager.getNamespaceIndex(),STATUS_VALUE_VARIABLE_NAME),
				                                              new LocalizedText(STATUS_VALUE_VARIABLE_CHINESE_NAME,Locale.CHINESE));
	
		statusValueVariable.setDataTypeId(Identifiers.Boolean);
		statusValueVariable.setValue(defaultDataValue);		
		statusValueVariable.setAccessLevel(NodeManager.READ_WRITE);
		
		nodeManager.addNodeAndReference(rootNode, statusValueVariable, Identifiers.HasComponent);
	}	
	
	private static void createControlSignal(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{	
		Variant defaultValue = new Variant(new Boolean(false));		

		DateTime now = DateTime.currentTime();
		
		DataValue defaultDataValue = new DataValue(defaultValue, StatusCode.GOOD,now,now);			
		
		final NodeId controlSignalVariableId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+CONTROL_SIGNAL_VARIABLE_NAME);
		CacheVariable controlSignalVariable = new CacheVariable(nodeManager, 
				                                                controlSignalVariableId, 
				                                                new QualifiedName(nodeManager.getNamespaceIndex(),CONTROL_SIGNAL_VARIABLE_NAME),
				                                                new LocalizedText(CONTROL_SIGNAL_VARIABLE_CHINESE_NAME,Locale.CHINESE));
	
		controlSignalVariable.setDataTypeId(Identifiers.Boolean);
		controlSignalVariable.setValue(defaultDataValue);		
		controlSignalVariable.setAccessLevel(NodeManager.READ_WRITE);
		
		nodeManager.addNodeAndReference(rootNode, controlSignalVariable, Identifiers.HasComponent);
	}	
	
	private static void createMethod(NodeManager nodeManager, UaNode rootNode, String name) throws StatusException
	{
		NodeId openCloseMethodId = new NodeId(nodeManager.getNamespaceIndex(),name+"."+OPEN_CLOSE_METHOD_NAME);
		
		PlainMethod method = new PlainMethod(nodeManager, 
				                             openCloseMethodId,
				                             new QualifiedName(nodeManager.getNamespaceIndex(),OPEN_CLOSE_METHOD_NAME),
				                             new LocalizedText(OPEN_CLOSE_METHOD_CHINESE_NAME,Locale.CHINESE));

		Argument[] inputs = new Argument[1];
		inputs[0] = new Argument();
		inputs[0].setName(OPERATION_PARAMETER_NAME);
		inputs[0].setDataType(Identifiers.Boolean);
		inputs[0].setValueRank(ValueRanks.Scalar);
		inputs[0].setArrayDimensions(null);
		inputs[0].setDescription(new LocalizedText(OPERATION_PARAMETER_CHINESE_NAME, Locale.CHINESE));
		
		method.setInputArguments(inputs);

		Argument[] outputs = new Argument[1];
		outputs[0] = new Argument();
		outputs[0].setName(RESULT_PARAMETER_NAME);
		outputs[0].setDataType(Identifiers.String);
		outputs[0].setValueRank(ValueRanks.Scalar);
		outputs[0].setArrayDimensions(null);
		outputs[0].setDescription(new LocalizedText(RESULT_PARAMETER_CHINESE_NAME, Locale.CHINESE));
		method.setOutputArguments(outputs);

		nodeManager.addNodeAndReference(rootNode, method, Identifiers.HasComponent);
	}
	
	CacheVariable m_statusValue;
	CacheVariable m_controlSignal;	
	
	private PlainMethod m_method;

	static String TYPE_NAME                             = "ValveType";	
	static String TYPE_CHINESE_NAME                     = "阀门类型";	
	static String STATUS_VALUE_VARIABLE_NAME            = "StatusValue";
	static String STATUS_VALUE_VARIABLE_CHINESE_NAME    = "状态值";
	static String CONTROL_SIGNAL_VARIABLE_NAME          = "ControlSignal";
	static String CONTROL_SIGNAL_VARIABLE_CHINESE_NAME  = "控制命令";
	static String OPEN_CLOSE_METHOD_NAME                = "OpenCloseOperation";
	static String OPEN_CLOSE_METHOD_CHINESE_NAME 	    = "开关操作";
	static String OPERATION_PARAMETER_NAME              = "Operation";
	static String OPERATION_PARAMETER_CHINESE_NAME      = "操作";
	static String RESULT_PARAMETER_NAME                 = "Result";
	static String RESULT_PARAMETER_CHINESE_NAME         = "结果";

	static UaObjectTypeNode s_typeNode;	
}
