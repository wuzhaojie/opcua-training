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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Object utils
 * 
 */
public class ObjectUtils {

	private static boolean printObjectIds = false;
	private static boolean showByteDataAsHex = true;
	private static boolean showFullClassName = true;
	private static boolean showDataType = false;

	/**
	 * @return the showDataType
	 */
	public static boolean isShowDataType() {
		return showDataType;
	}

	/**
	 * @param showDataType the showDataType to set
	 */
	public static void setShowDataType(boolean showDataType) {
		ObjectUtils.showDataType = showDataType;
	}


	/**
	 * @return the showFullClassName
	 */
	public static boolean isShowFullClassName() {
		return showFullClassName;
	}

	/**
	 * Define whether to show the full class name (with path) or just the SimpleName.
	 * @param showFullClassName the showFullClassName to set
	 */
	public static void setShowFullClassName(boolean showFullClassName) {
		ObjectUtils.showFullClassName = showFullClassName;
	}

	/**
	 * @return the showByteDataAsHex
	 */
	public static boolean isShowByteDataAsHex() {
		return showByteDataAsHex;
	}

	/**
	 * Define that {@link #printFieldsDeep(Object)} will use
	 * {@link CryptoUtil#toHex()} to print the byte arrays, instead of
	 * {@link Arrays#toString()}.
	 * 
	 * @param showByteDataAsHex
	 *            the showByteDataAsHex to set
	 */
	public static void setShowByteDataAsHex(boolean showByteDataAsHex) {
		ObjectUtils.showByteDataAsHex = showByteDataAsHex;
	}

	/**
	 * @return the printObjectIds
	 */
	public static boolean isPrintObjectIds() {
		return printObjectIds;
	}

	/**
	 * Define whether the {@link #printFieldsDeep(Object)} method should show
	 * the object IDs. If it does, it will not necessarily show all the field
	 * values.
	 * 
	 * @param printObjectIds
	 *            the printObjectIds to set
	 */
	public static void setPrintObjectIds(boolean printObjectIds) {
		ObjectUtils.printObjectIds = printObjectIds;
	}

	/**
	 * Null-safe object value comparison.
	 * 
	 * @param o1 an object or null
	 * @param o2 an object or null
	 * @return true of objects are the same or both are null, otherwise false
	 */
    public static boolean objectEquals(Object o1, Object o2) {
        if (o1==o2) return true;
        if (o1==null && o2==null) return true;
        if (o1==null || o2==null) return false;
        return o1.equals(o2);
    }
    
    public static int hashCode(Object obj) {
        return (obj==null?0:obj.hashCode());
    }

	public static boolean equals(Collection<?> c1, Collection<?> c2)
	{
		if (c1.size()!=c2.size()) return false;
		Iterator<?> i1 = c1.iterator();
		Iterator<?> i2 = c2.iterator();
		while (i1.hasNext())
			if (!ObjectUtils.objectEquals(i1.next(), i2.next()))
				return false;
		return true;
	}
	
	public static boolean equals(Object left, Object right){
		if(left == null){
			return right == null;
		}else{
			return left.equals(right);
		}
	}
	
    public static String toString(Object obj) {
        if (obj instanceof Object[]) {            
            return Arrays.toString((Object[])obj); 
        }
        if (obj instanceof double[]) return Arrays.toString((double[])obj); 
        if (obj instanceof boolean[]) return Arrays.toString((boolean[])obj); 
        if (obj instanceof byte[]) return (showByteDataAsHex ? CryptoUtil.toHex((byte[])obj, 0) : Arrays.toString((byte[])obj)); 
        if (obj instanceof char[]) return Arrays.toString((char[])obj); 
        if (obj instanceof float[]) return Arrays.toString((float[])obj); 
        if (obj instanceof int[]) return Arrays.toString((int[])obj); 
        if (obj instanceof long[]) return Arrays.toString((long[])obj); 
        if (obj instanceof short[]) return Arrays.toString((short[])obj);
        return obj.toString();
    }	
    


	public static String printFieldsDeep(Object o)
	{
		Set<Object> visitedObjects = new HashSet<Object>();
		StringBuilder sb = new StringBuilder();
		_printFieldsDeep(sb, o, "", visitedObjects);
		return sb.toString();
	}
	
