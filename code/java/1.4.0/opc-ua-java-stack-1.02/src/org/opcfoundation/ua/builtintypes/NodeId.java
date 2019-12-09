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

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opcfoundation.ua.common.NamespaceTable;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.CryptoUtil;


/**
 * An identifier of a node in the address space of an OPC UA server.
 * The class Id is immutable, and hash-equals-comparable with NodeIds and ExpandedNodeIds 
 * with a NamespaceIndex and no ServerIndex.  
 * If the given object is byte[] it must not be changed.  
 * <p>
 * NodeIds are equals comparable with ExpandedNodeIds that are constructed with NamespaceIndex
 * and no ServerIndex.
 * 
 * @see ExpandedNodeId An identifier optional ServerIndex and/or explicit NamespaceUri
 * @see NamespaceTable 
 */
public final class NodeId implements Comparable<NodeId> {

	public static final NodeId ZERO = new NodeId(0, UnsignedInteger.getFromBits(0));
	
	/** Considered null node id */	
	public static final NodeId NULL_NUMERIC = new NodeId(0, UnsignedInteger.getFromBits(0));
	public static final NodeId NULL_STRING = NodeId.get(IdType.String, 0, "");
	public static final NodeId NULL_GUID = NodeId.get(IdType.Guid, 0, new UUID(0, 0));
	public static final NodeId NULL_OPAQUE = NodeId.get(IdType.Opaque, 0, new byte[0]);
	public static final NodeId NULL = NULL_NUMERIC;
	
	/** Identifier of "NodeId" in UA AddressSpace */
	public static final NodeId ID = Identifiers.NodeId;

	
	IdType type;
	final int namespaceIndex;
	final Object value;

	public static NodeId get(IdType type, int namespaceIndex, Object value)
	{
		if (type==IdType.Guid) return new NodeId(namespaceIndex, (UUID)value);
		if (type==IdType.Numeric) return new NodeId(namespaceIndex, (UnsignedInteger)value);
		if (type==IdType.Opaque) return new NodeId(namespaceIndex, (byte[])value);
		if (type==IdType.String) return new NodeId(namespaceIndex, (String)value);
		throw new IllegalArgumentException("bad type");
	}
	
	/**
	 * Create new NodeId
	 * 
	 * @param namespaceIndex 0..65535
	 * @param value the value should be a positive integer, as it is converted to an UnsignedInteger
	 */
	public NodeId(int namespaceIndex, int value)
	{
		this(namespaceIndex, UnsignedInteger.getFromBits(value));
	}
	
	/**
	 * Create new NodeId
	 * 
	 * @param namespaceIndex 0..65535
	 * @param value UnsignedInteger
	 */
	public NodeId(int namespaceIndex, UnsignedInteger value)
	{
		if (value==null) throw new IllegalArgumentException("Numeric NodeId cannot be null");
		if (namespaceIndex<0 || namespaceIndex>65535) 
			throw new IllegalArgumentException("namespaceIndex out of bounds");		
		this.value = value;
		this.namespaceIndex = namespaceIndex;
		type = IdType.Numeric;
	}
	
	/**
	 * Create new NodeId
	 * 
	 * @param namespaceIndex 0..65535
	 * @param value String or null
	 */
	public NodeId(int namespaceIndex, String value)
	{
		if (namespaceIndex<0 || namespaceIndex>65535) 
			throw new IllegalArgumentException("namespaceIndex out of bounds");		
		if (value!=null && value.length()>4096) throw new IllegalArgumentException("The length is restricted to 4096 characters");
		type = IdType.String;
		this.value = value;
		this.namespaceIndex = namespaceIndex;
	}
	
	/**
	 * Create new NodeId
	 * 
	 * @param namespaceIndex 0..65535
	 * @param value GUID value
	 */
	public NodeId(int namespaceIndex, UUID value)
	{
		if (namespaceIndex<0 || namespaceIndex>65535) 
			throw new IllegalArgumentException("namespaceIndex out of bounds");
		if (value==null) throw new IllegalArgumentException("Numeric NodeId cannot be null");
		type = IdType.Guid;
		this.value = value;
		this.namespaceIndex = namespaceIndex;
	}
	
