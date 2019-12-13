package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserTokenPolicies;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.server.UserValidator;
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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleConsoleServer {

  private static Logger logger = LoggerFactory.getLogger(SampleConsoleServer.class);

  protected static String APP_NAME = "SampleConsoleServer";
  protected static String discoveryServerUrl = "opc.tcp://localhost:4840";
  protected final UserValidator userValidator = new MyUserValidator();
  protected final DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();
  protected UaServer server;

  protected void initialize(int port, int httpsPort, String applicationName)
      throws SecureIdentityException, IOException, UaServerException {

    //创建UaServer
    server = new UaServer();

    // Use PKI files to keep track of the trusted and rejected client
    final PkiDirectoryCertificateStore certificateStore = new PkiDirectoryCertificateStore();
    final DefaultCertificateValidator validator = new DefaultCertificateValidator(certificateStore);
    validator.setValidationListener(validationListener);//通过自定义函数进行授权认证
    server.setCertificateValidator(validator);

    ApplicationDescription appDescription = new ApplicationDescription();
    appDescription.setApplicationName(new LocalizedText(applicationName + "@hostname"));
    appDescription.setApplicationUri("urn:hostname:OPCUA:" + applicationName);
    appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + applicationName);
    appDescription.setApplicationType(ApplicationType.Server);

    server.setPort(Protocol.OpcTcp, port);

    server.setServerName("OPCUA/" + applicationName);

    server.setBindAddresses(EndpointUtil.getInetAddresses(server.isEnableIPv6()));

    logger.info("Loading certificates..");

    File privatePath = new File(certificateStore.getBaseDir(), "private");

    int[] keySizes = null;
    final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
        "Sample Organisation", /* Private Key Password */"opcua", /* Key File Path */privatePath,
        /* Issuer Certificate & Private Key */null,
        /* Key Sizes for instance certificates to create */keySizes,
        /* Enable renewing the certificate */true);
    server.setApplicationIdentity(identity);

    Set<SecurityPolicy> supportedSecurityPolicies = new HashSet<SecurityPolicy>();
    supportedSecurityPolicies.add(SecurityPolicy.NONE);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_101);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_102);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_103);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_104);

    Set<MessageSecurityMode> supportedMessageSecurityModes = new HashSet<MessageSecurityMode>();
    supportedMessageSecurityModes.add(MessageSecurityMode.None);
    supportedMessageSecurityModes.add(MessageSecurityMode.Sign);
    supportedMessageSecurityModes.add(MessageSecurityMode.SignAndEncrypt);

    server.getSecurityModes().addAll(SecurityMode.combinations(supportedMessageSecurityModes, supportedSecurityPolicies));

    server.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_USERNAME_PASSWORD);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_CERTIFICATE);
    server.setUserValidator(userValidator);

    try {
      server.setDiscoveryServerUrl(discoveryServerUrl);
    } catch (URISyntaxException e) {
      logger.error("DiscoveryURL is not valid", e);
    }

    server.init();

    server.getSessionManager().setMaxSessionCount(500);
    server.getSessionManager().setMaxSessionTimeout(3600000); // one hour
    server.getSubscriptionManager().setMaxSubscriptionCount(50);
  }

  public static void println(String string) {
    System.out.println(string);
  }

  protected void run() throws UaServerException {
    server.start();
  }

  private void addReverseConnection() {
    try {
      this.server.addReverseConnection("opc.tcp://DESKTOP-DLNM7N9:5432", "opc.tcp://DESKTOP-DLNM7N9:52520/OPCUA/SampleConsoleServer");
    } catch (Exception e) {
      logger.error("Cannot add Reverse Connection", e);
    }
  }


  public static void main(String[] args) throws Exception {

    SampleConsoleServer sampleConsoleServer = new SampleConsoleServer();

    sampleConsoleServer.initialize(52520, 52444, APP_NAME);

    sampleConsoleServer.run();

    sampleConsoleServer.addReverseConnection();
  }

}
