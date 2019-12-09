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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.encoding.IEncodeable;

/**
 * Table containing descriptions of stub classes. 
 * 
 * @see EncodeableDesc
 */
public class EncodeableDescTable {
	
	private Map<Class<? extends IEncodeable>, EncodeableDesc> classMap = new HashMap<Class<? extends IEncodeable>, EncodeableDesc>();
	private Map<ExpandedNodeId, EncodeableDesc> idMap = new HashMap<ExpandedNodeId, EncodeableDesc>();
	private Map<ExpandedNodeId, EncodeableDesc> binIdMap = new HashMap<ExpandedNodeId, EncodeableDesc>();
	private Map<ExpandedNodeId, EncodeableDesc> xmlIdMap = new HashMap<ExpandedNodeId, EncodeableDesc>();
	private Map<Class<? extends IEncodeable>, EncodeableDesc> _classMap = Collections.unmodifiableMap(classMap);
	private Map<ExpandedNodeId, EncodeableDesc> _idMap = Collections.unmodifiableMap(idMap);
	private Map<ExpandedNodeId, EncodeableDesc> _binIdMap = Collections.unmodifiableMap(binIdMap);
	private Map<ExpandedNodeId, EncodeableDesc> _xmlIdMap = Collections.unmodifiableMap(xmlIdMap);
	
	public EncodeableDesc get(Class<?> clazz)
	{
		return classMap.get(clazz);
	}
	
	public EncodeableDesc get(ExpandedNodeId id)
	{
		return idMap.get(id);
	}

	
	public void addStructureInfo(EncodeableDesc s)
	{
		classMap.put(s.clazz, s);
		//classMap.put(getArrayClass(s.clazz), s);
		idMap.put(s.binaryId, s);
		idMap.put(s.xmlId, s);
		idMap.put(s.id, s);
		binIdMap.put(s.binaryId, s);
		xmlIdMap.put(s.binaryId, s);
	}
	
	public Map<ExpandedNodeId, EncodeableDesc> getIdMap()
	{
		return _idMap;		
	}
	
	public Map<ExpandedNodeId, EncodeableDesc> getBinIdMap()
	{
		return _binIdMap;
	}
	
	public Map<ExpandedNodeId, EncodeableDesc> getXmlIdMap()
	{
		return _xmlIdMap;
	}
	
	public Map<Class<? extends IEncodeable>, EncodeableDesc> getClassMap()
	{
		return _classMap;
	}
	
}
