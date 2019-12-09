import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.ServerStatusListener;
import com.prosysopc.ua.client.UaClient;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Locale;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.ServerState;
import org.opcfoundation.ua.core.ServerStatusDataType;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.SecurityMode;

public class OpcUaClient {

  private static String APP_NAME = "UaClient";
  private static String APP_URI = "urn:localhost:UA:UaClient";
  private static String PRODUCT_URI = "urn:sunwayland.com:UA:UaClient";

  private static String SERVER_URL = "opc.tcp://172.18.15.58:4850/UaServer";

  private static String USERNAME = "opcua";
  private static String PASSWORD = "opcua";

  private UaClient m_client;

  public OpcUaClient() {
  }

  public Boolean connect() {
    try {
      Initialize();
      m_client.connect();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public void disconnect() {
    m_client.disconnect();
  }

  public String getServerUrl() {
    return SERVER_URL;
  }

  private void Initialize() throws SecureIdentityException, IOException, SessionActivationException, URISyntaxException {
    m_client = new UaClient(SERVER_URL);

    initClientIdentity();

    initSecuritySetting();

    // 设置服务器状态监视器
    m_client.addServerStatusListener(serverStatusListener);
  }

  private void initClientIdentity() throws SecureIdentityException, IOException {
    // 初始化应用程序描述
    ApplicationDescription appDescription = new ApplicationDescription();
    appDescription.setApplicationName(new LocalizedText(APP_NAME, Locale.ENGLISH));

    appDescription.setApplicationUri(APP_URI);
    appDescription.setProductUri(PRODUCT_URI);
    appDescription.setApplicationType(ApplicationType.Client);

    // 初始化证书管理文件
    PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();
    m_client.setCertificateValidator(validator);
    validator.setValidationListener(validationListener);

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

  private void initSecuritySetting() throws SessionActivationException {
    // 设置连接模式
    m_client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT);

    // 设置连接密码
    m_client.setUserIdentity(new UserIdentity(USERNAME, PASSWORD));
  }

  private ServerStatusListener serverStatusListener = new ServerStatusListener() {
    @Override
    public void onShutdown(UaClient uaClient, long secondsTillShutdown, LocalizedText shutdownReason) {
      System.out.println("服务器关闭，原因: " + shutdownReason.getText());
    }

    @Override
    public void onStateChange(UaClient uaClient, ServerState oldState, ServerState newState) {
    }

    @Override
    public void onStatusChange(UaClient uaClient, ServerStatusDataType status) {

    }
  };

  private CertificateValidationListener validationListener = new CertificateValidationListener() {

    @Override
    public ValidationResult onValidate(Cert certificate,
        ApplicationDescription applicationDescription,
        EnumSet<CertificateCheck> passedChecks) {
      return ValidationResult.AcceptPermanently;
    }
  };
}
