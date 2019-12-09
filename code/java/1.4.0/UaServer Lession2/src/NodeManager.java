import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.core.Identifiers;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.opcua.FolderType;

public class NodeManager extends NodeManagerUaNode {

	public NodeManager(UaServer server) {
		super(server,NODE_MANAGER_URI);
	}
	
	public void createAddressSpace() throws StatusException
	{
		createType();
		
		FolderType deviceFolder = createDeviceFolder();
		
		createObject(deviceFolder);
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
				                                 new LocalizedText("设备",Locale.CHINESE));
		
		UaObject objectFolder = this.getServer().getNodeManagerRoot().getObjectsFolder();
		objectFolder.addReference(deviceFolder, Identifiers.Organizes, false);
		return deviceFolder;
	}
	
	private void createObject(FolderType folder) throws StatusException
	{
		AnalogInputType level = new AnalogInputType(this, "Level", "水位计");
		folder.addReference(level, Identifiers.Organizes, false);

		AnalogInputType temperature = new AnalogInputType(this, "Temperature", "温度计");
		folder.addReference(temperature, Identifiers.Organizes, false);
	}	
			
	private static String NODE_MANAGER_URI = "urn:localhost:UA:ADDRESSSPACE";
	
}
