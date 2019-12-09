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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;

/**
 *
 * 
 */
public class ChunkUtils {

	private static Charset UTF8 = Charset.forName("UTF8"); 

	public static int getMessageType(ByteBuffer chunk)
	{
		chunk.position(0);
		return chunk.getInt();
	}
	
	public static int getSecureChannelId(ByteBuffer chunk)
	{
		chunk.position(8);
		return chunk.getInt();
	}
	
	public static int getTokenId(ByteBuffer chunk)
	{
		chunk.position(12);
		return chunk.getInt();
	}
	
	/**
	 * Get sequence number of a symmetric message
	 * @param chunk
	 * @return
	 */
	public static int getSequenceNumber(ByteBuffer chunk)
	{
		chunk.position(16);
		return chunk.getInt();
	}
	
	public static byte[] getRecvCertificateThumbprint(ByteBuffer chunk)
	{
		chunk.position(12);
		int policyUriLength = chunk.getInt();
		if (policyUriLength>0)
			chunk.position( chunk.position()+policyUriLength );
		int senderCertLength = chunk.getInt();
		if (senderCertLength>0)
			chunk.position( chunk.position()+senderCertLength );
		return getByteString(chunk);
	}
		
	public static int getRequestId(ByteBuffer chunk)
	{
		chunk.position(20);
		return chunk.getInt();
	}
	
	public static String getAbortMessage(ByteBuffer chunk)
	throws ServiceResultException
	{
		chunk.position(8);
		return getString(chunk);		
	}

	public static String getSecurityPolicyUri(ByteBuffer chunk)
	throws ServiceResultException
	{
		chunk.position(12);
		return getString(chunk);		
	}	
	
	public static String getString(ByteBuffer chunk)
	throws ServiceResultException
	{
		byte dada[] = getByteString(chunk);
		String result = new String(dada, UTF8);
		return result;
	}	

	public static byte[] getByteString(ByteBuffer chunk)
	{
		int length = chunk.getInt();
		if (length==-1) return null;
		if ( (length<-1) || (length > chunk.remaining()) ) 
			new ServiceResultException(StatusCodes.Bad_CommunicationError, "Unexpected length");
		byte dada[] = new byte[length];
		chunk.get(dada);
		return dada;
	}
	
	
	
}
