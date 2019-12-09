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

package org.opcfoundation.ua.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opcfoundation.ua.builtintypes.BuiltinsMap;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class ReflectionUtils {

	/**
	 * Returns all methods public, protected and private
	 * 
	 * @param clazz
	 * @return all methods
	 */
	public static Method[] getAllMethods(Class<?> clazz)
	{
		Set<Method> result = new HashSet<Method>();				
		_getAllMethods(clazz, result);		
		return result.toArray(new Method[result.size()]);
	}
	
	private static void _getAllMethods(Class<?> clazz, Collection<Method> result)
	{
		for (Method m : clazz.getDeclaredMethods())
			result.add(m);
	}

	public static Field[] getAllFields(Class<?> clazz)
	{
		List<Field> result = new ArrayList<Field>();				
		_getAllFields(clazz, result);		
		return result.toArray(new Field[result.size()]);
	}
	
	private static void _getAllFields(Class<?> clazz, Collection<Field> result)
	{
		for (Field m : clazz.getDeclaredFields())
			result.add(m);
	}	

	/**
	 * Get array version of a class.
	 * 
	 * E.g. Object -> Object[]
	 *      Object[] -> Object[][]
	 * 
	 * @param clazz
	 * @return array class
	 */
	public static Class<?> getRespectiveArrayClass(Class<?> clazz)
	{
		Integer bt = BuiltinsMap.ID_MAP.get(clazz);
		if (bt!=null) 
			return BuiltinsMap.ARRAY_LIST.get(bt);
		
		String name = "[L"+clazz.getCanonicalName()+";";
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
	}	
	
	public static Class<?> getComponentClass(Class<?> clazz)
	{
		if (!clazz.isArray()) return clazz;
		return clazz.getComponentType();
	}
	
	
}
