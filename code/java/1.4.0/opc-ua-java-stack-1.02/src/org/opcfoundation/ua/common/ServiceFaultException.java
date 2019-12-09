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

import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.ServiceFault;
import org.opcfoundation.ua.core.StatusCodes;

/**
 * ServiceFaultException is an error that occurs in execution an operation.
 * It wraps {@link ServiceFault} into an exception. 
 */
public class ServiceFaultException extends ServiceResultException {

	private static final long serialVersionUID = 1L;
	
	ServiceFault serviceFault;
	DiagnosticInfo di;

	public ServiceFaultException(Throwable t)
	{
		super(t);
		this.serviceFault = ServiceFault.toServiceFault(t);
	}
	
	public ServiceFaultException(ServiceFault serviceFault) 
	{
		super(serviceFault.getResponseHeader()==null?new StatusCode(StatusCodes.Bad_InternalError):
			serviceFault.getResponseHeader().getServiceDiagnostics()==null?new StatusCode(StatusCodes.Bad_InternalError):
			serviceFault.getResponseHeader().getServiceDiagnostics().getInnerStatusCode()==null?new StatusCode(StatusCodes.Bad_InternalError): serviceFault.getResponseHeader().getServiceDiagnostics().getInnerStatusCode()  );
		this.serviceFault = serviceFault;
	}

	public ServiceFault getServiceFault()
	{
		return serviceFault;
	}
	
	@Override
	public String toString() {
		return serviceFault.toString();
	}
	
	@Override
	public String getMessage() {
		return serviceFault.toString();
	}
	
    public StatusCode getStatusCode()
    {
    	if (serviceFault.getResponseHeader()==null || serviceFault.getResponseHeader().getServiceResult()==null)
    		return new StatusCode(StatusCodes.Bad_InternalError);
    	return serviceFault.getResponseHeader().getServiceResult();
    }

}
