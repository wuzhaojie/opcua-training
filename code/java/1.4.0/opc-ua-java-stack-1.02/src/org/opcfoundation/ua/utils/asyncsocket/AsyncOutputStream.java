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

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Asyncronous output stream of asynchronous socket.
 * There are two positions properties: Bytes written and bytes flushed.
 * <p>
 * Flushing of data can be monitored asyncronously with Alarm object.
 * e.g.
 *    byte[] data;
 *    long pos = os.getPosition();
 *    os.write(data);
 *    Alarm a = os.createAlarm(pos + data.length, flushListener);
 *    
 * 
 * @see BufferMonitor
 * @see AsyncSocketImpl
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public abstract class AsyncOutputStream extends OutputStream {

	/**
	 * Write to stream
	 * 
	 * @param src
	 */
	public abstract void write(ByteBuffer src);
	
	/**
	 * Write to stream
	 * 
	 * @param src
	 * @param length
	 */
	public abstract void write(ByteBuffer src, int length);
	
	/**
	 * Offers byte buffer to the output stream for write. The ownership of the byte buffer
	 * and its back-end will be taken over by the stream.
	 * 
	 * @param buf buffer to offer
	 */
	public abstract void offer(ByteBuffer buf);

	/**
	 * Get the position of stream that has been flushed. This position lags behind getPosition() value
	 * 
	 * @return bytes flushed from the stream
	 */
	public abstract long getFlushPosition();
	
	/**
	 * Get the position of the stream 
	 * 
	 * @return number of bytes written to the stream
	 */
	public abstract long getPosition();
	
	/**
	 * Get number of bytes remaining to be written
	 * @return the number of unflushed bytes
	 */
	public abstract long getUnflushedBytes();
	
	/**
	 * Create an object that monitors for flush position of the output stream.
	 *  
	 * @param position position to trigger
	 * @param flushListener alarm listener
	 * @return alarm
	 */
	public abstract BufferMonitor createMonitor(long position, MonitorListener flushListener);	
	
}
