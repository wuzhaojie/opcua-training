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

package org.opcfoundation.ua.transport.tcp.nio;

import java.util.List;

import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.tcp.impl.ErrorMessage;
import org.opcfoundation.ua.transport.tcp.impl.SecurityToken;
import org.opcfoundation.ua.transport.tcp.impl.TcpMessageType;

public interface InputMessage {

	/**
	 * Get message if available. If message is not available, then error is.
	 * 
	 * @return message or null
	 */
	IEncodeable getMessage();
	
	/**
	 * Get error if avaiable.  
	 * 
	 * @return error or null
	 */
	Exception getError();
		
	/**
	 * Get message type. One of the following: 
	 *  {@link TcpMessageType#OPEN} Open Channel async message
	 *  {@link TcpMessageType#CLOSE} Close Channel async message
	 *  {@link TcpMessageType#MESSAGE} Service Request, or {@link ErrorMessage}
	 *  
	 * @return message type
	 */
	int getMessageType();
	
	/**
	 * Get secure channel Id. Secure channel is 0 when opening a new secure channel. 
	 * 
	 * @return.
	 */
	int getSecureChannelId();	
	
	/**
	 * Get request id. Identifier is secure channel specific.  
	 * 
	 * @return
	 */
	int getRequestId();	
	
	
	/**
	 * Return sequence number of each chunk
	 * @return list of sequence numbers
	 */
	List<Integer> getSequenceNumbers();
	
	/**
	 * Get security token
	 * 
	 * @return {@link SecurityConfiguration} if async message, {@link SecurityToken} is sync message
	 */
	Object getToken();

	/**
	 * Security policy uri for async message
	 * 
	 * @return
	 */
//	String getSecurityPolicyUri();

	/**
	 * 
	 * 
	 * @return
	 */
//	byte[] getSenderCertificate();

//	byte[] getReceiverCertificateThumbprint();
	
	
}
