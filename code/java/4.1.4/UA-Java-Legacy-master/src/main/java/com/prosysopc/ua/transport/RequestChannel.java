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

package com.prosysopc.ua.transport;

import com.prosysopc.ua.builtintypes.ServiceRequest;
import com.prosysopc.ua.builtintypes.ServiceResponse;
import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.core.StatusCodes;
import com.prosysopc.ua.encoding.IEncodeable;

/**
 * RequestChannel is a channel to do service requests with.
 */
public interface RequestChannel {

	/**
	 * Sends a request over the secure channel. <p>
	 *
	 * If the operation timeouts or the thread is interrupted a
	 * ServiceResultException is thrown with {@link StatusCodes#Bad_Timeout}.<p>
	 *
	 * @param request the request
	 * @return the response
	 * @throws ServiceResultException if error
	 */
	IEncodeable serviceRequest(ServiceRequest request) throws ServiceResultException;
	
	/**
	 * Asynchronous operation to send a request over the secure channel.
	 *
	 * @param request the request
	 * @return the result
	 */
	AsyncResult<ServiceResponse> serviceRequestAsync(ServiceRequest request);
	
}
