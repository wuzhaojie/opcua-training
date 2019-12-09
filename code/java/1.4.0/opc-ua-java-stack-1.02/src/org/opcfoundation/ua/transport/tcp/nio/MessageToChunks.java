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

import org.opcfoundation.ua.common.RuntimeServiceResultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.EncoderMode;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.encoding.binary.BinaryEncoder;
import org.opcfoundation.ua.encoding.binary.EncoderCalc;
import org.opcfoundation.ua.transport.tcp.impl.ChunkFactory;
import org.opcfoundation.ua.transport.tcp.impl.TcpConnectionParameters;
import org.opcfoundation.ua.utils.bytebuffer.ByteBufferArrayWriteable;
import org.opcfoundation.ua.utils.bytebuffer.ByteQueue;

/**
 * Encodes messages to chunks.
 * Returns an array of plaintexts whose content is partially filled.
 * Chunk size, padding and plaintext is written, but footer and header is missing.
 * ByteOrder is Little Endian.  
 * <p>
 * Encoder also asserts that message size and chunk count is with-in limits.  
 */
public class MessageToChunks implements Callable<ByteBuffer[]> 
{
	MessageType type;
	IEncodeable msg;
	TcpConnectionParameters ctx;
	EncoderContext encoderCtx;
	ChunkFactory chunkFactory;

	public MessageToChunks(IEncodeable msg, TcpConnectionParameters ctx, EncoderContext encoderCtx, ChunkFactory chunkFactory, MessageType type)
	{
		if (msg==null || ctx==null || chunkFactory==null)
			throw new IllegalArgumentException("null arg");
		this.msg = msg;
		this.ctx = ctx;
		this.encoderCtx = encoderCtx;
		this.chunkFactory = chunkFactory;
		this.type = type;
	}
	
	@Override
	public ByteBuffer[] call() 
	throws RuntimeServiceResultException
	{		
	  try {
		EncoderCalc calc = new EncoderCalc();
		calc.setEncoderContext(encoderCtx);
		if (type == MessageType.Encodeable)
			calc.putEncodeable(null, msg);
		else
			calc.putMessage(msg);
		int len = calc.getLength();
		
		if (len>ctx.maxSendMessageSize && ctx.maxSendMessageSize!=0)
			throw new ServiceResultException(StatusCodes.Bad_TcpMessageTooLarge);
		
		ByteQueue bq = new ByteQueue();
		bq.order(ByteOrder.LITTLE_ENDIAN);
		bq.setWriteLimit(len);
		bq.setByteBufferFactory(chunkFactory);
		bq.setChunkSize(chunkFactory.maxPlaintextSize);
		
		ByteBufferArrayWriteable array = new ByteBufferArrayWriteable(bq);
		array.order(ByteOrder.LITTLE_ENDIAN);
		
		BinaryEncoder enc = new BinaryEncoder(array);
		enc.setEncoderContext(encoderCtx);
		enc.setEncoderMode(EncoderMode.NonStrict);
		
		if (type == MessageType.Message)
			enc.putMessage(msg);
		else
			enc.putEncodeable(null, msg);
		
		ByteBuffer[] plaintexts = bq.getChunks(len);
		
		if (plaintexts.length>ctx.maxRecvChunkCount && ctx.maxRecvChunkCount!=0)
			throw new ServiceResultException(StatusCodes.Bad_TcpMessageTooLarge);

		return plaintexts;
	  } catch (ServiceResultException e) {
		  throw new RuntimeServiceResultException(e);
	  }
	}
	
}
