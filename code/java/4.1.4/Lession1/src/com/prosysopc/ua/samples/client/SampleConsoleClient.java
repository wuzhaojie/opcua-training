package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.MethodCallStatusException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaAddress;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.GlobalServerList;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.ServerList;
import com.prosysopc.ua.client.ServerListBase;
import com.prosysopc.ua.client.ServerListException;
import com.prosysopc.ua.client.ServerStatusListener;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.UaClientListener;
import com.prosysopc.ua.nodes.UaInstance;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.EUInformation;
import com.prosysopc.ua.stack.core.EndpointDescription;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.stack.core.Range;
import com.prosysopc.ua.stack.core.ServerCapability;
import com.prosysopc.ua.stack.core.ServerOnNetwork;
import com.prosysopc.ua.stack.core.UserTokenPolicy;
import com.prosysopc.ua.stack.transport.security.HttpsSecurityPolicy;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.utils.CertificateUtils;
import com.prosysopc.ua.types.opcua.AnalogItemType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sample OPC UA client, running from the console.
 */
public class SampleConsoleClient {

  private static final Logger logger = LoggerFactory.getLogger(SampleConsoleClient.class);

  // Action codes for readAction, etc.
  protected static final int ACTION_ALL = -4;
  protected static final int ACTION_BACK = -2;
  protected static final int ACTION_RETURN = -1;
  protected static final int ACTION_ROOT = -3;
  protected static final int ACTION_TRANSLATE = -6;
  protected static final int ACTION_UP = -5;

  protected static String APP_NAME = "SampleConsoleClient";

  protected static final List<String> cmdSequence = new ArrayList<String>();

  protected static boolean stackTraceOnException = false;

  protected String defaultServerAddress = "opc.tcp://localhost:52520/OPCUA/SampleConsoleServer";

  public static void main(String[] args) throws Exception {

    SampleConsoleClient sampleConsoleClient = new SampleConsoleClient();
    try {
      if (!sampleConsoleClient.parseCmdLineArgs(args)) {
        usage();
        return;
      }
    } catch (IllegalArgumentException e) {
      if (e.getMessage() != null) {
        println("Invalid cmd line argument: " + e.getMessage());
      }
      usage();
      return;
    }

    sampleConsoleClient.initialize(args);
    // Show the menu, which is the main loop of the client application
    sampleConsoleClient.mainMenu();

    println(APP_NAME + ": Closed");

  }

  protected static void usage() {
    println("Usage: " + APP_NAME + " [-d] [-t] [-n] [-?] [serverUri]");
    println("   -d         Connect to a discovery server");
    println("   -n nodeId  Define the NodeId to select after connect (requires serverUri)");
    println("   -s n|s|e[bits]   Define the security mode (n=none/s=sign/e=signAndEncrypt). Default is none.");
    println(
        "                    Oprionally, define the bit strength (128 or 256) you want to use for encryption. Default is 128");
    println(
        "   -k keySize Define the size of the public key of the application certificate (default 1024; other valid values 2048, 4096)");
    println("   -m nodeId  Subscribe to the given node at start up");
    println("   -t         Output stack trace for errors");
    println("   -dt        Show the DataType of read values When displaying them.");
    println("   -?         Show this help text");
    println(
        "   serverUri  The address of the server to connect to. If you do not specify it, you will be prompted for it.");
    println("");
    println(" Examples of valid arguments:");
    println("   opc.tcp://localhost:4841                            (UA Demo Server)");
    println("   opc.tcp://localhost:52520/OPCUA/SampleConsoleServer (Prosys Sample Server)");
    println("   opc.tcp://localhost:51210/UA/SampleServer           (OPC Foundation Sample Server)");
    println("   -d opc.tcp://localhost:4840/UADiscovery             (OPC Foundation Discovery Server)");
  }

