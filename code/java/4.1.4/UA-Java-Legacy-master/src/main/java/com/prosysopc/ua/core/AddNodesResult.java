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
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.StatusCode;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.AbstractStructure;
import com.prosysopc.ua.utils.ObjectUtils;


public class AddNodesResult extends AbstractStructure {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.AddNodesResult.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.AddNodesResult_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.AddNodesResult_Encoding_DefaultXml.getValue());
	
    protected com.prosysopc.ua.builtintypes.StatusCode StatusCode;
    protected NodeId AddedNodeId;
    
    public AddNodesResult() {}
    
    public AddNodesResult(StatusCode StatusCode, NodeId AddedNodeId)
    {
        this.StatusCode = StatusCode;
        this.AddedNodeId = AddedNodeId;
    }
    
    public StatusCode getStatusCode()
    {
        return StatusCode;
    }
    
    public void setStatusCode(StatusCode StatusCode)
    {
        this.StatusCode = StatusCode;
    }
    
    public NodeId getAddedNodeId()
    {
        return AddedNodeId;
    }
    
    public void setAddedNodeId(NodeId AddedNodeId)
    {
        this.AddedNodeId = AddedNodeId;
    }
    
    /**
      * Deep clone
      *
      * @return cloned AddNodesResult
      */
    public AddNodesResult clone()
    {
        AddNodesResult result = (AddNodesResult) super.clone();
        result.StatusCode = StatusCode;
        result.AddedNodeId = AddedNodeId;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AddNodesResult other = (AddNodesResult) obj;
        if (StatusCode==null) {
            if (other.StatusCode != null) return false;
        } else if (!StatusCode.equals(other.StatusCode)) return false;
        if (AddedNodeId==null) {
            if (other.AddedNodeId != null) return false;
        } else if (!AddedNodeId.equals(other.AddedNodeId)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((StatusCode == null) ? 0 : StatusCode.hashCode());
        result = prime * result
                + ((AddedNodeId == null) ? 0 : AddedNodeId.hashCode());
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
		return "AddNodesResult: "+ ObjectUtils.printFieldsDeep(this);
	}

}
