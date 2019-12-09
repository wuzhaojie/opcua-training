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

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;

/**
 * Reads statuscode description annotations from generated StatusCode class
 * using reflection. Based on StatusCodeDescriptions.
 * 
 * @see StatusCode
 * @author Otso Palonen (otso.palonen@prosys.fi)
 */
public class IdentifierDescriptions {

	private static Map<String, NodeId> NAME_MAP = null;

	private static synchronized void readDescriptions() {
		if (NAME_MAP != null)
			return;

		NAME_MAP = new HashMap<String, NodeId>();

		Class<?> clazz;
		try {
			clazz = Class.forName("org.opcfoundation.ua.core.Identifiers");
			for (Field f : clazz.getFields()) {
				if (!f.getType().equals(NodeId.class))
					continue;
				f.setAccessible(true);
				NodeId nodeId = (NodeId) f.get(null);
				String name = f.getName();
				NAME_MAP.put(name, nodeId);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	public static NodeId toNodeId(String name) {
		readDescriptions();
		NodeId nodeId = NAME_MAP.get(name);
		if (nodeId == null)
			throw new IllegalArgumentException("NodeId not found: " + name);
		return nodeId;
	}

}