  protected static int parseAction(String s) {
    if (s.equals("x")) {
      return ACTION_RETURN;
    }
    if (s.equals("b")) {
      return ACTION_BACK;
    }
    if (s.equals("r")) {
      return ACTION_ROOT;
    }
    if (s.equals("a")) {
      return ACTION_ALL;
    }
    if (s.equals("u")) {
      return ACTION_UP;
    }
    if (s.equals("t")) {
      return ACTION_TRANSLATE;
    }
    return Integer.parseInt(s);
  }

  protected static void print(String string) {
    System.out.print(string);

  }

  protected static void printException(Exception e) {
    if (stackTraceOnException) {
      e.printStackTrace();
    } else {
      println(e.toString());
      if (e instanceof MethodCallStatusException) {
        MethodCallStatusException me = (MethodCallStatusException) e;
        final StatusCode[] results = me.getInputArgumentResults();
        if (results != null) {
          for (int i = 0; i < results.length; i++) {
            StatusCode s = results[i];
            if (s.isBad()) {
              println("Status for Input #" + i + ": " + s);
              DiagnosticInfo d = me.getInputArgumentDiagnosticInfos()[i];
              if (d != null) {
                println("  DiagnosticInfo:" + i + ": " + d);
              }
            }
          }
        }
      }
      if (e.getCause() != null) {
        println("Caused by: " + e.getCause());
      }
    }
  }

  protected static void printf(String format, Object... args) {
    System.out.printf(format, args);
  }

  protected static void println(String string) {
    System.out.println(string);
  }

  protected static int readAction() {
    return parseAction(readInput(true).toLowerCase());
  }

  protected static String readInput(boolean useCmdSequence) {
    return readInput(useCmdSequence, null);
  }

