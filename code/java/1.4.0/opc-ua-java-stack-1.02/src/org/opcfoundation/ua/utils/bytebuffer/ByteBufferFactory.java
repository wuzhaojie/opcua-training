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

package org.opcfoundation.ua.utils.bytebuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public abstract class ByteBufferFactory {

	public static final ByteBufferFactory DEFAULT_ENDIAN_HEAP_BYTEBUFFER_FACTORY = 
		new ByteBufferFactory() {
			@Override
			public ByteBuffer allocate(int capacity) {
				ByteBuffer result = ByteBuffer.allocate(capacity);
				result.order(ByteOrder.nativeOrder());
				return null;
			}};	
	
	public static final ByteBufferFactory LITTLE_ENDIAN_HEAP_BYTEBUFFER_FACTORY = 
		new ByteBufferFactory() {
			@Override
			public ByteBuffer allocate(int capacity) {
				ByteBuffer result = ByteBuffer.allocate(capacity);
				result.order(ByteOrder.LITTLE_ENDIAN);
				return result;
			}};  
	public static final ByteBufferFactory BIG_ENDIAN_HEAP_BYTEBUFFER_FACTORY = 
		new ByteBufferFactory() {
			@Override
			public ByteBuffer allocate(int capacity) {
				ByteBuffer result = ByteBuffer.allocate(capacity);
				result.order(ByteOrder.BIG_ENDIAN);
				return result;
			}};  
	
	public abstract ByteBuffer allocate(int capacity);
	
}
