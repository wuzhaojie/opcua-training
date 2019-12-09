/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaValueNode;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.io.IoManagerListener;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.AttributeWriteMask;
import com.prosysopc.ua.stack.core.TimestampsToReturn;
import com.prosysopc.ua.stack.utils.NumericRange;

/**
 * A sample implementation of an IoManagerListener.
 */
public class MyIoManagerListener implements IoManagerListener {
  private static Logger logger = LoggerFactory.getLogger(MyIoManagerListener.class);

  @Override
  public AccessLevelType onGetUserAccessLevel(ServiceContext serviceContext, NodeId nodeId, UaVariable node) {
    // The AccessLevel defines the accessibility of the Variable.Value
    // attribute

    // Define anonymous access
    // if (serviceContext.getSession().getUserIdentity().getType()
    // .equals(UserTokenType.Anonymous))
    // return AccessLevelType.of();
    if (node.getHistorizing()) {
      return AccessLevelType.of(AccessLevelType.CurrentRead, AccessLevelType.CurrentWrite, AccessLevelType.HistoryRead);
    } else {
      return AccessLevelType.of(AccessLevelType.CurrentRead, AccessLevelType.CurrentWrite);
    }
  }

  @Override
  public Boolean onGetUserExecutable(ServiceContext serviceContext, NodeId nodeId, UaMethod node) {
    // Enable execution of all methods that are allowed by default
    return true;
  }

  @Override
  public AttributeWriteMask onGetUserWriteMask(ServiceContext serviceContext, NodeId nodeId, UaNode node) {
    // Enable writing to everything that is allowed by default
    // The WriteMask defines the writable attributes, except for Value,
    // which is controlled by UserAccessLevel (above)

    // The following would deny write access for anonymous users:
    // if
    // (serviceContext.getSession().getUserIdentity().getType().equals(
    // UserTokenType.Anonymous))
    // return AttributeWriteMask.of();

    return AttributeWriteMask.of(AttributeWriteMask.Fields.values());
  }

  @Override
  public boolean onReadNonValue(ServiceContext serviceContext, NodeId nodeId, UaNode node, UnsignedInteger attributeId,
      DataValue dataValue) throws StatusException {
    return false;
  }

  @Override
  public boolean onReadValue(ServiceContext serviceContext, NodeId nodeId, UaValueNode node, NumericRange indexRange,
      TimestampsToReturn timestampsToReturn, DateTime minTimestamp, DataValue dataValue) throws StatusException {
    if (logger.isDebugEnabled()) {
      logger.debug("onReadValue: nodeId=" + nodeId + (node != null ? " node=" + node.getBrowseName() : ""));
    }
    return false;
  }

  @Override
  public boolean onWriteNonValue(ServiceContext serviceContext, NodeId nodeId, UaNode node, UnsignedInteger attributeId,
      DataValue dataValue) throws StatusException {
    return false;
  }

  @Override
  public boolean onWriteValue(ServiceContext serviceContext, NodeId nodeId, UaValueNode node, NumericRange indexRange,
      DataValue dataValue) throws StatusException {
    logger.info("onWriteValue: nodeId=" + nodeId + (node != null ? " node=" + node.getBrowseName() : "")
        + (indexRange != null ? " indexRange=" + indexRange : "") + " value=" + dataValue);
    return false;
  }
}
