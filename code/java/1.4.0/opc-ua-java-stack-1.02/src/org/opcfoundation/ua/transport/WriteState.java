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

package org.opcfoundation.ua.transport;

import java.util.EnumSet;

/**
 * Message write states.
 * 
 * Initial states: Queued
 * Final states: Flushed, Error
 * 
 * The only allowed state transition paths: 
 *   Queued -> Writing -> Flushed 
 *   Queued -> Writing -> Error
 *    
 * @see AsyncWrite
 */
public enum WriteState {
	Ready,
	Queued,			// Message has been placed in write queue
	Writing,		// Message is being written
	Written,		// Message has been written. As always with internet, transmission is not quaranteed.
	Canceled,		// Message was canceled
	Error;			// Error by stack or abortion. (e.g. connection closed, aborted). See getException().
	
	public static final EnumSet<WriteState> FINAL_STATES = EnumSet.of(Written, Error, Canceled); 
	public static final EnumSet<WriteState> CANCELABLE_STATES = EnumSet.of(Ready, Queued); 
	
	public boolean isFinal()
	{
		return this==Error || this==Written || this==Canceled;
	}
}
