/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaBrowsePath;
import com.prosysopc.ua.UaNodeId;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaNodeFactoryException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaObjectType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.ModellingRule;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaInstantiationException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.instantiation.TypeDefinitionBasedNodeBuilderConfiguration;
import com.prosysopc.ua.server.nodes.CacheVariable;
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.server.nodes.PlainVariable;
import com.prosysopc.ua.server.nodes.UaDataTypeNode;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaObjectTypeNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.typedictionary.EnumerationSpecification;
import com.prosysopc.ua.types.opcua.Ids;
import com.prosysopc.ua.types.opcua.server.BaseEventTypeNode;
import com.prosysopc.ua.types.opcua.server.ExclusiveLevelAlarmTypeNode;
import com.prosysopc.ua.types.opcua.server.ExclusiveLimitState;
import com.prosysopc.ua.types.opcua.server.FolderTypeNode;

/**
 * A sample customized node manager, which actually just overrides the standard NodeManagerUaNode
 * and initializes the nodes for the demo.
 */
public class MyNodeManager extends NodeManagerUaNode {
  public static final String NAMESPACE = "http://www.prosysopc.com/OPCUA/SampleAddressSpace";
  private static final Logger logger = LoggerFactory.getLogger(MyNodeManager.class);
  private static boolean stackTraceOnException;

  private static void printException(Exception e) {
    if (stackTraceOnException) {
      e.printStackTrace();
    } else {
      println(e.toString());
      if (e.getCause() != null) {
        println("Caused by: " + e.getCause());
      }
    }
  }

  protected static void println(String string) {
    System.out.println(string);
  }

  private ExclusiveLevelAlarmTypeNode myAlarm;

  private UaObjectNode myDevice;

  // private MyEventType myEvent;

  private UaVariableNode myLevel;

  private PlainMethod myMethod;

  private CallableListener myMethodManagerListener;

  private FolderTypeNode myObjectsFolder;

  private PlainVariable<Boolean> mySwitch;

  double dx = 1;

  final MyEventManagerListener myEventManagerListener = new MyEventManagerListener();

  /**
   * Creates a new instance of MyNodeManager
   *
   * @param server the server in which the node manager is created.
   * @param namespaceUri the namespace URI for the nodes
   * @throws StatusException if something goes wrong in the initialization
   * @throws UaInstantiationException if something goes wrong regarding object instantiation
   */
  public MyNodeManager(UaServer server, String namespaceUri) throws StatusException, UaInstantiationException {
    super(server, namespaceUri);
  }

  /**
   * Defines the objects for which to store event history.
   * 
   * @return the objects to historize
   */
  public UaObjectNode[] getHistorizableEvents() {
    return new UaObjectNode[] {myObjectsFolder, myDevice};
  }

  /**
   * Defines the variables for which to store history.
   * 
   * @return the variables to historize
   */
  public UaVariableNode[] getHistorizableVariables() {
    return new UaVariableNode[] {myLevel, mySwitch};
  }

  /**
   * An example of triggering a custom event.
   */
  public void sendEvent() {
    // If the type has TypeDefinitionId, you can use the class with createEvent()
    MyEventType ev = createEvent(MyEventType.class);
    ev.setMessage(new LocalizedText("MyEvent"));
    ev.setMyVariable(new Random().nextInt());
    ev.setMyProperty("Property Value " + ev.getMyVariable());
    ev.triggerEvent(null);
  }

  /**
   * Runs one round of simulation.
   */
  public void simulate() {
    final DataValue v = myLevel.getValue();
    Double nextValue = v.isNull() ? 0 : v.getValue().doubleValue() + dx;
    if (nextValue <= 0) {
      dx = 1;
    } else if (nextValue >= 100) {
      dx = -1;
    }
    try {
      ((CacheVariable) myLevel).updateValue(nextValue);
      if (nextValue > myAlarm.getHighHighLimit()) {
        activateAlarm(700, ExclusiveLimitState.HighHigh);
      } else if (nextValue > myAlarm.getHighLimit()) {
        activateAlarm(500, ExclusiveLimitState.High);
      } else if (nextValue < myAlarm.getLowLowLimit()) {
        activateAlarm(700, ExclusiveLimitState.Low);
      } else if (nextValue < myAlarm.getLowLimit()) {
        activateAlarm(500, ExclusiveLimitState.LowLow);
      } else {
        inactivateAlarm();
      }
    } catch (Exception e) {
      logger.error("Error while simulating", e);
      // printException(e);
      throw new RuntimeException(e); // End the task
    }

  }

