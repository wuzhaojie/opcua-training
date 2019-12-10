/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.server.MonitoredDataItem;
import com.prosysopc.ua.server.NodeManagerListener;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.Subscription;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.core.AggregateFilterResult;
import com.prosysopc.ua.stack.core.MonitoringFilter;
import com.prosysopc.ua.stack.core.MonitoringMode;
import com.prosysopc.ua.stack.core.MonitoringParameters;
import com.prosysopc.ua.stack.core.NodeAttributes;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.core.ViewDescription;
import com.prosysopc.ua.stack.utils.NumericRange;
import java.util.List;

/**
 * A sample implementation of a NodeManagerListener.
 */
public class MyNodeManagerListener implements NodeManagerListener {

  @Override
  public void onAfterAddNode(ServiceContext serviceContext, NodeId parentNodeId, UaNode parent, NodeId nodeId,
      UaNode node, NodeClass nodeClass, QualifiedName browseName, NodeAttributes attributes,
      UaReferenceType referenceType, ExpandedNodeId typeDefinitionId, UaNode typeDefinition) throws StatusException {

  }

  @Override
  public void onAfterAddReference(ServiceContext serviceContext, NodeId sourceNodeId, UaNode sourceNode,
      ExpandedNodeId targetNodeId, UaNode targetNode, NodeId referenceTypeId, UaReferenceType referenceType,
      boolean isForward) throws StatusException {

  }

  @Override
  public void onAfterCreateMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem item) {
    //
  }

  @Override
  public void onAfterDeleteMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem item) {
    //
  }

  @Override
  public void onAfterModifyMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem item) {
    //
  }

  @Override
  public void onAddNode(ServiceContext serviceContext, NodeId parentNodeId, UaNode parent, NodeId nodeId,
      NodeClass nodeClass, QualifiedName browseName, NodeAttributes attributes, UaReferenceType referenceType,
      ExpandedNodeId typeDefinitionId, UaNode typeDefinition) throws StatusException {
    // Notification of a node addition request.
    // Note that NodeManagerTable#setNodeManagementEnabled(true) must be
    // called to enable these methods.
    // Anyway, we just check the user access.
    checkUserAccess(serviceContext);
  }

  @Override
  public void onAddReference(ServiceContext serviceContext, NodeId sourceNodeId, UaNode sourceNode,
      ExpandedNodeId targetNodeId, UaNode targetNode, NodeId referenceTypeId, UaReferenceType referenceType,
      boolean isForward) throws StatusException {
    // Notification of a reference addition request.
    // Note that NodeManagerTable#setNodeManagementEnabled(true) must be
    // called to enable these methods.
    // Anyway, we just check the user access.
    checkUserAccess(serviceContext);
  }

  @Override
  public boolean onBrowseNode(ServiceContext serviceContext, ViewDescription view, NodeId nodeId, UaNode node,
      UaReference reference) {
    // Perform custom filtering, for example based on the user
    // doing the browse. The method is called separately for each reference.
    // Default is to return all references for everyone
    return true;
  }

  @Override
  public void onCreateMonitoredDataItem(ServiceContext serviceContext, Subscription subscription, NodeId nodeId,
      UaNode node, UnsignedInteger attributeId, NumericRange indexRange, MonitoringParameters params,
      MonitoringFilter filter, AggregateFilterResult filterResult, MonitoringMode monitoringMode)
      throws StatusException {
    // Notification of a monitored item creation request

    // You may, for example start to monitor the node from a physical
    // device, only once you get a request for it from a client
  }

  @Override
  public void onDeleteMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem monitoredItem) {
    // Notification of a monitored item delete request
  }

  @Override
  public void onDeleteNode(ServiceContext serviceContext, NodeId nodeId, UaNode node, boolean deleteTargetReferences)
      throws StatusException {
    // Notification of a node deletion request.
    // Note that NodeManagerTable#setNodeManagementEnabled(true) must be
    // called to enable these methods.
    // Anyway, we just check the user access.
    checkUserAccess(serviceContext);
  }

  @Override
  public void onDeleteReference(ServiceContext serviceContext, NodeId sourceNodeId, UaNode sourceNode,
      ExpandedNodeId targetNodeId, UaNode targetNode, NodeId referenceTypeId, UaReferenceType referenceType,
      boolean isForward, boolean deleteBidirectional) throws StatusException {
    // Notification of a reference deletion request.
    // Note that NodeManagerTable#setNodeManagementEnabled(true) must be
    // called to enable these methods.
    // Anyway, we just check the user access.
    checkUserAccess(serviceContext);
  }

  @Override
  public void onGetReferences(ServiceContext serviceContext, ViewDescription viewDescription, NodeId nodeId,
      UaNode node, List<UaReference> references) {
    // Add custom references that are not defined in the nodes here.
    // Useful for non-UaNode-based node managers - or references.
  }

  @Override
  public void onModifyMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem item, UaNode node, MonitoringParameters params, MonitoringFilter filter,
      AggregateFilterResult filterResult) {
    // Notification of a monitored item modification request
  }

  private void checkUserAccess(ServiceContext serviceContext) throws StatusException {
    // Do not allow for anonymous users
    if (serviceContext.getSession().getUserIdentity().getType().equals(UserTokenType.Anonymous)) {
      throw new StatusException(StatusCodes.Bad_UserAccessDenied);
    }
  }

}

