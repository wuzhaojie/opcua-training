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

package org.opcfoundation.ua.encoding;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.Structure;
import org.opcfoundation.ua.common.NamespaceTable;
import org.opcfoundation.ua.common.ServerTable;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.encoding.binary.IEncodeableSerializer;
import org.opcfoundation.ua.utils.StackUtils;

/**
 *
 * 
 */
public class EncoderContext {
	private static EncoderContext defaultInstance = new EncoderContext(NamespaceTable.getDefaultInstance(), null, StackUtils.getDefaultSerializer());

	public static EncoderContext getDefaultInstance() {
		return defaultInstance ;
	}
	
	public Object decode(ExtensionObject[] values) throws DecodingException {
		return decode(values, null);
	}
	
	public Object decode(ExtensionObject[] values, NamespaceTable namespaceTable) throws DecodingException {
		Object value;
		int n = values.length;
		Structure[] newValue = new Structure[n];
		for (int i = 0; i < n; i++) {
			ExtensionObject obj = values[i];
			if (obj != null) newValue[i] = obj.decode(this, namespaceTable);
		}
		value = newValue;
		if (n > 0) {
			Class<? extends Structure> valueClass = null;
			for (int i = 0; i < n; i++)
				if (newValue[i] != null) {
					Class<? extends Structure> newClass = newValue[i]
							.getClass();
					if (valueClass == null)
						valueClass = newClass;
					else if (!newClass.isAssignableFrom(valueClass))
						if (valueClass.isAssignableFrom(newClass))
							valueClass = newClass;
						else {
							valueClass = null;
							break;
						}
				}
			if (valueClass != null)
				value = Arrays.copyOf(newValue, n, ((Structure[]) Array
						.newInstance(valueClass, 0)).getClass());
		}
		return value;
	}

	public NamespaceTable namespaceTable;
	public ServerTable serverTable;
	public IEncodeableSerializer encodeableSerializer;	

	public int maxMessageSize = 4*1024*1024*1024;
	
	// 0 = norestriction
    public int maxStringLength = 0; //UnsignedShort.MAX_VALUE.intValue();
    public int maxByteStringLength = 0; //UnsignedShort.MAX_VALUE.intValue() * 16;
    public int maxArrayLength = 0; //UnsignedShort.MAX_VALUE.intValue();

	public EncoderContext(NamespaceTable namespaceTable, 
			ServerTable serverTable,
			IEncodeableSerializer encodeableSerializer,
			int maxMessageSize) {
		this.encodeableSerializer = encodeableSerializer;
		this.namespaceTable = namespaceTable;
		this.serverTable = serverTable;
		this.maxMessageSize = maxMessageSize;
	}

	public EncoderContext(NamespaceTable namespaceTable,
	                      ServerTable serverTable,
	                      IEncodeableSerializer encodeableSerializer) {
		this.encodeableSerializer = encodeableSerializer;
		this.namespaceTable = namespaceTable;
		this.serverTable = serverTable;
	}

	public int getMaxMessageSize() {
		return maxMessageSize;
	}
	
	public void setMaxMessageSize(int encodeMessageMaxSize) {
		this.maxMessageSize = encodeMessageMaxSize;
	}
	
	public NamespaceTable getNamespaceTable() {
		return namespaceTable;
	}

	public void setNamespaceTable(NamespaceTable namespaceTable) {
		this.namespaceTable = namespaceTable;
	}

	public NodeId getEncodeableNodeId(Class<? extends IEncodeable> clazz, EncodeType type) throws ServiceResultException {
		return namespaceTable.toNodeId(encodeableSerializer.getNodeId(clazz, type));
	}

	public ServerTable getServerTable() {
		return serverTable;
	}

	public void setServerTable(ServerTable serverTable) {
		this.serverTable = serverTable;
	}

	public IEncodeableSerializer getEncodeableSerializer() {
		return encodeableSerializer;
	}

	public Class<? extends IEncodeable> getEncodeableClass(NodeId id) {
		return encodeableSerializer.getClass(namespaceTable.toExpandedNodeId(id));
	}

	public void setEncodeableSerializer(IEncodeableSerializer encodeableSerializer) {
		this.encodeableSerializer = encodeableSerializer;
	}

	public NodeId toNodeId(ExpandedNodeId id) throws EncodingException {
		try {
			return namespaceTable.toNodeId(id);
		} catch (ServiceResultException e) {
			throw new EncodingException("Could not get namespace index for given id");
		}
	}
	
	public int getMaxStringLength() {
		return maxStringLength;
	}

	public void setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
	}

	public int getMaxByteStringLength() {
		return maxByteStringLength;
	}

	public void setMaxByteStringLength(int maxByteStringLength) {
		this.maxByteStringLength = maxByteStringLength;
	}

	public int getMaxArrayLength() {
		return maxArrayLength;
	}

	public void setMaxArrayLength(int maxArrayLength) {
		this.maxArrayLength = maxArrayLength;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("   namespaceTable = "+namespaceTable + "\n");		
		sb.append("   serverTable = "+serverTable + "\n");
		sb.append("   maxMessageSize = "+maxMessageSize + "\n");
		sb.append("   maxStringLength = "+maxStringLength + "\n");
		sb.append("   maxByteStringLength = "+maxByteStringLength + "\n");
		sb.append("   maxArrayLength = "+maxArrayLength + "\n");
		return sb.toString();
	}
}