  protected static String readInput(boolean useCmdSequence, String defaultValue) {
    // You can provide "commands" already from the command line, in which
    // case they will be kept in cmdSequence
    if (useCmdSequence && !cmdSequence.isEmpty()) {
      String cmd = cmdSequence.remove(0);
      try {
        // Negative int values are used to pause for n seconds
        int i = Integer.parseInt(cmd);
        if (i < 0) {
          try {
            TimeUnit.SECONDS.sleep(-i);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          return readInput(useCmdSequence, defaultValue);
        }
      } catch (NumberFormatException e) {
        // never mind
      }
      return cmd;
    }
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    String s = null;
    do {
      try {
        s = stdin.readLine();
        if ((s == null) || (s.length() == 0)) {
          s = defaultValue;
          break;
        }
      } catch (IOException e) {
        printException(e);
      }
    } while ((s == null) || (s.length() == 0));
    return s;
  }

  protected UaClient client;
  protected UaClientListener clientListener = new MyUaClientListener();

  protected boolean connectToDiscoveryServer = false;

  protected NodeId nodeId = null;
  protected String passWord;
  protected SecurityMode securityMode = SecurityMode.NONE;

  protected ServerStatusListener serverStatusListener = new MyServerStatusListener();
  protected String serverAddress = null;

  protected int sessionCount = 0;
  protected boolean isInReverseConnectionMode = false;

  protected String userName;

  protected DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();
  protected int reversePort = 0;

  public SampleConsoleClient() {

  }

  private String nodeClassToStr(NodeClass nodeClass) {
    return "[" + nodeClass + "]";
  }

  protected void connect() throws ServerConnectionException {
    if (!client.isConnected()) {
      try {
        if (Protocol.OpcHttps == (client.getAddress() == null ? null : client.getAddress().getProtocol())) {
          println(
              "Using HttpsSecurityPolicies " + Arrays.toString(client.getHttpsSettings().getHttpsSecurityPolicies()));
        } else {
          String securityPolicy =
              client.getEndpoint() == null ? client.getSecurityMode().getSecurityPolicy().getPolicyUri()
                  : client.getEndpoint().getSecurityPolicyUri();
          println("Using SecurityPolicy " + securityPolicy);
        }

        // Define the session name that is visible in the server
        client.setSessionName(String.format("%s@%s Session%d", APP_NAME,
            ApplicationIdentity.getActualHostNameWithoutDomain(), ++sessionCount));

        client.connect();
        try {
          println("ServerStatus: " + client.getServerStatus());
          // println("Endpoint: " + client.getEndpoint());
        } catch (StatusException ex) {
          printException(ex);
        }
      } catch (InvalidServerEndpointException e) {
        print("Invalid Endpoint: ");
        printException(e);
        try {
          // In case we have selected a wrong endpoint, print out the
          // supported ones
          printEndpoints(client.discoverEndpoints());
        } catch (Exception ex) {
          // never mind, if the endpoints are not available
        }
      } catch (ServerConnectionException e) {
        printException(e);
        try {
          // In case we have selected an unavailable security mode,
          // print out the supported ones
          printSecurityModes(client.getSupportedSecurityModes());
        } catch (ServerConnectionException e1) {
          // never mind, if the security modes are not available
        } catch (ServiceException e1) {
          // never mind, if the security modes are not available
        }
      } catch (SessionActivationException e) {
        printException(e);
        try {
          printUserIdentityTokens(client.getSupportedUserIdentityTokens());
        } catch (ServiceException e1) {
          // never mind, if not available
        }
        return; // No point to continue
      } catch (ServiceException e) {
        printException(e);
      }
    }
  }

  protected void disconnect() {
    client.disconnect();
  }

  protected boolean discover() throws URISyntaxException, ServerListException {
    String[] discoveryUrls;
    discoveryUrls = discoverServer(client.getAddress() == null ? "" : client.getAddress().getAddress());
    if (discoveryUrls != null) {
      EndpointDescription endpoint = discoverEndpoints(discoveryUrls);
      if (endpoint != null) {
        client.disconnect();
        client.setEndpoint(endpoint);
        return true;
      }
    }
    return false;
  }

  protected EndpointDescription discoverEndpoints(String[] discoveryUrls) throws URISyntaxException {
    if (discoveryUrls != null) {
      UaClient discoveryClient = new UaClient();
      int i = 0;
      List<EndpointDescription> edList = new ArrayList<EndpointDescription>();

      println("Available endpoints: ");
      println(String.format("%s - %-50s - %-20s - %-20s - %s", "#", "URI", "Security Mode", "Security Policy",
          "Transport Profile"));
      for (String url : discoveryUrls) {
        discoveryClient.setAddress(UaAddress.parse(url));
        try {
          for (EndpointDescription ed : discoveryClient.discoverEndpoints()) {
            println(String.format("%s - %-50s - %-20s - %-20s - %s", i++, ed.getEndpointUrl(), ed.getSecurityMode(),
                ed.getSecurityPolicyUri().replaceFirst("http://opcfoundation.org/UA/SecurityPolicy#", ""),
                ed.getTransportProfileUri().replaceFirst("http://opcfoundation.org/UA-Profile/Transport/", "")));
            edList.add(ed);
          }
        } catch (Exception e) {
          println("Cannot discover Endpoints from URL " + url + ": " + e.getMessage());
        }
      }
      System.out.println("-------------------------------------------------------");
      println("- Enter endpoint number to select that one");
      println("- Enter x to return to cancel");
      System.out.println("-------------------------------------------------------");
      // // Select an endpoint with the same protocol as the
      // // original request, if available
      // URI uri = new URI(url);
      // if (uri.getScheme().equals(client.getProtocol().toString()))
      // {
      // connectUrl = url;
      // println("Selected application "
      // + serverApp.getApplicationName().getText()
      // + " at " + url);
      // break;
      // } else if (connectUrl == null)
      // connectUrl = url;

      EndpointDescription endpoint = null;
      while (endpoint == null) {
        try {
          int n = readAction();
          if (n == ACTION_RETURN) {
            return null;
          } else {
            return edList.get(n);
          }
        } catch (Exception e) {

        }
      }
    } else {
      println("No suitable discoveryUrl available: using the current Url");
    }
    return null;
  }

  /**
   * Discover server applications from a GDS, LDS or the server itself.
   *
   * @throws ServerListException if the server list cannot be retrieved
   */
  protected String[] discoverServer(String uri) throws ServerListException {
    // Discover a new server list from a discovery server at URI
    ServerListBase serverList;
    try {
      // try to use as a GDS first
      serverList = new GlobalServerList(uri);
    } catch (ServerListException e) {
      // otherwise, use as LDS or standard server
      serverList = new ServerList(uri);
    }
    println("");
    if (serverList.size() == 0 && serverList.getServersOnNetwork().length == 0) {
      println("No servers found");
      return null;
    }

    if (!serverList.isEmpty()) {
      println("Local servers:");
      println(String.format("%s - %-40s - %-15s - %-50s - %s", "#", "Name", "Type", "ProductUri", "ApplicationUri"));
      for (int i = 0; i < serverList.size(); i++) {
        final ApplicationDescription s = serverList.get(i);
        println(String.format("%d - %-40s - %-15s - %-50s - %s", i, s.getApplicationName().getText(),
            s.getApplicationType(), s.getProductUri(), s.getApplicationUri()));
      }
    }
    int n = serverList.size();
    List<ServerOnNetwork> serversOnNetwork = Arrays.asList(serverList.getServersOnNetwork());
    if (!serversOnNetwork.isEmpty()) {
      println("Servers on network:");
      println(String.format("%s - %-40s - %-20s - %s", "#", "Name", "Capabilities", "DiscoveryUrl"));
      for (int i = 0; i < serversOnNetwork.size(); i++) {
        final ServerOnNetwork s = serversOnNetwork.get(i);
        String capabilitiesString;
        try {
          EnumSet<ServerCapability> capabilities = ServerCapability.getSet(s.getServerCapabilities());
          capabilitiesString = ServerCapability.toString(capabilities);
        } catch (IllegalArgumentException e) {
          // If the capabilities is invalid, display the original as list, and the exception
          capabilitiesString = Arrays.toString(s.getServerCapabilities()) + " (NOTE! " + e.getMessage() + ")";
        }
        println(String.format("%d - %-40s - %-20s - %s", n + i, s.getServerName(), capabilitiesString,
            s.getDiscoveryUrl()));
      }
    }
    println("-------------------------------------------------------");
    println("- Enter client number to select that one");
    println("- Enter x to return to cancel");
    println("-------------------------------------------------------");
    do {
      int action = readAction();
      switch (action) {
        case ACTION_RETURN:
          return null;
        default:
          if (action < n) {
            return serverList.get(action).getDiscoveryUrls();
          } else {
            return new String[]{serversOnNetwork.get(action - n).getDiscoveryUrl()};
          }
      }
    } while (true);
  }

  protected String getCurrentNodeAsString(UaNode node) {
    String nodeStr = "";
    String typeStr = "";
    String analogInfoStr = "";
    nodeStr = node.getDisplayName().getText();
    UaType type = null;
    if (node instanceof UaInstance) {
      type = ((UaInstance) node).getTypeDefinition();
    }
    typeStr = (type == null ? nodeClassToStr(node.getNodeClass()) : type.getDisplayName().getText());

    // This is the way to access type specific nodes and their
    // properties, for example to show the engineering units and
    // range for all AnalogItems
    if (node instanceof AnalogItemType) {
      try {
        AnalogItemType analogNode = (AnalogItemType) node;
        EUInformation units = analogNode.getEngineeringUnits();
        analogInfoStr = units == null ? "" : " Units=" + units.getDisplayName().getText();
        Range range = analogNode.getEURange();
        analogInfoStr =
            analogInfoStr + (range == null ? "" : String.format(" Range=(%f; %f)", range.getLow(), range.getHigh()));
      } catch (Exception e) {
        printException(e);
      }
    }

    String currentNodeStr =
        String.format("*** Current Node: %s: %s (ID: %s)%s", nodeStr, typeStr, node.getNodeId(), analogInfoStr);
    return currentNodeStr;
  }

  protected void initialize(String[] args)
      throws URISyntaxException, SecureIdentityException, IOException, SessionActivationException, ServerListException {

    if (serverAddress == null || serverAddress.isEmpty()) {
      client = new UaClient();
    } else {
      client = new UaClient(serverAddress);
    }

    // Add listener
    client.setListener(clientListener);

    // Use PKI files to keep track of the trusted and rejected server
    // certificates...
    final PkiDirectoryCertificateStore certStore = new PkiDirectoryCertificateStore();
    final DefaultCertificateValidator validator = new DefaultCertificateValidator(certStore);
    client.setCertificateValidator(validator);
    // ...and react to validation results with a custom handler (to prompt
    // the user what to do, if necessary)
    validator.setValidationListener(validationListener);

    // *** Application Description is sent to the server
    ApplicationDescription appDescription = new ApplicationDescription();
    // 'localhost' (all lower case) in the ApplicationName and
    // ApplicationURI is converted to the actual host name of the computer
    // in which the application is run
    appDescription.setApplicationName(new LocalizedText(APP_NAME + "@localhost"));
    appDescription.setApplicationUri("urn:localhost:OPCUA:" + APP_NAME);
    appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + APP_NAME);
    appDescription.setApplicationType(ApplicationType.Client);

    // *** Certificates
    logger.info("Loading certificates..");

    File privatePath = new File(certStore.getBaseDir(), "private");

    int[] keySizes = null;
    // If you wish to use big certificates (4096 bits), you will need to
    // define two certificates for your application, since to interoperate
    // with old applications, you will also need to use a small certificate
    // (up to 2048 bits).

    // 4096 bits can only be used with Basic256Sha256 security profile,
    // which is currently not enabled by default, so we will also not define
    // this by default.

    // Use 0 to use the default keySize and default file names as before
    // (for other values the file names will include the key size).
    // keySizes = new int[] { 0, 4096 };

    // *** Application Identity
    // Define the client application identity, including the security
    // certificate
    final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
        "Sample Organisation", /* Private Key Password, optional */"opcua", /* Key File Path */privatePath,
        /* CA certificate & private key, optional */null,
        /* Key Sizes for instance certificates to create, optional */keySizes,
        /* Enable renewing the certificate */true);