  /**
   * Activates an alarm, if it is not active already, or if the severity changes.
   *
   * @param severity the severity to set for the alarm
   * @param limitState the limit state to set
   */
  private void activateAlarm(int severity, ExclusiveLimitState limitState) {
    if (myAlarm.isEnabled() && (!myAlarm.isActive() || (myAlarm.getSeverity().getValue() != severity))) {
      println("Simulating alarm, MyNodeManager.activateAlarm: severity=" + severity);
      myAlarm.setActive(true);
      myAlarm.setRetain(true);
      myAlarm.setAcked(false); // Also sets confirmed to false
      myAlarm.setSeverity(severity);
      myAlarm.getLimitStateNode().setCurrentLimitState(limitState);

      triggerEvent(myAlarm);

      // If you wish to check whether any clients are monitoring your
      // alarm, you can use the following

      // logger.info("myAlarm is monitored=" +
      // myAlarm.isMonitoredForEvents());
    }
  }

  /**
   * A sample implementation of creating different types and instances manually.
   * 
   * @throws StatusException if something goes wrong in the initialization
   * @throws UaInstantiationException if something goes wrong regarding object instantiation
   */
  private void createAddressSpace() throws StatusException, UaInstantiationException {
    // +++ My nodes +++

    int ns = getNamespaceIndex();

    // My Event Manager Listener
    this.getEventManager().setListener(myEventManagerListener);

    // UA types and folders which we will use
    final UaObject objectsFolder = getServer().getNodeManagerRoot().getObjectsFolder();
    final UaType baseObjectType = getServer().getNodeManagerRoot().getType(Identifiers.BaseObjectType);
    final UaType baseDataVariableType = getServer().getNodeManagerRoot().getType(Identifiers.BaseDataVariableType);

    // Folder for my objects
    final NodeId myObjectsFolderId = new NodeId(ns, "MyObjectsFolder");
    myObjectsFolder = createInstance(FolderTypeNode.class, "MyObjects", myObjectsFolderId);

    this.addNodeAndReference(objectsFolder, myObjectsFolder, Identifiers.Organizes);

    // My Device Type

    // The preferred way to create types is to use Information Models, but this example shows how
    // you can do that also with your own code

    final NodeId myDeviceTypeId = new NodeId(ns, "MyDeviceType");
    UaObjectType myDeviceType = new UaObjectTypeNode(this, myDeviceTypeId, "MyDeviceType", Locale.ENGLISH);
    this.addNodeAndReference(baseObjectType, myDeviceType, Identifiers.HasSubtype);

    // My Device

    final NodeId myDeviceId = new NodeId(ns, "MyDevice");
    myDevice = new UaObjectNode(this, myDeviceId, "MyDevice", Locale.ENGLISH);
    myDevice.setTypeDefinition(myDeviceType);
    myObjectsFolder.addReference(myDevice, Identifiers.HasComponent, false);

    // My Level Type

    final NodeId myLevelTypeId = new NodeId(ns, "MyLevelType");
    UaType myLevelType = this.addType(myLevelTypeId, "MyLevelType", baseDataVariableType);

    // My Level Measurement

    final NodeId myLevelId = new NodeId(ns, "MyLevel");
    UaDataType doubleType = getServer().getNodeManagerRoot().getDataType(Identifiers.Double);
    myLevel = new CacheVariable(this, myLevelId, "MyLevel", LocalizedText.NO_LOCALE);
    myLevel.setDataType(doubleType);
    myLevel.setTypeDefinition(myLevelType);
    myDevice.addComponent(myLevel);

    // My Switch
    // Use PlainVariable and addComponent() to add it to myDevice
    // Note that we use NodeIds instead of UaNodes to define the data type
    // and type definition

    NodeId mySwitchId = new NodeId(ns, "MySwitch");
    mySwitch = new PlainVariable<Boolean>(this, mySwitchId, "MySwitch", LocalizedText.NO_LOCALE);
    mySwitch.setDataTypeId(Identifiers.Boolean);
    mySwitch.setTypeDefinitionId(Identifiers.BaseDataVariableType);
    myDevice.addComponent(mySwitch); // addReference(...Identifiers.HasComponent...);

    // Initial value
    mySwitch.setCurrentValue(false);

    // A sample alarm node
    createAlarmNode(myLevel);

    // A sample custom event type
    createMyEventType();

    // A sample enumeration type
    createMyEnumNode();

    // A sample method node
    createMethodNode();
  }

