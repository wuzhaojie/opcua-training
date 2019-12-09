/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.utils;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An OutputStream that only calculates the number of bytes written to it.
 *
 */
public class SizeCalculationOutputStream extends OutputStream{

	private final AtomicInteger calc;
	
	/**
	 * Create new {@link SizeCalculationOutputStream} with byte count of 0.
	 */
	public SizeCalculationOutputStream() {
		calc = new AtomicInteger();
	}
	
	@Override
	public void write(int b) {
		calc.incrementAndGet();
	}
	
	@Override
	public void write(byte[] b, int off, int len) {
		calc.addAndGet(len);
	}
	
	@Override
	public void write(byte[] b) {
		//NOTE, if b is null will throw NPE, which the the same logic as in OutputStream.
		calc.addAndGet(b.length);
	}
	
	/**
	 * Gets current byte count of the stream.
	 */
	public int getLength() {
		return calc.get();
	}

	/**
	 * Resets current byte count to 0.
	 */
	public void reset() {
		calc.set(0);
	}
	
}
