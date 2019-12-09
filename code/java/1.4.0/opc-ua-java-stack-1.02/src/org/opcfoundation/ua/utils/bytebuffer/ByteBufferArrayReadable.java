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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class ByteBufferArrayReadable implements IBinaryReadable {

	ByteQueue q;
	
	public ByteBufferArrayReadable(ByteBuffer[] bufs) {		
		if (bufs == null)
			throw new IllegalArgumentException("null");
		q = new ByteQueue();
		for (ByteBuffer buf : bufs)
			q.offer(buf);
	}	
	
	public ByteBufferArrayReadable(ByteQueue q) {		
		if (q == null)
			throw new IllegalArgumentException("null");
		this.q = q;
	}		
	
	@Override
	public ByteOrder order() {
		return q.order();
	}

	@Override
	public void order(ByteOrder order) {
		q.order(order);
	}
	
	@Override
	public byte get() throws IOException {
		return q.getReadChunk().get();
	}

	@Override
	public void get(byte[] dst, int offset, int length) throws IOException {
		q.get(dst, offset, length);		
	}

	@Override
	public void get(byte[] dst) throws IOException {
		q.get(dst);
	}

	@Override
	public void get(ByteBuffer buf) throws IOException {
		q.get(buf);
	}

	@Override
	public void get(ByteBuffer buf, int length) throws IOException {
		q.get(buf, length);
	}

	@Override
	public double getDouble() throws IOException {
		if (q.getReadChunk().remaining()>=8)
			return q.getReadChunk().getDouble();
		return q.get(8).getDouble();
	}

	@Override
	public float getFloat() throws IOException {
		if (q.getReadChunk().remaining()>=4)
			return q.getReadChunk().getFloat();
		return q.get(4).getFloat();
	}

	@Override
	public int getInt() throws IOException {
		if (q.getReadChunk().remaining()>=4)
			return q.getReadChunk().getInt();
		return q.get(4).getInt();
	}

	@Override
	public long getLong() throws IOException {
		if (q.getReadChunk().remaining()>=8)
			return q.getReadChunk().getLong();
		return q.get(8).getLong();
	}

	@Override
	public short getShort() throws IOException {
		if (q.getReadChunk().remaining()>=2)
			return q.getReadChunk().getShort();
		return q.get(2).getShort();
	}


	@Override
	public long limit() {
		return q.getBytesWritten();
	}

	@Override
	public long position() {
		return q.getBytesRead();
	}


}
