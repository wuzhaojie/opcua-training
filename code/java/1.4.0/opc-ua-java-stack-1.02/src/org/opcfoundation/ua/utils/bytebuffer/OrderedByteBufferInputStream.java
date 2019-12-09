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
import java.util.TreeMap;

/**
 * Input stream with a sequence of ByteBuffers as backend.
 * ByteBuffers can be submitted in random order.
 * Input stream sleeps until data becomes available.
 * Sequence number determines the order of how the data becomes visible 
 * to the input stream.   
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class OrderedByteBufferInputStream extends InputStream {

	/** Active byte buffer */
	ByteBuffer cur;
	/** Current sequence number */
	int next = 0;
	/** last sequence number */
	int last = -1;
	/** close sequence number */
	int close = -1;
	/** Sorted byte buffers */
	TreeMap<Integer, ByteBuffer> bufs = new TreeMap<Integer, ByteBuffer>();
	
	public OrderedByteBufferInputStream() {
		super();
	}
	
	/**
	 * Submits a byte buffer to the use of input stream
	 * @param sequenceNumber
	 * @param buf
	 */
	public synchronized void offer(int sequenceNumber, ByteBuffer buf)
	{			
		if (sequenceNumber<0 || bufs.containsKey(sequenceNumber) || sequenceNumber<next)
			throw new IllegalArgumentException("sequence number");
		if (close>0 && sequenceNumber>=close)
			throw new RuntimeException("Cannot put data at "+sequenceNumber+" because the stream was closed at "+close);
		if (sequenceNumber>last)
			last = sequenceNumber;
		bufs.put(sequenceNumber, buf);
		this.notify();
	}
	
	/**
	 * Submits a byte buffer for the input stream to use
	 * 
	 * @param buf
	 */
	public void offer(ByteBuffer buf)
	{
		offer(last+1, buf);
	}

	public void close()
	{
		close(last+1);
	}
	
	public synchronized void close(int sequenceNumber)
	{
		if (close>=0) return; //throw new RuntimeException("Already closed");
		if (sequenceNumber<0 || sequenceNumber<=last)
			throw new IllegalArgumentException("sequence number illegal");
		close = sequenceNumber;
		notifyAll();
	}
	
	public synchronized void forceClose()
	{
		close = next;
		notifyAll();
	}
	
	/**
	 * Returns a byte buffer with data or null if end of stream 
	 * @return byte buffer with data or null if end of stream
	 */
	private synchronized ByteBuffer getByteBuffer() 
	throws InterruptedIOException {
		if (cur!=null && cur.hasRemaining()) return cur;
		if (cur!=null && !cur.hasRemaining()) cur = null;
		while (cur==null) {
			if (next>=close && close>0) return null;
			cur = bufs.remove(next);
			if (cur==null)
				try {
					wait();
				} catch (InterruptedException e) {
					throw new InterruptedIOException();
				}
			else { 
				next++;
				if (!cur.hasRemaining()) cur = null;
			}
		}
		return cur;
	}
	
	@Override
	public synchronized int read() throws IOException {
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
			buf.put(b, off, n);
			off += n;
			bytesRead += n;
			len -= n;
		}
		return bytesRead;
	}
	
	@Override
	public synchronized int available() throws IOException {
		int result = 0;
		if (cur!=null) result += cur.remaining();
		for (int i = next; i<=last; i++) {
			ByteBuffer b = bufs.get(i);
			if (b==null) break;
			result += b.remaining();
		}
		return result;
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
