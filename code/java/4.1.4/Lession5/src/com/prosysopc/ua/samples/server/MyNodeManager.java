package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNodeFactoryException;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;

/**
 * @ClassName MyNodeManager
 * @Description TODO
 * @Author wzj
 * @Date 2019/12/12 18:56
 * @Version 1.0
 **/
public class MyNodeManager extends NodeManagerUaNode {

  public MyNodeManager(UaServer uaServer, String s) {
    super(uaServer, s);
  }

  @Override
  protected void init() throws StatusException, UaNodeFactoryException {

    super.init();
  }
}
