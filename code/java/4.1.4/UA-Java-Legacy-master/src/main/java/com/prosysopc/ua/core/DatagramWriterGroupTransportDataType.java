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
import com.prosysopc.ua.builtintypes.UnsignedByte;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.ObjectUtils;


public class DatagramWriterGroupTransportDataType extends WriterGroupTransportDataType {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.DatagramWriterGroupTransportDataType.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.DatagramWriterGroupTransportDataType_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.DatagramWriterGroupTransportDataType_Encoding_DefaultXml.getValue());
	
    protected UnsignedByte MessageRepeatCount;
    protected Double MessageRepeatDelay;
    
    public DatagramWriterGroupTransportDataType() {}
    
    public DatagramWriterGroupTransportDataType(UnsignedByte MessageRepeatCount, Double MessageRepeatDelay)
    {
        this.MessageRepeatCount = MessageRepeatCount;
        this.MessageRepeatDelay = MessageRepeatDelay;
    }
    
    public UnsignedByte getMessageRepeatCount()
    {
        return MessageRepeatCount;
    }
    
    public void setMessageRepeatCount(UnsignedByte MessageRepeatCount)
    {
        this.MessageRepeatCount = MessageRepeatCount;
    }
    
    public Double getMessageRepeatDelay()
    {
        return MessageRepeatDelay;
    }
    
    public void setMessageRepeatDelay(Double MessageRepeatDelay)
    {
        this.MessageRepeatDelay = MessageRepeatDelay;
    }
    
    /**
      * Deep clone
      *
      * @return cloned DatagramWriterGroupTransportDataType
      */
    public DatagramWriterGroupTransportDataType clone()
    {
        DatagramWriterGroupTransportDataType result = (DatagramWriterGroupTransportDataType) super.clone();
        result.MessageRepeatCount = MessageRepeatCount;
        result.MessageRepeatDelay = MessageRepeatDelay;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DatagramWriterGroupTransportDataType other = (DatagramWriterGroupTransportDataType) obj;
        if (MessageRepeatCount==null) {
            if (other.MessageRepeatCount != null) return false;
        } else if (!MessageRepeatCount.equals(other.MessageRepeatCount)) return false;
        if (MessageRepeatDelay==null) {
            if (other.MessageRepeatDelay != null) return false;
        } else if (!MessageRepeatDelay.equals(other.MessageRepeatDelay)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((MessageRepeatCount == null) ? 0 : MessageRepeatCount.hashCode());
        result = prime * result
                + ((MessageRepeatDelay == null) ? 0 : MessageRepeatDelay.hashCode());
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
		return "DatagramWriterGroupTransportDataType: "+ ObjectUtils.printFieldsDeep(this);
	}

}