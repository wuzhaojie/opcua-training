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
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.core.FilterOperator;



public class ContentFilterElement extends Object implements Structure, Cloneable {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.ContentFilterElement);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.ContentFilterElement_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.ContentFilterElement_Encoding_DefaultXml);
	
    protected FilterOperator FilterOperator;
    protected ExtensionObject[] FilterOperands;
    
    public ContentFilterElement() {}
    
    public ContentFilterElement(FilterOperator FilterOperator, ExtensionObject[] FilterOperands)
    {
        this.FilterOperator = FilterOperator;
        this.FilterOperands = FilterOperands;
    }
    
    public FilterOperator getFilterOperator()
    {
        return FilterOperator;
    }
    
    public void setFilterOperator(FilterOperator FilterOperator)
    {
        this.FilterOperator = FilterOperator;
    }
    
    public ExtensionObject[] getFilterOperands()
    {
        return FilterOperands;
    }
    
    public void setFilterOperands(ExtensionObject[] FilterOperands)
    {
        this.FilterOperands = FilterOperands;
    }
    
    /**
      * Deep clone
      *
      * @return cloned ContentFilterElement
      */
    public ContentFilterElement clone()
    {
        ContentFilterElement result = new ContentFilterElement();
        result.FilterOperator = FilterOperator;
        result.FilterOperands = FilterOperands==null ? null : FilterOperands.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ContentFilterElement other = (ContentFilterElement) obj;
        if (FilterOperator==null) {
            if (other.FilterOperator != null) return false;
        } else if (!FilterOperator.equals(other.FilterOperator)) return false;
        if (FilterOperands==null) {
            if (other.FilterOperands != null) return false;
        } else if (!Arrays.equals(FilterOperands, other.FilterOperands)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((FilterOperator == null) ? 0 : FilterOperator.hashCode());
        result = prime * result
                + ((FilterOperands == null) ? 0 : Arrays.hashCode(FilterOperands));
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
		return "ContentFilterElement: "+ObjectUtils.printFieldsDeep(this);
	}

}
