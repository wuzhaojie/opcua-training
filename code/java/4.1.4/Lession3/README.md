## Lession3
- mydevice信息模型创建

- 历史

- 报警

- 事件

- 方法
  - 如果在信息模型中自定义方法节点，则需要通过实现CallableListener来处理相关的方法调用。


### Server

- debug createAddressSpace函数，讲解mydevice信息模型创建过程：
  - 历史点、报警对象、事件对象、自定义方法；
  
- debug startSimulation函数，讲解事件、历史、报警数据的产生和存储；

- 通过uaexpert调用server，同时在server debug，观察server的处理：
  - 历史读；
  - 报警读；
  - 事件读；
  - 方法调用；

### Client

- 在client端实现并演示如上uaexpert展示的功能；