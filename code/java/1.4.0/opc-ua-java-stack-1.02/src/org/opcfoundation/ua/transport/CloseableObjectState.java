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
 * Generic object states.
 * 
 * Initial states: Closed, Opening
 * Final states: Closed
 * 
 * State transition paths: 
 *   Closed
 *   Closed-> Opening -> Open -> Closing -> Closed 
 *   Opening -> Open -> Closing -> Closed
 * 
 */
public enum CloseableObjectState {

	Closed,			// Closed
	Opening,		// Opening 
	Open,			// Open
	Closing;		// Closing
	
	public final static EnumSet<CloseableObjectState> CLOSED_STATES = EnumSet.of(Closed, Opening); 
	public final static EnumSet<CloseableObjectState> OPEN_STATES = EnumSet.of(Open, Closing); 
	public final static EnumSet<CloseableObjectState> POST_OPENING_STATES = EnumSet.of(Open, Closing, Closed); 
	
	public boolean isOpen()
	{
		return this == Open || this == Closing;
	}
	
	public boolean isClosed()
	{
		return this == Closed || this == Opening;
	}
	
}
