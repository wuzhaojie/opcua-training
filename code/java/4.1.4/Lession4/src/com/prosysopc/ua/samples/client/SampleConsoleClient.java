package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.UaClientListener;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleConsoleClient {

  private static final Logger logger = LoggerFactory.getLogger(SampleConsoleClient.class);

  protected static String APP_NAME = "SampleConsoleClient";
  protected String serverAddress = "opc.tcp://localhost:52520/OPCUA/SampleConsoleServer";

  protected DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();
  protected UaClientListener clientListener = new MyUaClientListener();
  protected int sessionCount = 0;

  protected UaClient client;

  public SampleConsoleClient() {
  }

  protected void initialize() throws ServiceException, IOException, SecureIdentityException {

    client = new UaClient();

    //设置client监听器，用于处理建立连接之后的事件
    client.setListener(clientListener);

    client.setReversePort(5432);

    client.setReverseConnectionListener(new MyReverseConnectionListener());

    final PkiDirectoryCertificateStore certStore = new PkiDirectoryCertificateStore();
    final DefaultCertificateValidator validator = new DefaultCertificateValidator(certStore);
    client.setCertificateValidator(validator);
    validator.setValidationListener(validationListener);

    // *** Certificates
    logger.info("Loading certificates..");

    ApplicationDescription appDescription = new ApplicationDescription();
    appDescription.setApplicationName(new LocalizedText(APP_NAME + "@localhost"));
    appDescription.setApplicationUri("urn:localhost:OPCUA:" + APP_NAME);
    appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + APP_NAME);
    appDescription.setApplicationType(ApplicationType.Client);

    File privatePath = new File(certStore.getBaseDir(), "private");

    final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
        "Sample Organisation",
        "opcua",
        privatePath,
        null,
        null,
        true);

    client.setApplicationIdentity(identity);

    // - Default is to use Anonymous authentication, like this:
    client.setUserIdentity(new UserIdentity());

    client.setLocale(Locale.ENGLISH);
    client.setTimeout(30000);
    client.setStatusCheckTimeout(10000);
    client.setSecurityMode(SecurityMode.NONE);
    client.getEndpointConfiguration().setMaxByteStringLength(Integer.MAX_VALUE);
    client.getEndpointConfiguration().setMaxArrayLength(Integer.MAX_VALUE);
  }

  protected void connect() throws ServerConnectionException, ServiceException {
    if (!client.isConnected()) {
      client.setSessionName(String.format("%s@%s Session%d",
          APP_NAME,
          ApplicationIdentity.getActualHostNameWithoutDomain(),
          ++sessionCount));

      client.connect();
    }
  }

  public static void main(String[] args) throws Exception {

    SampleConsoleClient sampleConsoleClient = new SampleConsoleClient();

    sampleConsoleClient.initialize();

    sampleConsoleClient.connect();

  }
}
