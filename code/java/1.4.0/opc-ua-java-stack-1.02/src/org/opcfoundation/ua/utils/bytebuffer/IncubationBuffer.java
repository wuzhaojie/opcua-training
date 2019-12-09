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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.opcfoundation.ua.utils.IncubationQueue;

/**
 * Input stream with a sequence of ByteBuffers as backend.
 * The data in ByteBuffers in read in the order they are "incubated"
 * The data becomes available when the ByteBuffers are "hatched"
 * Input stream blocks until data becomes available.
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class IncubationBuffer extends InputStream {

	protected final static ByteBuffer CLOSED_MARKER = ByteBuffer.allocate(0);
	protected IncubationQueue<ByteBuffer> queue = new IncubationQueue<ByteBuffer>(true);
	protected ByteBuffer cur;
	
	public IncubationBuffer() {
		super();
	}
	
	/**
	 * Submits a byte buffer to the use of input stream
	 * @param buf byte buffer to offer for use
	 */
	public void incubate(ByteBuffer buf)
	{
		synchronized(queue) {
		queue.incubate(buf);
		}
	}
	
	/**
	 * Makes the byte buffer available to input stream reader
	 * @param buf
	 */
	public void hatch(ByteBuffer buf)
	{
		synchronized(queue) {
		queue.hatch(buf);
		}
	}
	
	public void close()
	{
		synchronized(queue) {
		queue.incubate(CLOSED_MARKER);
		queue.hatch(CLOSED_MARKER);
		}
	}
	
	public void forceClose()
	{
		synchronized(queue) {
		queue.clear();
		queue.incubate(CLOSED_MARKER);
		queue.hatch(CLOSED_MARKER);
		}
	}
	
	/**
	 * Returns a byte buffer with data or null if end of stream 
	 * @return byte buffer with data or null if end of stream
	 */
	private ByteBuffer getByteBuffer() 
	throws InterruptedIOException {
		synchronized(queue) {
		if (cur==CLOSED_MARKER) return null;
		if (cur!=null && cur.hasRemaining()) return cur;
		if (cur!=null && !cur.hasRemaining()) cur = null;
		try {
			cur = queue.removeNextHatched();
		} catch (InterruptedException e) {
			throw new InterruptedIOException();
		}
		if (cur==CLOSED_MARKER) return null;
		return cur;
		}
	}
	
	@Override
	public int read() throws IOException {
		ByteBuffer b = getByteBuffer();
		if (b==null) return -1;		
		return b.get() & 0xff;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = 0;
		while (len>0) {
			ByteBuffer buf = getByteBuffer();
			if (buf==null) return (bytesRead>0) ? bytesRead : -1;
			int n = Math.min(buf.remaining(), len);
			buf.get(b, off, n);
			off += n;
			bytesRead += n;
			len -= n;
		}
		return bytesRead;
	}

	@Override
	public int available() throws IOException {
		synchronized(queue) {
		
		int result = 0;
		if (cur!=null) result += cur.remaining();
		Iterator<ByteBuffer> i = queue.iterator();
		while (i.hasNext()) {
			ByteBuffer o = i.next();
			if (!queue.isHatched(o)) break;
			result += o.remaining();
		}
		return result;
		
		}
	}
	
	/**
	 * Get all offered chunks. Available only after stream is closed 
	 * @return
	 */
/*
    [Ei toimi koska puskurit poistetaan getByteBuffer]	
	public synchronized ByteBuffer[] getChunks() {
		if (close<0) throw new RuntimeException("Not closed");
		ByteBuffer[] result = new ByteBuffer[close];
		for (int i=0; i<close; i++)
			result[i] = bufs.get(i);
		return result;
	}
*/
}