  /**
   * Create a sample alarm node structure.
   *
   * @param source the variable that is the source of the alarm
   *
   * @throws StatusException if something goes wrong in the initialization
   * @throws UaInstantiationException if something goes wrong regarding object instantiation
   */
  private void createAlarmNode(UaVariable source) throws StatusException, UaInstantiationException {

    // Level Alarm from the LevelMeasurement

    // See the Spec. Part 9. Appendix B.2 for a similar example

    int ns = this.getNamespaceIndex();
    final NodeId myAlarmId = new NodeId(ns, source.getNodeId().getValue() + ".Alarm");
    String name = source.getBrowseName().getName() + "Alarm";

    // Since the HighHighLimit and others are Optional nodes,
    // we need to define them to be instantiated.
    TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
    conf.addOptional(UaBrowsePath.from(Ids.LimitAlarmType, UaQualifiedName.standard("HighHighLimit")));
    conf.addOptional(UaBrowsePath.from(Ids.LimitAlarmType, UaQualifiedName.standard("HighLimit")));
    conf.addOptional(UaBrowsePath.from(Ids.LimitAlarmType, UaQualifiedName.standard("LowLimit")));
    conf.addOptional(UaBrowsePath.from(Ids.LimitAlarmType, UaQualifiedName.standard("LowLowLimit")));

    // The configuration must be set to be used
    // this.getNodeManagerTable().setNodeBuilderConfiguration(conf.build()); //global
    // this.setNodeBuilderConfiguration(conf.build()); //local to this NodeManager
    // createNodeBuilder(ExclusiveLevelAlarmTypeNode.class, conf.build()); //NodeBuilder specific
    // (createInstance uses this internally)

    // for purpose of this sample program, it is set to this manager, normally this would be set
    // once after creating this NodeManager
    this.setNodeBuilderConfiguration(conf.build());

    myAlarm = createInstance(ExclusiveLevelAlarmTypeNode.class, name, myAlarmId);

    // ConditionSource is the node which has this condition
    myAlarm.setSource(source);
    // Input is the node which has the measurement that generates the alarm
    myAlarm.setInput(source);

    myAlarm.setMessage(new LocalizedText("Level exceeded"));
    myAlarm.setSeverity(500); // Medium level warning
    myAlarm.setHighHighLimit(90.0);
    myAlarm.setHighLimit(70.0);
    myAlarm.setLowLimit(30.0);
    myAlarm.setLowLowLimit(10.0);
    myAlarm.setEnabled(true);
    myDevice.addComponent(myAlarm); // addReference(...Identifiers.HasComponent...)

    // + HasCondition, the SourceNode of the reference should normally
    // correspond to the Source set above
    source.addReference(myAlarm, Identifiers.HasCondition, false);

    // + EventSource, the target of the EventSource is normally the
    // source of the HasCondition reference
    myDevice.addReference(source, Identifiers.HasEventSource, false);

    // + HasNotifier, these are used to link the source of the EventSource
    // up in the address space hierarchy
    myObjectsFolder.addReference(myDevice, Identifiers.HasNotifier, false);
  }

  /**
   * Create a sample method.
   *
   * @throws StatusException if something goes wrong in the initialization
   */
  private void createMethodNode() throws StatusException {
    int ns = this.getNamespaceIndex();
    final NodeId myMethodId = new NodeId(ns, "MyMethod");
    myMethod = new PlainMethod(this, myMethodId, "MyMethod", Locale.ENGLISH);
    Argument[] inputs = new Argument[2];
    inputs[0] = new Argument();
    inputs[0].setName("Operation");
    inputs[0].setDataType(Identifiers.String);
    inputs[0].setValueRank(ValueRanks.Scalar);
    inputs[0].setArrayDimensions(null);
    inputs[0].setDescription(new LocalizedText(
        "The operation to perform on parameter: valid functions are sin, cos, tan, pow", Locale.ENGLISH));
    inputs[1] = new Argument();
    inputs[1].setName("Parameter");
    inputs[1].setDataType(Identifiers.Double);
    inputs[1].setValueRank(ValueRanks.Scalar);
    inputs[1].setArrayDimensions(null);
    inputs[1].setDescription(new LocalizedText("The parameter for operation", Locale.ENGLISH));
    myMethod.setInputArguments(inputs);

    Argument[] outputs = new Argument[1];
    outputs[0] = new Argument();
    outputs[0].setName("Result");
    outputs[0].setDataType(Identifiers.Double);
    outputs[0].setValueRank(ValueRanks.Scalar);
    outputs[0].setArrayDimensions(null);
    outputs[0].setDescription(new LocalizedText("The result of 'operation(parameter)'", Locale.ENGLISH));
    myMethod.setOutputArguments(outputs);

    this.addNodeAndReference(myDevice, myMethod, Identifiers.HasComponent);

    // Create the listener that handles the method calls
    myMethodManagerListener = new MyMethodManagerListener(myMethod);
    MethodManagerUaNode m = (MethodManagerUaNode) this.getMethodManager();
    m.addCallListener(myMethodManagerListener);
  }

