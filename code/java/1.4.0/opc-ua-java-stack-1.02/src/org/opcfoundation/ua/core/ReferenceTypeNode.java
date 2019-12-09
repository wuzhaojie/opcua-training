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
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReferenceNode;
import org.opcfoundation.ua.core.TypeNode;



public class ReferenceTypeNode extends TypeNode implements Structure, Cloneable {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.ReferenceTypeNode);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.ReferenceTypeNode_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.ReferenceTypeNode_Encoding_DefaultXml);
	
    protected Boolean IsAbstract;
    protected Boolean Symmetric;
    protected LocalizedText InverseName;
    
    public ReferenceTypeNode() {}
    
    public ReferenceTypeNode(NodeId NodeId, NodeClass NodeClass, QualifiedName BrowseName, LocalizedText DisplayName, LocalizedText Description, UnsignedInteger WriteMask, UnsignedInteger UserWriteMask, ReferenceNode[] References, Boolean IsAbstract, Boolean Symmetric, LocalizedText InverseName)
    {
        super(NodeId, NodeClass, BrowseName, DisplayName, Description, WriteMask, UserWriteMask, References);
        this.IsAbstract = IsAbstract;
        this.Symmetric = Symmetric;
        this.InverseName = InverseName;
    }
    
    public Boolean getIsAbstract()
    {
        return IsAbstract;
    }
    
    public void setIsAbstract(Boolean IsAbstract)
    {
        this.IsAbstract = IsAbstract;
    }
    
    public Boolean getSymmetric()
    {
        return Symmetric;
    }
    
    public void setSymmetric(Boolean Symmetric)
    {
        this.Symmetric = Symmetric;
    }
    
    public LocalizedText getInverseName()
    {
        return InverseName;
    }
    
    public void setInverseName(LocalizedText InverseName)
    {
        this.InverseName = InverseName;
    }
    
    /**
      * Deep clone
      *
      * @return cloned ReferenceTypeNode
      */
    public ReferenceTypeNode clone()
    {
        ReferenceTypeNode result = new ReferenceTypeNode();
        result.NodeId = NodeId;
        result.NodeClass = NodeClass;
        result.BrowseName = BrowseName;
        result.DisplayName = DisplayName;
        result.Description = Description;
        result.WriteMask = WriteMask;
        result.UserWriteMask = UserWriteMask;
        if (References!=null) {
            result.References = new ReferenceNode[References.length];
            for (int i=0; i<References.length; i++)
                result.References[i] = References[i].clone();
        }
        result.IsAbstract = IsAbstract;
        result.Symmetric = Symmetric;
        result.InverseName = InverseName;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ReferenceTypeNode other = (ReferenceTypeNode) obj;
        if (NodeId==null) {
            if (other.NodeId != null) return false;
        } else if (!NodeId.equals(other.NodeId)) return false;
        if (NodeClass==null) {
            if (other.NodeClass != null) return false;
        } else if (!NodeClass.equals(other.NodeClass)) return false;
        if (BrowseName==null) {
            if (other.BrowseName != null) return false;
        } else if (!BrowseName.equals(other.BrowseName)) return false;
        if (DisplayName==null) {
            if (other.DisplayName != null) return false;
        } else if (!DisplayName.equals(other.DisplayName)) return false;
        if (Description==null) {
            if (other.Description != null) return false;
        } else if (!Description.equals(other.Description)) return false;
        if (WriteMask==null) {
            if (other.WriteMask != null) return false;
        } else if (!WriteMask.equals(other.WriteMask)) return false;
        if (UserWriteMask==null) {
            if (other.UserWriteMask != null) return false;
        } else if (!UserWriteMask.equals(other.UserWriteMask)) return false;
        if (References==null) {
            if (other.References != null) return false;
        } else if (!Arrays.equals(References, other.References)) return false;
        if (IsAbstract==null) {
            if (other.IsAbstract != null) return false;
        } else if (!IsAbstract.equals(other.IsAbstract)) return false;
        if (Symmetric==null) {
            if (other.Symmetric != null) return false;
        } else if (!Symmetric.equals(other.Symmetric)) return false;
        if (InverseName==null) {
            if (other.InverseName != null) return false;
        } else if (!InverseName.equals(other.InverseName)) return false;
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
                + ((NodeClass == null) ? 0 : NodeClass.hashCode());
        result = prime * result
                + ((BrowseName == null) ? 0 : BrowseName.hashCode());
        result = prime * result
                + ((DisplayName == null) ? 0 : DisplayName.hashCode());
        result = prime * result
                + ((Description == null) ? 0 : Description.hashCode());
        result = prime * result
                + ((WriteMask == null) ? 0 : WriteMask.hashCode());
        result = prime * result
                + ((UserWriteMask == null) ? 0 : UserWriteMask.hashCode());
        result = prime * result
                + ((References == null) ? 0 : Arrays.hashCode(References));
        result = prime * result
                + ((IsAbstract == null) ? 0 : IsAbstract.hashCode());
        result = prime * result
                + ((Symmetric == null) ? 0 : Symmetric.hashCode());
        result = prime * result
                + ((InverseName == null) ? 0 : InverseName.hashCode());
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
		return "ReferenceTypeNode: "+ObjectUtils.printFieldsDeep(this);
	}

}
