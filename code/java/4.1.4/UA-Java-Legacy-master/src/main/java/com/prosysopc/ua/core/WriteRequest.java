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
import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.utils.AbstractStructure;
import com.prosysopc.ua.utils.ObjectUtils;
import java.util.Arrays;


public class WriteRequest extends AbstractStructure implements ServiceRequest {

	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.WriteRequest);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.WriteRequest_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.WriteRequest_Encoding_DefaultXml);
	
    protected RequestHeader RequestHeader;
    protected WriteValue[] NodesToWrite;
    
    public WriteRequest() {}
    
    public WriteRequest(RequestHeader RequestHeader, WriteValue[] NodesToWrite)
    {
        this.RequestHeader = RequestHeader;
        this.NodesToWrite = NodesToWrite;
    }
    
    public RequestHeader getRequestHeader()
    {
        return RequestHeader;
    }
    
    public void setRequestHeader(RequestHeader RequestHeader)
    {
        this.RequestHeader = RequestHeader;
    }
    
    public WriteValue[] getNodesToWrite()
    {
        return NodesToWrite;
    }
    
    public void setNodesToWrite(WriteValue[] NodesToWrite)
    {
        this.NodesToWrite = NodesToWrite;
    }
    
    /**
      * Deep clone
      *
      * @return cloned WriteRequest
      */
    public WriteRequest clone()
    {
        WriteRequest result = (WriteRequest) super.clone();
        result.RequestHeader = RequestHeader==null ? null : RequestHeader.clone();
        if (NodesToWrite!=null) {
            result.NodesToWrite = new WriteValue[NodesToWrite.length];
            for (int i=0; i<NodesToWrite.length; i++)
                result.NodesToWrite[i] = NodesToWrite[i].clone();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        WriteRequest other = (WriteRequest) obj;
        if (RequestHeader==null) {
            if (other.RequestHeader != null) return false;
        } else if (!RequestHeader.equals(other.RequestHeader)) return false;
        if (NodesToWrite==null) {
            if (other.NodesToWrite != null) return false;
        } else if (!Arrays.equals(NodesToWrite, other.NodesToWrite)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((RequestHeader == null) ? 0 : RequestHeader.hashCode());
        result = prime * result
                + ((NodesToWrite == null) ? 0 : Arrays.hashCode(NodesToWrite));
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
		return ObjectUtils.printFieldsDeep(this);
	}
	
}
