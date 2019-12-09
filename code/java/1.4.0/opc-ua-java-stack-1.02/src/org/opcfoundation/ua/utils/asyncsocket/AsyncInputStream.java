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

package org.opcfoundation.ua.utils.asyncsocket;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Asyncronous input stream of asynchronous socket.
 * There are two positions properties: Bytes received (to buffer) and bytes read.
 * <p>
 * Synchronous reads can be made with use of alarm, e.g.
 *     BufferMonitor a = is.createBufferMonitor(is.getPosition() + 1000, null);
 *     a.waitForState(AlarmState.FINAL_STATES);
 *     byte[] data = new byte[1000];
 *     is.read(data); 
 *
 * <p>
 * Note there is a limit (40kb) how much data is buffered. To up the limit set an alarm.
 * Creating an alarm to position Long.MAX_VALUE removes any limits of the buffer. 
 *
 * @see AsyncSocketImpl 
 * @see BufferMonitor
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public abstract class AsyncInputStream extends InputStream {
	
	/**
	 * Reads some number of bytes from the input stream and stores them into
	 * the buffer array <code>b</code>. The number of bytes actually read is
	 * returned as an integer.  This method blocks until input data is
	 * available, end of file is detected, or an exception is thrown.
	 *
	 * @param      b   the buffer into which the data is read.
	 * @return     the total number of bytes read into the buffer, or
	 *             <code>-1</code> is there is no more data because the end of
	 *             the stream has been reached.
	 * @see        java.io.InputStream#read(byte[], int, int)
	 */
	public abstract int read(byte b[]);
	
    /**
	 * Reads up to <code>len</code> bytes of data from the input stream into
	 * an array of bytes.  An attempt is made to read as many as
	 * <code>len</code> bytes, but a smaller number may be read.
	 * The number of bytes actually read is returned as an integer.
	 *
	 * @param      b     the buffer into which the data is read.
	 * @param      off   the start offset in array <code>b</code>
	 *                   at which the data is written.
	 * @param      len   the maximum number of bytes to read.
	 * @return     the total number of bytes read into the buffer, or
	 *             <code>-1</code> if there is no more data because the end of
	 *             the stream has been reached.
	 * @see        java.io.InputStream#read()
	 */
	public abstract int read(byte b[], int off, int len);
	
	/**
	 * Get the number of bytes received to this stream
	 * 
	 * @return bytes received
	 */
	public abstract long getReceivedBytes();
	
	/**
	 * Get stream position.
	 * 
	 * @return number of bytes read 
	 */
	public abstract long getPosition();
	
	/**
	 * Create an object that monitors the position input stream. Alarm is triggered when buffer reaches
	 * the requested position.
	 * <t>
	 * The buffer size is enlarged when a monitor with a position outside the buffer range is created.
	 * For instance, there is an input stream. Its position is 5, buffer size is 5. It received data to 10 and stops.
	 * If monitor at position 20 is created, the stream received 10 more additional bytes.   
	 * 
	 * @param position position at which to trigger an alarm
	 * @param listener alarm listener
	 * @return alarm
	 */
	public abstract BufferMonitor createMonitor(long position, MonitorListener listener);

	/**
	 * Get the number of bytes the stream buffers from TCP stack.
	 * @return buffer size
	 */
	public abstract int getBufferSize();
	
	/**
	 * Set a hint number for buffering. Buffer reads ahead values from TCP Stack.
	 * The given number is minimum for buffering.  
	 * 
	 * @param size new buffer size value
	 */
	public abstract void setBufferSize(int size);
	
	/**
	 * Read to byte buffer
	 * 
	 * @param dst
	 */
	public abstract void read(ByteBuffer dst);
	
	/**
	 * Read the given number of bytes to byte buffer.
	 * 
	 * @param dst
	 * @param length
	 */
	public abstract void read(ByteBuffer dst, int length);
	
	/**
	 * Read from the stream. Returns a readable byte buffer.
	 * 
	 * @param len
	 * @return byte buffer
	 */
	public abstract ByteBuffer read(int len);
	
	/**
	 * Read from the input stream. Returns readable byte buffers.
	 * This is the most efficient read method as it does not copy byte data.
	 * 
	 * @param len
	 * @return byte buffer
	 */
	public abstract ByteBuffer[] readChunks(int len);	
	
	/**
	 * Peek ahead into buffered content. Use available() to see how many bytes there 
	 * is buffered. Use setBufferSize() to control how much to buffer. 
	 * 
	 * @param buf
	 */
	public abstract void peek(byte[] buf);
	
	/**
	 * Peek bytes to buffer 
	 * @param buf
	 * @param off
	 * @param len
	 */
	public abstract void peek(byte[] buf, int off, int len);
	
	/**
	 * Peek bytes to returned buffer
	 * 
	 * @param len
	 * @return byte buffer 
	 */
	public abstract ByteBuffer peek(int len);
	
	/**
	 * Peek bytes to byte chunks.
	 * (Efficient for big chunks as there is no byte copying)
	 * 
	 * @param len
	 * @return byte buffer array
	 */
	public abstract ByteBuffer[] peekChunks(int len);
	
	
	/**
	 * Return the number of bytes available in the buffer. 
	 */
	public abstract int available();
	
}
