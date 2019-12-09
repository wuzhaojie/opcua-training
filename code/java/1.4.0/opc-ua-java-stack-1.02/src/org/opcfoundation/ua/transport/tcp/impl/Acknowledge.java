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
 * Acknowledge is a message used in TCP Handshake.
 * 
 */
public class Acknowledge implements IEncodeable {

	
	UnsignedInteger ProtocolVersion;
	UnsignedInteger ReceiveBufferSize;	// Largest chunk receiver will receive
	UnsignedInteger SendBufferSize;		// Largest chunk sender will send
	UnsignedInteger MaxMessageSize;		// Max size for any request message
	UnsignedInteger MaxChunkCount;		// Max number of chunks in request message
	
	static
	{
		try {
			fields = new Field[]{
					Acknowledge.class.getDeclaredField("ProtocolVersion"),
			Acknowledge.class.getDeclaredField("ReceiveBufferSize"),
			Acknowledge.class.getDeclaredField("SendBufferSize"),
			Acknowledge.class.getDeclaredField("MaxMessageSize"),
			Acknowledge.class.getDeclaredField("MaxChunkCount")
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

	public Acknowledge() {}

	public Acknowledge(
			UnsignedInteger protocolVersion,
			UnsignedInteger receiveBufferSize, 
			UnsignedInteger sendBufferSize,
			UnsignedInteger maxMessageSize, 
			UnsignedInteger maxChunkCount
			) {
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

	public UnsignedInteger getMaxMessageSize() {
		return MaxMessageSize;
	}

	public void setMaxMessageSize(UnsignedInteger maxMessageSize) {
		MaxMessageSize = maxMessageSize;
	}

	public UnsignedInteger getMaxChunkCount() {
		return MaxChunkCount;
	}

	public void setMaxChunkCount(UnsignedInteger maxChunkCount) {
		MaxChunkCount = maxChunkCount;
	}
	
}
