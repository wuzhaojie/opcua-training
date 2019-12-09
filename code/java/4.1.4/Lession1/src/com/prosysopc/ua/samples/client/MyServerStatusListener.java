/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.client.ServerStatusListener;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.core.ServerStatusDataType;

/**
 * A sampler listener for server status changes.
 */
public class MyServerStatusListener implements ServerStatusListener {
  @Override
  public void onShutdown(UaClient uaClient, long secondsTillShutdown, LocalizedText shutdownReason) {
    // Called when the server state changes to Shutdown
    SampleConsoleClient.printf("Server shutdown in %d seconds. Reason: %s\n", secondsTillShutdown,
        shutdownReason.getText());
  }

  @Override
  public void onStateChange(UaClient uaClient, ServerState oldState, ServerState newState) {
    // Called whenever the server state changes
    SampleConsoleClient.printf("ServerState changed from %s to %s\n", oldState, newState);
    if (newState.equals(ServerState.Unknown)) {
      SampleConsoleClient.println("ServerStatusError: " + uaClient.getServerStatusError());
    }
  }

  @Override
  public void onStatusChange(UaClient uaClient, ServerStatusDataType status, StatusCode code) {
    // Called whenever the server status changes, typically every
    // StatusCheckInterval defined in the UaClient.
    // SampleConsoleClient.println("ServerStatus: " + status + ", code: " + code);
  }
}
