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

package org.opcfoundation.ua.transport.tcp.io;

import java.util.concurrent.atomic.AtomicInteger;

import org.opcfoundation.ua.utils.StackUtils;

/**
 * Secure Channel sequence number
 */
public class SequenceNumber {	
	
	/**
	 * Sequence number used for output 
	 */
	private AtomicInteger sendSequenceNumber = new AtomicInteger( 1 /*StackUtils.RANDOM.nextInt()*/ );
	
	/**
	 * Sequence number used for input
	 */
	private AtomicInteger recvSequenceNumber = null;	
	
	/**
	 * Check whether recv sequence number has been set. 
	 * 
	 * @return true if setRecvSequenceNumber has been called
	 */
	public boolean hasRecvSequenceNumber() {
		return recvSequenceNumber != null;
	}

	/** 
	 * Get current recv sequence number
	 *  
	 * @return recv number or null
	 */
	public Integer getRecvSequenceNumber() {
		return recvSequenceNumber == null ? null : recvSequenceNumber.get();
	}
	
	/** 
	 * Get next recv sequence number
	 *  
	 * @return recv number or null
	 */
	public Integer getNextRecvSequenceNumber() {
		return recvSequenceNumber == null ? null : recvSequenceNumber.incrementAndGet();
	}
	
	
	public void setRecvSequenceNumber(int value) {
		recvSequenceNumber = new AtomicInteger(value);
	}
	
	/**
	 * Tests whether value matches expected sequence number and sets a new value.
	 * 
	 * If value has never been set before the test passes and new value is set.
	 * 
	 * Test passes if the value is one larger than previous value or if previous value is
	 * 4294966271 or larger.
	 * 
	 * @param value value to test
	 * @return true if value matches
	 */
	public boolean testAndSetRecvSequencenumber(int newValue) {
		if (recvSequenceNumber==null) {
			recvSequenceNumber = new AtomicInteger(newValue);
			return true;
		}
		int oldValue = recvSequenceNumber.get();
		boolean wrapAround = (oldValue & 0x00000000ffffffff) >= 4294966271L;
		boolean exactMatch = oldValue +1 == newValue;
		boolean wrapMatch = wrapAround & (newValue<1024);
		boolean match = exactMatch | wrapMatch;
		
		if (match) 
			recvSequenceNumber.set(newValue);
		
		return match;
	}
	
	/**
	 * Get the next send sequence number.
	 * Send sequence number wraps between 4294966271 and 4294967295 to a new
	 * value that is below 1024.
	 * 
	 * @return send sequnce numner.
	 */
	public int getNextSendSequencenumber() {
		long oldValue = sendSequenceNumber.get() & 0x00000000ffffffff;
		boolean mustWrap = oldValue == 4294967295L;
		boolean canWrap = oldValue >= 4294966271L;
		boolean wraps = mustWrap || (canWrap && StackUtils.RANDOM.nextBoolean()); 
		long newValue = oldValue +1;
		
		if (wraps) newValue = StackUtils.RANDOM.nextInt(1024);
		sendSequenceNumber.set( (int) newValue );
		return (int) newValue;
	}
	
	public int getCurrentSendSequenceNumber() {
		return sendSequenceNumber.get();
	}
	
	public void setCurrentSendSequenceNumber(int newValue) {
		sendSequenceNumber.set(newValue);
	}	
	
}
