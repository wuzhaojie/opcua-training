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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Sends events when chunks are complete
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class ByteBufferArrayWriteable2 implements IBinaryWriteable {

	ByteBuffer[] bufs;
	int i = 0;
	ByteBuffer cur;
	ByteOrder order;
	ChunkListener listener;

	public interface ChunkListener {
		void onChunkComplete(ByteBuffer[] chunks, int index);
	}	
	
	public ByteBufferArrayWriteable2(ByteBuffer[] bufs, ChunkListener listener) {
		if (bufs==null || listener==null)
			throw new IllegalArgumentException("null arg");
		this.bufs = bufs;
		this.listener = listener;
		cur = bufs[0];		
	}
	
	protected void fireChunkComplete(int index)
	{
		if (listener!=null)
			listener.onChunkComplete(bufs, index);
	}

	@Override
	public ByteOrder order() {
		return order;
	}

	@Override
	public void order(ByteOrder order) {
		this.order = order;
		cur.order(order);
	}

	private void checkChunk()
	{
		if (!cur.hasRemaining())
			fireChunkComplete(i);
	}

	private void prepareNextChunk()
	{
		while (!cur.hasRemaining()) {
			i++;
			if (i>=bufs.length)
				throw new BufferOverflowException();
			cur = bufs[i];
			cur.order(order); 
		}
	}
	
	void _put(int value)
	throws IOException
	{
		prepareNextChunk();
		cur.put((byte)value);
		checkChunk();
	}
	
	@Override
	public void put(byte b) throws IOException {
		prepareNextChunk();
		cur.put(b);
		checkChunk();		
	}

	@Override
	public void put(ByteBuffer src) throws IOException {
		while (src.hasRemaining()) {
			prepareNextChunk();
			ByteBufferUtils.copyRemaining(src, cur);
			checkChunk();
		}
	}

	@Override
	public void put(ByteBuffer src, int length) throws IOException {
		while (length>0) {
			prepareNextChunk();
			int n = Math.min(length, Math.min(src.remaining(), cur.remaining()));
			ByteBufferUtils.copy(src, cur, n);
			length -= n;
			checkChunk();
		}
	}

	@Override
	public void put(byte[] src, int offset, int length) throws IOException {		
		while (length>0) {
			prepareNextChunk();
			int n = Math.min(length, cur.remaining());
			cur.put(src, offset, n);
			offset += n;
			length -= n;
			checkChunk();
		}
	}

	@Override
	public void put(byte[] src) throws IOException {
		put(src, 0, src.length);
	}

	@Override
	public void putDouble(double value) throws IOException {
		putLong(Double.doubleToLongBits(value));
	}

	@Override
	public void putFloat(float value) throws IOException {
		putInt(Float.floatToIntBits(value));
	}

	@Override
	public void putInt(int value) throws IOException {
		if (order == ByteOrder.BIG_ENDIAN) {
			_put(value >> 24);
			_put(value >> 16);
			_put(value >> 8);
			_put(value);
		} else {
			_put(value);
			_put(value >> 8);
			_put(value >> 16);
			_put(value >> 24);
		}
	}

	@Override
	public void putLong(long value) throws IOException {
		if (order == ByteOrder.BIG_ENDIAN) {
			_put((int) (value >> 56));
			_put((int) (value >> 48));
			_put((int) (value >> 40));
			_put((int) (value >> 32));
			_put((int) (value >> 24));
			_put((int) (value >> 16));
			_put((int) (value >> 8));
			_put((int) (value));
		} else {
			_put((int) (value));
			_put((int) (value >> 8));
			_put((int) (value >> 16));
			_put((int) (value >> 24));
			_put((int) (value >> 32));
			_put((int) (value >> 40));
			_put((int) (value >> 48));
			_put((int) (value >> 56));
		}
	}

	@Override
	public void putShort(short value) throws IOException {
		if (order == ByteOrder.BIG_ENDIAN) {
			_put(value >> 8);
			_put(value);
		} else {
			_put(value);
			_put(value >> 8);
		}
	}

	@Override
	public void flush() {
	}
	
}
