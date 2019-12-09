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

import java.util.Arrays;


public class UadpWriterGroupMessageDataType extends WriterGroupMessageDataType {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UadpWriterGroupMessageDataType.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UadpWriterGroupMessageDataType_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.UadpWriterGroupMessageDataType_Encoding_DefaultXml.getValue());
	
    protected UnsignedInteger GroupVersion;
    protected DataSetOrderingType DataSetOrdering;
    protected UnsignedInteger NetworkMessageContentMask;
    protected Double SamplingOffset;
    protected Double[] PublishingOffset;
    
    public UadpWriterGroupMessageDataType() {}
    
    public UadpWriterGroupMessageDataType(UnsignedInteger GroupVersion, DataSetOrderingType DataSetOrdering, UnsignedInteger NetworkMessageContentMask, Double SamplingOffset, Double[] PublishingOffset)
    {
        this.GroupVersion = GroupVersion;
        this.DataSetOrdering = DataSetOrdering;
        this.NetworkMessageContentMask = NetworkMessageContentMask;
        this.SamplingOffset = SamplingOffset;
        this.PublishingOffset = PublishingOffset;
    }
    
    public UnsignedInteger getGroupVersion()
    {
        return GroupVersion;
    }
    
    public void setGroupVersion(UnsignedInteger GroupVersion)
    {
        this.GroupVersion = GroupVersion;
    }
    
    public DataSetOrderingType getDataSetOrdering()
    {
        return DataSetOrdering;
    }
    
    public void setDataSetOrdering(DataSetOrderingType DataSetOrdering)
    {
        this.DataSetOrdering = DataSetOrdering;
    }
    
    public UnsignedInteger getNetworkMessageContentMask()
    {
        return NetworkMessageContentMask;
    }
    
    public void setNetworkMessageContentMask(UnsignedInteger NetworkMessageContentMask)
    {
        this.NetworkMessageContentMask = NetworkMessageContentMask;
    }
    
    public Double getSamplingOffset()
    {
        return SamplingOffset;
    }
    
    public void setSamplingOffset(Double SamplingOffset)
    {
        this.SamplingOffset = SamplingOffset;
    }
    
    public Double[] getPublishingOffset()
    {
        return PublishingOffset;
    }
    
    public void setPublishingOffset(Double[] PublishingOffset)
    {
        this.PublishingOffset = PublishingOffset;
    }
    
    /**
      * Deep clone
      *
      * @return cloned UadpWriterGroupMessageDataType
      */
    public UadpWriterGroupMessageDataType clone()
    {
        UadpWriterGroupMessageDataType result = (UadpWriterGroupMessageDataType) super.clone();
        result.GroupVersion = GroupVersion;
        result.DataSetOrdering = DataSetOrdering;
        result.NetworkMessageContentMask = NetworkMessageContentMask;
        result.SamplingOffset = SamplingOffset;
        result.PublishingOffset = PublishingOffset==null ? null : PublishingOffset.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UadpWriterGroupMessageDataType other = (UadpWriterGroupMessageDataType) obj;
        if (GroupVersion==null) {
            if (other.GroupVersion != null) return false;
        } else if (!GroupVersion.equals(other.GroupVersion)) return false;
        if (DataSetOrdering==null) {
            if (other.DataSetOrdering != null) return false;
        } else if (!DataSetOrdering.equals(other.DataSetOrdering)) return false;
        if (NetworkMessageContentMask==null) {
            if (other.NetworkMessageContentMask != null) return false;
        } else if (!NetworkMessageContentMask.equals(other.NetworkMessageContentMask)) return false;
        if (SamplingOffset==null) {
            if (other.SamplingOffset != null) return false;
        } else if (!SamplingOffset.equals(other.SamplingOffset)) return false;
        if (PublishingOffset==null) {
            if (other.PublishingOffset != null) return false;
        } else if (!Arrays.equals(PublishingOffset, other.PublishingOffset)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((GroupVersion == null) ? 0 : GroupVersion.hashCode());
        result = prime * result
                + ((DataSetOrdering == null) ? 0 : DataSetOrdering.hashCode());
        result = prime * result
                + ((NetworkMessageContentMask == null) ? 0 : NetworkMessageContentMask.hashCode());
        result = prime * result
                + ((SamplingOffset == null) ? 0 : SamplingOffset.hashCode());
        result = prime * result
                + ((PublishingOffset == null) ? 0 : Arrays.hashCode(PublishingOffset));
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
		return "UadpWriterGroupMessageDataType: "+ ObjectUtils.printFieldsDeep(this);
	}

}
