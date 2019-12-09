/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.common;

import java.util.Arrays;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.ServiceResult;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.StatusCodes;

/**
 * Generic Exception
 * 
 * 
 * @see StatusCodes
 */
public class ServiceResultException extends Exception {

    private static final long serialVersionUID = 988605552235028178L;

    final protected StatusCode statusCode;
    final protected String text;

    public ServiceResultException(String message)
    {
    	this(new StatusCode(StatusCodes.Bad_UnexpectedError),  message);
    }
    
    public ServiceResultException(int statusCode)
    {
        this(StatusCode.getFromBits(statusCode), StatusCodeDescriptions.getStatusCodeDescription(statusCode));
    }

    public ServiceResultException(int statusCode, String text)
    {
        this(StatusCode.getFromBits(statusCode), text);
    }
    
    public ServiceResultException(UnsignedInteger statusCode)
    {
        this(new StatusCode(statusCode), StatusCodeDescriptions.getStatusCodeDescription(statusCode.intValue()));
    }

    public ServiceResultException(UnsignedInteger statusCode, String text)
    {
        this(new StatusCode(statusCode), text);
    }    

    public ServiceResultException(UnsignedInteger statusCode, Throwable reason, String text)
    {
    	super(text, reason);
        if (statusCode==null)
            throw new IllegalArgumentException("statusCode is null");        
        this.statusCode = new StatusCode(statusCode);
        this.text = text;        
    }    
    
    public ServiceResultException(StatusCode statusCode)
    {
        this(statusCode, statusCode.getDescription()!=null ? statusCode.getDescription() : "");
    }

    public ServiceResultException(StatusCode statusCode, String text)
    {
        if (statusCode==null)
            throw new IllegalArgumentException("statusCode is null");
        this.statusCode = statusCode;
        this.text = text;
    }

    public ServiceResultException(StatusCode statusCode, Throwable reason, String text)
    {
    	super(text, reason);
        if (statusCode==null)
            throw new IllegalArgumentException("statusCode is null");        
        this.statusCode = statusCode;
        this.text = text;        
    }

    public ServiceResultException(UnsignedInteger statusCode, Throwable reason)
    {
    	super(reason.getMessage(), reason);
        if (statusCode==null)
            throw new IllegalArgumentException("statusCode is null");        
        this.statusCode = new StatusCode(statusCode);
        this.text = statusCode.toString() + ", " + reason.getMessage();        
    }
    
    public ServiceResultException(StatusCode statusCode, Throwable reason)
    {
    	super(reason.getMessage(), reason);
        if (statusCode==null)
            throw new IllegalArgumentException("statusCode is null");        
        this.statusCode = statusCode;
        this.text = statusCode.toString() + ", " + reason.getMessage();        
    }

    public ServiceResultException(Throwable reason)
    {
    	super(reason);
        this.statusCode = new StatusCode(StatusCodes.Bad_UnexpectedError);
        this.text = reason.getMessage();        
    }
    
    @Override
    public String getMessage() {
        if (text!=null)
            return String.format("%s (code=0x%08X, description=\"%s\")", statusCode.getName(), statusCode.getValueAsIntBits(), text);
        return statusCode.toString();
    }
    
    public StatusCode getStatusCode() {
        return statusCode;
    }
        
    public String getAdditionalTextField()
    {
        return text;
    }
    
    /**
     * Converts the error into a service result
     * 
     * @return a new service result object
     */
    public ServiceResult toServiceResult()
    {
    	ServiceResult res = new ServiceResult();
    	if (statusCode==null)
    		res.setCode(new StatusCode(StatusCodes.Bad_UnexpectedError));
    	else
    		res.setCode(statusCode);
    	res.setSymbolicId(statusCode.toString());
    	res.setLocalizedText(new LocalizedText(getMessage(), ""));
    	res.setAdditionalInfo(Arrays.toString(getStackTrace()));
    	return res;
    }
        
}
