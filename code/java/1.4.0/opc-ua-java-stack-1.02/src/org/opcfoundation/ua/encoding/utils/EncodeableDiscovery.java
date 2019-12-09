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

package org.opcfoundation.ua.encoding.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.encoding.IEncodeable;

/**
 * This class discovers builtin encodeable nodeIds and 
 * corresponding classes.
 * 
 */
public class EncodeableDiscovery {

	private static Map<NodeId, Class<IEncodeable>> DEFAULT;
	
	/**
	 * Return default NodeId -> Class mapping.
	 * 
	 * @return default encodeable table
	 */
	public static synchronized Map<NodeId, Class<IEncodeable>> getDefault() 
	{
		if (DEFAULT!=null) return DEFAULT;
		
		Map<NodeId, Class<IEncodeable>> map = new HashMap<NodeId, Class<IEncodeable>>();
		try {
			discoverDefaultEncodeables(map);			
		} catch (RuntimeException e) {
			// Unrecoverable problem as installation is faulty if this occurs
			throw new Error(e);
		}
		DEFAULT = Collections.unmodifiableMap(map);
		return DEFAULT;
	}
	
	/**
	 * Discover default encodeables. Encodeables are discovered by inspecting
	 * all fields from Identifiers class using reflection.
	 * 
	 * @param map encodeable table to fill with builtin encodeables
	 */
	@SuppressWarnings("unchecked")
	public static void discoverDefaultEncodeables(Map<NodeId, Class<IEncodeable>> map) 
	{
		// Discover builtin classes
		Class<?> clazz = Identifiers.class;
		ClassLoader cl = clazz.getClassLoader();
		int index = clazz.getCanonicalName().lastIndexOf(".");
		String prefix = clazz.getCanonicalName().substring(0, index);
		for (Field f : clazz.getFields())
		{
			f.setAccessible(true);
			try {
				String className = prefix+"."+f.getName();
				Class<IEncodeable> c = (Class<IEncodeable>) cl.loadClass(className);
				if (!IEncodeable.class.isAssignableFrom(c)) continue;
				for (Field cf : c.getFields()) {
					cf.setAccessible(true);
					if (!cf.getType().equals(NodeId.class)) continue;
					NodeId nodeId;
					try {
						nodeId = (NodeId) cf.get(null);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException("Failed to load default identifiers", e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException("Failed to load default identifiers", e);
					}
					if (nodeId==null)
						throw new RuntimeException("Failed to load default identifiers");
					map.put(nodeId, c);
				}
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
	}

	
}
