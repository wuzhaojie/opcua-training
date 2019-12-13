## Lession2

- 地址空间(定义服务器端的数据以及如何对其进行管理)
  - 标准节点管理器：
    - 始终包含基本节点管理器NodeManagerRoot；
    - namespaceIndex = 0
    - Objects、Types、Views
    
  - 自定义节点管理器(方法1：继承NodeManagerUaNode)：
    - 需要创建自己的namespace、自己的节点管理器："myNodeManager = new NodeManagerUaNode(server, "http://www.prosysopc.com/OPCUA/SampleAddressSpace");"
    - 通过NodeManagerUaNode实例的myNodeManager管理所有UaNode:
      - NodeManagerUaNode.addNodeAndReference()
      - UaNode.addReference()
      - UaNode.addComponent()
      - UaNode.addProperty()
    - server为每个namespace分配一个NameSpaceIndex；
    - debug createAddressSpace函数，演示如何在代码中手动创建不同的节点类型：
      - 如何使用信息模型来 __初始化__ 类型。标准节点实例必须使用NodeManagerUaNode.createInstance()创建，而不能使用new构造函数。如：FolderTypeNode类型的myObjectsFolder。
      - 如何以更简单的方式 __实例化__ 具有完整结构的对象(基本的构建块)。UaObjectTypeNode、UaObjectNode、CacheVariable可以使用new。
 
  - 自定义节点管理器(方法2：继承NodeManager)：
    - 通过NodeManagerUaNode节点管理器管理节点时，每个节点都会实例化一个UaNode对象，占用很大内存空间；
    - 像所有节点管理器一样：使用server、namespaceUri作为参数；
    - 要支持OPC UA浏览服务，必须实现一些抽象方法，NodeManager提供了接口。例如：getBrowseName、getDisplayName、getNodeClass等。实现
      这些接口时，传入的UaNode参数始终为null，因为这里继承NodeManager，UaNode未实现；
    - 比较NodeId是否相等使用：getNamespaceTable().nodeIdEquals()；
    - NodeId和ExpandedNodeId相互转换：getNamespaceTable().toNodeId()、getNamespaceTable().toExpandedNodeId()
    - 使用命名空间索引而不是URI；
      
  - IoManager：
    - 用于处理来自客户端的读、写请求。
    - SDK中提供了默认实现：IoManagerUaNode(继承IoManager)。也可以直接继承IoManager(myBigNodeManager示例)进行实现。好处是不用为每一个节点实例化一个uaNode对象；
    - IoManagerListener(MyNodeManager示例)。它可以让您保留标准的IoManager（或IoManagerUaNode），但仍可以为某些方法提供自己的定义。
    - 用于管理Attribute的服务，读、写节点属性；SDK中定义好了一些服务于接口，需要自己进行实现。例如:readAttribute、writeAttribute、
      readValue、readNonValue、writeNonValue；
    - CacheVariable、CacheProperty、PlainVariable、PlainProperty可以缓存一个值；
    - 实现接口时：如果操作异步完成，并且您不知道操作是否成功，则应该返回false。
    
  -事件、警报和条件
    - 事件管理器：
      - 要添加对OPC UA事件的支持，必须使用事件管理器并使用事件对象来触发事件。
      - 事件管理器用于处理与标准事件和条件管理有关的命令。
        - 默认事件管理器： 
          - NodeManagerUaNode默认使用的事件管理器是EventManagerUaNode。处理来自client端的命令enable、disable、acknowledge等。
        - 自定义事件管理器：
          - 可以通过这种方式创建自己的事件管理器（EventManagerUaNode myEventManager = new MyEventManager(myNodeManager)）;
          - 示例中将默认事件管理器进行替换"this.getEventManager().setListener(myEventManagerListener)"。可以让函数返回false来
          使用默认实现。
      - （OPC UA Specification Part 9）描述condition types和condition method。
      
    - 定义事件、条件
      - 要在地址空间中对事件进行实际建模并触发它们，可以使用SDK中定义的事件类型。事件有两种主要类型：正常事件和条件。事件只是对客户端应用程序的通知，
      而条件也可以包含状态。因此，条件节点通常在地址空间中也可以用作节点。
        - 正常事件：
          ```
            MyEventType ev = createEvent(MyEventType.class);
            ev.setMessage("MyEvent");
            ev.setMyVariable(new Random().nextInt());
            ev.setMyProperty("Property Value " + ev.getMyVariable());
          ```
        - 自定义事件类型:
          - 如果希望在事件中使用自定义字段，则需要定义自定义事件类型。这些字段被定义为事件类型的属性或变量组件。
      - 条件:ExclusiveLevelAlarmType是特定的条件类型，用于初始化报警节点。
      
    - 触发事件
      - 触发正常事件：ev.triggerEvent(null)；(参数为null)。
      - 触发条件：event.triggerEvent(now, now, myEventId); （参数不为null）。
      
    - 历史
      - 历史管理器使您能够处理所有的历史数据和事件。SDK中没有默认功能，需要自己跟踪历史数据并实现服务。有两种方式实现历史管理器：
        - 自定义HistoryManager子类，然后通过myNodeManager.getHistoryManager()将历史记录管理器设置为自己的历史管理器。
        - 定义一个新的listener并实现其中的功能（MyHistorian implements HistoryManagerListener）。
  
  - 节点类型
    - 节点类型和节点实例的区别
      - OPC UA中的节点类型通过一个数据结构来定义。数据结构中通过HasModellingRule引用来明确标明哪些内部节点是必须要创建的(Mandatory)，哪些节点是可选的(optional)。
      - 节点实例(Objects、Variables)是根据节点类型来创建的，标记为Mandatory必须创建，标记为optional的选择创建。
      - JDK中通过NodeBuilder来创建节点实例，NodeBuilder可以从NodeManagerUaNode中访问到。JDK在NodeManagerUaNode类中封装了createInstance方法，隐藏了NodeBuilder调用。
      
    - NodeManagerUaNode使用UaNode去管理地址空间中的节点。我们可以通过继承、重写UaNode提供的功能实现自己的节点类型。
      - 通用节点
        - 通用节点类型在"com.prosysopc.ua.server.nodes"中定义。
      - OPC UA标准节点类型
        - OPC UA规范定义了一些标准的节点类型，这些类型通常含有特定的结构。SDK包含了对这些标准类型的定义，在com.prosysopc.ua.types.opcua中。
          服务端特定实现在"server"包中，客户端特定实现在"client"包中。
    
  - 信息建模：
    - 加载信息模型：
      - "server.getAddressSpace().loadModel(new File("SampleTypes.xml").toURI());",xml文件通过建模工具生成；
    - 代码生成：
      - 如果要在java应用程序中使用*.xml文档中定义的类型，可以通过"codegen"将"*.xml"转为java类；
      - 注册模型：为了让JDK使用生成的类代替基本的实现，必须使SDK知道生成的类；注意区分注册服务（p121注册节点）；
      - 加载信息模型。必须在加载模型之前完成模型的注册。
   
- 熟悉ua基础数据类型；
- 简单信息模型创建；
- 两种数据仿真方式演示：client写入、server端自动生成模拟数据；

## Server

- debug ComplianceNodes、NonUaNodeComplianceTest、MyBigNodeManager信息模型的创建过程:
  - attribute（属性）的初始化过程；
  - properties（特性）的初始化过程；
  - 通过uaexpert查看代码的执行效果；
  
  注释掉item.notifyDataChange并查看数据变化；
  
- 模拟数据并通过uaexpert查看数据模拟效果；

## Client

- 调用browse方法浏览server信息模型，查找可进行数据读写的variable节点；

- 通过client进行数据读写访问，同时通过uaexpert查看数据写入效果；

- 通过uaexpert演示数据写入，并通过client进行数据读取；