package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.DataTypeConversionException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.ServerStatusListener;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.UaClientListener;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.Attributes;
import com.prosysopc.ua.stack.core.BrowseDirection;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.types.opcua.Ids;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleConsoleClient {

  private static final Logger logger = LoggerFactory.getLogger(SampleConsoleClient.class);

  protected static String APP_NAME = "SampleConsoleClient";
  protected String serverAddress = "opc.tcp://localhost:52520/OPCUA/SampleConsoleServer";
  protected int sessionCount = 0;
  protected double t = 0;
  protected List<NodeId> dataItems = new LinkedList<>();

  protected DefaultCertificateValidatorListener validationListener = new MyCertificateValidationListener();
  protected ServerStatusListener serverStatusListener = new MyServerStatusListener();
  protected UaClientListener clientListener = new MyUaClientListener();

  protected UaClient client;

  public SampleConsoleClient() {
  }

  protected static void printf(String format, Object... args) {
    System.out.printf(format, args);
  }

  protected void initialize() throws ServiceException, IOException, SecureIdentityException {

    client = new UaClient(serverAddress);

    //设置client监听器，用于处理建立连接之后的事件
    client.setListener(clientListener);

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
    client.addServerStatusListener(serverStatusListener);
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

  protected List<NodeId> browseAllDataItem() throws ServiceResultException, ServiceException, StatusException {
    NodeId objects = new NodeId(2, "ComplianceNodes");
    NodeId DataItems = null;
    NodeId organizes = client.getAddressSpace().getNamespaceTable().toNodeId(Ids.Organizes);
    NodeId hasComponet = client.getAddressSpace().getNamespaceTable().toNodeId(Ids.HasComponent);
    List<ReferenceDescription> referenceDescriptions = client.getAddressSpace().browse(objects, BrowseDirection.Forward, organizes);
    for (int i = 0; i < referenceDescriptions.size(); i++) {
      System.out.println(referenceDescriptions.get(i).getBrowseName().toString());
      if (referenceDescriptions.get(i).getBrowseName().toString().equals("2:DataItems")) {
        ExpandedNodeId expandedNodeId = referenceDescriptions.get(i).getNodeId();
        DataItems = client.getAddressSpace().getNamespaceTable().toNodeId(expandedNodeId);
        referenceDescriptions = client.getAddressSpace().browse(DataItems, BrowseDirection.Forward, hasComponet);
        for (int j = 0; j < referenceDescriptions.size(); j++) {
          if (referenceDescriptions.get(j).getBrowseName().toString().equals("2:DoubleDataItem")) {
            ExpandedNodeId dataItem = referenceDescriptions.get(j).getNodeId();
            dataItems.add(client.getAddressSpace().getNamespaceTable().toNodeId(dataItem));
          }
        }
      }
    }
    return dataItems;
  }

  void simulate() throws InterruptedException {
    while (true) {
      t = t + (Math.PI / 180);
      double value = 100 * Math.sin(t);
      for (int i = 0; i < dataItems.size(); i++) {
        try {
          boolean status = client.writeAttribute(dataItems.get(i), Attributes.Value, value, true);
          if (status) {
            System.out.println("write real success！");
          } else {
            System.out.println("write real failed！");
          }
        } catch (ServiceException e) {
          e.printStackTrace();
        } catch (StatusException e) {
          e.printStackTrace();
        } catch (DataTypeConversionException e) {
          e.printStackTrace();
        }
      }
      Thread.sleep(3000);
    }
  }

  public static void main(String[] args) throws Exception {

    SampleConsoleClient sampleConsoleClient = new SampleConsoleClient();

    sampleConsoleClient.initialize();

    sampleConsoleClient.connect();

    sampleConsoleClient.browseAllDataItem();

    sampleConsoleClient.simulate();
  }
}
