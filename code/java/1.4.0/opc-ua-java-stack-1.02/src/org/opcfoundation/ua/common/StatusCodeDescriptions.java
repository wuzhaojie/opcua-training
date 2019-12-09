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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.utils.Description;

/**
 * Reads statuscode description annotations from generated StatusCode class
 * using reflection.
 * 
 * @see StatusCode
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class StatusCodeDescriptions {

    private static Map<UnsignedInteger, String> ERROR_NAMES = null;
    private static Map<UnsignedInteger, String> ERROR_DESCRIPTIONS = null;
    private static Map<String, UnsignedInteger> ERROR_NAMES_REV = null;

    private static final int MASK = StatusCode.SEVERITY_MASK | StatusCode.SUBCODE_MASK;      
    
    private static synchronized void readDescriptions() {
    	if (ERROR_NAMES!=null) return;
    	ERROR_NAMES = new HashMap<UnsignedInteger, String>();
    	ERROR_DESCRIPTIONS = new HashMap<UnsignedInteger, String>();
    	ERROR_NAMES_REV = new HashMap<String, UnsignedInteger>();
    	try {
			Class<?> clazz = Class.forName("org.opcfoundation.ua.core.StatusCodes");			
			
			for (Field f : clazz.getFields()) {
				if (!f.getType().equals(UnsignedInteger.class)) continue;
				f.setAccessible(true);
				UnsignedInteger statusCode = (UnsignedInteger) f.get(null);
				int code = statusCode.intValue() & MASK;
				String name = f.getName();
				Description _summary = f.getAnnotation(Description.class);
				String summary = _summary==null?"":_summary.value();				
				statusCode = UnsignedInteger.getFromBits(code);				
				ERROR_DESCRIPTIONS.put(statusCode, summary);
				ERROR_NAMES.put(statusCode, name);
				ERROR_NAMES_REV.put(name, statusCode);
			}			
		} catch (Exception e) {
		}
    }
    
    public static String getStatusCode(int statuscode)
    {
    	readDescriptions();
    	UnsignedInteger i = UnsignedInteger.getFromBits(statuscode & MASK);
    	return ERROR_NAMES.get(i);
    }

    public static String getStatusCodeDescription(int statuscode)
    {
    	readDescriptions();
    	UnsignedInteger i = UnsignedInteger.getFromBits(statuscode & MASK);
    	return ERROR_DESCRIPTIONS.get(i);
    }

    public static String getStatusCodeDescription(StatusCode statusCode)
    {
    	readDescriptions();
    	UnsignedInteger i = UnsignedInteger.getFromBits(statusCode.getValueAsIntBits());
    	return ERROR_DESCRIPTIONS.get(i);
    }

    public static UnsignedInteger getStatusCode(String description)
    {
    	readDescriptions();
    	return ERROR_NAMES_REV.get(description);
    }
    
}
