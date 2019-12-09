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

package org.opcfoundation.ua.transport.tcp.impl;

import java.lang.reflect.Field;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.encoding.IEncodeable;

/**
 * Hello is a message used in TCP Handshake.
 * 
 */
public class Hello implements IEncodeable {

	UnsignedInteger ProtocolVersion;
	UnsignedInteger ReceiveBufferSize;	// The largest message that the sender can receive
	UnsignedInteger SendBufferSize;		// The largest message that the sender will send
	UnsignedInteger MaxMessageSize;		// Max size for any response message
	UnsignedInteger MaxChunkCount;		// Max number of chunks in any response message
	String EndpointUrl;
	
	static
	{
		try {
			fields = new Field[]{
					Hello.class.getDeclaredField("ProtocolVersion"),
			Hello.class.getDeclaredField("ReceiveBufferSize"),
			Hello.class.getDeclaredField("SendBufferSize"),
			Hello.class.getDeclaredField("MaxMessageSize"),
			Hello.class.getDeclaredField("MaxChunkCount"),
			Hello.class.getDeclaredField("EndpointUrl")
			};
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
	}
	
	private static Field[] fields;
	
	/**
	 * @return the fields
	 */
	public static Field[] getFields() {
		return fields;
	}
	
	public Hello() {}
	
	public Hello(
			UnsignedInteger protocolVersion, 
			UnsignedInteger receiveBufferSize,
			UnsignedInteger sendBufferSize,
			UnsignedInteger maxMessageSize,
			UnsignedInteger maxChunkCount,
			String endpointUrl
			) {
		EndpointUrl = endpointUrl;
		MaxChunkCount = maxChunkCount;
		MaxMessageSize = maxMessageSize;
		ProtocolVersion = protocolVersion;
		ReceiveBufferSize = receiveBufferSize;
		SendBufferSize = sendBufferSize;
	}

	public UnsignedInteger getProtocolVersion() {
		return ProtocolVersion;
	}

	public void setProtocolVersion(UnsignedInteger protocolVersion) {
		ProtocolVersion = protocolVersion;
	}

	public UnsignedInteger getReceiveBufferSize() {
		return ReceiveBufferSize;
	}

	public void setReceiveBufferSize(UnsignedInteger receiveBufferSize) {
		ReceiveBufferSize = receiveBufferSize;
	}

	public UnsignedInteger getSendBufferSize() {
		return SendBufferSize;
	}

	public void setSendBufferSize(UnsignedInteger sendBufferSize) {
		SendBufferSize = sendBufferSize;
	}

	public UnsignedInteger getMaxChunkCount() {
		return MaxChunkCount;
	}

	public void setMaxChunkCount(UnsignedInteger maxChunkCount) {
		MaxChunkCount = maxChunkCount;
	}

	public String getEndpointUrl() {
		return EndpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		EndpointUrl = endpointUrl;
	}

	public UnsignedInteger getMaxMessageSize() {
		return MaxMessageSize;
	}

	public void setMaxMessageSize(UnsignedInteger maxMessageSize) {
		MaxMessageSize = maxMessageSize;
	}
		
	
}
