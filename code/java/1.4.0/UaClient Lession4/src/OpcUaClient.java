import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.UaClient;

public class OpcUaClient {

	public OpcUaClient() {
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
	
	public void readHistory() throws ServerConnectionException, DecodingException, ServiceException, StatusException, ParseException
	{	
		String variableName = new String("Temperature.Value");	
		NodeId id = new NodeId(2, variableName);

		DateTime startTime = DateTime.parseDateTime("2013/12/20 00:00:00");
		DateTime endTime = DateTime.parseDateTime("2013/12/20 24:00:00");		
		
		DataValue[] values = null;
		values = m_client.historyReadRaw(id, startTime, endTime, 0, false, null, TimestampsToReturn.Source);
		
		if (null == values) return;
		
		System.out.println("History value " + variableName + ":");

		for (int i=0; i < values.length; i++)
		{
			System.out.println(values[i].getValue().floatValue() + "  " + 
					           values[i].getStatusCode().toString() + "  " +
					           values[i].getSourceTimestamp().toString());
		}
	}	
			
	private static String APP_NAME    = "UaClient";
	private static String APP_URI     = "urn:localhost:UA:UaClient";
	private static String PRODUCT_URI = "urn:sunwayland.com:UA:UaClient";	
	
	private static String SERVER_URL = "opc.tcp://172.18.15.58:4850/UaServer";
	
	private UaClient m_client;
	
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
