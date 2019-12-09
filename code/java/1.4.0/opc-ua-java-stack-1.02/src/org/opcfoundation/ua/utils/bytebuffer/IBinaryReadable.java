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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



/**
 * IBinaryReadable is a readable stream.
 * 
 * IBinaryReadable throws {@link EOFException} if end of stream is reached.
 * 
 * @see ByteBufferReadable
 * @see InputStreamReadable
 * @see ByteBufferArrayReadable
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public interface IBinaryReadable {

    byte get()
    throws IOException;

    void get(byte[] dst, int offset, int length)
    throws IOException;
    
    void get(byte[] dst)
    throws IOException;
    
    /**
     * Get buf fully 
     * 
     * @param buf
     * @throws IOException
     */
    void get(ByteBuffer buf)
    throws IOException;
    
    /**
     * Get fully length bytes
     * 
     * @param buf
     * @param length
     * @throws IOException
     */
    void get(ByteBuffer buf, int length)
    throws IOException;
    
    short getShort()
    throws IOException;
    
    int getInt()
    throws IOException;
    
    long getLong()
    throws IOException;
    
    float getFloat()
    throws IOException;
    
    double getDouble()
    throws IOException;
    
	long limit() throws IOException;
	
	ByteOrder order();

	void order(ByteOrder order);
    
	long position() throws IOException;
    
}
