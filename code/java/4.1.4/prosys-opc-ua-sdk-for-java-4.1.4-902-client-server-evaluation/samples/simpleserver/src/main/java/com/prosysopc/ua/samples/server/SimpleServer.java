/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserTokenPolicies;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.EndpointDescription;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.utils.EndpointUtil;

/**
 * A very minimal server application.
 */
public class SimpleServer {

  /**
   * Entry point to the application.
   */
  public static void main(String[] args) throws Exception {
    UaServer server = new UaServer();

    /*
     * Enable or disable IPv6 networking (enabled by default). Java 6 does not support IPv6 and
     * would throw an exception, therefore the below code checks for java version first.
     */
    if (System.getProperty("java.version").startsWith("1.6")) {
      server.setEnableIPv6(false);
    }

    // Set port to listen for incoming connections
    // This sample only supports opc.tcp
    server.setPort(Protocol.OpcTcp, 52530);

    // Optional server name part of the URI
    server.setServerName("OPCUA/SimpleServer");

    // Bind to all network interfaces
    server.setBindAddresses(EndpointUtil.getInetAddresses(server.isEnableIPv6()));

    // This sample does not support security
    server.getSecurityModes().add(SecurityMode.NONE);

    // This sample does not define user authentication methods
    server.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);

    initializeApplicationIdentity(server);

    // Starts the server
    server.start();

    // Prints connection address that clients can use.
    // NOTE! if multiple SecurityModes are supported, each will have their own EndpointDescription
    System.out.println("Server started, connection address:");
    for (EndpointDescription ed : server.getEndpoints()) {
      System.out.println(ed.getEndpointUrl());
    }

    // Wait for shutdown
    System.out.println("Enter 'x' to shutdown");
    Scanner sc = new Scanner(System.in);
    sc.nextLine(); // blocks until input given
    System.out.println("Shutting down..");
    server.shutdown(2, new LocalizedText("Shutdown by user"));
    System.out.println("Server stopped.");
  }

  /**
   * Define a minimal ApplicationIdentity.
   */
  private static void initializeApplicationIdentity(UaServer server)
      throws SecureIdentityException, IOException, UnknownHostException {
    // Application Description is sent to clients
    ApplicationDescription appDescription = new ApplicationDescription();
    appDescription.setApplicationName(new LocalizedText("SimpleServer", Locale.ENGLISH));

    // The 'localhost' (all lower case) part in the URI is converted to the actual
    // host name of the computer in which the application is run
    appDescription.setApplicationUri("urn:localhost:UA:SimpleServer");

    appDescription.setProductUri("urn:prosysopc.com:UA:SimpleServer");
    appDescription.setApplicationType(ApplicationType.Server);


    // Due to SDK design, we must have a certificate in order to have any endpoints, therefore
    // creating a certificate please see SampleConsoleServer.initialize for more information.

    File privateKeyPath = new File("PKI/CA/private");
    String organization = "Sample Organization";
    String privateKeyPassword = "opcua";
    ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription, organization,
        privateKeyPassword, privateKeyPath, true);

    server.setApplicationIdentity(identity);
  }

}
