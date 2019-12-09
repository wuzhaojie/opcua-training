/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.common;


import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;

/**
 * The table of name space URIs for a server. The table enables mapping between
 * name space indexes and URIs.
 * 
 * Use {@link #add} to add entries to the table. Use {@link #getIndex} to find the
 * index of an URI or {@link #getUri} to find the Uri of an index.
 * 
 */
public class NamespaceTable extends UriTable {

	public static String OPCUA_NAMESPACE = "http://opcfoundation.org/UA/";
	private static NamespaceTable defaultInstance;

	public static NamespaceTable createFromArray(String[] namespaceArray) {
		NamespaceTable result = new NamespaceTable();
		result.addAll(namespaceArray);
		return result;
	}

	public NamespaceTable() {
		add(0, OPCUA_NAMESPACE);
	}

	/**
	 * Compare 2 ExpandedNodeId objects. This method is intended for cases 
	 * where one ExpandedNodeId is defined with NamespaceUri and another 
	 * is defined with NamespaceIndex.
	 * @param n1 first
	 * @param n2 second
	 * @return true if they are equal
	 */
	public boolean nodeIdEquals(ExpandedNodeId n1, ExpandedNodeId n2) {
		if(ExpandedNodeId.isNull(n1) && ExpandedNodeId.isNull(n2)){
			return true;
		}
		if(ExpandedNodeId.isNull(n1) || ExpandedNodeId.isNull(n2)){
			return false; //other is now null
		}		
		if (!n1.getValue().equals(n2.getValue()))
			return false;
		int i1 = n1.getNamespaceUri() == null ? n1.getNamespaceIndex() : getIndex(n1.getNamespaceUri());
		int i2 = n2.getNamespaceUri() == null ? n2.getNamespaceIndex() : getIndex(n2.getNamespaceUri());
		return i1 == i2;
	}
	
	/**
	 * Compare 1 ExpandedNodeId and 1 NodeId. This method is intended for cases 
	 * where the ExpandedNodeId is defined with NamespaceUri and a comparison 
	 * to NodeId which has NamespaceIndex is wanted.
	 * @param n1 first
	 * @param n2 second
	 * @return true if they are equal
	 */
	public boolean nodeIdEquals(NodeId n1, ExpandedNodeId n2) {
		if(NodeId.isNull(n1) && ExpandedNodeId.isNull(n2)){
			return true;
		}
		if(NodeId.isNull(n1) || ExpandedNodeId.isNull(n2)){
			return false; //other is now null
		}	
		if (!n1.getValue().equals(n2.getValue()))
			return false;
		int i1 = n1.getNamespaceIndex();
		int i2 = n2.getNamespaceUri() == null ? n2.getNamespaceIndex() : getIndex(n2.getNamespaceUri());
		return i1 == i2;
	}

	/**
	 * Convert the nodeId to an ExpandedNodeId using the namespaceUris of the
	 * table
	 * 
	 * @param nodeId
	 *            the node ID
	 * @return The respective ExpandedNodeId
	 * @return
	 */
	public ExpandedNodeId toExpandedNodeId(NodeId nodeId) {
		return new ExpandedNodeId(null, getUri(nodeId.getNamespaceIndex()), nodeId.getValue());
	}

	/**
	 * Convert the expandedNodeId to a NodeId using the name space indexes of the
	 * table
	 * 
	 * @param expandedNodeId
	 *            the expanded node ID
	 * @return The respective NodeId
	 * @throws ServiceResultException
	 *             if there is no entry for the namespaceUri used in the
	 *             expandedNodeId
	 */
	public NodeId toNodeId(ExpandedNodeId expandedNodeId)
			throws ServiceResultException {
		// TODO: serverIndex==0 is valid reference to the local server, so it
		// should be accepted as well // jaro
		if (ExpandedNodeId.isNull(expandedNodeId))
			return NodeId.NULL;
		if (!expandedNodeId.isLocal())
			throw new ServiceResultException(
					"Cannot convert ExpandedNodeId with server index to NodeId");
		String uri = expandedNodeId.getNamespaceUri();
		if (uri == null)
			return NodeId.get(expandedNodeId.getIdType(), expandedNodeId
					.getNamespaceIndex(), expandedNodeId.getValue());
		int index = this.getIndex(uri);
		if (index < 0)
			throw new ServiceResultException(
					"Index for uri \""+uri+"\" not found in NamespaceTable");
		return NodeId.get(expandedNodeId.getIdType(), index, expandedNodeId
				.getValue());
	}

	/**
	 * Check if the node IDs refer to the same name space. Compares the NamespaceIndex of the IDs.
	 * 
	 * @param nodeId1
	 * @param nodeId2
	 * @return true if the nodes are in the same name space
	 */
	public boolean namespaceEquals(NodeId nodeId1, NodeId nodeId2) {
		return nodeId1.getNamespaceIndex() == nodeId2.getNamespaceIndex();
	}

	/**
	 * Check if the node IDs refer to the same name space. The expandedNodeId is
	 * checked for the NamespaceUri or Index depending on which is used.
	 * 
	 * @param nodeId
	 * @param expandedNodeId
	 * @return true if the nodes are in the same name space
	 */
	public boolean namespaceEquals(NodeId nodeId, ExpandedNodeId expandedNodeId) {
		int expandedNamespaceIndex = expandedNodeId.getNamespaceUri() != null ? getIndex(expandedNodeId
				.getNamespaceUri()) : expandedNodeId.getNamespaceIndex();
		return nodeId.getNamespaceIndex() == expandedNamespaceIndex;
	}

	/**
	 * Check if the node IDs refer to the same name space. The expandedNodeIds are
	 * checked for the NamespaceUri or Index depending on which is used.
	 * 
	 * @param expandedNodeId1
	 * @param expandedNodeId2
	 * @return true if the nodes are in the same name space
	 */
	public boolean namespaceEquals(ExpandedNodeId expandedNodeId1, ExpandedNodeId expandedNodeId2) {
		int expandedNamespaceIndex1 = expandedNodeId1.getNamespaceUri() != null ? getIndex(expandedNodeId1
				.getNamespaceUri()) : expandedNodeId1.getNamespaceIndex();
		int expandedNamespaceIndex2 = expandedNodeId2.getNamespaceUri() != null ? getIndex(expandedNodeId2
				.getNamespaceUri()) : expandedNodeId2.getNamespaceIndex();
		return expandedNamespaceIndex1 == expandedNamespaceIndex2;
	}

	/**
	 * @return a default instance which can be used when no other namespace
	 *         table is available. DO NOT use, if you have a valid application
	 *         context with an initialized namespace table available.
	 */
	public static NamespaceTable getDefaultInstance() {
		if (defaultInstance == null)
			defaultInstance = NamespaceTable.createFromArray(new String[]{NamespaceTable.OPCUA_NAMESPACE});
		return defaultInstance;
	}

}
