import java.util.EnumSet;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.Identifiers;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.CacheVariable;
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
	
	public NodeManager(UaServer server) {
		super(server,NODE_MANAGER_URI);
		
		m_historyManager = new HistoryManager();
		
		this.getHistoryManager().setListener(m_historyManager);
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
		AnalogInputType temperature = new AnalogInputType(this, "Temperature", "温度计");
		folder.addReference(temperature, Identifiers.Organizes, false);
	}	
			
	public void registrateHistoryNode(CacheVariable variable)
	{
		variable.addDataChangeListener(m_historyManager);
	}
	
	private static String NODE_MANAGER_URI = "urn:localhost:UA:ADDRESSSPACE";
	
	private HistoryManager m_historyManager;
}
