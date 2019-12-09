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
import com.prosysopc.ua.stack.transport.security.HttpsSecurityPolicy;
import com.prosysopc.ua.stack.transport.security.KeyPair;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import com.prosysopc.ua.stack.utils.EndpointUtil;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;
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

  public static void main(String[] args) throws Exception {

    SampleConsoleServer sampleConsoleServer = new SampleConsoleServer();

    sampleConsoleServer.initialize(52520, 52444, APP_NAME);

    sampleConsoleServer.run();
  }

  protected void initialize(int port, int httpsPort, String applicationName)
      throws SecureIdentityException, IOException, UaServerException {

    //创建UaServer
    server = new UaServer();

    //默认开启IPV6
    //java6不支持IPV6
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("1.6")) {
      server.setEnableIPv6(false);//关闭IPV6
    } else {
      server.setEnableIPv6(true);//开启IPV6
    }

    // Use PKI files to keep track of the trusted and rejected client
    final PkiDirectoryCertificateStore certificateStore = new PkiDirectoryCertificateStore();
    final DefaultCertificateValidator validator = new DefaultCertificateValidator(certificateStore);
    validator.setValidationListener(validationListener);//通过自定义函数进行授权认证
    server.setCertificateValidator(validator);

    // *** Application Description is sent to the clients
    ApplicationDescription appDescription = new ApplicationDescription();
    // 'localhost' (all lower case) in the ApplicationName and
    // ApplicationURI is converted to the actual host name of the computer
    // (including the possible domain part) in which the application is run.
    // (as available from ApplicationIdentity.getActualHostName())
    // 'hostname' is converted to the host name without the domain part.
    // (as available from
    // ApplicationIdentity.getActualHostNameWithoutDomain())
    appDescription.setApplicationName(new LocalizedText(applicationName + "@hostname"));
    appDescription.setApplicationUri("urn:hostname:OPCUA:" + applicationName);
    appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + applicationName);
    appDescription.setApplicationType(ApplicationType.Server);

    // *** Server Endpoints
    // TCP Port number for the UA TCP protocol
    server.setPort(Protocol.OpcTcp, port);
    // TCP Port for the HTTPS protocol
    server.setPort(Protocol.OpcHttps, httpsPort);

    // optional server name part of the URI (default for all protocols)
    server.setServerName("OPCUA/" + applicationName);

    // Optionally restrict the InetAddresses to which the server is bound.
    // You may also specify the addresses for each Protocol.
    // This is the default (isEnableIPv6 defines whether IPv6 address should
    // be included in the bound addresses. Note that it requires Java 7 or
    // later to work in practice in Windows):

    server.setBindAddresses(EndpointUtil.getInetAddresses(server.isEnableIPv6()));

    // *** Certificates
    logger.info("Loading certificates..");

    File privatePath = new File(certificateStore.getBaseDir(), "private");

    // Define a certificate for a Certificate Authority (CA) which is used
    // to issue the keys. Especially
    // the HTTPS certificate should be signed by a CA certificate, in order
    // to make the .NET applications trust it.
    //
    // If you have a real CA, you should use that instead of this sample CA
    // and create the keys with it.
    // Here we use the IssuerCertificate only to sign the HTTPS certificate
    // (below) and not the Application Instance Certificate.
    KeyPair issuerCertificate =
        ApplicationIdentity.loadOrCreateIssuerCertificate("ProsysSampleCA", privatePath, "opcua", 3650, false);

    // If you wish to use big certificates (4096 bits), you will need to
    // define two certificates for your application, since to interoperate
    // with old applications, you will also need to use a small certificate
    // (up to 2048 bits).

    // Also, 4096 bits can only be used with Basic256Sha256 security
    // profile, which is currently not enabled by default, so we will also
    // leave the the keySizes array as null. In that case, the default key
    // size defined by CertificateUtils.getKeySize() is used.
    int[] keySizes = null;

    // Use 0 to use the default keySize and default file names as before
    // (for other values the file names will include the key size).
    // keySizes = new int[] { 0, 4096 };

    // *** Application Identity

    // Define the Server application identity, including the Application
    // Instance Certificate (but don't sign it with the issuerCertificate as
    // explained above).
    final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
        "Sample Organisation", /* Private Key Password */"opcua", /* Key File Path */privatePath,
        /* Issuer Certificate & Private Key */null,
        /* Key Sizes for instance certificates to create */keySizes,
        /* Enable renewing the certificate */true);

    // Create the HTTPS certificate bound to the hostname.
    // The HTTPS certificate must be created, if you enable HTTPS.
    String hostName = ApplicationIdentity.getActualHostName();
    identity.setHttpsCertificate(ApplicationIdentity.loadOrCreateHttpsCertificate(appDescription, hostName, "opcua",
        issuerCertificate, privatePath, true));

    server.setApplicationIdentity(identity);

    // *** Security settings
    /*
     * Define the security modes to support for the Binary protocol.
     *
     * Note that different versions of the specification might add/deprecate some modes, in this
     * example all the modes are added, but you should add some way in your application to configure
     * these. The set is empty by default, you must add at least one SecurityMode for the server to
     * start.
     */
    Set<SecurityPolicy> supportedSecurityPolicies = new HashSet<SecurityPolicy>();

    /*
     * This policy does not support any security. Should only be used in isolated networks.
     */
    supportedSecurityPolicies.add(SecurityPolicy.NONE);

    // Modes defined in previous versions of the specification
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_101);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_102);
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_103);

    /*
     * Per the 1.04 specification, only these should be used. However in practice this list only
     * contains very new security policies, which most of the client applications as of today that
     * are used might not be unable to (yet) use.
     */
    supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_104);

    Set<MessageSecurityMode> supportedMessageSecurityModes = new HashSet<MessageSecurityMode>();

    /*
     * This mode does not support any security. Should only be used in isolated networks. This is
     * also the only mode, which does not require certificate exchange between the client and server
     * application (when used in conjunction of only ANONYMOUS UserTokenPolicy).
     */
    supportedMessageSecurityModes.add(MessageSecurityMode.None);

    /*
     * This mode support signing, so it is possible to detect if messages are tampered. Note that
     * they are not encrypted.
     */
    supportedMessageSecurityModes.add(MessageSecurityMode.Sign);

    /*
     * This mode signs and encrypts the messages. Only this mode is recommended outside of isolated
     * networks.
     */
    supportedMessageSecurityModes.add(MessageSecurityMode.SignAndEncrypt);

    /*
     * This creates all possible combinations (NONE pairs only with None) of the configured
     * MessageSecurityModes and SecurityPolicies) for opc.tcp communication.
     */
    server.getSecurityModes()
        .addAll(SecurityMode.combinations(supportedMessageSecurityModes, supportedSecurityPolicies));

    /*
     *
     * NOTE! The MessageSecurityMode.None for HTTPS means Application level authentication is not
     * used. If used in combination with the UserTokenPolicy ANONYMOUS anyone can access the server
     * (but the traffic is encrypted). HTTPS mode is always encrypted, therefore the given
     * MessageSecurityMode only affects if the UA certificates are exchanged when forming the
     * Session.
     */
    server.getHttpsSecurityModes().addAll(SecurityMode
        .combinations(EnumSet.of(MessageSecurityMode.None, MessageSecurityMode.Sign), supportedSecurityPolicies));

    // The TLS security policies to use for HTTPS
    Set<HttpsSecurityPolicy> supportedHttpsSecurityPolicies = new HashSet<HttpsSecurityPolicy>();
    // (HTTPS was defined starting from OPC UA Specification 1.02)
    supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_102);
    supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_103);
    // Only these are recommended by the 1.04 Specification
    supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_104);
    server.getHttpsSettings().setHttpsSecurityPolicies(supportedHttpsSecurityPolicies);

    // Number of threads to reserve for the HTTPS server, default is 10
    // server.setHttpsWorkerThreadCount(10);

    // Define the certificate validator for the HTTPS certificates;
    // we use the same validator that we use for Application Instance Certificates
    server.getHttpsSettings().setCertificateValidator(validator);

    // Define the supported user authentication methods
    server.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_USERNAME_PASSWORD);
    server.addUserTokenPolicy(UserTokenPolicies.SECURE_CERTIFICATE);

    // Define a validator for checking the user accounts
    server.setUserValidator(userValidator);

    // Register to the local discovery server (if present)
    try {
      server.setDiscoveryServerUrl(discoveryServerUrl);
    } catch (URISyntaxException e) {
      logger.error("DiscoveryURL is not valid", e);
    }

    // *** 'init' creates the service handlers and the default endpoints
    // *** according to the settings defined above
    server.init();

    // "Safety limits" for ill-behaving clients
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

}
