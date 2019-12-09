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

import java.util.EnumSet;

/**
 * Server Socket states.
 * 
 * Initial states: Waiting
 * Final states: Triggered, Canceled, Error
 * 
 * State transitions:
 *   Waiting -> Triggered  
 *   Waiting -> Canceled
 *   Waiting -> Closed
 *   Waiting -> Error 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public enum BufferMonitorState {
	
	Waiting,		// Alarm is waiting 
	Triggered,		// Alarm has been triggered
	Canceled,		// Canceled by user
	Closed,			// Stream was closed before trigger position could be reached
	Error;			// Error occured before trigger position could be reached

	public static final EnumSet<BufferMonitorState> FINAL_STATES = EnumSet.of(Triggered, Canceled, Error, Closed);
	public static final EnumSet<BufferMonitorState> UNREACHABLE = EnumSet.of(Error, Closed);
	
	public boolean isFinal() {
		return FINAL_STATES.contains(this);
	}
	
	/**
	 * Tests if the state is unreachable
	 * 
	 * @return if true the state is unreachable
	 */
	public boolean isUnreachable() {
		return this == Error || this == Closed;
	}
	
}
