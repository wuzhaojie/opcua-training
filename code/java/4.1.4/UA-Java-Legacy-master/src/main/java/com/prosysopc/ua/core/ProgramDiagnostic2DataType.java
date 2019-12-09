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

import com.prosysopc.ua.builtintypes.DateTime;
import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.Variant;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.utils.AbstractStructure;
import com.prosysopc.ua.utils.ObjectUtils;

import java.util.Arrays;


public class ProgramDiagnostic2DataType extends AbstractStructure {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ProgramDiagnostic2DataType.getValue());
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ProgramDiagnostic2DataType_Encoding_DefaultBinary.getValue());
	public static final ExpandedNodeId XML = new ExpandedNodeId(null, NamespaceTable.OPCUA_NAMESPACE, Identifiers.ProgramDiagnostic2DataType_Encoding_DefaultXml.getValue());
	
    protected NodeId CreateSessionId;
    protected String CreateClientName;
    protected DateTime InvocationCreationTime;
    protected DateTime LastTransitionTime;
    protected String LastMethodCall;
    protected NodeId LastMethodSessionId;
    protected Argument[] LastMethodInputArguments;
    protected Argument[] LastMethodOutputArguments;
    protected Variant[] LastMethodInputValues;
    protected Variant[] LastMethodOutputValues;
    protected DateTime LastMethodCallTime;
    protected StatusResult LastMethodReturnStatus;
    
    public ProgramDiagnostic2DataType() {}
    
    public ProgramDiagnostic2DataType(NodeId CreateSessionId, String CreateClientName, DateTime InvocationCreationTime, DateTime LastTransitionTime, String LastMethodCall, NodeId LastMethodSessionId, Argument[] LastMethodInputArguments, Argument[] LastMethodOutputArguments, Variant[] LastMethodInputValues, Variant[] LastMethodOutputValues, DateTime LastMethodCallTime, StatusResult LastMethodReturnStatus)
    {
        this.CreateSessionId = CreateSessionId;
        this.CreateClientName = CreateClientName;
        this.InvocationCreationTime = InvocationCreationTime;
        this.LastTransitionTime = LastTransitionTime;
        this.LastMethodCall = LastMethodCall;
        this.LastMethodSessionId = LastMethodSessionId;
        this.LastMethodInputArguments = LastMethodInputArguments;
        this.LastMethodOutputArguments = LastMethodOutputArguments;
        this.LastMethodInputValues = LastMethodInputValues;
        this.LastMethodOutputValues = LastMethodOutputValues;
        this.LastMethodCallTime = LastMethodCallTime;
        this.LastMethodReturnStatus = LastMethodReturnStatus;
    }
    
    public NodeId getCreateSessionId()
    {
        return CreateSessionId;
    }
    
    public void setCreateSessionId(NodeId CreateSessionId)
    {
        this.CreateSessionId = CreateSessionId;
    }
    
    public String getCreateClientName()
    {
        return CreateClientName;
    }
    
    public void setCreateClientName(String CreateClientName)
    {
        this.CreateClientName = CreateClientName;
    }
    
    public DateTime getInvocationCreationTime()
    {
        return InvocationCreationTime;
    }
    
    public void setInvocationCreationTime(DateTime InvocationCreationTime)
    {
        this.InvocationCreationTime = InvocationCreationTime;
    }
    
    public DateTime getLastTransitionTime()
    {
        return LastTransitionTime;
    }
    
    public void setLastTransitionTime(DateTime LastTransitionTime)
    {
        this.LastTransitionTime = LastTransitionTime;
    }
    
    public String getLastMethodCall()
    {
        return LastMethodCall;
    }
    
    public void setLastMethodCall(String LastMethodCall)
    {
        this.LastMethodCall = LastMethodCall;
    }
    
    public NodeId getLastMethodSessionId()
    {
        return LastMethodSessionId;
    }
    
    public void setLastMethodSessionId(NodeId LastMethodSessionId)
    {
        this.LastMethodSessionId = LastMethodSessionId;
    }
    
    public Argument[] getLastMethodInputArguments()
    {
        return LastMethodInputArguments;
    }
    
    public void setLastMethodInputArguments(Argument[] LastMethodInputArguments)
    {
        this.LastMethodInputArguments = LastMethodInputArguments;
    }
    
    public Argument[] getLastMethodOutputArguments()
    {
        return LastMethodOutputArguments;
    }
    
    public void setLastMethodOutputArguments(Argument[] LastMethodOutputArguments)
    {
        this.LastMethodOutputArguments = LastMethodOutputArguments;
    }
    
    public Variant[] getLastMethodInputValues()
    {
        return LastMethodInputValues;
    }
    
    public void setLastMethodInputValues(Variant[] LastMethodInputValues)
    {
        this.LastMethodInputValues = LastMethodInputValues;
    }
    
    public Variant[] getLastMethodOutputValues()
    {
        return LastMethodOutputValues;
    }
    
    public void setLastMethodOutputValues(Variant[] LastMethodOutputValues)
    {
        this.LastMethodOutputValues = LastMethodOutputValues;
    }
    
    public DateTime getLastMethodCallTime()
    {
        return LastMethodCallTime;
    }
    
    public void setLastMethodCallTime(DateTime LastMethodCallTime)
    {
        this.LastMethodCallTime = LastMethodCallTime;
    }
    
    public StatusResult getLastMethodReturnStatus()
    {
        return LastMethodReturnStatus;
    }
    
    public void setLastMethodReturnStatus(StatusResult LastMethodReturnStatus)
    {
        this.LastMethodReturnStatus = LastMethodReturnStatus;
    }
    
    /**
      * Deep clone
      *
      * @return cloned ProgramDiagnostic2DataType
      */
    public ProgramDiagnostic2DataType clone()
    {
        ProgramDiagnostic2DataType result = (ProgramDiagnostic2DataType) super.clone();
        result.CreateSessionId = CreateSessionId;
        result.CreateClientName = CreateClientName;
        result.InvocationCreationTime = InvocationCreationTime;
        result.LastTransitionTime = LastTransitionTime;
        result.LastMethodCall = LastMethodCall;
        result.LastMethodSessionId = LastMethodSessionId;
        if (LastMethodInputArguments!=null) {
            result.LastMethodInputArguments = new Argument[LastMethodInputArguments.length];
            for (int i=0; i<LastMethodInputArguments.length; i++)
                result.LastMethodInputArguments[i] = LastMethodInputArguments[i].clone();
        }
        if (LastMethodOutputArguments!=null) {
            result.LastMethodOutputArguments = new Argument[LastMethodOutputArguments.length];
            for (int i=0; i<LastMethodOutputArguments.length; i++)
                result.LastMethodOutputArguments[i] = LastMethodOutputArguments[i].clone();
        }
        result.LastMethodInputValues = LastMethodInputValues==null ? null : LastMethodInputValues.clone();
        result.LastMethodOutputValues = LastMethodOutputValues==null ? null : LastMethodOutputValues.clone();
        result.LastMethodCallTime = LastMethodCallTime;
        result.LastMethodReturnStatus = LastMethodReturnStatus==null ? null : LastMethodReturnStatus.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ProgramDiagnostic2DataType other = (ProgramDiagnostic2DataType) obj;
        if (CreateSessionId==null) {
            if (other.CreateSessionId != null) return false;
        } else if (!CreateSessionId.equals(other.CreateSessionId)) return false;
        if (CreateClientName==null) {
            if (other.CreateClientName != null) return false;
        } else if (!CreateClientName.equals(other.CreateClientName)) return false;
        if (InvocationCreationTime==null) {
            if (other.InvocationCreationTime != null) return false;
        } else if (!InvocationCreationTime.equals(other.InvocationCreationTime)) return false;
        if (LastTransitionTime==null) {
            if (other.LastTransitionTime != null) return false;
        } else if (!LastTransitionTime.equals(other.LastTransitionTime)) return false;
        if (LastMethodCall==null) {
            if (other.LastMethodCall != null) return false;
        } else if (!LastMethodCall.equals(other.LastMethodCall)) return false;
        if (LastMethodSessionId==null) {
            if (other.LastMethodSessionId != null) return false;
        } else if (!LastMethodSessionId.equals(other.LastMethodSessionId)) return false;
        if (LastMethodInputArguments==null) {
            if (other.LastMethodInputArguments != null) return false;
        } else if (!Arrays.equals(LastMethodInputArguments, other.LastMethodInputArguments)) return false;
        if (LastMethodOutputArguments==null) {
            if (other.LastMethodOutputArguments != null) return false;
        } else if (!Arrays.equals(LastMethodOutputArguments, other.LastMethodOutputArguments)) return false;
        if (LastMethodInputValues==null) {
            if (other.LastMethodInputValues != null) return false;
        } else if (!Arrays.equals(LastMethodInputValues, other.LastMethodInputValues)) return false;
        if (LastMethodOutputValues==null) {
            if (other.LastMethodOutputValues != null) return false;
        } else if (!Arrays.equals(LastMethodOutputValues, other.LastMethodOutputValues)) return false;
        if (LastMethodCallTime==null) {
            if (other.LastMethodCallTime != null) return false;
        } else if (!LastMethodCallTime.equals(other.LastMethodCallTime)) return false;
        if (LastMethodReturnStatus==null) {
            if (other.LastMethodReturnStatus != null) return false;
        } else if (!LastMethodReturnStatus.equals(other.LastMethodReturnStatus)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((CreateSessionId == null) ? 0 : CreateSessionId.hashCode());
        result = prime * result
                + ((CreateClientName == null) ? 0 : CreateClientName.hashCode());
        result = prime * result
                + ((InvocationCreationTime == null) ? 0 : InvocationCreationTime.hashCode());
        result = prime * result
                + ((LastTransitionTime == null) ? 0 : LastTransitionTime.hashCode());
        result = prime * result
                + ((LastMethodCall == null) ? 0 : LastMethodCall.hashCode());
        result = prime * result
                + ((LastMethodSessionId == null) ? 0 : LastMethodSessionId.hashCode());
        result = prime * result
                + ((LastMethodInputArguments == null) ? 0 : Arrays.hashCode(LastMethodInputArguments));
        result = prime * result
                + ((LastMethodOutputArguments == null) ? 0 : Arrays.hashCode(LastMethodOutputArguments));
        result = prime * result
                + ((LastMethodInputValues == null) ? 0 : Arrays.hashCode(LastMethodInputValues));
        result = prime * result
                + ((LastMethodOutputValues == null) ? 0 : Arrays.hashCode(LastMethodOutputValues));
        result = prime * result
                + ((LastMethodCallTime == null) ? 0 : LastMethodCallTime.hashCode());
        result = prime * result
                + ((LastMethodReturnStatus == null) ? 0 : LastMethodReturnStatus.hashCode());
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
		return "ProgramDiagnostic2DataType: "+ ObjectUtils.printFieldsDeep(this);
	}

}
