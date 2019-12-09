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

/** 
 * Tcp connection quotas. Quota values are negotiated between client and server. 
 * The negotiated values are stored in {@link TcpConnectionLimits}.
 */
public class TcpQuotas {

	public static final TcpQuotas DEFAULT_CLIENT_QUOTA = new TcpQuotas(Integer.MAX_VALUE, TcpMessageLimits.MaxBufferSize, TcpMessageLimits.DefaultChannelLifetime, TcpMessageLimits.DefaultSecurityTokenLifeTime); 
	public static final TcpQuotas DEFAULT_SERVER_QUOTA = new TcpQuotas(Integer.MAX_VALUE, TcpMessageLimits.MaxBufferSize, TcpMessageLimits.DefaultChannelLifetime, TcpMessageLimits.DefaultSecurityTokenLifeTime); 
	
	public final int maxMessageSize;
	public final int maxBufferSize;
	public final int channelLifetime;
	public final int securityTokenLifetime;
	
	public TcpQuotas(int maxMessageSize, int maxBufferSize,
			int channelLifetime, int securityTokenLifetime) {
		
		if (maxBufferSize < TcpMessageLimits.MinBufferSize)
			throw new IllegalArgumentException();
		
		this.maxMessageSize = maxMessageSize;
		this.maxBufferSize = maxBufferSize;
		this.channelLifetime = channelLifetime;
		this.securityTokenLifetime = securityTokenLifetime;
	}
	
}
