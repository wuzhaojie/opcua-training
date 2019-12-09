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
package org.opcfoundation.ua.transport.endpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.AsyncResult;
import org.opcfoundation.ua.transport.CloseableObjectState;
import org.opcfoundation.ua.transport.ServerSecureChannel;
import org.opcfoundation.ua.transport.impl.AsyncResultImpl;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.transport.tcp.impl.SecurityToken;
import org.opcfoundation.ua.utils.AbstractState;
import org.opcfoundation.ua.utils.StackUtils;

/**
 * Super class for endpoint secure channels.
 * 
 * Common mechanism:
 *  - Secure channel id
 *  - Security tokens
 *  - State & Error State
 */
public abstract class AbstractServerSecureChannel extends AbstractState<CloseableObjectState, ServiceResultException> implements ServerSecureChannel {

	/** Globally Unique Secure Channel ID */
	private int					secureChannelId;
	/** Collection of all Security Tokens */
	protected Map<Integer, SecurityToken> tokens = new ConcurrentHashMap<Integer, SecurityToken>();
	/** The active token, This token is used in write operations */
	protected SecurityToken			activeToken;
	
	/** Logger */
	static Logger logger = LoggerFactory.getLogger(AbstractServerSecureChannel.class);
	
	protected AbstractServerSecureChannel(int secureChannelId) {
		super(CloseableObjectState.Closed);
		this.secureChannelId = secureChannelId;
	}

	public int getSecureChannelId() {
		return secureChannelId;
	}

	public SecurityToken getActiveSecurityToken() {
		return activeToken;
	}
	
	public void setActiveSecurityToken(SecurityToken token) {
		if (token==null) 
			throw new IllegalArgumentException("null");
		logger.debug("Switching to new security token {}", token.getTokenId());
		this.activeToken = token;
		pruneInvalidTokens();
	}
	
	public synchronized SecurityToken getSecurityToken(int tokenId) {
		logger.debug("tokens({})={}", tokens.size(), tokens.values());
		return tokens.get(tokenId);
	}
	
	private void pruneInvalidTokens()
	{	
		if (logger.isDebugEnabled())
			logger.debug("pruneInvalidTokens: tokens({})={}", tokens.size(), tokens.values());
		for (SecurityToken t : tokens.values())
			if (!t.isValid()) {
				logger.debug("pruneInvalidTokens: remove Id={}", t.getTokenId());
				tokens.remove(t.getTokenId());
			}
	}

	public MessageSecurityMode getMessageSecurityMode() {
		SecurityToken token = getActiveSecurityToken();
		return token==null ? null : token.getMessageSecurityMode();
	}

	public SecurityPolicy getSecurityPolicy() {
		SecurityToken token = getActiveSecurityToken();
		return token==null ? null : token.getSecurityPolicy();
	}	
	
	public synchronized SecurityToken getLatestNonExpiredToken()
	{
		SecurityToken result = null;
		for (SecurityToken t : tokens.values())
		{
			if (t.isExpired()) continue;
			if (result==null) result = t;
			if (t.getCreationTime() > result.getCreationTime()) result = t;
		}
		return result;
	}
	
	public synchronized void setError(ServiceResultException e) {
		super.setError( e );
	}

	@Override
	protected void onListenerException(RuntimeException rte) {
		setError( StackUtils.toServiceResultException(rte) );
	}

	@Override
	public String toString() {
		return String.format("SecureChannelId=%d State=%s URL=%s RemoteAddress=%s",
				getSecureChannelId(), getState(), getConnectURL(), getRemoteAddress());
	}

	protected String getRemoteAddress() {
		if (getConnection() == null)
			return "(no connection)";
		return "" + getConnection().getRemoteAddress();
	}

	@Override
	public boolean isOpen() {
		return getState().isOpen();
	}

	@Override
	public void close() {
		if (getState()!=CloseableObjectState.Open) return;				
		setState(CloseableObjectState.Closing);
		setState(CloseableObjectState.Closed);
		logger.info("Channel closed: Id={}", getSecureChannelId());
		return;
	}

	@Override
	public AsyncResult<ServerSecureChannel> closeAsync() {
		AsyncResultImpl<ServerSecureChannel> result = new AsyncResultImpl<ServerSecureChannel>(); 
		if (getState()!=CloseableObjectState.Open) {
			result.setResult(this);
			return result;				
		}
		setState(CloseableObjectState.Closing);
		setState(CloseableObjectState.Closed);
		result.setResult(this);
		return result;
	}
	
}
