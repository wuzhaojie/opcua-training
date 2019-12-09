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

package org.opcfoundation.ua.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class StreamUtil {

	/**
	 * Reads entire resource from an input stream
	 * 
	 * @param is
	 * @return resource
	 * @throws IOException
	 */
	public static byte[] readFully(InputStream is) throws IOException {
		byte[] buf = new byte[4096];		
		ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
		for (;;) {
			int bytesRead = is.read(buf);
			if (bytesRead == -1)
				break;
			os.write(buf, 0, bytesRead);
		}

		return os.toByteArray();
	}
	
    public static void copyStream(InputStream is, OutputStream out)
    throws IOException
    {
    	ReadableByteChannel ic = Channels.newChannel(is);
    	WritableByteChannel oc = Channels.newChannel(out);
    	try {    		
    	    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    	    while (ic.read(buffer) != -1) {
    	    	buffer.flip();
    	    	oc.write(buffer);
    	    	buffer.compact();
    	    }
    	    buffer.flip();
    	    while (buffer.hasRemaining()) {
    	    	oc.write(buffer);
    	    }
    	} finally {
    		ic.close();
    		oc.close();
    	}
    }

	public static void read(InputStream is, ByteBuffer buf, int bytes)
	throws IOException
	{
		while (bytes>0 & buf.hasRemaining()) {
			int n = is.read(buf.array(), buf.position(), bytes);
			if (n < 0) throw new EOFException();
			buf.position( buf.position() + n );
			bytes -= n;
		}
	}
	
	public static void readFully(InputStream is, ByteBuffer buf)
	throws IOException
	{
		while (buf.hasRemaining()) {			
			int n = is.read(buf.array(), buf.position(), buf.remaining());
			if (n < 0) throw new EOFException();
			buf.position( buf.position() + n );
		}
	}
	
	public static void readFully(InputStream is, byte[] b)
	throws IOException
	{
		readFully(is, b, 0, b.length);		
	}
	
	public static void readFully(InputStream is, byte[] b, int off, int len)
	throws IOException
	{
		while (len > 0) {
			int n = is.read(b, off, len);
			if (n < 0) throw new EOFException();
			off += n;
			len -= n;
		}
	}
}
