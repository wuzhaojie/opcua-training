/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.EnumSet;
import java.util.Locale;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.NodeBuilderException;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaInstantiationException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.instantiation.NodeBuilderConfiguration;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.NamespaceTable;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.AttributeWriteMask;
import com.prosysopc.ua.stack.core.EUInformation;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.utils.AttributesUtil;
import com.prosysopc.ua.types.opcua.AnalogItemType;
import com.prosysopc.ua.types.opcua.DataItemType;
import com.prosysopc.ua.types.opcua.FolderType;
import com.prosysopc.ua.types.opcua.Ids;
import com.prosysopc.ua.types.opcua.MultiStateDiscreteType;
import com.prosysopc.ua.types.opcua.TwoStateDiscreteType;

public class ComplianceNodeManager extends NodeManagerUaNode {

  private FolderType accessLevelVariableFolder;
  private FolderType analogItemArrayFolder;
  private FolderType analogItemFolder;
  private FolderType dataItemFolder;
  private FolderType deepFolder;
  private FolderType multiStateFolder;
  private UaObject objectsFolder;
  private FolderType staticArrayVariableFolder;
  private FolderType staticDataFolder;
  private FolderType staticVariableFolder;
  private FolderType twoStateFolder;

  public ComplianceNodeManager(UaServer server, String namespaceUri) {
    super(server, namespaceUri);

    try {
      initialize();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize Compliance nodes", e);
    }
  }

  private void addDeepObject(UaNode parent, int depth, int maxDepth) {
    if (depth <= maxDepth) {
      final String name = String.format("DeepObject%02d", depth);
      UaObjectNode newObject = new UaObjectNode(this, new NodeId(getNamespaceIndex(), name), name, getDefaultLocale());
      try {
        addNodeAndReference(parent, newObject, Identifiers.Organizes);
      } catch (StatusException e) {
      }
      addDeepObject(newObject, depth + 1, maxDepth);
    }
  }

  private void createAccessLevelVariable(String name, AccessLevelType accessLevel, AccessLevelType userAccessLevel)
      throws StatusException {
    final NodeId nodeId = new NodeId(getNamespaceIndex(), name);

    // just some datatype
    UaDataType type = getServer().getNodeManagerRoot().getDataType(Identifiers.Int32);

    CacheVariable node = new CacheVariable(this, nodeId, name, Locale.ENGLISH);
    node.setDataType(type);
    node.setValueRank(ValueRanks.OneDimension);
    node.setValue(new DataValue(new Variant(0), StatusCode.GOOD, new DateTime(), new DateTime()));
    node.setAccessLevel(accessLevel);
    node.setUserAccessLevel(userAccessLevel);
    accessLevelVariableFolder.addReference(node, Identifiers.HasComponent, false);
  }

  private void createAnalogItem(AnalogData a) throws StatusException, UaInstantiationException, NodeBuilderException {
    NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
    conf.addOptional(Ids.BaseAnalogType_EngineeringUnits);
    conf.addOptional(Ids.DataItemType_Definition);
    if (a.getInstrumentRange() != null) {
      conf.addOptional(Ids.BaseAnalogType_InstrumentRange);
    }

    AnalogItemType item =
        createNodeBuilder(AnalogItemType.class, conf).setName(a.getDataTypeName() + "AnalogItem").build();
    item.setDefinition("Test definition of type " + a.getDataTypeName());
    item.setEngineeringUnits(new EUInformation("http://www.opcfoundation.org/UA/units/un/cefact", 5067858,
        new LocalizedText("m", Locale.ENGLISH), new LocalizedText("metre", Locale.ENGLISH)));
    item.setEURange(a.getEURange());
    if (a.getInstrumentRange() != null) {
      item.setInstrumentRange(a.getInstrumentRange());
    }
    item.setValue(new DataValue(new Variant(a.getInitialValue()), StatusCode.GOOD, new DateTime(), new DateTime()));
    initNode(item, a, analogItemFolder);
  }

