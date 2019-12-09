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
 * Socket states.
 * 
 * Initial states: Ready
 * Final states: Error, Closed
 * 
 * State transitions: 
 *   Ready -> Connecting -> Connected -> Closed
 *   Ready -> Connecting -> Connected -> Error
 *   Ready -> Connecting -> Error
 *   Ready -> Closed
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public enum SocketState {
	
	Ready,				// Initial state
	Connecting,			// Connecting
	Connected,			// Connected
	Error,				// Closed with Error, see getError()
	Closed;				// Closed and unusable
	
	public static final EnumSet<SocketState> OPEN_STATES = EnumSet.of(Ready, Connecting, Connected);
	public static final EnumSet<SocketState> ERROR_STATES = EnumSet.of(Error);
	public static final EnumSet<SocketState> INITIAL_STATES = EnumSet.of(Ready);
	public static final EnumSet<SocketState> NON_FINAL_STATES = EnumSet.of(Ready, Connecting, Connected);
	public static final EnumSet<SocketState> FINAL_STATES = EnumSet.of(Closed, Error);
	public static final EnumSet<SocketState> CONNECTED_TRANSITION_STATES = EnumSet.of(Closed, Error);
	public static final EnumSet<SocketState> CONNECTING_TRANSITION_STATES = EnumSet.of(Closed, Connected, Error);
	
	public boolean isFinal()
	{
		return FINAL_STATES.contains(this);
	}
	
}
