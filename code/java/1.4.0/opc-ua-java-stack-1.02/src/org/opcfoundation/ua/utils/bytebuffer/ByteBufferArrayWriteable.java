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
public class ByteBufferArrayWriteable implements IBinaryWriteable {

	ByteQueue q;
	ByteBuffer tmp = ByteBuffer.allocate(8);
	
	public ByteBufferArrayWriteable(ByteBuffer[] bufs) {		
		if (bufs == null)
			throw new IllegalArgumentException("null");
		q = new ByteQueue();
		tmp.order(q.order());
		for (ByteBuffer buf : bufs)
			q.offer(buf);
	}	
	
	public ByteBufferArrayWriteable(ByteQueue q) {		
		if (q == null)
			throw new IllegalArgumentException("null");
		this.q = q;
		tmp.order(q.order());
	}		
	
	@Override
	public ByteOrder order() {
		return q.order();
	}

	@Override
	public void order(ByteOrder order) {
		tmp.order( order );
		q.order(order);
	}

	@Override
	public void put(byte b) throws IOException {
		q.getWriteChunk().put(b);
	}

	@Override
	public void put(ByteBuffer src) throws IOException {
		q.put(src);
	}

	@Override
	public void put(ByteBuffer src, int length) throws IOException {
		q.put(src, length);
	}

	@Override
	public void put(byte[] src, int offset, int length) throws IOException {
		q.put(src, offset, length);
	}

	@Override
	public void put(byte[] src) throws IOException {
		q.put(src);
	}

	@Override
	public void putDouble(double value) throws IOException {
		if (q.getWriteChunk().remaining()>8)
			q.getWriteChunk().putDouble(value);
		else {
			tmp.rewind();
			tmp.putDouble(value);
			tmp.rewind();
			q.put(tmp, 8);
		}
	}

	@Override
	public void putFloat(float value) throws IOException {
		if (q.getWriteChunk().remaining()>4)
			q.getWriteChunk().putFloat(value);
		else {
			tmp.rewind();
			tmp.putFloat(value);
			tmp.rewind();
			q.put(tmp, 4);		
		}
	}

	@Override
	public void putInt(int value) throws IOException {
		if (q.getWriteChunk().remaining()>4)
			q.getWriteChunk().putInt(value);
		else {
			tmp.rewind();
			tmp.putInt(value);
			tmp.rewind();
			q.put(tmp, 4);		
		}
	}

	@Override
	public void putLong(long value) throws IOException {
		if (q.getWriteChunk().remaining()>8)
			q.getWriteChunk().putLong(value);
		else {
			tmp.rewind();
			tmp.putLong(value);
			tmp.rewind();
			q.put(tmp, 8);		
		}
	}

	@Override
	public void putShort(short value) throws IOException {
		if (q.getWriteChunk().remaining()>2)
			q.getWriteChunk().putShort(value);
		else {
			tmp.rewind();
			tmp.putShort(value);
			tmp.rewind();
			q.put(tmp, 2);		
		}
	}

	@Override
	public void flush() {		
	}

}
