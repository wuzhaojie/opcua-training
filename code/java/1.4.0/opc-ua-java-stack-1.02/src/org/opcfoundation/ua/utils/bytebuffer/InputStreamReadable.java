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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Input stream reader
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class InputStreamReadable implements IBinaryReadable {

	ByteOrder order = ByteOrder.nativeOrder();
	InputStream is;
	long limit, position;
	
	public InputStreamReadable(InputStream is, long limit)
	{
		this.is = is;
		this.limit = limit;
	}

	/**
	 * Get next byte
	 * @return 0..255
	 * @throws IOException
	 */
	int _get()
	throws IOException
	{
		int value = is.read();
		if (value==-1)
			throw new EOFException();
		position++;
		return value & 0xff;
	}
	
	@Override
	public byte get() 
    throws IOException	
	{
		return (byte) _get();
	}

	@Override
	public void get(byte[] dst, int offset, int length) 
    throws IOException	
	{
		while (length>0) {
			int bytesRead = is.read(dst, offset, length);
			if (bytesRead==-1) throw new EOFException();
			position+=bytesRead;			
			offset += bytesRead;
			length -= bytesRead;
		}
	}

	@Override
	public void get(byte[] dst) 
    throws IOException	
	{
		get(dst, 0, dst.length);
	}

	@Override
	public void get(ByteBuffer buf) 
    throws IOException	
	{		
		for (;buf.hasRemaining();)
			buf.put((byte)_get());		
	}

	@Override
	public void get(ByteBuffer buf, int length) 
    throws IOException	
	{
		if (length<256) {
			for (int i=0; i<length; i++)
				buf.put((byte)_get());
		} else {
			byte[] b = new byte[length];
			get(b, 0, length);
			buf.put(b);
		}
	}

	@Override
	public double getDouble() 
    throws IOException	
	{
		return Double.longBitsToDouble(getLong());
	}

	@Override
	public float getFloat() 
    throws IOException	
	{
		return Float.intBitsToFloat(getInt());
	}

	@Override
	public int getInt() 
    throws IOException	
	{
		if (order == ByteOrder.BIG_ENDIAN)
		{		
			return 
				( _get() << 24) |
				( _get() << 16) | 
				( _get() << 8) |
				( _get() );
		} else {
			return 
				( _get() ) |
				( _get() << 8) |
				( _get() << 16) | 
				( _get() << 24);
		}
	}

	@Override
	public long getLong() 
    throws IOException	
	{
		if (order == ByteOrder.BIG_ENDIAN)
		{		
			return
			( ((long)_get()) << 56) |
			( ((long)_get()) << 48) | 
			( ((long)_get()) << 40) |
			( ((long)_get()) << 32) |		
			( ((long)_get()) << 24) |
			( ((long)_get()) << 16) | 
			( ((long)_get()) << 8) |
			( ((long)_get()) );		
		} else {		
			return 
			( ((long)_get())) |
			( ((long)_get()) << 8) |
			( ((long)_get()) << 16) | 
			( ((long)_get()) << 24) |
			( ((long)_get()) << 32) |		
			( ((long)_get()) << 40) |
			( ((long)_get()) << 48) | 
			( ((long)_get()) << 56);
		}
	}

	@Override
	public short getShort() 
    throws IOException	
	{
		if (order == ByteOrder.BIG_ENDIAN)
		{
			return (short) ( (_get() << 8) |  _get() ) ;
		} else {
			return (short) ( _get() | (_get() << 8) ) ;
		}
	}

	@Override
	public long limit() 
	{
		return limit;
	}
	
	@Override
	public long position() {
		return position;
	}
	
	public ByteOrder order() {
		return order;
	}

	public void order(ByteOrder order) {
		this.order = order;
	}

	public void skip(long bytes) throws IOException {
		is.skip(bytes);	
	}
	
}