  private void createAnalogItemArray(AnalogData a)
      throws StatusException, UaInstantiationException, NodeBuilderException {
    NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
    conf.addOptional(Ids.BaseAnalogType_EngineeringUnits);
    conf.addOptional(Ids.DataItemType_Definition);

    AnalogItemType item =
        createNodeBuilder(AnalogItemType.class, conf).setName(a.getDataTypeName() + "AnalogItemArray").build();
    item.setDefinition("Test definition of type " + a.getDataTypeName() + " as array");
    item.setValueRank(ValueRanks.OneDimension);
    item.setArrayDimensions(new UnsignedInteger[] {UnsignedInteger.ZERO});
    item.setEngineeringUnits(new EUInformation("http://www.opcfoundation.org/UA/units/un/cefact", 5067858,
        new LocalizedText("m", Locale.ENGLISH), new LocalizedText("metre", Locale.ENGLISH)));
    item.setEURange(a.getEURange());
    item.setValue(new DataValue(new Variant(a.getInitialValue()), StatusCode.GOOD, new DateTime(), new DateTime()));
    initNode(item, a, analogItemArrayFolder);
  }

  private void createDataItem(StaticData s) throws StatusException, UaInstantiationException, NodeBuilderException {
    NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
    conf.addOptional(n2e(Identifiers.DataItemType_Definition));

    DataItemType item = createNodeBuilder(DataItemType.class, conf).setName(s.getDataTypeName() + "DataItem").build();
    item.setDefinition("Test definition of type " + s.getDataTypeName());
    item.setValue(createValue(s.getInitialValue()));
    initNode(item, s, dataItemFolder);
  }

  private FolderType createFolder(String name, UaObject parent) throws UaInstantiationException, NodeBuilderException {
    FolderType node = createNodeBuilder(FolderType.class).setName(name).build();
    parent.addReference(node, Identifiers.Organizes, false);
    return node;
  }

  private void createMultiStateDiscreteItem(MultiState m)
      throws StatusException, UaInstantiationException, NodeBuilderException {
    MultiStateDiscreteType item = createNodeBuilder(MultiStateDiscreteType.class).setName(m.getName()).build();
    String[] statesString = m.getStates();
    LocalizedText[] states = new LocalizedText[statesString.length];
    for (int i = 0; i < states.length; i++) {
      states[i] = new LocalizedText(statesString[i], getDefaultLocale());
    }
    item.setValue(createValue(m.getInitialValue()));
    item.setEnumStrings(states);
    initNode(item, m, multiStateFolder);
  }

  private NodeId createNodeId(String name) {
    return new NodeId(getNamespaceIndex(), name);

  }

  private String createNodeName(CommonComplianceInfo i, String postfix) {
    return i.getBaseName() + postfix;
  }

  private void createStaticArrayVariable(CommonComplianceInfo s) throws StatusException {
    String nodeName = createNodeName(s, "Array");
    UaVariableNode node = new CacheVariable(this, createNodeId(nodeName), nodeName, Locale.ENGLISH);
    node.setValueRank(ValueRanks.OneDimension);
    node.setArrayDimensions(new UnsignedInteger[] {UnsignedInteger.ZERO});
    node.setValue(new DataValue(new Variant(s.getInitialValue()), StatusCode.GOOD, new DateTime(), new DateTime()));

    initNode(node, s, staticArrayVariableFolder);
  }

  private UaVariableNode createStaticVariable(CommonComplianceInfo s) throws StatusException {
    String nodeName = createNodeName(s, "");
    UaVariableNode node = new CacheVariable(this, createNodeId(nodeName), nodeName, Locale.ENGLISH);
    node.setValue(new DataValue(new Variant(s.getInitialValue()), StatusCode.GOOD, new DateTime(), new DateTime()));
    initNode(node, s, staticVariableFolder);
    return node;
  }

  private void createTwoStateDiscreteItem(TwoState t)
      throws StatusException, UaInstantiationException, NodeBuilderException {
    TwoStateDiscreteType item = createNodeBuilder(TwoStateDiscreteType.class).setName(t.getName()).build();
    item.setTrueState(new LocalizedText(t.getTrueState(), getDefaultLocale()));
    item.setFalseState(new LocalizedText(t.getFalseState(), getDefaultLocale()));
    item.setValue(createValue(t.getInitialValue()));
    initNode(item, t, twoStateFolder);
  }

  private DataValue createValue(Object value) {
    return new DataValue(new Variant(value), StatusCode.GOOD, DateTime.currentTime(), UnsignedShort.ZERO,
        DateTime.currentTime(), UnsignedShort.ZERO);
  }

