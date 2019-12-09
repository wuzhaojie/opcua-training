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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Writeable context
 *
 * @see OutputStreamWriteable
 * @see ByteBufferWriteable
 * @see ByteBufferArrayWriteable
 * @see ByteBufferArrayWriteable2
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public interface IBinaryWriteable {

    void put(byte b)
    throws IOException;
    
    /**
     * Put n bytes from the remaining of the byte array. 
     * This operation moves the pointer in byte buffer.
     * 
     * @param src
     * @throws IOException
     */
    void put(ByteBuffer src)
    throws IOException;
    
    /**
     * Put n bytes from the remaining of the byte buffer. 
     * This operation moves the pointer in byte buffer.
     * 
     * @param src
     * @param length
     * @throws IOException
     */
    void put(ByteBuffer src, int length)
    throws IOException;
    
    void put(byte[] src, int offset, int length)
    throws IOException;
    
    void put(byte[] src)
    throws IOException;
    
    void putShort(short value)
    throws IOException;
    
    void putInt(int value)
    throws IOException;
    
    void putLong(long value)
    throws IOException;
    
    void putFloat(float value)
    throws IOException;
    
    void putDouble(double value)
    throws IOException;
    	
	ByteOrder order();

	void order(ByteOrder order)
	throws IOException;

	void flush()
	throws IOException;
    
}
