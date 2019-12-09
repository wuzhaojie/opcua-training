/* ========================================================================
 * Copyright (c) 2005-2015 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================*/

package com.prosysopc.ua.core;

import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.ObjectUtils;


public class JsonDataSetWriterMessageDataType extends DataSetWriterMessageDataType {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.JsonDataSetWriterMessageDataType.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.JsonDataSetWriterMessageDataType_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.JsonDataSetWriterMessageDataType_Encoding_DefaultXml.getValue());
	
    protected UnsignedInteger NetworkMessageContentMask;
    protected UnsignedInteger DataSetMessageContentMask;
    
    public JsonDataSetWriterMessageDataType() {}
    
    public JsonDataSetWriterMessageDataType(UnsignedInteger NetworkMessageContentMask, UnsignedInteger DataSetMessageContentMask)
    {
        this.NetworkMessageContentMask = NetworkMessageContentMask;
        this.DataSetMessageContentMask = DataSetMessageContentMask;
    }
    
    public UnsignedInteger getNetworkMessageContentMask()
    {
        return NetworkMessageContentMask;
    }
    
    public void setNetworkMessageContentMask(UnsignedInteger NetworkMessageContentMask)
    {
        this.NetworkMessageContentMask = NetworkMessageContentMask;
    }
    
    public UnsignedInteger getDataSetMessageContentMask()
    {
        return DataSetMessageContentMask;
    }
    
    public void setDataSetMessageContentMask(UnsignedInteger DataSetMessageContentMask)
    {
        this.DataSetMessageContentMask = DataSetMessageContentMask;
    }
    
    /**
      * Deep clone
      *
      * @return cloned JsonDataSetWriterMessageDataType
      */
    public JsonDataSetWriterMessageDataType clone()
    {
        JsonDataSetWriterMessageDataType result = (JsonDataSetWriterMessageDataType) super.clone();
        result.NetworkMessageContentMask = NetworkMessageContentMask;
        result.DataSetMessageContentMask = DataSetMessageContentMask;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        JsonDataSetWriterMessageDataType other = (JsonDataSetWriterMessageDataType) obj;
        if (NetworkMessageContentMask==null) {
            if (other.NetworkMessageContentMask != null) return false;
        } else if (!NetworkMessageContentMask.equals(other.NetworkMessageContentMask)) return false;
        if (DataSetMessageContentMask==null) {
            if (other.DataSetMessageContentMask != null) return false;
        } else if (!DataSetMessageContentMask.equals(other.DataSetMessageContentMask)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((NetworkMessageContentMask == null) ? 0 : NetworkMessageContentMask.hashCode());
        result = prime * result
                + ((DataSetMessageContentMask == null) ? 0 : DataSetMessageContentMask.hashCode());
        return result;
    }
    


	public ExpandedNodeId getTypeId() {
		return ID;
	}

	public ExpandedNodeId getXmlEncodeId() {
		return XML;
	}

	public ExpandedNodeId getBinaryEncodeId() {
		return BINARY;
	}
	
	public String toString() {
		return "JsonDataSetWriterMessageDataType: "+ ObjectUtils.printFieldsDeep(this);
	}

}
