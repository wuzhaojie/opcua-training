/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.transport.tcp.nio;

import com.prosysopc.ua.transport.tcp.nio.SecureInputMessageBuilder.MessageListener;

/**
 * <p>Channel interface.</p>
 *
 */
public interface Channel {

	/**
	 * <p>addChannelListener.</p>
	 *
	 * @param listener a {@link SecureInputMessageBuilder.MessageListener} object.
	 */
	void addChannelListener(MessageListener listener);
	/**
	 * <p>removeChannelListener.</p>
	 *
	 * @param listener a {@link SecureInputMessageBuilder.MessageListener} object.
	 */
	void removeChannelListener(MessageListener listener);	
	
	public static interface ChannelListener {
		
		/**
		 * Handle input message.
		 * 
		 * @param message incoming message
		 * @return true if message is handled, false if not
		 */
		boolean handleMessage(InputMessage message);
		
	}
	
}
