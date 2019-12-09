/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import com.prosysopc.ua.stack.builtintypes.NodeId;

public interface CommonComplianceInfo {

  /**
   * Returns the "BaseName of the node", e.g. Byte, Int16
   */
  public String getBaseName();

  public NodeId getDataTypeId();

  public Object getInitialValue();
}
