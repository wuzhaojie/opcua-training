import java.util.EnumSet;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.opcua.FolderType;

public class NodeManager extends NodeManagerUaNode  {

	public static EnumSet<AccessLevel> READ_ONLY = 
                                 EnumSet.of(AccessLevel.CurrentRead);
	
	public static EnumSet<AccessLevel> READ_WRITE = 
                                 EnumSet.of(AccessLevel.CurrentRead, 
                                            AccessLevel.CurrentWrite);
	
	static EnumSet<AccessLevel> READ_WRITE_HISTORYREAD = 
		                         EnumSet.of(AccessLevel.CurrentRead, 
                                            AccessLevel.CurrentWrite,
                                            AccessLevel.HistoryRead);	
	
	public NodeManager(UaServer server) {
		super(server,NODE_MANAGER_URI);
		
		m_ioManager = new IoManager(this);
		
		this.getIoManager().setListener(m_ioManager);
	}
	
	public void createAddressSpace() throws StatusException
	{
		createType();
		
		FolderType deviceFolder = createDeviceFolder();
		
		createObject(deviceFolder);
	}
	
	public void processDataChanger(UaVariable variable, DataValue dataValue) throws StatusException
	{
		UaObject object = getObject(variable);
		
		if (null == object) return;
		
		UaType type = getObjectType(object);
		
		if (null == type) return;
		
		if (type.getNodeId() == AnalogInputType.getTypeNodeId())
		{
			AnalogInputType analogInputObject = (AnalogInputType)object;
			analogInputObject.processDataChange(variable, dataValue);
		}
	}

	private void createType() throws StatusException
	{
		AnalogInputType.createType(this);		
	}

	private FolderType createDeviceFolder()
	{
		NodeId deviceFolderId = new NodeId(this.getNamespaceIndex(), "Device");
		FolderType deviceFolder = new FolderType(this, deviceFolderId, 
				                                 new QualifiedName(this.getNamespaceIndex(),"Device"), 
				                                 new LocalizedText("�豸",Locale.CHINESE));
		
		UaObject objectFolder = this.getServer().getNodeManagerRoot().getObjectsFolder();
		objectFolder.addReference(deviceFolder, Identifiers.Organizes, false);
		return deviceFolder;
	}
	
	private void createObject(FolderType folder) throws StatusException
	{
		AnalogInputType temperature = new AnalogInputType(this, "Temperature", "�¶ȼ�");
		folder.addReference(temperature, Identifiers.Organizes, false);
	}	
	
	private UaObject getObject(UaVariable variable)
	{
		UaReference objectReference = variable.getReference(Identifiers.HasComponent, true);
			
		if (null == objectReference) return null;
		
		UaNode objectNode = objectReference.getSourceNode();

		if (objectNode.getNodeClass() != NodeClass.Object) return null;
		
		return (UaObject)objectNode;
	}
	
	private UaType getObjectType(UaObject object)
	{
		return object.getTypeDefinition();
	}
	
	private static String NODE_MANAGER_URI = "urn:localhost:UA:ADDRESSSPACE";
	
	private IoManager m_ioManager;
}
