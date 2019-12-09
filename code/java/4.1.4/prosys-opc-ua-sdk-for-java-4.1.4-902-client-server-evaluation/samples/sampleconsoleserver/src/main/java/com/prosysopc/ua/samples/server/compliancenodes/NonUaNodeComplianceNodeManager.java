/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.nodes.UaValueNode;
import com.prosysopc.ua.server.IoManager;
import com.prosysopc.ua.server.MonitoredDataItem;
import com.prosysopc.ua.server.MonitoredItem;
import com.prosysopc.ua.server.NodeManager;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.Subscription;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Attributes;
import com.prosysopc.ua.stack.core.EventNotifierType;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.core.TimestampsToReturn;
import com.prosysopc.ua.stack.utils.NumericRange;

public class NonUaNodeComplianceNodeManager extends NodeManager {

  /**
   * An IO Manager which provides the values for the attributes of the nodes.
   */
  public class NonUaNodeIoManager extends IoManager {

    /**
     * Constructor for the IoManager.
     *
     * @param nodeManager the node manager that uses this IO Manager.
     */
    public NonUaNodeIoManager(NodeManager nodeManager) {
      super(nodeManager);
    }

    @Override
    protected void readNonValue(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
        UnsignedInteger attributeId, DataValue dataValue) throws StatusException {
      Object value = null;
      final ExpandedNodeId expandedNodeId = getNamespaceTable().toExpandedNodeId(nodeId);
      if (attributeId.equals(Attributes.NodeId)) {
        value = nodeId;
      } else if (attributeId.equals(Attributes.BrowseName)) {
        value = getBrowseName(expandedNodeId, node);
      } else if (attributeId.equals(Attributes.DisplayName)) {
        value = getDisplayName(expandedNodeId, node, null);
      } else if (attributeId.equals(Attributes.Description)) {
        value = null;
      } else if (attributeId.equals(Attributes.NodeClass)) {
        value = getNodeClass(expandedNodeId, node);
      } else if (attributeId.equals(Attributes.WriteMask)) {
        value = UnsignedInteger.ZERO;
      } else if (attributeId.equals(Attributes.DataType)) {
        if (DATA_ITEM_TYPE_NAME.equals(nodeId.getValue())) {
          value = Identifiers.BaseDataType;
        } else {
          value = getDataItem(nodeId).getDataType();
        }
      } else if (attributeId.equals(Attributes.ValueRank)) {
        value = ValueRanks.Scalar;
      } else if (attributeId.equals(Attributes.ArrayDimensions)) {
        value = null;
      } else if (attributeId.equals(Attributes.AccessLevel)) {
        value = AccessLevelType.of(AccessLevelType.CurrentRead, AccessLevelType.CurrentWrite).getAsBuiltInType();
      } else if (attributeId.equals(Attributes.Historizing)) {
        value = false;
      } else if (attributeId.equals(Attributes.EventNotifier) && DATA_ITEM_FOLDER_NAME.equals(nodeId.getValue())) {
        value = EventNotifierType.of();
      } else {
        // not valid attribute, must throw error
        throw new StatusException(StatusCodes.Bad_AttributeIdInvalid);
      }
      dataValue.setValue(new Variant(value));
      dataValue.setServerTimestamp(DateTime.currentTime());
    }

    @Override
    protected void readValue(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaValueNode node,
        NumericRange indexRange, TimestampsToReturn timestampsToReturn, DateTime minTimestamp, DataValue dataValue)
        throws StatusException {
      NonUaNodeDataItem dataItem = getDataItem(nodeId);
      if (dataItem == null) {
        throw new StatusException(StatusCodes.Bad_NodeIdInvalid);
      }
      dataItem.getDataValue(dataValue);
      IoManager.applyIndexRangeToReadValue(dataValue, indexRange);
    }

    @Override
    protected boolean writeValue(ServiceContext serviceContext, Object operationContext, NodeId nodeId,
        UaValueNode node, NumericRange indexRange, DataValue dataValue) throws StatusException {
      NonUaNodeDataItem dataItem = getDataItem(nodeId);
      if (dataItem == null) {
        throw new StatusException(StatusCodes.Bad_NodeIdInvalid);
      }
      DataValue cur = new DataValue();
      dataItem.getDataValue(cur);
      DataValue newValue = IoManager.applyIndexRangeToWriteValue(cur, dataValue, indexRange);
      dataItem.setValue(newValue.getValue().getValue(), dataValue.getStatusCode());
      return true;
    }
  }

  public class NonUaNodeMyReference extends UaReference {

    private final NodeId referenceTypeId;
    private final ExpandedNodeId sourceId;
    private final ExpandedNodeId targetId;

    public NonUaNodeMyReference(ExpandedNodeId sourceId, ExpandedNodeId targetId, NodeId referenceType) {
      this.sourceId = sourceId;
      this.targetId = targetId;
      this.referenceTypeId = referenceType;
    }

    public NonUaNodeMyReference(NodeId sourceId, NodeId targetId, NodeId referenceType) {
      this(getNamespaceTable().toExpandedNodeId(sourceId), getNamespaceTable().toExpandedNodeId(targetId),
          referenceType);
    }