  /**
   * @throws StatusException if the necessary type node(s) are not found
   *
   */
  private void createMyEnumNode() throws StatusException {
    // An example showing how a new enumeration type can be defined in code.
    // Note that it is usually easier to define new types using information models and
    // generating Java code out of those. See the more about that in the
    // 'codegen' documentation.

    // 1. Create the type node...

    NodeId myEnumTypeId = new NodeId(this.getNamespaceIndex(), "MyEnumType");

    UaDataType myEnumType = new UaDataTypeNode(this, myEnumTypeId, "MyEnumType", LocalizedText.NO_LOCALE);

    // ... as sub type of Enumeration
    UaType enumerationType = getServer().getNodeManagerRoot().getType(Identifiers.Enumeration);
    enumerationType.addSubType(myEnumType);

    // 2. Add the EnumStrings property ...

    NodeId myEnumStringsId = new NodeId(this.getNamespaceIndex(), "MyEnumType_EnumStrings");;
    PlainProperty<LocalizedText[]> enumStringsProperty = new PlainProperty<LocalizedText[]>(this, myEnumStringsId,
        new QualifiedName("EnumStrings"), new LocalizedText("EnumStrings", LocalizedText.NO_LOCALE));
    enumStringsProperty.setDataTypeId(Identifiers.LocalizedText);
    enumStringsProperty.setValueRank(ValueRanks.OneDimension);
    enumStringsProperty.setArrayDimensions(new UnsignedInteger[] {UnsignedInteger.ZERO});
    enumStringsProperty.setAccessLevel(AccessLevelType.CurrentRead);
    enumStringsProperty.addReference(Identifiers.ModellingRule_Mandatory, Identifiers.HasModellingRule, false);

    myEnumType.addProperty(enumStringsProperty);

    // ... with Value
    enumStringsProperty.setCurrentValue(new LocalizedText[] {new LocalizedText("Zero"), new LocalizedText("One"),
        new LocalizedText("Two"), new LocalizedText("Three")});

    EnumerationSpecification myEnumSpecification = EnumerationSpecification.builder().setName("MyEnumType")
        .setTypeId(UaNodeId.fromLocal(myEnumTypeId, getNamespaceTable())).addMapping(0, "Zero").addMapping(1, "One")
        .addMapping(2, "Two").addMapping(3, "Three").build();
    // This makes DataTypeDefinition Attribute work for the enum
    getNodeManagerTable().getEncoderContext().addEnumerationSpecification(myEnumSpecification);


    // 3. Create the instance
    NodeId myEnumObjectId = new NodeId(this.getNamespaceIndex(), "MyEnumObject");
    CacheVariable myEnumVariable = new CacheVariable(this, myEnumObjectId, "MyEnumObject", LocalizedText.NO_LOCALE);
    myEnumVariable.setDataType(myEnumType);

    // .. as a component of myDevice
    myDevice.addComponent(myEnumVariable);

    // 4. Initialize the value
    myEnumVariable.setValue(new DataValue(new Variant(myEnumSpecification.createEnumerationFromInteger(1))));
  }

