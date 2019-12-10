package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaApplication;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserTokenPolicies;
import com.prosysopc.ua.server.NodeBuilderException;
import com.prosysopc.ua.server.NodeManagerListener;
import com.prosysopc.ua.server.UaInstantiationException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.server.UserValidator;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleConsoleServer {

  private static Logger logger = LoggerFactory.getLogger(SampleConsoleServer.class);

  protected static String APP_NAME = "SampleConsoleServer";

  protected MyHistorian myHistorian;
  protected MyNodeManager myNodeManager;
  protected final UserValidator userValidator = new MyUserValidator();
  protected NodeManagerListener myNodeManagerListener = new MyNodeManagerListener();
  protected final DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();

  protected UaServer server;

  public UaServer getServer() {
    return server;
  }

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
    // Uncomment to take the extra information models in use.
    /*
     * NOTE! requires that code for the the respective DI/ADI/PLC models are generated first using
     * the Codegen.
     */

    // // Register generated classes
    // server.registerModel(DiServerInformationModel.MODEL);
    // server.registerModel(AdiServerInformationModel.MODEL);
    //
    // // Load the standard information models
    // try {
    // // You can reference these bundled models either directly
    // server.getAddressSpace().loadModel(DiServerInformationModel.class.getResource("Opc.Ua.Di.NodeSet2.xml").toURI());
    //
    // // or via the codegenerated helper getLocationURI() method
    // server.getAddressSpace().loadModel(AdiServerInformationModel.getLocationURI());
    //
    // // You can also register and load model in one call
    // server.registerAndLoadModel(PlcServerInformationModel.MODEL,
    // PlcServerInformationModel.getLocationURI());
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
  }

  /**
   * Create a sample address space with a new folder, a device object, a level variable, and an alarm condition.
   * <p>
   * The method demonstrates the basic means to create the nodes and references into the address space.
   * <p>
   * Simulation of the level measurement is defined in
   *
   * @throws StatusException if the referred type nodes are not found from the address space
   */
  protected void createAddressSpace() throws StatusException, UaInstantiationException, NodeBuilderException {
    // Load the standard information models
    loadInformationModels();

    myNodeManager = new MyNodeManager(server, MyNodeManager.NAMESPACE);

    myNodeManager.getHistoryManager().setListener(myHistorian);

    logger.info("Address space created.");
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

    myHistorian = new MyHistorian(server.getAggregateCalculator());

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

  protected void initHistory() {
    for (UaVariableNode v : myNodeManager.getHistorizableVariables()) {
      myHistorian.addVariableHistory(v);
    }
    for (UaObjectNode o : myNodeManager.getHistorizableEvents()) {
      myHistorian.addEventHistory(o);
    }
  }

  protected void run(boolean enableSessionDiagnostics) throws UaServerException, StatusException {

    server.start();

    server.getNodeManagerRoot().getServerData().getServerDiagnosticsNode().setEnabled(true);

    startSimulation();
  }

  private final Runnable simulationTask = new Runnable() {
    @Override
    public void run() {
      if (server.isRunning()) {
        logger.debug("Simulating");
        simulate();
      }
    }
  };

  protected void simulate() {
    myNodeManager.simulate();
  }

  private final ScheduledExecutorService simulator = Executors.newScheduledThreadPool(10);

  protected void startSimulation() {
    simulator.scheduleAtFixedRate(simulationTask, 1000, 1000, TimeUnit.MILLISECONDS);
    logger.info("Simulation started.");
  }

  public static void main(String[] args) throws Exception {

    //Initialization and Start Up
    SampleConsoleServer sampleConsoleServer = new SampleConsoleServer();

    // Initialize the server
    sampleConsoleServer.initialize(52520, 52443, APP_NAME);

    // Create the address space
    sampleConsoleServer.createAddressSpace();

    sampleConsoleServer.run(true);

    sampleConsoleServer.initHistory();
  }
}
