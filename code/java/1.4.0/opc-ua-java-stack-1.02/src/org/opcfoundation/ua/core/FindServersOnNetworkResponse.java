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

import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.ObjectUtils;
import java.util.Arrays;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.core.ServerOnNetwork;


public class FindServersOnNetworkResponse extends Object implements ServiceResponse {

	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.FindServersOnNetworkResponse);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.FindServersOnNetworkResponse_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.FindServersOnNetworkResponse_Encoding_DefaultXml);
	
    protected ResponseHeader ResponseHeader;
    protected DateTime LastCounterResetTime;
    protected ServerOnNetwork[] Servers;
    
    public FindServersOnNetworkResponse() {}
    
    public FindServersOnNetworkResponse(ResponseHeader ResponseHeader, DateTime LastCounterResetTime, ServerOnNetwork[] Servers)
    {
        this.ResponseHeader = ResponseHeader;
        this.LastCounterResetTime = LastCounterResetTime;
        this.Servers = Servers;
    }
    
    public ResponseHeader getResponseHeader()
    {
        return ResponseHeader;
    }
    
    public void setResponseHeader(ResponseHeader ResponseHeader)
    {
        this.ResponseHeader = ResponseHeader;
    }
    
    public DateTime getLastCounterResetTime()
    {
        return LastCounterResetTime;
    }
    
    public void setLastCounterResetTime(DateTime LastCounterResetTime)
    {
        this.LastCounterResetTime = LastCounterResetTime;
    }
    
    public ServerOnNetwork[] getServers()
    {
        return Servers;
    }
    
    public void setServers(ServerOnNetwork[] Servers)
    {
        this.Servers = Servers;
    }
    
    /**
      * Deep clone
      *
      * @return cloned FindServersOnNetworkResponse
      */
    public FindServersOnNetworkResponse clone()
    {
        FindServersOnNetworkResponse result = new FindServersOnNetworkResponse();
        result.ResponseHeader = ResponseHeader==null ? null : ResponseHeader.clone();
        result.LastCounterResetTime = LastCounterResetTime;
        if (Servers!=null) {
            result.Servers = new ServerOnNetwork[Servers.length];
            for (int i=0; i<Servers.length; i++)
                result.Servers[i] = Servers[i].clone();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FindServersOnNetworkResponse other = (FindServersOnNetworkResponse) obj;
        if (ResponseHeader==null) {
            if (other.ResponseHeader != null) return false;
        } else if (!ResponseHeader.equals(other.ResponseHeader)) return false;
        if (LastCounterResetTime==null) {
            if (other.LastCounterResetTime != null) return false;
        } else if (!LastCounterResetTime.equals(other.LastCounterResetTime)) return false;
        if (Servers==null) {
            if (other.Servers != null) return false;
        } else if (!Arrays.equals(Servers, other.Servers)) return false;
        return true;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((ResponseHeader == null) ? 0 : ResponseHeader.hashCode());
        result = prime * result
                + ((LastCounterResetTime == null) ? 0 : LastCounterResetTime.hashCode());
        result = prime * result
                + ((Servers == null) ? 0 : Arrays.hashCode(Servers));
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
