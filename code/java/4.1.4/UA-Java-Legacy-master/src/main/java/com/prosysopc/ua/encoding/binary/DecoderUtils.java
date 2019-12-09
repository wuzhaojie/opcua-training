/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.encoding.binary;

import com.prosysopc.ua.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.core.ResponseHeader;

/**
 * <p>DecoderUtils class.</p>
 *
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class DecoderUtils {
	
	/**
	 * Fixes DiagnosticInfos of a Response header to point to the string
	 * table of the response header.
	 *
	 * @param rh a {@link ResponseHeader} object.
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
