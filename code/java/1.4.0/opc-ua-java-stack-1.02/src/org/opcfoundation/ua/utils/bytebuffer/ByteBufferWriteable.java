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
 * IWriteable implementation with ByteBuffer as backend
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class ByteBufferWriteable implements IBinaryWriteable {

	ByteBuffer buf;
	
	public ByteBufferWriteable(ByteBuffer buf)
	{
		if (buf == null)
			throw new IllegalArgumentException("null");
		this.buf = buf;
	}

	@Override
	public void put(byte b) {
		buf.put(b);
	}

	@Override
	public void put(ByteBuffer src) {
		buf.put(src);
	}

	@Override
	public void put(ByteBuffer src, int length) {
		if (src.hasArray()) {
			byte[] array = src.array();
			buf.put(array, src.arrayOffset() + src.position(), length);
		} else {
			for (int i=0; i<length; i++)
				buf.put(src.get());
		}
	}

	@Override
	public void put(byte[] src, int offset, int length) {
		buf.put(src, offset, length);
	}

	@Override
	public void put(byte[] src) {
		buf.put(src);
	}

	@Override
	public void putDouble(double value) {
		buf.putDouble(value);
	}

	@Override
	public void putFloat(float value) {
		buf.putFloat(value);
	}

	@Override
	public void putInt(int value) {
		buf.putInt(value);
	}

	@Override
	public void putLong(long value) {
		buf.putLong(value);
	}

	@Override
	public void putShort(short value) {
		buf.putShort(value);
	}

	@Override
	public ByteOrder order() {
		return buf.order();
	}

	@Override
	public void order(ByteOrder order) {
		buf.order(order);
	}

	@Override
	public void flush() {
	}
	
	public long position() throws IOException {
		return buf.position();
	}
	
	public void position(long newPosition) throws IOException {
		if (newPosition>=Integer.MAX_VALUE || newPosition<0) throw new IllegalArgumentException("index out of range");
		buf.position((int) newPosition);		
	}
	
}
