/* ========================================================================
 * Copyright (c) 2005-2014 The OPC Foundation, Inc. All rights reserved.
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

package org.opcfoundation.ua.core;

import org.opcfoundation.ua.builtintypes.Structure;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.ObjectUtils;
import java.util.Arrays;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.HistoryUpdateDetails;



public class DeleteEventDetails extends HistoryUpdateDetails implements Structure, Cloneable {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.DeleteEventDetails);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.DeleteEventDetails_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.DeleteEventDetails_Encoding_DefaultXml);
	
    protected byte[][] EventIds;
    
    public DeleteEventDetails() {}
    
    public DeleteEventDetails(NodeId NodeId, byte[][] EventIds)
    {
        super(NodeId);
        this.EventIds = EventIds;
    }
    
    public byte[][] getEventIds()
    {
        return EventIds;
    }
    
    public void setEventIds(byte[][] EventIds)
    {
        this.EventIds = EventIds;
    }
    
    /**
      * Deep clone
      *
      * @return cloned DeleteEventDetails
      */
    public DeleteEventDetails clone()
    {
        DeleteEventDetails result = new DeleteEventDetails();
        result.NodeId = NodeId;
        result.EventIds = EventIds==null ? null : EventIds.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DeleteEventDetails other = (DeleteEventDetails) obj;
        if (NodeId==null) {
            if (other.NodeId != null) return false;
        } else if (!NodeId.equals(other.NodeId)) return false;
        if (EventIds==null) {
            if (other.EventIds != null) return false;
        } else if (!Arrays.equals(EventIds, other.EventIds)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((NodeId == null) ? 0 : NodeId.hashCode());
        result = prime * result
                + ((EventIds == null) ? 0 : Arrays.hashCode(EventIds));
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
		return "DeleteEventDetails: "+ObjectUtils.printFieldsDeep(this);
	}

}
