/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.core.Attributes;
import com.prosysopc.ua.stack.core.NodeClass;

class AttributeUtil {

  private static final Map<NodeClass, UnsignedInteger[]> mappings = new HashMap<NodeClass, UnsignedInteger[]>();

  static {
    ArrayList<NodeClass> l = new ArrayList<NodeClass>();
    Map<NodeClass, List<UnsignedInteger>> iteration = new HashMap<NodeClass, List<UnsignedInteger>>();
    for (NodeClass n : NodeClass.ALL) {
      iteration.put(n, new ArrayList<UnsignedInteger>());
      if (n != NodeClass.Unspecified) {
        l.add(n);
      }
    }
    NodeClass[] all = l.toArray(new NodeClass[0]);

    add(iteration, Attributes.NodeId, all);
    add(iteration, Attributes.NodeClass, all);
    add(iteration, Attributes.BrowseName, all);
    add(iteration, Attributes.DisplayName, all);
    add(iteration, Attributes.Description, all);
    add(iteration, Attributes.WriteMask, all);
    add(iteration, Attributes.UserWriteMask, all);
    add(iteration, Attributes.IsAbstract, NodeClass.ReferenceType, NodeClass.ObjectType, NodeClass.VariableType,
        NodeClass.DataType);
    add(iteration, Attributes.Symmetric, NodeClass.ReferenceType);
    add(iteration, Attributes.InverseName, NodeClass.ReferenceType);
    add(iteration, Attributes.ContainsNoLoops, NodeClass.View);
    add(iteration, Attributes.EventNotifier, NodeClass.View, NodeClass.Object);
    add(iteration, Attributes.Value, NodeClass.Variable, NodeClass.VariableType);
    add(iteration, Attributes.DataType, NodeClass.Variable, NodeClass.VariableType);
    add(iteration, Attributes.ValueRank, NodeClass.Variable, NodeClass.VariableType);
    add(iteration, Attributes.ArrayDimensions, NodeClass.Variable, NodeClass.VariableType);
    add(iteration, Attributes.AccessLevel, NodeClass.Variable);
    add(iteration, Attributes.UserAccessLevel, NodeClass.Variable);
    add(iteration, Attributes.MinimumSamplingInterval, NodeClass.Variable);
    add(iteration, Attributes.Historizing, NodeClass.Variable);
    add(iteration, Attributes.Executable, NodeClass.Method);
    add(iteration, Attributes.UserExecutable, NodeClass.Method);

    for (Entry<NodeClass, List<UnsignedInteger>> e : iteration.entrySet()) {
      UnsignedInteger[] a = e.getValue().toArray(new UnsignedInteger[0]);
      mappings.put(e.getKey(), a);
    }

  }

  public static UnsignedInteger[] getSupportedAttributes(NodeClass nodeClass) {
    return mappings.get(nodeClass);
  }

  private static void add(Map<NodeClass, List<UnsignedInteger>> map, UnsignedInteger attribute,
      NodeClass... nodeClasses) {
    if (nodeClasses != null) {
      for (NodeClass n : nodeClasses) {
        map.get(n).add(attribute);
      }
    }
  }
}
