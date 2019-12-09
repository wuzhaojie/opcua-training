/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.stack.builtintypes.DataValue;

/**
 * A sampler listener for monitored data changes.
 */
public class MyMonitoredDataItemListener implements MonitoredDataItemListener {
  private final SampleConsoleClient client;

  public MyMonitoredDataItemListener(SampleConsoleClient client) {
    this.client = client;
  }

  @Override
  public void onDataChange(MonitoredDataItem sender, DataValue prevValue, DataValue value) {
    SampleConsoleClient.println(client.dataValueToString(sender.getNodeId(), sender.getAttributeId(), value));
  }

}
