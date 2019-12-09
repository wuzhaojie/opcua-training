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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Output stream writer
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class OutputStreamWriteable implements IBinaryWriteable {

	ByteOrder order = ByteOrder.nativeOrder();
	OutputStream out;
		
	public OutputStreamWriteable(OutputStream out)
	{
		if (out==null) throw new IllegalArgumentException("null arg");
		this.out = out;
	}
	
	public OutputStream getStream()
	{
		return out;
	}

	void _put(int value)
	throws IOException
	{
		out.write(value);
	}
	
	@Override
	public void put(byte b) throws IOException {
		_put(b);
	}

	@Override
	public void put(ByteBuffer src) throws IOException {
		if (src.hasArray()) {
			byte array[] = src.array();
			put(array, src.position(), src.remaining());
			src.position(src.limit()); 
		} else 
			for (;src.hasRemaining();)
				_put(src.get());
	}

	@Override
	public void put(ByteBuffer src, int length) throws IOException {		
		if (src.hasArray()) {
			byte array[] = src.array();
			put(array, src.position(), length);
			src.position(length); 
		} else {
			for (int i=0; i<length; i++)
				_put(src.get());
		}
	}

	@Override
	public void put(byte[] src, int offset, int length) throws IOException {
		out.write(src, offset, length);
	}

	@Override
	public void put(byte[] src) throws IOException {
		out.write(src);
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
	
	public ByteOrder order() {
		return order;
	}

	public void order(ByteOrder order) {
		this.order = order;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}
	

}
