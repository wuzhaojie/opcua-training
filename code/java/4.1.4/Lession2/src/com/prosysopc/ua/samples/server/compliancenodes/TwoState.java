/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.EnumSet;

import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.core.Identifiers;

public enum TwoState implements CommonComplianceInfo {
  TWO_1("TwoStateDiscreteItem", "TrueState", "FalseState", Boolean.TRUE);

  public static final EnumSet<TwoState> TWOSTATE_ITEMS = EnumSet.of(TWO_1);
  private final String name;
  private final String trueState;
  private final String falseState;

  private final Boolean initialValue;

  private TwoState(String name, String trueState, String falseState, Boolean initialValue) {
    this.name = name;
    this.trueState = trueState;
    this.falseState = falseState;
    this.initialValue = initialValue;
  }

  @Override
  public String getBaseName() {
    return name;
  }

  @Override
  public NodeId getDataTypeId() {
    return Identifiers.Boolean;
  }

  public String getFalseState() {
    return falseState;
  }

  @Override
  public Boolean getInitialValue() {
    return initialValue;
  }

  public String getName() {
    return name;
  }

  public String getTrueState() {
    return trueState;
  }

}
