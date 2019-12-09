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

import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.ObjectUtils;
import java.util.Arrays;
import org.opcfoundation.ua.core.RequestHeader;


public class FindServersRequest extends Object implements ServiceRequest {

	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers.FindServersRequest);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers.FindServersRequest_Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers.FindServersRequest_Encoding_DefaultXml);
	
    protected RequestHeader RequestHeader;
    protected String EndpointUrl;
    protected String[] LocaleIds;
    protected String[] ServerUris;
    
    public FindServersRequest() {}
    
    public FindServersRequest(RequestHeader RequestHeader, String EndpointUrl, String[] LocaleIds, String[] ServerUris)
    {
        this.RequestHeader = RequestHeader;
        this.EndpointUrl = EndpointUrl;
        this.LocaleIds = LocaleIds;
        this.ServerUris = ServerUris;
    }
    
    public RequestHeader getRequestHeader()
    {
        return RequestHeader;
    }
    
    public void setRequestHeader(RequestHeader RequestHeader)
    {
        this.RequestHeader = RequestHeader;
    }
    
    public String getEndpointUrl()
    {
        return EndpointUrl;
    }
    
    public void setEndpointUrl(String EndpointUrl)
    {
        this.EndpointUrl = EndpointUrl;
    }
    
    public String[] getLocaleIds()
    {
        return LocaleIds;
    }
    
    public void setLocaleIds(String[] LocaleIds)
    {
        this.LocaleIds = LocaleIds;
    }
    
    public String[] getServerUris()
    {
        return ServerUris;
    }
    
    public void setServerUris(String[] ServerUris)
    {
        this.ServerUris = ServerUris;
    }
    
    /**
      * Deep clone
      *
      * @return cloned FindServersRequest
      */
    public FindServersRequest clone()
    {
        FindServersRequest result = new FindServersRequest();
        result.RequestHeader = RequestHeader==null ? null : RequestHeader.clone();
        result.EndpointUrl = EndpointUrl;
        result.LocaleIds = LocaleIds==null ? null : LocaleIds.clone();
        result.ServerUris = ServerUris==null ? null : ServerUris.clone();
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FindServersRequest other = (FindServersRequest) obj;
        if (RequestHeader==null) {
            if (other.RequestHeader != null) return false;
        } else if (!RequestHeader.equals(other.RequestHeader)) return false;
        if (EndpointUrl==null) {
            if (other.EndpointUrl != null) return false;
        } else if (!EndpointUrl.equals(other.EndpointUrl)) return false;
        if (LocaleIds==null) {
            if (other.LocaleIds != null) return false;
        } else if (!Arrays.equals(LocaleIds, other.LocaleIds)) return false;
        if (ServerUris==null) {
            if (other.ServerUris != null) return false;
        } else if (!Arrays.equals(ServerUris, other.ServerUris)) return false;
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
                + ((EndpointUrl == null) ? 0 : EndpointUrl.hashCode());
        result = prime * result
                + ((LocaleIds == null) ? 0 : Arrays.hashCode(LocaleIds));
        result = prime * result
                + ((ServerUris == null) ? 0 : Arrays.hashCode(ServerUris));
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