    @Override
    public void delete() {
      throw new RuntimeException("StatusCodes.Bad_NotImplemented");
    }

    @Override
    public boolean getIsInverse(NodeId nodeId) {
      try {
        if (nodeId.equals(getNamespaceTable().toNodeId(sourceId))) {
          return false;
        }
        if (nodeId.equals(getNamespaceTable().toNodeId(targetId))) {
          return true;
        }
      } catch (ServiceResultException e) {
        throw new RuntimeException(e);
      }
      throw new RuntimeException("not a source nor target");
    }

    @Override
    public boolean getIsInverse(UaNode node) {
      return getIsInverse(node.getNodeId());
    }

    @Override
    public UaReferenceType getReferenceType() {
      try {
        return (UaReferenceType) getNodeManagerTable().getNode(getReferenceTypeId());
      } catch (StatusException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public NodeId getReferenceTypeId() {
      return referenceTypeId;
    }

    @Override
    public ExpandedNodeId getSourceId() {
      return sourceId;
    }

    @Override
    public UaNode getSourceNode() {
      return null;
    }

    @Override
    public ExpandedNodeId getTargetId() {
      return targetId;
    }

    @Override
    public UaNode getTargetNode() {
      return null;
    }

  }

  private static final String DATA_ITEM_FOLDER_NAME = "NonUaNodeComplianceTest";

  private static final String DATA_ITEM_TYPE_NAME = "NonUaNodeDataItemType";
  private static final Logger logger = LoggerFactory.getLogger(NonUaNodeComplianceNodeManager.class);

  private final ExpandedNodeId dataItemFolder;

  private final ExpandedNodeId dataItemType;
  private final NonUaNodeIoManager ioManager;

  private final Map<String, NonUaNodeDataItem> map = new ConcurrentHashMap<String, NonUaNodeDataItem>();

  private final Map<String, Collection<MonitoredDataItem>> monitoredItems =
      new ConcurrentHashMap<String, Collection<MonitoredDataItem>>();

  public NonUaNodeComplianceNodeManager(UaServer server, String namespaceUri) {
    super(server, namespaceUri);

    dataItemType = new ExpandedNodeId(null, getNamespaceIndex(), DATA_ITEM_TYPE_NAME);
    dataItemFolder = new ExpandedNodeId(null, getNamespaceIndex(), DATA_ITEM_FOLDER_NAME);

    try {
      getNodeManagerTable().getNodeManagerRoot().getObjectsFolder()
          .addReference(getNamespaceTable().toNodeId(dataItemFolder), Identifiers.Organizes, false);
    } catch (ServiceResultException e) {
      throw new RuntimeException(e);
    }

    for (StaticData t : StaticData.STATIC_DATAS) {
      NonUaNodeDataItem item = new NonUaNodeDataItem(this, t.getBaseName());
      item.setDataType(t.getDataTypeId());
      item.setValue(t.getInitialValue());
      map.put(item.getName(), item);
    }

    ioManager = new NonUaNodeIoManager(this);
  }

  public NonUaNodeIoManager getNonUaNodeIoManager() {
    return ioManager;
  }

  @Override
  public NodeId getVariableDataType(NodeId nodeId, UaValueNode variable) throws StatusException {
    if (nodeId == null) {
      nodeId = variable.getNodeId();
    }
    return getDataItem(nodeId).getDataType();
  }

  @Override
  public boolean hasNode(NodeId nodeId) {
    String n = (String) nodeId.getValue();
    if (map.containsKey(n)) {
      return true;
    }
    return n.equals(DATA_ITEM_FOLDER_NAME) || n.equals(DATA_ITEM_TYPE_NAME);
  }

  /**
   * Send a data change notification for all monitored data items that are monitoring the dataItme
   *
   * @param dataItem
   */
  public void notifyMonitoredDataItems(NonUaNodeDataItem dataItem) {
    // Get the list of items watching dataItem
    Collection<MonitoredDataItem> c = monitoredItems.get(dataItem.getName());
    if (c != null) {
      for (MonitoredDataItem item : c) {
        DataValue dataValue = new DataValue();
        dataItem.getDataValue(dataValue);
        item.notifyDataChange(dataValue);
      }
    }
  }

  private NonUaNodeDataItem getDataItem(ExpandedNodeId nodeId) {
    String name = (String) nodeId.getValue();
    return map.get(name);
  }

  private NonUaNodeDataItem getDataItem(NodeId nodeId) {
    return map.get(nodeId.getValue());
  }

  private String getNodeName(ExpandedNodeId nodeId) {
    String name = nodeId.getValue().toString();
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemType)) {
      name = DATA_ITEM_TYPE_NAME;
    }
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemFolder)) {
      name = DATA_ITEM_FOLDER_NAME;
    } else {
      NonUaNodeDataItem dataItem = getDataItem(nodeId);
      // Use the namespaceIndex of the NodeManager name space also for the
      // browse names
      if (dataItem != null) {
        name = dataItem.getName();
      }
    }
    return name;
  }

  @Override
  protected void afterCreateMonitoredDataItem(ServiceContext serviceContext, Subscription subscription,
      MonitoredDataItem item) {
    // Add all items that monitor the same node to the same collection
    final Object dataItemName = item.getNodeId().getValue();
    Collection<MonitoredDataItem> c = monitoredItems.get(dataItemName);
    if (c == null) {
      c = new CopyOnWriteArrayList<MonitoredDataItem>();
      monitoredItems.put((String) dataItemName, c);
    }
    c.add(item);
    logger.debug("afterCreateMonitoredDataItem: nodeId=" + item.getNodeId() + " c.size()=" + c.size());
  }

  @Override
  protected void deleteMonitoredItem(ServiceContext serviceContext, Subscription subscription, MonitoredItem item)
      throws StatusException {
    // Find the collection in which the monitoredItem is
    // and remove the item from the collection
    Object dataItemName = item.getNodeId().getValue();
    Collection<MonitoredDataItem> c = monitoredItems.get(dataItemName);
    if (c != null) {
      logger.debug("deleteMonitoredItem: collection size=" + c.size());
      c.remove(item);
      if (c.isEmpty()) {
        monitoredItems.remove(dataItemName);
        logger.debug("deleteMonitoredItem: monitoredItems size=" + monitoredItems.size());
      }
    }
  }

  @Override
  protected QualifiedName getBrowseName(ExpandedNodeId nodeId, UaNode node) {
    return new QualifiedName(getNamespaceIndex(), getNodeName(nodeId));
  }

  @Override
  protected LocalizedText getDisplayName(ExpandedNodeId nodeId, UaNode targetNode, Locale locale) {
    return new LocalizedText(getNodeName(nodeId), LocalizedText.NO_LOCALE);
  }

  @Override
  protected NodeClass getNodeClass(ExpandedNodeId nodeId, UaNode node) {
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemType)) {
      return NodeClass.VariableType;
    }
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemFolder)) {
      return NodeClass.Object;
    }
    return NodeClass.Variable; // All data items are variables
  }

  @Override
  protected NodeClass getNodeClass(NodeId nodeId, UaNode node) {
    return getNodeClass(getNamespaceTable().toExpandedNodeId(nodeId), node);
  }

  @Override
  protected UaReference[] getReferences(NodeId nodeId, UaNode node) {
    try {
      // Define reference to our type
      if (nodeId.equals(getNamespaceTable().toNodeId(dataItemType))) {
        return new UaReference[] {new NonUaNodeMyReference(new ExpandedNodeId(Identifiers.BaseDataVariableType),
            dataItemType, Identifiers.HasSubtype)};
      }
      // Define reference from and to our Folder for the DataItems
      if (nodeId.equals(getNamespaceTable().toNodeId(dataItemFolder))) {
        UaReference[] folderItems = new UaReference[map.size() + 2];
        // Inverse reference to the ObjectsFolder
        folderItems[0] = new NonUaNodeMyReference(new ExpandedNodeId(Identifiers.ObjectsFolder), dataItemFolder,
            Identifiers.Organizes);
        // Type definition reference
        folderItems[1] = new NonUaNodeMyReference(dataItemFolder,
            getTypeDefinition(getNamespaceTable().toExpandedNodeId(nodeId), node), Identifiers.HasTypeDefinition);
        int i = 2;
        // Reference to all items in the folder
        for (NonUaNodeDataItem d : map.values()) {
          folderItems[i] = new NonUaNodeMyReference(dataItemFolder,
              new ExpandedNodeId(null, getNamespaceIndex(), d.getName()), Identifiers.HasComponent);
          i++;
        }
        return folderItems;
      }
    } catch (ServiceResultException e) {
      throw new RuntimeException(e);
    }

    // Define references from our DataItems
    NonUaNodeDataItem dataItem = getDataItem(nodeId);
    if (dataItem == null) {
      return null;
    }
    final ExpandedNodeId dataItemId = new ExpandedNodeId(null, getNamespaceIndex(), dataItem.getName());

    // Inverse reference to the folder + Type definition
    return new UaReference[] {new NonUaNodeMyReference(dataItemFolder, dataItemId, Identifiers.HasComponent),
        new NonUaNodeMyReference(dataItemId, dataItemType, Identifiers.HasTypeDefinition)};
  }

  @Override
  protected ExpandedNodeId getTypeDefinition(ExpandedNodeId nodeId, UaNode node) {
    NonUaNodeDataItem v = map.get(nodeId.getValue());
    if (v != null) {
      return getNamespaceTable().toExpandedNodeId(v.getDataType());
    }
    // ExpandedNodeId.equals cannot be trusted, since some IDs are defined
    // with NamespaceIndex while others use NamespaceUri
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemType)) {
      return null;
    }
    if (getNamespaceTable().nodeIdEquals(nodeId, dataItemFolder)) {
      return getNamespaceTable().toExpandedNodeId(Identifiers.FolderType);
    }
    return dataItemType;
  }
}
