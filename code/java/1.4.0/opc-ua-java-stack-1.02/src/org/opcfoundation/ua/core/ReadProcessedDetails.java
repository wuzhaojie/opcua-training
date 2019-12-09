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
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.AggregateConfiguration;
import org.opcfoundation.ua.core.HistoryReadDetails;



public class ReadProcessedDetails extends HistoryReadDetails implements Structure, Cloneable {
	
	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.ReadProcessedDetails);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.ReadProcessedDetails_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.ReadProcessedDetails_Encoding_DefaultXml);
	
    protected DateTime StartTime;
    protected DateTime EndTime;
    protected Double ProcessingInterval;
    protected NodeId[] AggregateType;
    protected AggregateConfiguration AggregateConfiguration;
    
    public ReadProcessedDetails() {}
    
    public ReadProcessedDetails(DateTime StartTime, DateTime EndTime, Double ProcessingInterval, NodeId[] AggregateType, AggregateConfiguration AggregateConfiguration)
    {
        this.StartTime = StartTime;
        this.EndTime = EndTime;
        this.ProcessingInterval = ProcessingInterval;
        this.AggregateType = AggregateType;
        this.AggregateConfiguration = AggregateConfiguration;
    }
    
    public DateTime getStartTime()
    {
        return StartTime;
    }
    
    public void setStartTime(DateTime StartTime)
    {
        this.StartTime = StartTime;
    }
    
    public DateTime getEndTime()
    {
        return EndTime;
    }
    
    public void setEndTime(DateTime EndTime)
    {
        this.EndTime = EndTime;
    }
    
    public Double getProcessingInterval()
    {
        return ProcessingInterval;
    }
    
    public void setProcessingInterval(Double ProcessingInterval)
    {
        this.ProcessingInterval = ProcessingInterval;
    }
    
    public NodeId[] getAggregateType()
    {
        return AggregateType;
    }
    
    public void setAggregateType(NodeId[] AggregateType)
    {
        this.AggregateType = AggregateType;
    }
    
    public AggregateConfiguration getAggregateConfiguration()
    {
        return AggregateConfiguration;
    }
    
    public void setAggregateConfiguration(AggregateConfiguration AggregateConfiguration)
    {
        this.AggregateConfiguration = AggregateConfiguration;
    }
    
    /**
      * Deep clone
      *
      * @return cloned ReadProcessedDetails
      */
    public ReadProcessedDetails clone()
    {
        ReadProcessedDetails result = new ReadProcessedDetails();
        result.StartTime = StartTime;
        result.EndTime = EndTime;
        result.ProcessingInterval = ProcessingInterval;
        result.AggregateType = AggregateType==null ? null : AggregateType.clone();
        result.AggregateConfiguration = AggregateConfiguration==null ? null : AggregateConfiguration.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ReadProcessedDetails other = (ReadProcessedDetails) obj;
        if (StartTime==null) {
            if (other.StartTime != null) return false;
        } else if (!StartTime.equals(other.StartTime)) return false;
        if (EndTime==null) {
            if (other.EndTime != null) return false;
        } else if (!EndTime.equals(other.EndTime)) return false;
        if (ProcessingInterval==null) {
            if (other.ProcessingInterval != null) return false;
        } else if (!ProcessingInterval.equals(other.ProcessingInterval)) return false;
        if (AggregateType==null) {
            if (other.AggregateType != null) return false;
        } else if (!Arrays.equals(AggregateType, other.AggregateType)) return false;
        if (AggregateConfiguration==null) {
            if (other.AggregateConfiguration != null) return false;
        } else if (!AggregateConfiguration.equals(other.AggregateConfiguration)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((StartTime == null) ? 0 : StartTime.hashCode());
        result = prime * result
                + ((EndTime == null) ? 0 : EndTime.hashCode());
        result = prime * result
                + ((ProcessingInterval == null) ? 0 : ProcessingInterval.hashCode());
        result = prime * result
                + ((AggregateType == null) ? 0 : Arrays.hashCode(AggregateType));
        result = prime * result
                + ((AggregateConfiguration == null) ? 0 : AggregateConfiguration.hashCode());
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
		return "ReadProcessedDetails: "+ObjectUtils.printFieldsDeep(this);
	}

}