    client.setApplicationIdentity(identity);

    // Define our user locale - the default is Locale.getDefault()
    client.setLocale(Locale.ENGLISH);

    // Define the call timeout in milliseconds. Default is null - to
    // use the value of UaClient.getEndpointConfiguration() which is
    // 120000 (2 min) by default
    client.setTimeout(30000);

    // StatusCheckTimeout is used to detect communication
    // problems and start automatic reconnection.
    // These are the default values:
    client.setStatusCheckTimeout(10000);
    // client.setAutoReconnect(true);

    // Listen to server status changes
    client.addServerStatusListener(serverStatusListener);

    // Define the security mode
    // - Default (in UaClient) is BASIC128RSA15_SIGN_ENCRYPT
    // client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN_ENCRYPT);
    // client.setSecurityMode(SecurityMode.BASIC128RSA15_SIGN);
    // client.setSecurityMode(SecurityMode.NONE);

    // securityMode is defined from the command line
    client.setSecurityMode(securityMode);

    // The TLS security policies to use for HTTPS
    Set<HttpsSecurityPolicy> supportedHttpsModes = new HashSet<HttpsSecurityPolicy>();
    // HTTPS was added in UA 1.02
    supportedHttpsModes.addAll(HttpsSecurityPolicy.ALL_102);
    supportedHttpsModes.addAll(HttpsSecurityPolicy.ALL_103);
    supportedHttpsModes.addAll(HttpsSecurityPolicy.ALL_104);
    client.getHttpsSettings().setHttpsSecurityPolicies(supportedHttpsModes);

