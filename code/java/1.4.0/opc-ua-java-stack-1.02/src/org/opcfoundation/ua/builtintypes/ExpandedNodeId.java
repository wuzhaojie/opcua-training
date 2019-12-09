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

package org.opcfoundation.ua.builtintypes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.UUID;

import org.opcfoundation.ua.common.NamespaceTable;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.ObjectUtils;



/**
 * A NodeId that allows the NamespaceUri to be specified explicitly instead of NamespaceIndex.
 * ExpandedNodeId may still use NamespaceIndex.
 * <p>
 * Instances of ExpandedNodeId are equals comparable only within server context.
 * <p> 
 * ExpandedNodeIds are equals comparable with NodeIds if they are constructed with NamespaceIndex
 * and no ServerIndex.
 * 
 * @see NodeId Id with NamespaceIndex and not ServerIndex
 * @see NamespaceTable For converting ExpandedNodeIds to NodeIds
 */
public final class ExpandedNodeId implements Comparable<ExpandedNodeId>{

	/** Considered null node id */
	/** Considered null node id */	
	public static final ExpandedNodeId NULL_NUMERIC = new ExpandedNodeId(NodeId.NULL_NUMERIC);
	public static final ExpandedNodeId NULL_STRING = new ExpandedNodeId(NodeId.NULL_STRING);
	public static final ExpandedNodeId NULL_GUID = new ExpandedNodeId(NodeId.NULL_GUID);
	public static final ExpandedNodeId NULL_OPAQUE = new ExpandedNodeId(NodeId.NULL_OPAQUE);
	public static final ExpandedNodeId NULL = NULL_NUMERIC;
	
	/** Identifier of "NodeId" in UA AddressSpace */
	public static final NodeId ID = Identifiers.ExpandedNodeId;	
	
	IdType type;
	int namespaceIndex;
	UnsignedInteger serverIndex;
	String namespaceUri;
	Object value;
	
	/**
	 * Construct ExpandedNodeId using NamespaceIndex.
	 * 
	 * @param serverIndex Server Index (optional)
	 * @param namespaceIndex namespace index
	 * @param value value (must be UnsignedInteger, String, UUID, byte[] or null)
	 */
	public ExpandedNodeId(UnsignedInteger serverIndex, int namespaceIndex, Object value)
	{
		if (namespaceIndex<0 || namespaceIndex>65535) 
			throw new IllegalArgumentException("namespaceIndex out of bounds");
		this.serverIndex = serverIndex == null ? UnsignedInteger.ZERO : serverIndex;
		if (value instanceof Integer) value = UnsignedInteger.getFromBits((Integer)value);
		this.value = value;
		this.namespaceIndex = namespaceIndex;
		
		// add uri only for non-nulls
		if(namespaceIndex == 0){
			if(!ObjectUtils.equals(value, NodeId.NULL_NUMERIC.getValue()) 
				&& !ObjectUtils.equals(value, NodeId.NULL_STRING.getValue())
				&& !ObjectUtils.equals(value, NodeId.NULL_GUID.getValue())
				&& !ObjectUtils.equals(value, NodeId.NULL_OPAQUE.getValue())){
				
				this.namespaceUri = NamespaceTable.OPCUA_NAMESPACE;
			}
		}
			
		
		if (value==null) type = IdType.String; //changed from opaque
		else if (value instanceof UnsignedInteger) type = IdType.Numeric;
		else if (value instanceof String) type = IdType.String;
		else if (value instanceof UUID) type = IdType.Guid;
		else if (value instanceof byte[]) type = IdType.Opaque;
		else throw new IllegalArgumentException("value cannot be "+value.getClass().getName());		
	}
	
	/**
	 * Construct ExpandedNodeId using NamespaceUri.
	 * 
	 * @param serverIndex Server Index (optional)
	 * @param namespaceUri
	 * @param value value (must be UnsignedInteger, String, UUID or byte[])
	 */
	public ExpandedNodeId(UnsignedInteger serverIndex, String namespaceUri, Object value)
	{
		if (namespaceUri == null) 
			throw new NullPointerException("namespaceUri; value=" + value);
		if (namespaceUri.isEmpty()) 
			throw new IllegalArgumentException("namespaceUri not defined");
		this.serverIndex = serverIndex == null ? UnsignedInteger.ZERO : serverIndex;
		if (value instanceof Integer)
			value = UnsignedInteger.valueOf((Integer) value);
		this.value = value;
		this.namespaceUri = namespaceUri;
		
		if (value==null) type = IdType.String;
		else if (value instanceof UnsignedInteger) type = IdType.Numeric;
		else if (value instanceof String) type = IdType.String;
		else if (value instanceof UUID) type = IdType.Guid;
		else if (value instanceof byte[]) type = IdType.Opaque;
		else throw new IllegalArgumentException("value cannot be "+value.getClass().getName());
	}		
	
