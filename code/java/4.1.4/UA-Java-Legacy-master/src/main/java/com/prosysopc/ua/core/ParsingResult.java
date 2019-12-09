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

import com.prosysopc.ua.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.builtintypes.StatusCode;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.AbstractStructure;
import com.prosysopc.ua.utils.ObjectUtils;

import java.util.Arrays;


public class ParsingResult extends AbstractStructure {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ParsingResult.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ParsingResult_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ParsingResult_Encoding_DefaultXml.getValue());
	
    protected com.prosysopc.ua.builtintypes.StatusCode StatusCode;
    protected StatusCode[] DataStatusCodes;
    protected DiagnosticInfo[] DataDiagnosticInfos;
    
    public ParsingResult() {}
    
    public ParsingResult(StatusCode StatusCode, StatusCode[] DataStatusCodes, DiagnosticInfo[] DataDiagnosticInfos)
    {
        this.StatusCode = StatusCode;
        this.DataStatusCodes = DataStatusCodes;
        this.DataDiagnosticInfos = DataDiagnosticInfos;
    }
    
    public StatusCode getStatusCode()
    {
        return StatusCode;
    }
    
    public void setStatusCode(StatusCode StatusCode)
    {
        this.StatusCode = StatusCode;
    }
    
    public StatusCode[] getDataStatusCodes()
    {
        return DataStatusCodes;
    }
    
    public void setDataStatusCodes(StatusCode[] DataStatusCodes)
    {
        this.DataStatusCodes = DataStatusCodes;
    }
    
    public DiagnosticInfo[] getDataDiagnosticInfos()
    {
        return DataDiagnosticInfos;
    }
    
    public void setDataDiagnosticInfos(DiagnosticInfo[] DataDiagnosticInfos)
    {
        this.DataDiagnosticInfos = DataDiagnosticInfos;
    }
    
    /**
      * Deep clone
      *
      * @return cloned ParsingResult
      */
    public ParsingResult clone()
    {
        ParsingResult result = (ParsingResult) super.clone();
        result.StatusCode = StatusCode;
        result.DataStatusCodes = DataStatusCodes==null ? null : DataStatusCodes.clone();
        result.DataDiagnosticInfos = DataDiagnosticInfos==null ? null : DataDiagnosticInfos.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ParsingResult other = (ParsingResult) obj;
        if (StatusCode==null) {
            if (other.StatusCode != null) return false;
        } else if (!StatusCode.equals(other.StatusCode)) return false;
        if (DataStatusCodes==null) {
            if (other.DataStatusCodes != null) return false;
        } else if (!Arrays.equals(DataStatusCodes, other.DataStatusCodes)) return false;
        if (DataDiagnosticInfos==null) {
            if (other.DataDiagnosticInfos != null) return false;
        } else if (!Arrays.equals(DataDiagnosticInfos, other.DataDiagnosticInfos)) return false;
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
                + ((DataStatusCodes == null) ? 0 : Arrays.hashCode(DataStatusCodes));
        result = prime * result
                + ((DataDiagnosticInfos == null) ? 0 : Arrays.hashCode(DataDiagnosticInfos));
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
		return "ParsingResult: "+ ObjectUtils.printFieldsDeep(this);
	}

}
