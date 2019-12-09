import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;


public class OpcUaServer {

	public OpcUaServer() {
		m_server                = new UaServer();	
		m_appDescription        = new ApplicationDescription();
		m_certificateValidation = new CertificateValidation();
	}

	public void start() throws UaServerException, SecureIdentityException, IOException, StatusException
	{
		initialize();
		
		m_nodeManager = new NodeManager(m_server);
		
		m_nodeManager.createAddressSpace();
		
		m_server.start();
	}
		
	private void initialize() throws UaServerException, SecureIdentityException, IOException
	{		
		// 初始化服务器信息
		initServerIdentity();

		initCertificatePKI();
					
		initSecuritySetting();

		// 初始化服务器
		m_server.init();
	}
		
	public void stop() 
	{
		m_server.shutdown(2, new LocalizedText("Closed by user", Locale.ENGLISH));
	}
		
	private static final String APP_NAME    = "UaServer";
	private static final String SERVER_NAME = "UaServer";	
	private static final String APP_URI     = "urn:localhost:UA:UaServer";
	private static final String PRODUCT_URI = "urn:sunwayland.com:UA:UaServer";	
	
	private UaServer               m_server;	
	private NodeManager            m_nodeManager;
	private ApplicationDescription m_appDescription;
	private CertificateValidation  m_certificateValidation;
	
	private final void initServerIdentity() throws SecureIdentityException, IOException, UaServerException
	{
		// 初始化应用程序描述
		m_appDescription.setApplicationName(new LocalizedText(APP_NAME,Locale.ENGLISH));
		
		m_appDescription.setApplicationUri(APP_URI);		
		m_appDescription.setProductUri(PRODUCT_URI);
	
		// 初始化网络端口
		m_server.setUseAllIpAddresses(true);
		
		m_server.setUseLocalhost(true);
		
		m_server.setPort(4850);      
		
		m_server.setServerName(SERVER_NAME);    
	}
		
	private final void initCertificatePKI() throws SecureIdentityException, IOException
	{
		// 初始化证书管理中心		
		final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
		m_server.setCertificateValidator(validator);
		validator.setValidationListener(m_certificateValidation);
		
		File privateFile = new File(validator.getBaseDir(), "private");		
		// 将服务器描述信息与证书管理信息统一放入ApplicationIdentity中管理
		ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(
			m_appDescription,
			"Sunwayland",
			"Sunwayland",
			privateFile,			
			true,
			m_server.getHostNames()); 
		
		m_server.setApplicationIdentity(identity);
	}
		
	private final void initSecuritySetting() throws UaServerException
	{
		// 设置安全策略
		m_server.setSecurityModes(SecurityMode.NON_SECURE);

		m_server.addUserTokenPolicy(UserTokenPolicy.ANONYMOUS);
	}	
}