	/**
	 * Convenience constructor that creates ExpandedNodeId from 
	 * NamespaceIndex and Identifier of an nodeId.
	 * 
	 * @param serverIndex Server Index (optional)
	 * @param nodeId nodeId
	 */
	public ExpandedNodeId(UnsignedInteger serverIndex, NodeId nodeId) {
		this(serverIndex, nodeId.getNamespaceIndex(), nodeId.getValue());
    }

	/**
	 * Convenience constructor that creates ExpandedNodeId from 
	 * NamespaceIndex and Identifier of an nodeId. Server Index is null.
	 * 
	 * @param nodeId nodeId
	 */
	public ExpandedNodeId(NodeId nodeId) {
		this(null, nodeId.getNamespaceIndex(), nodeId.getValue());
    }
	
	/**
	 * Convenience constructor that creates ExpandedNodeId from namespaceUri and value. Server index is 0
	 * @param namespaceUri
	 * @param value
	 */
	public ExpandedNodeId(String namespaceUri, Object value){
		this(UnsignedInteger.ZERO, namespaceUri, value);
	}


	/**
	 * Tests whether this node is null node
	 * 
	 * @return true if this node is a null node
	 */
	public boolean isNullNodeId() {		
		return equals(ExpandedNodeId.NULL);
	}
	/*
	 * returns true if the nodeId is absolute, i.e. it refers to an external server (with namespaceUri or serverIndex). 
	 *
	 */
	public boolean isAbsolute(){
		return ((namespaceUri != null && !namespaceUri.isEmpty()) || !isLocal());
	}

	public IdType getIdType()
	{
		return type;
	}
	
	/**
	 * Get NamespaceIndex if this ExpandedNodeId was constructed with one. 
	 *  
	 * @return NamespaceIndex
	 */
	public int getNamespaceIndex()
	{
		return namespaceIndex;
	}
	
	public Object getValue()
	{
		return value;
	}

	public UnsignedInteger getServerIndex()
	{
		return serverIndex;
	}
	
	/**
	 * Get NamespaceUri if this ExpandedNodeId was constructed with one. 
	 *  
	 * @return NamespaceUri or null
	 */
	public String getNamespaceUri()
	{
		return namespaceUri;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		if (value!=null)
			hashCode += value instanceof byte[] ? 3*Arrays.hashCode((byte[])value) : 3*value.hashCode();
		
		// Does not calc using nsIdx/nsUri (one or both can be defined and cannot do idx->uri mapping here)
		if (serverIndex!=null) hashCode += serverIndex.hashCode()*17;
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return this.equals(ExpandedNodeId.NULL);
		if (obj instanceof NodeId) {
			if ((namespaceUri!=null && namespaceUri != NamespaceTable.OPCUA_NAMESPACE) || !isLocal()) return false;
			NodeId other = (NodeId) obj;
			if (other.namespaceIndex!=namespaceIndex || other.type!=type) return false;
			if (this.value==other.value) return true;
			if (other.type==IdType.Opaque) 
				// Deep compare
				return Arrays.equals((byte[])value, (byte[])other.value);			
			return other.value.equals(value);
		} else
		if (obj instanceof ExpandedNodeId) {
			ExpandedNodeId other = (ExpandedNodeId) obj;
			if (namespaceUri!=null) {
				if (other.namespaceUri==null || !other.namespaceUri.equals(namespaceUri)) return false;
			} else {
				if (other.namespaceUri!=null) return false;
				if (other.namespaceIndex!=namespaceIndex) return false;
			}
			if (!isLocal()) {
				if (other.isLocal() || !other.serverIndex.equals(serverIndex)) return false;				
			} else {
				if (!other.isLocal()) return false;
			}
			if (other.type!=type) return false;
			if (this.value==other.value) return true;
			if (other.type==IdType.Opaque) 
				// Deep compare
				return Arrays.equals((byte[])value, (byte[])other.value);
			if (other.value != null) {
				return other.value.equals(value);
			} else {
				return value == null;
			}
		} else
		return false;
	}
	
