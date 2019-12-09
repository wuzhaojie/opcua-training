import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;

public class OpcUaServer {

	private static final String APP_NAME    = "UaServer";
	private static final String SERVER_NAME = "UaServer";
	private static final String APP_URI     = "urn:localhost:UaServer";
	private static final String PRODUCT_URI = "urn:sunwayland.com:UaServer";

	private UaServer               m_server;
	private ApplicationDescription m_appDescription;
	private IdentityValidation     m_identityValidation;
	private CertificateValidation  m_certificateValidation;

	public OpcUaServer() {
		m_server                = new UaServer();	
		m_appDescription        = new ApplicationDescription();
		m_identityValidation    = new IdentityValidation();
		m_certificateValidation = new CertificateValidation();		
	}

	private void initialize() throws UaServerException, SecureIdentityException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, ServiceResultException
	{
		// 初始化服务器信息
		initServerSetting();

		initCertificatePKI();

		initSecuritySetting();

		// 初始化服务器
		m_server.init();
	}

	public void start() throws UaServerException, SecureIdentityException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, ServiceResultException
	{
		initialize();
	
		m_server.start();
	}

	public void stop() 
	{
		m_server.shutdown(2, new LocalizedText("OPC UA Shutdown", Locale.ENGLISH));
	}

	private final void initServerSetting() throws SecureIdentityException, IOException, UaServerException
	{
		// 初始化应用程序描述
		m_appDescription.setApplicationName(new LocalizedText(APP_NAME,Locale.ENGLISH));
		
		m_appDescription.setApplicationUri(APP_URI);	
		m_appDescription.setProductUri(PRODUCT_URI);
	
		m_server.setUseAllIpAddresses(true);
		
		// 初始化网络端口
		m_server.setPort(4850); 
		
		// 服务器注册名
		m_server.setServerName(SERVER_NAME);
	}
	
	private final void initCertificatePKI() throws SecureIdentityException, IOException, ServiceResultException, InvalidKeySpecException, NoSuchAlgorithmException
	{
		// 初始化证书管理中心		
		final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
		m_server.setCertificateValidator(validator);
		validator.setValidationListener(m_certificateValidation);
		
		File privateFile = new File(validator.getBaseDir(), "private");
		
		ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(
			m_appDescription,
			"Sunwayland",
			"Sunwayland",
			privateFile,			
			true,
			"localhost");
		
		m_server.setApplicationIdentity(identity); 
	}
	
	private final void initSecuritySetting() throws UaServerException
	{
		// 设置安全策略
		m_server.setSecurityModes(SecurityMode.ALL);
		
		m_server.addUserTokenPolicy(UserTokenPolicy.SECURE_USERNAME_PASSWORD);
		m_server.setUserValidator(m_identityValidation);
	}
}