	/**
	 * Create new NodeId from byte[]. value must not be modified after it has been
	 * contributed to the node id because precalculated hash code will be ruined.  
	 * 
	 * @param namespaceIndex 0..65535
	 * @param value byte[] or null
	 */
	public NodeId(int namespaceIndex, byte[] value)
	{
		if (namespaceIndex<0 || namespaceIndex>65535) 
			throw new IllegalArgumentException("namespaceIndex out of bounds");
		if (value!=null && value.length>4096) throw new IllegalArgumentException("The length is restricted to 4096 bytes");
		type = IdType.Opaque;
		this.value = value;
		this.namespaceIndex = namespaceIndex;
	}	
	
	/**
	 * Whether the object represents a Null NodeId.
	 * @return Whether the object represents a Null NodeId.
	 */
	public boolean isNullNodeId() {
		if (this.value == null)
			return true;
		if (this.namespaceIndex != 0)
			return false;
		// Note: equals checks for IsNull, so we cannot use equals
		switch (this.type) {
		case Numeric:
			return ((UnsignedInteger) this.value).intValue() == 0;
		case String:
			return ((String)this.value).length() == 0;
		case Guid:
			return this.value.equals(NULL_GUID.value);
		case Opaque:
			return java.util.Arrays.equals((byte[])this.value, (byte[])NULL_OPAQUE.value);
		default:
			return false;
		}
	}
	
	/**
	 * Check if nodeId is null or a NullNodeId.
	 * @param nodeId
	 * @return true if (nodeId == null) || nodeId.isNullNodeId()
	 */
	public static boolean isNull(NodeId nodeId) {
		return (nodeId == null) || nodeId.isNullNodeId();
	}
	
	public IdType getIdType()
	{
		return type;
	}
	
	public int getNamespaceIndex()
	{
		return namespaceIndex;
	}
	
	/**
	 * 
	 * @return the value, UnsignedInteger, UUID, String, byte[], or null (null opaque) 
	 */
	public Object getValue()
	{
		return value;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 13*namespaceIndex;
		if (value != null)
			if (value instanceof byte[])
				hashCode += 3 * Arrays.hashCode((byte[]) value);
			else
				hashCode += 3 * value.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return isNull(this);
		if (obj instanceof NodeId) {
			NodeId other = (NodeId) obj;
			if (isNull(this) || isNull(other)) return isNull(this) == isNull(other); //handle null
			if (other.namespaceIndex!=namespaceIndex || other.type!=type) return false;
			if (this.value==other.value) return true;
			if (other.type==IdType.Opaque) 
				// Deep compare
				return Arrays.equals((byte[])value, (byte[])other.value);			
			return other.value.equals(value);
		} else
		if (obj instanceof ExpandedNodeId) {
			ExpandedNodeId other = (ExpandedNodeId) obj;
			if ((other.namespaceUri!=null && other.namespaceUri != NamespaceTable.OPCUA_NAMESPACE) || !other.isLocal()) return false;
			if (this.namespaceIndex!=other.namespaceIndex || this.type!=other.type) return false;
			if (this.value==other.value) return true;
			if (this.type==IdType.Opaque) 
				// Deep compare
				return Arrays.equals((byte[])other.value, (byte[])this.value);
			return this.value.equals(other.value);
		} else
		return false;
	}
	