	private static void _printFieldsDeep(StringBuilder sb, Object o, String indent, Set<Object> visitedObjects)
	{
		Class<?> c = o.getClass();
		sb.append(c.getSimpleName());
		if (isPrintObjectIds())
			sb.append(" (id="+System.identityHashCode(o)+")");
		sb.append("\n");
		if (!visitedObjects.add(o) && visitedObjects.size() > 10000) 
			return;
		
		indent += "  ";
		while (!c.equals(Object.class)) {
			for (Field f : ReflectionUtils.getAllFields(c))
			{
				boolean isStatic = (f.getModifiers() & 8) == 8;
				boolean isArray = f.getType().isArray();

				if (isStatic) continue;
				f.setAccessible(true);
				sb.append(indent+ f.getName()+"=");
				try {
					Object dada = f.get(o);		
					if (dada==null)
						sb.append("null\n");
					else if (isArray) {
				        if (dada instanceof double[]) sb.append( Arrays.toString((double[])dada) +"\n"); 
				        else if (dada instanceof boolean[]) sb.append( Arrays.toString((boolean[])dada) +"\n"); 
				        else if (dada instanceof byte[]) sb.append( 
				        		(showByteDataAsHex ? CryptoUtil.toHex((byte[])dada, 0) : Arrays.toString((byte[])dada)) +"\n"); 
				        else if (dada instanceof char[]) sb.append( Arrays.toString((char[])dada) +"\n"); 
				        else if (dada instanceof float[]) sb.append( Arrays.toString((float[])dada) +"\n"); 
				        else if (dada instanceof int[]) sb.append( Arrays.toString((int[])dada) +"\n"); 
				        else if (dada instanceof long[]) sb.append( Arrays.toString((long[])dada) +"\n"); 
				        else if (dada instanceof short[]) sb.append( Arrays.toString((short[])dada) +"\n");
				        else {
			        		sb.append(getClassName(dada.getClass().getComponentType())+"["+Array.getLength(dada)+"]\n");
				        	for (int i=0; i<Array.getLength(dada); i++) {
				        		Object subDada = Array.get(dada, i);
				        		sb.append(indent+"  ["+i+"]=");
				        		try{
				        			if (subDada == null)
				        				sb.append("null\n");
									else if (isClassPrintable(subDada)) {
										appendObjectAsString(sb, subDada, indent);
									} else {
										_printFieldsDeep(sb, subDada, indent, visitedObjects);
									}
				        		}
				        		catch (Exception e) {
									System.out.println("ERROR From ObjectUtils / _printfieldsDeep");
									if(subDada == null){
										System.out.println("Subdada null");
										System.out.println(sb.toString());
									}
								}
				        		
				        	}
				        }
					} else {
		        		if (isClassPrintable(dada)) {
		        			appendObjectAsString(sb, dada, indent);
		        		} else {
		        			_printFieldsDeep(sb, dada, indent, visitedObjects);
		        		}
					}
					//sb.append("\n");
				} catch (IllegalArgumentException e) {
					sb.append("?");
					continue;
				} catch (IllegalAccessException e) {
					sb.append("?");
					continue;
				}
			}
			c = c.getSuperclass();		
		}
	}

	/**
	 * @param clazz
	 * @return
	 */
	private static Serializable getClassName(Class<?> clazz) {
		return showFullClassName ? clazz : clazz.getSimpleName();
	}

	/**
	 * @param sb
	 * @param obj
	 * @param indent 
	 */
	private static void appendObjectAsString(StringBuilder sb, Object obj, String indent) {
		if (showClassName(obj)) {
			sb.append("{");
			sb.append(getClassName(obj.getClass()));
			sb.append("}");
		}
		String s = MultiDimensionArrayUtils.toString(obj);
		if (s != null)
			s = s.replace("\n", "\n  " + indent);
		sb.append(s);
		sb.append("\n");
	}

	private static boolean showClassName(Object subDada) {
		return isShowDataType() && !subDada.getClass().getCanonicalName().startsWith("java");
	}

	/**
	 * @param subDada
	 * @return
	 */
	private static boolean isClassPrintable(Object subDada) {
		return subDada.getClass().getCanonicalName().startsWith("java") ||
				subDada.getClass().getCanonicalName().startsWith("org.opcfoundation.ua.builtintypes");
	}
	
	public static String printFields(Object o)
	{
		Class<?> c = o.getClass();
		StringBuilder sb = new StringBuilder();
		
		while (!c.equals(Object.class)) {
			for (Field f : c.getDeclaredFields())
			{
				boolean isStatic = (f.getModifiers() & 8) == 8;
				boolean isArray = f.getType().isArray();

				if (isStatic) continue;
				f.setAccessible(true);
				if (sb.length()>0) sb.append(", ");
				sb.append(f.getName()+"=");
				try {
					Object dada = f.get(o);		
					if (dada==null)
						sb.append("null");
					else if (isArray) 
						sb.append(toString(dada));
					else				
						sb.append(dada);
				} catch (IllegalArgumentException e) {
					sb.append("?");
					continue;
				} catch (IllegalAccessException e) {
					sb.append("?");
					continue;
				}
			}
			c = c.getSuperclass();
		}
		
		return sb.toString();
	}


}