	@Override
	public int compareTo(ExpandedNodeId other) {
		int value;
		if (namespaceUri != null && other.namespaceUri != null)
			value = namespaceUri.compareTo(other.namespaceUri);
		else
			value = namespaceIndex - other.namespaceIndex;
		if (value == 0)
			value = type.getValue() - other.type.getValue();
		if (value == 0)
			switch (type) {
			case Numeric:
				value = ((UnsignedInteger) this.value)
						.compareTo((UnsignedInteger) other.value);
				break;
			case String:
				value = ((String) this.value).compareTo((String) other.value);
				break;
			case Guid:
				value = ((UUID) this.value).compareTo((UUID) other.value);
				break;
			case Opaque:
				value = Arrays
						.equals((byte[]) this.value, (byte[]) other.value) ? 0
						: 1;
				break;
			}
		return value;
	}	
	
	@Override
	public String toString() {
		try {
			String srvPart = !isLocal() ? "srv="+serverIndex+";" : "";
			String nsPart = namespaceUri!=null ? "nsu="+URLEncoder.encode(namespaceUri, "ISO8859-1")+";" : namespaceIndex>0 ? "ns="+namespaceIndex+";" : "";
			if (type == IdType.Numeric) return srvPart+nsPart+"i="+value;
			if (type == IdType.String) return srvPart+nsPart+"s="+value;
			if (type == IdType.Guid) return srvPart+nsPart+"g="+value;
			if (type == IdType.Opaque) {
				if (value==null) return srvPart+nsPart+"b=null";
				return srvPart+nsPart+"b="+new String( CryptoUtil.base64Encode((byte[])value) );
			}
		} catch (UnsupportedEncodingException e) {
		}
		return "error";
	}
	
	/**
	 * @param s
	 * @param parts
	 * @param n
	 * @throws ClassCastException
	 */
	private static void assertExpandedNodeIdParts(String s, String[] parts, final int n)
			throws ClassCastException {
		if (parts.length < n)
			throwExpandedNodeIdCastException(s);
	}
	
	/**
	 * @param s
	 * @throws ClassCastException
	 */
	private static void throwExpandedNodeIdCastException(String s)
			throws ClassCastException {
		throw new ClassCastException("String is not a valid ExpandedNodeId: "
				+ s);
	}
	
	/**
	 * @param s
	 * @param value
	 * @return
	 */
	public static ExpandedNodeId parseExpandedNodeId(String s) {
		String[] parts = s.split(";");
		assertExpandedNodeIdParts(s, parts, 1);

		int svrIndex = 0;
		int nsIndex = 0;
		NodeId nodeIdValue = NodeId.parseNodeId(parts[parts.length - 1]);
		ExpandedNodeId returnable = null;
		for (int i = 0; i < parts.length - 1; i++) {
			String[] subParts = parts[i].split("=");
			assertExpandedNodeIdParts(s, subParts, 2);
			if (subParts[0].equalsIgnoreCase("svr"))
				svrIndex = Integer.parseInt(subParts[1]);
			else if (subParts[0].equalsIgnoreCase("ns")) {
				nsIndex = Integer.parseInt(subParts[1]);
				returnable = new ExpandedNodeId(
						UnsignedInteger.valueOf(svrIndex), nsIndex,
						nodeIdValue.getValue());
			} else if (subParts[0].equalsIgnoreCase("nsu")) {
				String ns = subParts[1];
				returnable = new ExpandedNodeId(
						UnsignedInteger.valueOf(svrIndex), ns,
						nodeIdValue.getValue());
			} else
				throwExpandedNodeIdCastException(s);
		}
		return returnable;
	}

	/**
	 * Check if nodeId is null or a NullNodeId.
	 * @param nodeId
	 * @return true if (nodeId == null) || nodeId.isNullNodeId()
	 */
	public static boolean isNull(ExpandedNodeId nodeId) {
		return (nodeId == null) || nodeId.isNullNodeId();
	}

	/**
	 * Check if the nodeId refers to a local node, i.e. a node that is in the server's own namespace. 
	 * @return true, if serverIndex == 0 (or null)
	 */
	public boolean isLocal() {
		return (serverIndex == null) || (serverIndex.getValue() == 0);
	}
}