    // Define a custom certificate validator for the HTTPS certificates
    client.getHttpsSettings().setCertificateValidator(validator);

    // If the server supports user authentication, you can set the user
    // identity.
    if (userName == null) {
      // - Default is to use Anonymous authentication, like this:
      client.setUserIdentity(new UserIdentity());
    } else {
      // - Use username/password authentication (note requires security,
      // above):
      if (passWord == null) {
        print("Enter password for user " + userName + ":");
        passWord = readInput(false);
      }
      client.setUserIdentity(new UserIdentity(userName, passWord));
    }
    // - Read the user certificate and private key from files:
    // client.setUserIdentity(new UserIdentity(new java.net.URL(
    // "my_certificate.der"), new java.net.URL("my_protectedkey.pfx"),
    // "my_protectedkey_password"));

    // Session timeout 10 minutes; default is one hour
    // client.setSessionTimeout(600000);

    // Set endpoint configuration parameters
    client.getEndpointConfiguration().setMaxByteStringLength(Integer.MAX_VALUE);
    client.getEndpointConfiguration().setMaxArrayLength(Integer.MAX_VALUE);

    // TCP Buffer size parameters - these may help with high traffic
    // situations.
    // See http://fasterdata.es.net/host-tuning/background/ for some hints
    // how to use them
    // TcpConnection.setReceiveBufferSize(700000);
    // TcpConnection.setSendBufferSize(700000);

