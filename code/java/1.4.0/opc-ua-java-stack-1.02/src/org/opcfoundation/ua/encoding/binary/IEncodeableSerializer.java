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

import java.util.Collection;

import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.EncodeableSerializer;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.encoding.EncodeType;
import org.opcfoundation.ua.encoding.EncodingException;
import org.opcfoundation.ua.encoding.IDecoder;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.encoding.IEncoder;
import org.opcfoundation.ua.encoding.utils.SerializerComposition;

/**
 * IEncodeableSerializer serializes IEncodeable classes.
 * 
 * There are two implementations; Reflection based {@link EncodeableReflectionSerializer}
 * and code-generate based {@link EncodeableSerializer}.  
 * 
 * @see SerializerComposition Implementation that unifies a set of serializers 
 * @see EncodeableReflectionSerializer Reflection based implementation
 * @see EncodeableSerializer Code-generated implementation 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public interface IEncodeableSerializer {

	/**
	 * Get the set of node ids this serializes knows how to decode.
	 *
	 * @param result a collection to be filled with node ids
	 */
	void getSupportedNodeIds(Collection<ExpandedNodeId> result);
	
	/**
	 * Returns a set of classes this serializes knows how to encode
	 */
	void getSupportedClasses(Collection<Class<? extends IEncodeable>> result);
	
	Class<? extends IEncodeable> getClass(ExpandedNodeId id);

	ExpandedNodeId getNodeId(Class<? extends IEncodeable> clazz, EncodeType type);
	
	/**
	 * Deserialize an object from a binary stream. 
	 * Reading NodeId must be omited.
	 *   
	 * @param clazz
	 * @param decoder
	 * @return deserialized object
	 * @throws EncodingException 
	 */
	IEncodeable getEncodeable(Class<? extends IEncodeable> clazz, IDecoder decoder)
	throws DecodingException;
	
	/**
	 * Serialize object to encoder.
	 * Serialization of NodeId is omited.
	 * 
	 * @param encodeable
	 * @param encoder
	 */
	void putEncodeable(Class<? extends IEncodeable> clazz, IEncodeable encodeable, IEncoder encoder)
	throws EncodingException;
	
	/** 
	 * Calculate encodeable size in bytes.
	 * Omit NodeId.
	 * 
	 * @param encodeable
	 * @param calculator
	 */
	void calcEncodeable(Class<? extends IEncodeable> clazz, IEncodeable encodeable, IEncoder calculator)
	throws EncodingException;
	
}
