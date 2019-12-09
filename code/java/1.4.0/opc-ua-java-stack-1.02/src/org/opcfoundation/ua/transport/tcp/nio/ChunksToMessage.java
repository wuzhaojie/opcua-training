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

package org.opcfoundation.ua.transport.tcp.nio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Callable;

import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.encoding.binary.BinaryDecoder;
import org.opcfoundation.ua.transport.tcp.impl.TcpConnectionParameters;
import org.opcfoundation.ua.utils.bytebuffer.ByteBufferArrayReadable;

/**
 * This {@link Callable} class unserializes chunk plaintexts into a message. 
 * 
 */
public class ChunksToMessage implements Callable<IEncodeable> {

	Class<? extends IEncodeable> expectedType;
	TcpConnectionParameters ctx;
	EncoderContext encoderCtx;
	ByteBuffer[] plaintexts;
	
	/**
	 * 
	 * @param ctx
	 * @param expectedType type or null (if message expected)
	 * @param plaintexts
	 */
	public ChunksToMessage(TcpConnectionParameters ctx, EncoderContext encoderCtx, Class<? extends IEncodeable> expectedType, ByteBuffer...plaintexts)
	{
		this.expectedType = expectedType;
		this.plaintexts = plaintexts;
		this.ctx = ctx;
		this.encoderCtx = encoderCtx;
	}
	
	@Override
	public IEncodeable call() throws Exception {
		ByteBufferArrayReadable readable = new ByteBufferArrayReadable(plaintexts);
		readable.order(ByteOrder.LITTLE_ENDIAN);
		BinaryDecoder dec = new BinaryDecoder(readable);
		dec.setEncoderContext(encoderCtx);
		if (expectedType!=null)
			return dec.getEncodeable(null, expectedType);
		else
			return dec.getMessage();
	}

}