    if (isInReverseConnectionMode) {
      println("Waiting Server-initiated connections at port: " + reversePort);
    } else {
      println("Connecting to " + serverAddress);
    }
  }

  protected void mainMenu() throws ServerListException, URISyntaxException {

    if (connectToDiscoveryServer) {
      if (!discover()) {
        return;
      }
    }

    // Try to connect to the server already at this point.
    connect();

    // You have one node selected all the time, and all operations
    // target that. We can initialize that to the standard ID of the
    // RootFolder (unless it was specified from command line).

    // Identifiers contains a list of all standard node IDs
    if (nodeId == null) {
      nodeId = Identifiers.RootFolder;
    }

    /******************************************************************************/
    /* Wait for user command to execute next action. */
    do {
      printMenu(nodeId);

      try {
        switch (readAction()) {
          case ACTION_RETURN:
            disconnect();
            return;
          case 0:
            if (discover()) {
              connect();
            }
            break;
          case 1:
            connect();
            break;
          case 2:
            disconnect();
            break;
          default:
            continue;
        }
      } catch (Exception e) {
        printException(e);
      }

    } while (true);
    /******************************************************************************/
  }

  protected void printCurrentNode(NodeId nodeId) {
    if (client.isConnected()) {
      // Find the node from the NodeCache
      try {
        UaNode node = client.getAddressSpace().getNode(nodeId);
        if (node == null) {
          return;
        }
        String currentNodeStr = getCurrentNodeAsString(node);
        if (currentNodeStr != null) {
          println(currentNodeStr);
          println("");
        }
      } catch (ServiceException e) {
        printException(e);
      } catch (AddressSpaceException e) {
        printException(e);
      }
    }
  }

  protected void printEndpoints(EndpointDescription[] endpoints) {
    println("Endpoints supported by the server (by Discovery Service)");
    for (EndpointDescription e : endpoints) {
      println(String.format("%s [%s,%s]", e.getEndpointUrl(), e.getSecurityPolicyUri(), e.getSecurityMode()));
    }

  }

  protected void printMenu(NodeId nodeId) {
    println("");
    println("");
    println("");
    if (client.isConnected()) {
      println("*** Connected to: " + client.getAddress());
      println("");
      if (nodeId != null) {
        printCurrentNode(nodeId);
      }
    } else {
      println("*** NOT connected to: " + client.getAddress());
    }

    System.out.println("-------------------------------------------------------");
    println("- Enter x to close client");
    System.out.println("-------------------------------------------------------");
    System.out.println("- Enter 0 to start discovery                          -");
    System.out.println("- Enter 1 to connect to server                        -");
    System.out.println("- Enter 2 to disconnect from server                   -");
    System.out.println("-------------------------------------------------------");
  }

  protected void printSecurityModes(List<SecurityMode> supportedSecurityModes) {
    println("SecurityModes supported by the server:");
    for (SecurityMode m : supportedSecurityModes) {
      println(m.toString());
    }
  }

  protected void printUserIdentityTokens(UserTokenPolicy[] supportedUserIdentityTokens) {
    println("The server supports the following user tokens:");
    for (UserTokenPolicy p : supportedUserIdentityTokens) {
      println(p.getTokenType().toString());
    }

  }

  protected String promptServerAddress() throws IllegalArgumentException {
    while (true) {
      println("Enter the connection URL of the server to connect to\n(press enter to use the default address="
          + defaultServerAddress + "):");
      String url = readInput(false, defaultServerAddress);
      try {
        UaAddress.validate(url);
        return url;
      } catch (URISyntaxException e) {
        print(e.getMessage() + "\n\n");
      }
    }
  }

  protected void parseSecurityMode(String arg) {
    char secModeStr = arg.charAt(0);
    int level = 0;
    if (arg.length() > 1) {
      level = Integer.parseInt(arg.substring(1, 2));
    }
    if (secModeStr == 'n') {
      securityMode = SecurityMode.NONE;
    } else if (secModeStr == 's') {
      switch (level) {
        default:
        case 1:
          securityMode = SecurityMode.BASIC128RSA15_SIGN;
          break;
        case 2:
          securityMode = SecurityMode.BASIC256_SIGN;
          break;
        case 3:
          securityMode = SecurityMode.BASIC256SHA256_SIGN;
          break;
      }
    } else if (secModeStr == 'e') {
      switch (level) {
        default:
        case 1:
          securityMode = SecurityMode.BASIC128RSA15_SIGN_ENCRYPT;
          break;
        case 2:
          securityMode = SecurityMode.BASIC256_SIGN_ENCRYPT;
          break;
        // Will be available in a new stack
        case 3:
          securityMode = SecurityMode.BASIC256SHA256_SIGN_ENCRYPT;
          break;
      }
    } else {
      throw new IllegalArgumentException(
          "parameter for SecuirtyMode (-s) is invalid, expected 'n', 's' or 'e'; was '" + secModeStr + "'");
    }
  }

  protected void promptSecurityMode() {
    println("Select the security mode to use.");
    println("(n=None,s=Sign,e=SignAndEncrypt)");
    while (true) {
      try {
        parseSecurityMode(readInput(false).toLowerCase());
        break;
      } catch (IllegalArgumentException e) {
        printException(e);
      }
    }
  }

  protected boolean parseCmdLineArgs(String[] args) throws IllegalArgumentException {
    int i = 0;
    boolean secModeSet = false;
    while ((args.length > i) && ((args[i].startsWith("-") || args[i].startsWith("/")))) {
      if (args[i].equals("-d")) {
        println("Connecting to a discovery server.");
        connectToDiscoveryServer = true;
      } else if (args[i].equals("-n")) {
        //nodeId = NodeId.parseNodeId(args[++i]);
      } else if (args[i].equals("-s")) {
        String arg = args[++i];
        parseSecurityMode(arg);
        secModeSet = true;
      } else if (args[i].equals("-k")) {
        CertificateUtils.setKeySize(Integer.parseInt(args[++i]));
      } else if (args[i].equals("-m")) {
        //initialMonitoredItems.add(args[++i]);
      } else if (args[i].equals("-u")) {
        userName = args[++i];
      } else if (args[i].equals("-p")) {
        passWord = args[++i];
      } else if (args[i].equals("-t")) {
        //stackTraceOnException = true;
      } else if (args[i].equals("-r")) {
        //isInReverseConnectionMode = true;
      } else if (args[i].equals("-dt")) {
        //showReadValueDataType = true;
      } else if (args[i].equals("-w")) {
        // do nothing, was handled at startup
      } else if (args[i].equals("-?")) {
        return false;
      } else {
        throw new IllegalArgumentException(args[i]);
      }
      i++;
    }
    if (i < args.length) {
      serverAddress = args[i++];
      while ((i < args.length) && !args[i].startsWith("#")) {
        cmdSequence.add(args[i++]);
      }
    }
    if (serverAddress == null) {
      serverAddress = promptServerAddress();
      if (!secModeSet) {
        promptSecurityMode();
      }
    }
    return true;
  }

}
