import java.util.EnumSet;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.opcua.FolderType;

public class NodeManager extends NodeManagerUaNode {

	public static EnumSet<AccessLevel> READ_ONLY = 
        EnumSet.of(AccessLevel.CurrentRead);

	public static EnumSet<AccessLevel> READ_WRITE = 
	        EnumSet.of(AccessLevel.CurrentRead, 
	                   AccessLevel.CurrentWrite);
	
	static EnumSet<AccessLevel> READ_WRITE_HISTORYREAD = 
	        EnumSet.of(AccessLevel.CurrentRead, 
	                   AccessLevel.CurrentWrite,
	                   AccessLevel.HistoryRead);	
	
	public NodeManager(UaServer server) throws StatusException {
		super(server,NODE_MANAGER_URI);
		
		m_methodManager = new MethodManager(this);
		
		MethodManagerUaNode methodManager = (MethodManagerUaNode) this.getMethodManager();

		methodManager.addCallListener(m_methodManager);
	}
	
	public void createAddressSpace() throws StatusException
	{
		createType();
		
		FolderType deviceFolder = createDeviceFolder();
		
		createObject(deviceFolder);
	}
	
	private void createType() throws StatusException
	{
		ValveType.createType(this);
	}

	private FolderType createDeviceFolder()
	{
		NodeId deviceFolderId = new NodeId(this.getNamespaceIndex(), "Device");
		FolderType deviceFolder = new FolderType(this, deviceFolderId, 
				                                 new QualifiedName(this.getNamespaceIndex(),"Device"), 
				                                 new LocalizedText("…Ë±∏",Locale.CHINESE));
		
		UaObject objectFolder = this.getServer().getNodeManagerRoot().getObjectsFolder();
		objectFolder.addReference(deviceFolder, Identifiers.Organizes, false);
		return deviceFolder;
	}
	
	private void createObject(FolderType folder) throws StatusException
	{
		ValveType waterInValve = new ValveType(this, "Valve", "∑ß√≈");
		folder.addReference(waterInValve, Identifiers.Organizes, false);
	}
	
	public boolean processMethodCall(UaNode objectNode, 
            UaMethod method, 	
            Variant[] inputArguments,
            StatusCode[] inputArgumentResults,
            DiagnosticInfo[] inputArgumentDiagnosticInfos,
            Variant[] outputs) throws StatusException 
	{	
		if (objectNode.getNodeClass() != NodeClass.Object) return false;
		
		UaObject object = (UaObject)objectNode;
		
		UaType type = object.getTypeDefinition();
		
		if (null == type) return false;
		
		boolean callResult = false;
		
		if (type.getNodeId() == ValveType.getTypeNodeId())
		{
			ValveType valveObject = (ValveType)object;
			callResult = valveObject.processMethodCall(method, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos, outputs);
		}		
		
		return callResult;
	}
	
	private static String NODE_MANAGER_URI = "urn:localhost:UA:ADDRESSSPACE";

	private MethodManager m_methodManager;
}