	@Override
	public int compareTo(NodeId other) {
		int value = namespaceIndex - other.namespaceIndex;
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
				// TODO: does not keep order
				value = Arrays
						.equals((byte[]) this.value, (byte[]) other.value) ? 0
						: 1;
				break;
			}
		return value;
	}	
	
	@Override
	public String toString() {
		String nsPart = namespaceIndex>0 ? "ns="+namespaceIndex+";" : "";
		if (type == IdType.Numeric) return nsPart+"i="+value;
		if (type == IdType.String) return nsPart+"s="+value;
		if (type == IdType.Guid) return nsPart+"g="+value;
		if (type == IdType.Opaque) {
			if (value==null) return nsPart+"b=null";
			return nsPart+"b="+new String( CryptoUtil.base64Encode((byte[])value) );
		}
		return "error";
	}
	
	/**
	 * Convert String representation to NodeId.
	 * If the namespace is expressed with URI, then the
	 * <t>namespaceTable</t> field is required.
	 * 
	 * The String representation is in the following notations:
	 *  ns=[id];i=[number]
	 *  i=[number]
	 *  
	 *  ns=[id];s=[string]
	 *  s=[string]
	 *  
	 *  ns=[id];g=[guid]
	 *  g=[guid]
	 *  	
	 *  ns=[id];b=[base64]
	 *  b=[base64]
	 * 
	 * @param nodeIdRef
	 * @return nodeid
	 * @throws IllegalArgumentException
	 * @throws NamespaceNotFoundException
	 * @deprecated Use parseNodeId() instead (renamed for method name consistency with other similar classes)
	 */
	@Deprecated
	public static NodeId decode(String nodeIdRef)
	throws IllegalArgumentException
	{
		return parseNodeId(nodeIdRef);
	}
	
	
	/**
	 * Convert String representation to NodeId.
	 * If the namespace is expressed with URI, then the
	 * <t>namespaceTable</t> field is required.
	 * 
	 * The String representation is in the following notations:
	 *  ns=[id];i=[number]
	 *  i=[number]
	 *  
	 *  ns=[id];s=[string]
	 *  s=[string]
	 *  
	 *  ns=[id];g=[guid]
	 *  g=[guid]
	 *  	
	 *  ns=[id];b=[base64]
	 *  b=[base64]
	 * 
	 * @param nodeIdRef
	 * @return nodeid
	 * @throws IllegalArgumentException
	 * @throws NamespaceNotFoundException
	 */
	public static NodeId parseNodeId(String nodeIdRef)
	throws IllegalArgumentException
	{
		if (nodeIdRef==null) throw new IllegalArgumentException("null arg");
		
		Matcher m;
		
		m = NONE_STRING.matcher(nodeIdRef);
		if (m.matches()) {			
			String obj = m.group(1);
			return new NodeId(0, obj);
		}
		
		m = NONE_INT.matcher(nodeIdRef);
		if (m.matches()) {			
			Integer obj = Integer.valueOf( m.group(1) );
			return new NodeId(0, obj);
		}
		
		m = NONE_GUID.matcher(nodeIdRef);
		if (m.matches()) {			
			UUID obj = UUID.fromString( m.group(1) );
			return new NodeId(0, obj);
		}
		
		m = NONE_OPAQUE.matcher(nodeIdRef);
		if (m.matches()) {			
			byte[] obj = CryptoUtil.base64Decode( m.group(1) );
			return new NodeId(0, obj);
		}		
		

		m = INT_INT.matcher(nodeIdRef);
		if (m.matches()) {			
			Integer nsi = Integer.valueOf( m.group(1) );
			Integer obj = Integer.valueOf( m.group(2) );
			return new NodeId(nsi, obj);
		}

		m = INT_STRING.matcher(nodeIdRef);
		if (m.matches()) {			
			Integer nsi = Integer.valueOf( m.group(1) );
			String obj = m.group(2);
			return new NodeId(nsi, obj);
		}
		
		m = INT_GUID.matcher(nodeIdRef);
		if (m.matches()) {			
			Integer nsi = Integer.valueOf( m.group(1) );
			UUID obj = UUID.fromString( m.group(2) );
			return new NodeId(nsi, obj);
		}
		
		m = INT_OPAQUE.matcher(nodeIdRef);
		if (m.matches()) {			
			Integer nsi = Integer.valueOf( m.group(1) );
			byte[] obj = CryptoUtil.base64Decode( m.group(2) );
			return new NodeId(nsi, obj);
		}
		
		throw new IllegalArgumentException("Invalid string representation of a nodeId: " + nodeIdRef);
	}

	static final Pattern INT_INT = Pattern.compile("ns=(\\d*);i=(\\d*)");	
	static final Pattern NONE_INT = Pattern.compile("i=(\\d*)");	

	static final Pattern INT_STRING = Pattern.compile("ns=(\\d*);s=(.*)");	
	static final Pattern NONE_STRING = Pattern.compile("s=(.*)");	
	
	static final Pattern INT_GUID = Pattern.compile("ns=(\\d*);g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");	
	static final Pattern NONE_GUID = Pattern.compile("g=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");	

	static final Pattern INT_OPAQUE = Pattern.compile("ns=(\\d*);b=([0-9a-zA-Z\\+/=]*)");	
	static final Pattern NONE_OPAQUE = Pattern.compile("b=([0-9a-zA-Z\\+/=]*)");

	/**
	 * Create a new random NodeId. Because GUID values are always unique, this method also always returns a unique NodeId. 
	 * @param namespaceIndex
	 * @return a new NodeId initialized with a random GUID.
	 */
	public static NodeId randomGUID(int namespaceIndex) {
		// TODO Auto-generated method stub
		return new NodeId(namespaceIndex,UUID.randomUUID());
	}	
	
	public static boolean equals(NodeId left, NodeId right){
		if(left == null && right != null){
			return false;
		}else{
			return left.equals(right);
		}
	}
}
