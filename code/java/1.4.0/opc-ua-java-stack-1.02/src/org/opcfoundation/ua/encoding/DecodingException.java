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

package org.opcfoundation.ua.encoding;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;

/**
 *
 * 
 */
public class DecodingException extends ServiceResultException {

	private static final long serialVersionUID = 1L;

	public DecodingException() {
		super(StatusCodes.Bad_DecodingError);
	}
	
	public DecodingException(Exception e) {
		super(StatusCodes.Bad_DecodingError, e, e.getMessage());
	}
	
	public DecodingException(Exception e, String message) {
		super(StatusCodes.Bad_DecodingError, e, message);
	}
	
	public DecodingException(Throwable reason) {
		super(reason);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(UnsignedInteger statusCode, Throwable reason) {
		super(statusCode, reason);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(String message, Exception e) {
		super(StatusCodes.Bad_DecodingError, e, message);
	}

	public DecodingException(String message) {
		super(StatusCodes.Bad_DecodingError, message);
	}

	public DecodingException(int statusCode, String text) {
		super(statusCode, text);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(int statusCode) {
		super(statusCode);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(StatusCode statusCode, String text) {
		super(statusCode, text);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(StatusCode statusCode, Throwable reason,
			String text) {
		super(statusCode, reason, text);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(StatusCode statusCode) {
		super(statusCode);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(UnsignedInteger statusCode, String text) {
		super(statusCode, text);
		// TODO Auto-generated constructor stub
	}

	public DecodingException(UnsignedInteger statusCode) {
		super(statusCode);
		// TODO Auto-generated constructor stub
	}

	
	
}
