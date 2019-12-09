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

package org.opcfoundation.ua.utils.bytebuffer;

import java.nio.ByteBuffer;

/**
 *
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class ByteBufferUtils {

	/**
	 * Copies as much as possible
	 * @param src
	 * @param dst
	 */
	public static void copyRemaining(ByteBuffer src, ByteBuffer dst)
	{
		int n = Math.min(src.remaining(), dst.remaining());
		copy(src, dst, n);
	}
	
	public static void copy(ByteBuffer src, ByteBuffer dst, int length)
	{
		int srcLimit = src.limit();
		int dstLimit = dst.limit();
		src.limit(src.position() + length);
		dst.limit(dst.position() + length);
		dst.put(src);
		src.limit(srcLimit);
		dst.limit(dstLimit);		
	}		
	
	/**
	 * Concatenate two arrays to one
	 * @param chunks 
	 * @return concatenation of all chunks
	 */
	public static byte[] concatenate(byte[]...chunks)
	{
		int len = 0;
		for (byte[] chunk : chunks)
			len += chunk.length;
		byte result[] = new byte[len];
		int pos = 0;
		for (byte[] chunk : chunks)
		{
			System.arraycopy(chunk, 0, result, pos, chunk.length);
			pos += chunk.length;			
		}
		return result;
	}
	
	
}
