/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.client.MonitoredEventItem;
import com.prosysopc.ua.client.MonitoredEventItemListener;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.Variant;

/**
 * A sampler listener for monitored event notifications.
 */
public class MyMonitoredEventItemListener implements MonitoredEventItemListener {
  private final SampleConsoleClient client;
  private final QualifiedName[] requestedEventFields;

  public MyMonitoredEventItemListener(SampleConsoleClient client, QualifiedName[] requestedEventFields) {
    this.requestedEventFields = requestedEventFields;
    this.client = client;
  }

  @Override
  public void onEvent(MonitoredEventItem sender, Variant[] eventFields) {
    SampleConsoleClient.println(client.eventToString(sender.getNodeId(), requestedEventFields, eventFields));
  }

}
