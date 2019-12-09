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
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.ObjectUtils;

import java.util.Arrays;


public class EnumDefinition extends DataTypeDefinition {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.EnumDefinition.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.EnumDefinition_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.EnumDefinition_Encoding_DefaultXml.getValue());
	
    protected EnumField[] Fields;
    
    public EnumDefinition() {}
    
    public EnumDefinition(EnumField[] Fields)
    {
        this.Fields = Fields;
    }
    
    public EnumField[] getFields()
    {
        return Fields;
    }
    
    public void setFields(EnumField[] Fields)
    {
        this.Fields = Fields;
    }
    
    /**
      * Deep clone
      *
      * @return cloned EnumDefinition
      */
    public EnumDefinition clone()
    {
        EnumDefinition result = (EnumDefinition) super.clone();
        if (Fields!=null) {
            result.Fields = new EnumField[Fields.length];
            for (int i=0; i<Fields.length; i++)
                result.Fields[i] = Fields[i].clone();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EnumDefinition other = (EnumDefinition) obj;
        if (Fields==null) {
            if (other.Fields != null) return false;
        } else if (!Arrays.equals(Fields, other.Fields)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((Fields == null) ? 0 : Arrays.hashCode(Fields));
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
		return "EnumDefinition: "+ ObjectUtils.printFieldsDeep(this);
	}

}
