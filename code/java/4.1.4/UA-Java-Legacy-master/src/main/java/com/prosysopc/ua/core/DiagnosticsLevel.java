/* ========================================================================
 * Copyright (c) 2005-2015 The OPC Foundation, Inc. All rights reserved.
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

package com.prosysopc.ua.core;

import com.prosysopc.ua.builtintypes.Enumeration;
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import java.util.EnumSet;


public enum DiagnosticsLevel implements Enumeration {

    Basic,
    Advanced,
    Info,
    Log,
    Debug;
	
	public static final NodeId ID = Identifiers.DiagnosticsLevel;
	public static EnumSet<DiagnosticsLevel> NONE = EnumSet.noneOf( DiagnosticsLevel.class );
	public static EnumSet<DiagnosticsLevel> ALL = EnumSet.allOf( DiagnosticsLevel.class );
	
	@Override
	public int getValue() {
		return ordinal();
	}

	public static DiagnosticsLevel valueOf(int value)
	{
		if (value<0 || value>=values().length) return null;
		return values()[value];
	}

	public static DiagnosticsLevel valueOf(Integer value)
	{
		return value == null ? null : valueOf(value.intValue());
	}

	public static DiagnosticsLevel valueOf(UnsignedInteger value)
	{
		return value == null ? null : valueOf(value.intValue());
	}

	public static DiagnosticsLevel[] valueOf(int[] value)
	{
		DiagnosticsLevel[] result = new DiagnosticsLevel[value.length];
		for(int i=0; i<value.length; i++)
		  result[i] = valueOf(value[i]);
		return result;
	}

	public static DiagnosticsLevel[] valueOf(Integer[] value)
	{
		DiagnosticsLevel[] result = new DiagnosticsLevel[value.length];
		for(int i=0; i<value.length; i++)
		  result[i] = valueOf(value[i]);
		return result;
	}
	
	public static DiagnosticsLevel[] valueOf(UnsignedInteger[] value)
	{
		DiagnosticsLevel[] result = new DiagnosticsLevel[value.length];
		for(int i=0; i<value.length; i++)
		  result[i] = valueOf(value[i]);
		return result;
	}

}
