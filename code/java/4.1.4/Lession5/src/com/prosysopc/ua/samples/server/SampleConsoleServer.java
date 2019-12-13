package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaApplication;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserTokenPolicies;
import com.prosysopc.ua.samples.myModel.server.ServerInformationModel;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.server.UserValidator;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.MessageSecurityMode;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import com.prosysopc.ua.stack.utils.EndpointUtil;
import com.prosysopc.ua.types.opcua.server.BuildInfoTypeNode;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleConsoleServer {

  private static Logger logger = LoggerFactory.getLogger(SampleConsoleServer.class);

  protected static String APP_NAME = "SampleConsoleServer";
  protected final UserValidator userValidator = new MyUserValidator();
  protected final DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();

  protected UaServer server;

  public static void println(String string) {
    System.out.println(string);
  }

  /**
   * Load information models into the address space. Also register classes, to be able to use the respective Java classes with
   * NodeManagerUaNode.createInstance().
   *
   * See the Codegen Manual on instructions how to use your own models.
   */
  protected void loadInformationModels() {
    // Register generated classes
    server.registerModel(ServerInformationModel.MODEL);

    // Load the standard information models
    try {
      // or via the codegenerated helper getLocationURI() method
      server.getAddressSpace().loadModel(
          new File("E:\\src\\3\\opcua-training\\code\\java\\4.1.4\\Lession5\\src\\com\\prosysopc\\ua\\samples\\mymodel.xml").toURI());

      // You can also register and load model in one call
      //server.registerAndLoadModel(PlcServerInformationModel.MODEL, PlcServerInformationModel.getLocationURI());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Initialize the information to the Server BuildInfo structure
   */
  protected void initBuildInfo() {
    // Initialize BuildInfo - using the version info from the SDK
    // You should replace this with your own build information

    //根据jdk提供的xml构建标准地址空间，可以替换为自己的地址空间
    //xml文件位置:/com/prosysopc/ua/types/opcua/server/Opc.Ua.NodeSet2.xml
    final BuildInfoTypeNode buildInfo =
        server.getNodeManagerRoot().getServerData().getServerStatusNode().getBuildInfoNode();

    buildInfo.setProductName(APP_NAME);

    final String implementationVersion = UaApplication.getSdkVersion();
    if (implementationVersion != null) {
      int splitIndex = implementationVersion.lastIndexOf("-");
      final String softwareVersion = splitIndex == -1 ? "dev" : implementationVersion.substring(0, splitIndex);
      String buildNumber = splitIndex == -1 ? "dev" : implementationVersion.substring(splitIndex + 1);

      buildInfo.setManufacturerName("Prosys OPC Ltd");
      buildInfo.setSoftwareVersion(softwareVersion);
      buildInfo.setBuildNumber(buildNumber);

    }

    final URL classFile = UaServer.class.getResource("/com/prosysopc/ua/samples/server/SampleConsoleServer.class");
    if (classFile != null && classFile.getFile() != null) {
      final File mfFile = new File(classFile.getFile());
      GregorianCalendar c = new GregorianCalendar();
      c.setTimeInMillis(mfFile.lastModified());
      buildInfo.setBuildDate(new DateTime(c));
    }
  }

  protected void initialize(int port, int httpsPort, String applicationName)
      throws SecureIdentityException, IOException, UaServerException {

    server = new UaServer();

    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("1.6")) {
      server.setEnableIPv6(false);
    } else {
      server.setEnableIPv6(true);
    }

    server.setPort(Protocol.OpcTcp, port);
    server.setServerName("OPCUA/" + applicationName);
    server.setBindAddresses(EndpointUtil.getInetAddresses(server.isEnableIPv6()));

    // 初始化证书管理中心
    final PkiDirectoryCertificateStore certificateStore = new PkiDirectoryCertificateStore();
    final DefaultCertificateValidator validator = new DefaultCertificateValidator(certificateStore);
    validator.setValidationListener(validationListener);
    server.setCertificateValidator(validator);

    ApplicationDescription appDescription = new ApplicationDescription();
    appDescription.setApplicationName(new LocalizedText(applicationName + "@hostname"));
    appDescription.setApplicationUri("urn:hostname:OPCUA:" + applicationName);
    appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + applicationName);
    appDescription.setApplicationType(ApplicationType.Server);

    //初始化证书
    logger.info("Loading certificates..");
    File privatePath = new File(certificateStore.getBaseDir(), "private");
    ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(
        appDescription,
        "Sample Organisation",
        "opcua",
        privatePath,
        true,
        "localhost");
    server.setApplicationIdentity(identity);

    //opc ua各个版本协议栈支持的安全策略；
    Set<SecurityPolicy> supportedSecurityPolicies = new HashSet<SecurityPolicy>();
    supportedSecurityPolicies.add(SecurityPolicy.NONE);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_101);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_102);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_103);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_104);

    //opc ua支持的安全模式；
    Set<MessageSecurityMode> supportedMessageSecurityModes = new HashSet<MessageSecurityMode>();
    supportedMessageSecurityModes.add(MessageSecurityMode.None);
    supportedMessageSecurityModes.add(MessageSecurityMode.Sign);
    supportedMessageSecurityModes.add(MessageSecurityMode.SignAndEncrypt);

    //设置opc ua server安全模式和安全策略；
    server.getSecurityModes().addAll(SecurityMode.combinations(supportedMessageSecurityModes, supportedSecurityPolicies));

    //设置opc ua server支持的用户认证和授权方式；
    server.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_USERNAME_PASSWORD);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_CERTIFICATE);

    server.setUserValidator(userValidator);

    server.init();

    initBuildInfo();

    server.getSessionManager().setMaxSessionCount(500);
    server.getSessionManager().setMaxSessionTimeout(3600000);
    server.getSubscriptionManager().setMaxSubscriptionCount(50);
  }

  protected void run(boolean enableSessionDiagnostics) throws UaServerException, StatusException {

    server.start();

    server.getNodeManagerRoot().getServerData().getServerDiagnosticsNode().setEnabled(true);
  }

  public static void main(String[] args) throws Exception {

    //Initialization and Start Up
    SampleConsoleServer sampleConsoleServer = new SampleConsoleServer();

    // Initialize the server
    sampleConsoleServer.initialize(52520, 52443, APP_NAME);

    // Create the address space
    sampleConsoleServer.loadInformationModels();

    sampleConsoleServer.run(true);
  }
}


