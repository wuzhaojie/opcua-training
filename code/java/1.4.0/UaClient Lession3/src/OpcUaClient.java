import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
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
		m_subscription = null;
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
	
	public void startSubscription() throws ServiceException, StatusException
	{
		if (null == m_subscription)
		{		
			m_subscription = new OpcUaSubscription(m_client);
			m_subscription.createMonitoredItem();
		} 
		
		m_subscription.startSubscription();
	}
	
	public void stopSubscription() throws ServiceException, StatusException
	{
		if (null != m_subscription)
		{
			m_subscription.stopSubscription();
		}
	}

	public String getServerUrl()
	{
		return SERVER_URL;
	}
		
	private static String APP_NAME    = "UaClient";
	private static String APP_URI     = "urn:localhost:UA:UaClient";
	private static String PRODUCT_URI = "urn:sunwayland.com:UA:UaClient";	
	
	private static String SERVER_URL = "opc.tcp://172.18.15.58:4850/UaServer";
	
	private UaClient m_client;
	private OpcUaSubscription m_subscription;
	
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
