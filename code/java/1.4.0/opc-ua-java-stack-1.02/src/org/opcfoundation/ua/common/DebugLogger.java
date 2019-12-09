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

package org.opcfoundation.ua.common;

import org.opcfoundation.ua.transport.CloseableObject;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.ConnectionMonitor.ConnectListener;
import org.opcfoundation.ua.transport.ServerConnection;
import org.opcfoundation.ua.transport.ServerConnection.SecureChannelListener;
import org.opcfoundation.ua.transport.ServerSecureChannel;
import org.opcfoundation.ua.utils.IStatefulObject;
import org.opcfoundation.ua.utils.StateListener;
import org.slf4j.Logger;

/**
 * Connect monitor prints to logger server's connect and secure channel events.
 * 
 * Example:
 * 		UABinding binding;
 *		binding.addConnectionListener(new ConnectMonitor()); 
 * 
 * @author Toni Kalajainen (toni.kalajainen@iki.fi)
 */
public class DebugLogger implements ConnectListener, SecureChannelListener, StateListener<CloseableObjectState> {
	Logger logger;
	public DebugLogger(Logger logger) {
		this.logger = logger;
	}
	public void onConnect(Object sender, ServerConnection connection) {
		logger.info("{}: {}", sender, connection);
		if (connection instanceof CloseableObject) {
			((CloseableObject)connection).addStateListener( this);
		
		}
		connection.addSecureChannelListener(this);		
		
	}
	@SuppressWarnings("unchecked")
	public void onSecureChannelAttached(Object sender, ServerSecureChannel channel) {
		logger.info("{}: {}", sender, channel);
		if (channel instanceof IStatefulObject<?, ?>)
		{
			IStatefulObject<CloseableObjectState, ServiceResultException> so = (IStatefulObject<CloseableObjectState, ServiceResultException>) channel;
			so.addStateListener(this);
		}
	}
	public void onStateTransition(IStatefulObject<CloseableObjectState, ?> sender, CloseableObjectState oldState, CloseableObjectState newState) {
		logger.info("{}: {}", sender, sender);
		if (sender.getError()!=null) {
			Throwable e = sender.getError();
			logger.debug("onStateTransition: failed", e);
		}			
	}
	
	@Override
	public void onSecureChannelDetached(Object sender, ServerSecureChannel channel) {
	}
	@Override
	public void onClose(Object sender, ServerConnection connection) {
		// TODO Auto-generated method stub
		
	}
}
