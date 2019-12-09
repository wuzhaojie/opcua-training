/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import java.net.SocketAddress;

import com.prosysopc.ua.stack.transport.ReverseConnectionListener;

public class MyReverseConnectionListener implements ReverseConnectionListener {

  @Override
  public boolean onConnect(String serverApplicationUri, String endpointUrl, SocketAddress remoteAddress) {
    /*
     * You can perform here validation for reverse connections. Return false, if you wish to stop
     * the connection. Note that the connection is already initiated by the server at the
     * SocketAddress.
     * 
     * NOTE! The endpointUrl parameter is sent by the Server and used in the Client in calls forming
     * the higher level communication channel. It is not the actual address the client is connecting
     * (as the socket is already open to the remoteAddress) and most of the time is one of the
     * endpointUrls used in normal non-reverse connections for the Server.
     */
    System.out.println("Accepting reverse connection to server: " + serverApplicationUri + " at: " + remoteAddress
        + " , using endpointUrl: " + endpointUrl);
    return true;
  }

}