  private void initFolders() throws StatusException, UaInstantiationException, NodeBuilderException {
    objectsFolder = getServer().getNodeManagerRoot().getObjectsFolder();

    staticDataFolder = createFolder("ComplianceNodes", objectsFolder);
    staticVariableFolder = createFolder("StaticVariables", staticDataFolder);
    staticArrayVariableFolder = createFolder("StaticArrayVariables", staticDataFolder);
    dataItemFolder = createFolder("DataItems", staticDataFolder);
    analogItemFolder = createFolder("AnalogItems", staticDataFolder);
    analogItemArrayFolder = createFolder("AnalogItemArrays", staticDataFolder);
    accessLevelVariableFolder = createFolder("AccessLevels", staticDataFolder);
    twoStateFolder = createFolder("TwoStateItems", staticDataFolder);
    multiStateFolder = createFolder("MultiStateItems", staticDataFolder);
  }

  private void initialize() throws StatusException, UaInstantiationException, NodeBuilderException {
    // make folders
    initFolders();

    // Static test variables
    for (StaticData s : StaticData.STATIC_DATAS) {
      createStaticVariable(s);
    }

    // Static test array variables
    for (StaticData s : StaticData.STATIC_DATA_ARRAYS) {
      createStaticArrayVariable(s);
    }

    // DataItem test variables
    for (StaticData d : StaticData.DATA_ITEMS) {
      createDataItem(d);
    }

    // AnalogItem test variables
    for (AnalogData a : AnalogData.ANALOG_ITEMS) {
      createAnalogItem(a);
    }

    // AnalogItemArray test variables
    for (AnalogData a : AnalogData.ANALOG_ITEM_ARRAYS) {
      createAnalogItemArray(a);
    }

    // Folder for deep object chain
    deepFolder = createNodeBuilder(FolderType.class).setName("DeepFolder").build();
    staticDataFolder.addReference(deepFolder, Identifiers.Organizes, false);
    addDeepObject(deepFolder, 1, 20);

    AccessLevelType none = AccessLevelType.of();
    AccessLevelType readOnly = AccessLevelType.CurrentRead;
    AccessLevelType writeOnly = AccessLevelType.CurrentWrite;
    AccessLevelType readWrite =
        AccessLevelType.of(AccessLevelType.Fields.CurrentRead, AccessLevelType.Fields.CurrentWrite);

    // AccessLevel nodes
    createAccessLevelVariable("AccessLevelCurrentReadWrite", readWrite, readWrite);
    createAccessLevelVariable("AccessLevelCurrentRead", readOnly, readOnly);
    createAccessLevelVariable("AccessLevelCurrentWrite", writeOnly, writeOnly);
    createAccessLevelVariable("AccessLevelCurrentReadNotUser", readOnly, none);
    createAccessLevelVariable("AccessLevelCurrentWriteNotUser", writeOnly, none);

    // TwoState nodes
    for (TwoState t : TwoState.TWOSTATE_ITEMS) {
      createTwoStateDiscreteItem(t);
    }

    // MultiState nodes
    for (MultiState m : MultiState.MULTISTATE_ITEMS) {
      createMultiStateDiscreteItem(m);
    }

  }

  // set all common settings for the node
  private void initNode(UaVariable node, CommonComplianceInfo info, FolderType parent) throws StatusException {
    EnumSet<AttributeWriteMask.Fields> set =
        AttributesUtil.getSupportedWriteAccess(AttributeUtil.getSupportedAttributes(node.getNodeClass()));
    // Compiance test tool requires writing attributes

    // removing these as the may break something with ctt
    set.remove(AttributeWriteMask.Fields.NodeId);
    set.remove(AttributeWriteMask.Fields.NodeClass);
    set.remove(AttributeWriteMask.Fields.ArrayDimensions);
    set.remove(AttributeWriteMask.Fields.AccessLevel);
    set.remove(AttributeWriteMask.Fields.UserAccessLevel);
    set.remove(AttributeWriteMask.Fields.BrowseName);
    set.remove(AttributeWriteMask.Fields.DataType);
    set.remove(AttributeWriteMask.Fields.WriteMask);
    set.remove(AttributeWriteMask.Fields.UserWriteMask);

    AttributeWriteMask writeMask = AttributeWriteMask.of(set);
    node.setWriteMask(writeMask);
    node.setUserWriteMask(writeMask);

    if (!((info instanceof TwoState) || (info instanceof MultiState))) {
      node.setDataTypeId(info.getDataTypeId());
    }

    // add node to address space
    parent.addReference(node, Identifiers.HasComponent, false);

  }

  private ExpandedNodeId n2e(NodeId id) {
    return new ExpandedNodeId(NamespaceTable.OPCUA_NAMESPACE, id.getValue());
  }

}
