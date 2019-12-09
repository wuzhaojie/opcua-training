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



public class SignatureData extends Object implements Structure, Cloneable {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.SignatureData);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.SignatureData_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.SignatureData_Encoding_DefaultXml);
	
    protected String Algorithm;
    protected byte[] Signature;
    
    public SignatureData() {}
    
    public SignatureData(String Algorithm, byte[] Signature)
    {
        this.Algorithm = Algorithm;
        this.Signature = Signature;
    }
    
    public String getAlgorithm()
    {
        return Algorithm;
    }
    
    public void setAlgorithm(String Algorithm)
    {
        this.Algorithm = Algorithm;
    }
    
    public byte[] getSignature()
    {
        return Signature;
    }
    
    public void setSignature(byte[] Signature)
    {
        this.Signature = Signature;
    }
    
    /**
      * Deep clone
      *
      * @return cloned SignatureData
      */
    public SignatureData clone()
    {
        SignatureData result = new SignatureData();
        result.Algorithm = Algorithm;
        result.Signature = Signature;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SignatureData other = (SignatureData) obj;
        if (Algorithm==null) {
            if (other.Algorithm != null) return false;
        } else if (!Algorithm.equals(other.Algorithm)) return false;
        if (Signature==null) {
            if (other.Signature != null) return false;
        } else if (!Signature.equals(other.Signature)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((Algorithm == null) ? 0 : Algorithm.hashCode());
        result = prime * result
                + ((Signature == null) ? 0 : Signature.hashCode());
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
		return "SignatureData: "+ObjectUtils.printFieldsDeep(this);
	}

}
