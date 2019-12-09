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

package org.opcfoundation.ua.builtintypes;

import java.util.Arrays;
import java.util.Locale;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;



public class ServiceResult {

	/**
	 * Create service result with stack trace from an exception.
	 * If exception is ServiceResultException, use its reported status code,    
	 * if not the error code will be set to Bad_UnexpectedError. 
	 * @param t throwable
	 * @return service result
	 */
	public static ServiceResult toServiceResult(Throwable t)
	{
		ServiceResult res = new ServiceResult();
    	res.setCode(t instanceof ServiceResultException ? ((ServiceResultException)t).getStatusCode() : new StatusCode(StatusCodes.Bad_UnexpectedError));
		res.setSymbolicId( res.toString() );
    	res.setLocalizedText(new LocalizedText(t.getMessage(), ""));
    	res.setAdditionalInfo(Arrays.toString(t.getStackTrace()));		
    	return res;
	}
	
	private StatusCode code;
	private String symbolicId;
	private String namespaceUri;
	private LocalizedText localizedText;
	private String additionalInfo;
	private ServiceResult innerResult;

	public ServiceResult() {
		initialize();
	}

	//TODO added by Mikko
	public ServiceResult(StatusCode code) {
		initialize(code);
	}
	//TODO ADDED BY MIKKO
	public ServiceResult(StatusCode code, Throwable e) {
		initialize(code, e);
	}

	//TODO ADDED BY MIKKO
	public ServiceResult(UnsignedInteger code, Throwable e) {
		initialize(new StatusCode(code), e);
	}
	public ServiceResult(UnsignedInteger code) {
		initialize(new StatusCode(code));
	}
	public boolean isBad() {
		if (code == null) return false;
		return code.isBad();
	}
	
	
	//TODO ADDED BY MIKKO
	private String lookUpSymbolicId(StatusCode code) {
		return code.getName(); 
	}
	
	//TODO ADDED BY MIKKO
	public static String buildExceptionTrace(Throwable e) {
		if (e == null)
			return null;
		else
			return e.getStackTrace().toString();
	}
	
	
	private void initialize() {
		//TODO SCHSANGE this to StatusCode.Good
		initialize(StatusCode.GOOD, null);
	}

	private void initialize(StatusCode code) {
		this.code = code;
		symbolicId = lookUpSymbolicId(code);
		localizedText = null;
		additionalInfo = null;
	}

	private void initialize(StatusCode code, Throwable e) {
		assert(e != null);
		this.code = code;
		symbolicId = lookUpSymbolicId(this.code);
		localizedText = new LocalizedText(e.getMessage(), Locale.ENGLISH);
		additionalInfo = buildExceptionTrace(e);
	}

	public boolean isGood() {
		if (code == null) return false;
		return code.isGood();
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public StatusCode getCode() {
		return code;
	}

	public void setCode(StatusCode code) {
		this.code = code;
	}

	public ServiceResult getInnerResult() {
		return innerResult;
	}

	public void setInnerResult(ServiceResult innerResult) {
		this.innerResult = innerResult;
	}

	public LocalizedText getLocalizedText() {
		return localizedText;
	}

	public void setLocalizedText(LocalizedText localizedText) {
		this.localizedText = localizedText;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	public void setNamespaceUri(String namespaceUri) {
		this.namespaceUri = namespaceUri;
	}

	public String getSymbolicId() {
		return symbolicId;
	}

	public void setSymbolicId(String symbolicId) {
		this.symbolicId = symbolicId;
	}


}
