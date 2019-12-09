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

package org.opcfoundation.ua.encoding.binary;

import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.core.ResponseHeader;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class DecoderUtils {
	
	/**
	 * Fixes DiagnosticInfos of a Response header to point to the string
	 * table of the response header.
	 * @param rh
	 */
	public static void fixResponseHeader(ResponseHeader rh)
	{
		String[] stringTable = rh.getStringTable();
		if (stringTable == null) return;
		DiagnosticInfo di = rh.getServiceDiagnostics();
		if (di==null) return;
		_fixDI(di, stringTable);
	}
	
	private static void _fixDI(DiagnosticInfo di, String[] stringTable)
	{
		di.setStringArray(stringTable);
		if (di.getInnerDiagnosticInfo()!=null)
			_fixDI(di.getInnerDiagnosticInfo(), stringTable);
	}

}
