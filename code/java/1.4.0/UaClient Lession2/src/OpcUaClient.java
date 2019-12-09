import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.UaClient;

public class OpcUaClient {

	public OpcUaClient() {
		variableValue1 = (float) 0.0;
		variableValue2 = (float) 0.0;
	}
	
	public Boolean connect() throws SecureIdentityException, IOException, InvalidServerEndpointException, ConnectException, ServiceException, URISyntaxException
	{
		try {
			Initialize();
			m_client.connect();
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void disconnect()
	{
		m_client.disconnect();
	}
	
	public String getServerUrl()
	{
		return SERVER_URL;
	}
	
	public void browse() throws ServiceException, StatusException
	{
		m_client.getAddressSpace().setReferenceTypeId(Identifiers.HierarchicalReferences);
		
		List<ReferenceDescription> objectNodes = m_client.getAddressSpace().browse(new NodeId(2,"Device"));

		for (int i=0; i<objectNodes.size(); i++)
		{
			System.out.println(objectNodes.get(i).getDisplayName().getText());	
			
			NodeId objectId = new NodeId(objectNodes.get(i).getNodeId().getNamespaceIndex(), (String)objectNodes.get(i).getNodeId().getValue());
			List<ReferenceDescription> memberNodes = m_client.getAddressSpace().browse(objectId);
				
			for (int j=0; j<memberNodes.size(); j++)
			{
				System.out.println("成员： " + memberNodes.get(j).getDisplayName().getText());	
			}
			
			System.out.println();
		} 
	}	

	public void read() throws ServiceException
	{
		String variableName1 = new String("Level.Value");
		String variableName2 = new String("Temperature.Value");		
		
		NodeId[] variableIds = new NodeId[2];
		
		variableIds[0] = new NodeId(2, variableName1); 
		variableIds[1] = new NodeId(2, variableName2); 
		
		DataValue[] dataValues = m_client.readValues(variableIds, TimestampsToReturn.Neither);
		
		if (dataValues.length != 2) return;
		
		System.out.println(variableName1 + " = " + dataValues[0].getValue().floatValue());
		System.out.println(variableName2 + " = " + dataValues[1].getValue().floatValue());
	}
	
	public void write() throws ServiceException, StatusException
	{
		String variableName1 = new String("Level.Value");
		String variableName2 = new String("Temperature.Value");		
		
		NodeId[] variableIds = new NodeId[2];		
		variableIds[0] = new NodeId(2, variableName1); 
		variableIds[1] = new NodeId(2, variableName2); 
		
		variableValue1 = variableValue1 + 1;
		variableValue2 = variableValue2 + 2;
		
		Variant[] values = new Variant[2];		
		values[0] = new Variant(variableValue1);
		values[1] = new Variant(variableValue2);
				
		StatusCode[] writeResult = m_client.writeValues(variableIds, values);
		
		if (writeResult[0].isGood()|| writeResult[1].isGood())
		{
			System.out.println("操作成功");
		}
	}
		
	private static String APP_NAME    = "UaClient";
	private static String APP_URI     = "urn:localhost:UA:UaClient";
	private static String PRODUCT_URI = "urn:sunwayland.com:UA:UaClient";	
	
	private static String SERVER_URL = "opc.tcp://172.18.15.58:4850/UaServer";
	
	private UaClient m_client;
	
	private float variableValue1;
	private float variableValue2;
	
	private void Initialize() throws SecureIdentityException, IOException, SessionActivationException, URISyntaxException
	{
		m_client = new UaClient(SERVER_URL);		
		
		initClientIdentity();
		
		initSecuritySetting();		
	}
	
	private void initClientIdentity() throws SecureIdentityException, IOException
	{
		// 初始化应用程序描述
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText(APP_NAME, Locale.ENGLISH));

		appDescription.setApplicationUri(APP_URI);
		appDescription.setProductUri(PRODUCT_URI);
		appDescription.setApplicationType(ApplicationType.Client);

		// 初始化证书管理文件	
		PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
		m_client.setCertificateValidator(validator);

		File privateFile = new File(validator.getBaseDir(), "private");			
	
		// 将客户端描述信息与证书管理信息统一放入ApplicationIdentity中管理
		final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(
				appDescription, 
				"Sunwayland",
				"Sunwayland",
				privateFile,
				true);
		
		m_client.setApplicationIdentity(identity);
	}
	
	private void initSecuritySetting() throws SessionActivationException
	{
		// 设置连接模式
		m_client.setSecurityMode(SecurityMode.NONE);
	}
	

}
