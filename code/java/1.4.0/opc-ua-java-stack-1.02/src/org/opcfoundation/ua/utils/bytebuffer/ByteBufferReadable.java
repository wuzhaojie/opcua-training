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
public class ByteBufferReadable implements IBinaryReadable {
	
	ByteBuffer buf;
	
	public ByteBufferReadable(ByteBuffer buf) {		
		if (buf == null)
			throw new IllegalArgumentException("null");
		this.buf = buf;
	}

	public ByteBufferReadable(byte[] buf) {		
		if (buf == null)
			throw new IllegalArgumentException("null");
		this.buf = ByteBuffer.wrap(buf);		
	}

	
	@Override
	public byte get() {
		return buf.get();
	}

	@Override
	public void get(byte[] dst, int offset, int length) {
		buf.get(dst, offset, length);
	}

	@Override
	public void get(byte[] dst) {
		buf.get(dst);
	}

	@Override
	public void get(ByteBuffer buf) {		
		if (buf.hasArray()) {
			this.buf.get(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
			buf.position(buf.capacity());
		} else {
			buf.put(buf);
		}
	}

	@Override
	public void get(ByteBuffer buf, int length) {
		if (buf.hasArray()) {
			this.buf.get(buf.array(), buf.arrayOffset() + buf.position(), length);
			buf.position(buf.position() + length);
		} else {
//			int len = Math.min( Math.min( buf.remaining(), this.buf.remaining() ), length);
			int len = length;
			int origLimit = this.buf.limit();
			try {
				this.buf.limit(this.buf.position()+len);
				buf.put(this.buf);
			} finally {
				this.buf.limit(origLimit);
			}
		}
	}

	@Override
	public double getDouble() {
		return buf.getDouble();
	}

	@Override
	public float getFloat() {
		return buf.getFloat();
	}

	@Override
	public int getInt() {
		return buf.getInt();
	}

	@Override
	public long getLong() {
		return buf.getLong();
	}

	@Override
	public short getShort() {
		return buf.getShort();
	}

	@Override
	public long limit() {
		return buf.limit();
	}
	
	@Override
	public long position() {
		return buf.position();
	}
	
	public ByteOrder order() {
		return buf.order();
	}

	public void order(ByteOrder order) {
		buf.order(order);
	}

	public void position(int newPosition) throws IOException {
		buf.position(newPosition);
	}

	public void position(long newPosition) throws IOException {
		if (newPosition>=Integer.MAX_VALUE || newPosition<0) throw new IllegalArgumentException("index out of range");
		buf.position((int) newPosition);
	}

	public void skip(long bytes) throws IOException {
		long newPosition = bytes + position();
		position( newPosition );
	}
	
}
