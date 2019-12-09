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
 * Servcer Socket states.
 * 
 * Initial states: Ready
 * Final states: Closed, Error
 * 
 * State transitions:
 *   Ready -> Bound -> Closed  
 *   Ready -> Bound -> Error
 *   Ready -> Closed  
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public enum ServerSocketState {
	
	Ready,		// Channel is open but not bound
	Bound,		// Server is bound and accepting connections 
	Error,		// Error occured. See getError(). 
	Closed;		// Channel has been closed and system resources have been released
	
	public static final EnumSet<ServerSocketState> OPEN_STATES = EnumSet.of(Ready, Bound);
	public static final EnumSet<ServerSocketState> ERROR_STATES = EnumSet.of(Error);
	public static final EnumSet<ServerSocketState> INITIAL_STATES = EnumSet.of(Ready);
	public static final EnumSet<ServerSocketState> FINAL_STATES = EnumSet.of(Closed, Error);
	public static final EnumSet<ServerSocketState> READY_TRANSITION_STATES = EnumSet.of(Bound, Closed);
	public static final EnumSet<ServerSocketState> BOUND_TRANSITION_STATES = EnumSet.of(Closed, Error);
		
}