  /**
   * A sample custom event type.
   * <p>
   * NOTE that it is usually easier to create new types using the Information Models and import them
   * from XML to the server. You can also generate the respective Java types with the 'codegen' from
   * the same XML. In this example, we will construct the type into the address space "manually".
   * MyEventType is also hand-coded and is registered to be used to create the instances of that
   * type.
   * <p>
   * When the type definition is in the address space, and the respective Java class is registered
   * to the server, it will create those instances, for example as shown in {@link #sendEvent()}.
   *
   * @throws StatusException if something goes wrong in the initialization
   */
  private void createMyEventType() throws StatusException {
    int ns = this.getNamespaceIndex();

    NodeId myEventTypeId = new NodeId(ns, MyEventType.MY_EVENT_ID);
    UaObjectType myEventType = new UaObjectTypeNode(this, myEventTypeId, "MyEventType", LocalizedText.NO_LOCALE);
    getServer().getNodeManagerRoot().getType(Identifiers.BaseEventType).addSubType(myEventType);

    NodeId myVariableId = new NodeId(ns, MyEventType.MY_VARIABLE_ID);
    PlainVariable<Integer> myVariable =
        new PlainVariable<Integer>(this, myVariableId, MyEventType.MY_VARIABLE_NAME, LocalizedText.NO_LOCALE);
    myVariable.setDataTypeId(Identifiers.Int32);
    // The modeling rule must be defined for the mandatory elements to
    // ensure that the event instances will also get the elements.
    myVariable.addModellingRule(ModellingRule.Mandatory);
    myEventType.addComponent(myVariable);

    NodeId myPropertyId = new NodeId(ns, MyEventType.MY_PROPERTY_ID);
    PlainProperty<Integer> myProperty =
        new PlainProperty<Integer>(this, myPropertyId, MyEventType.MY_PROPERTY_NAME, LocalizedText.NO_LOCALE);
    myProperty.setDataTypeId(Identifiers.String);
    myProperty.addModellingRule(ModellingRule.Mandatory);
    myEventType.addProperty(myProperty);

    getServer().registerClass(MyEventType.class, myEventTypeId);
  }

  /**
   * Changes myAlarm to inactive state and triggers an event about the change. If the event is not
   * acknowledged, it will keep the retain state set, meaning that it can still show up in the
   * client applications alarm view, for example.
   */
  private void inactivateAlarm() {
    if (myAlarm.isEnabled() && myAlarm.isActive()) {
      println("Simulating alarm (inactive), MyNodeManager.inactivateAlarm");
      myAlarm.setActive(false);
      myAlarm.setRetain(!myAlarm.isAcked());
      myAlarm.getLimitStateNode().setCurrentLimitState(ExclusiveLimitState.None);

      triggerEvent(myAlarm);
    }
  }

  /**
   * Send an event notification.
   *
   * @param event The event to trigger.
   */
  private void triggerEvent(BaseEventTypeNode event) {
    // Trigger event
    final DateTime now = DateTime.currentTime();
    // Use your own EventId to keep track of your events, if you need to (for example when alarms
    // are acknowledged)
    ByteString myEventId = getNextUserEventId();
    // If you wish, you can record the full event ID that is provided by triggerEvent, although your
    // own 'myEventId' is usually enough to keep track of the event.
    /* ByteString fullEventId = */event.triggerEvent(now, now, myEventId);
  }

  protected ByteString getNextUserEventId() {
    return myEventManagerListener.getNextUserEventId();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.server.NodeManagerUaNode#init()
   */
  @Override
  protected void init() throws StatusException, UaNodeFactoryException {
    super.init();

    createAddressSpace();
  }

  void addNode(String name) {
    // Initialize NodeVersion property, to enable ModelChangeEvents
    myObjectsFolder.initNodeVersion();

    getServer().getNodeManagerRoot().beginModelChange();
    try {
      NodeId nodeId = new NodeId(this.getNamespaceIndex(), UUID.randomUUID());

      UaNode node =
          this.getNodeFactory().createNode(NodeClass.Variable, nodeId, name, Locale.ENGLISH, Identifiers.PropertyType);
      myObjectsFolder.addComponent(node);
    } catch (UaNodeFactoryException e) {
      printException(e);
    } catch (IllegalArgumentException e) {
      printException(e);
    } finally {
      getServer().getNodeManagerRoot().endModelChange();
    }
  }

  void deleteNode(QualifiedName nodeName) throws StatusException {
    UaNode node = myObjectsFolder.getComponent(nodeName);
    if (node != null) {
      getServer().getNodeManagerRoot().beginModelChange();
      try {
        this.deleteNode(node, true, true);
      } finally {
        getServer().getNodeManagerRoot().endModelChange();
      }
    } else {
      println("MyObjects does not contain a component with name " + nodeName);
    }
  }
}
