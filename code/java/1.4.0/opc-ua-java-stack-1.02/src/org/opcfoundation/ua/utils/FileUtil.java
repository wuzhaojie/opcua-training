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

package org.opcfoundation.ua.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import org.opcfoundation.ua.utils.bytebuffer.ByteQueue;


public class FileUtil {

    /**
     * Creates and writes a binary file 
     * @param file file
     * @param data data
     * @throws IOException on i/o problems
     */    
    public static void writeFile(File file, byte[] data)
    throws IOException
    {        
        file.createNewFile();
        file.setWritable(true);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(data.length);
            raf.seek(0);
            raf.write(data);            
        } finally {
            raf.close();
        }
    }

    /**
     * Reads entire binary file 
     * @param file file
     * @return contents of binary file
     * @throws IOException on i/o problems
     */    
    public static byte[] readFile(File file)
    throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        try {
            long size = file.length();
            if (size>Integer.MAX_VALUE) 
                throw new IOException("File too big");
            int len = (int) size;
            byte data [] = new byte[len];
            int pos = 0;
            
            while (pos<size) {
                int read = fis.read(data, pos, len-pos);
                pos += read;
            }
            return data;
        } finally {
            fis.close();
        }
    }
    
    /**
     * Reads entire binary file to a byte array
     *  
     * @param url 
     * @return contents of binary file
     * @throws IOException on i/o problems
     */    
    public static byte[] readFile(URL url)
    throws IOException
    {    	
    	ByteQueue q = new ByteQueue();
        InputStream is = url.openStream();
        try {
        	byte[] buf = new byte[1024];
        	for (;;) {
        		int bytesRead = is.read(buf);
        		if (bytesRead==-1) break;
        		q.put(buf, 0, bytesRead);
        	}  
            byte[] result = new byte[ (int) q.getBytesWritten() ];
            q.get(result);
            return result;
        } finally {
            is.close();
        }
    }    
    
    /**
     * Reads entire binary file to a byte array.
     * Note the stream is not closed.
     *  
     * @param is input stream
     * @return contents of the stream 
     * @throws IOException on i/o problems
     */    
    public static byte[] readStream(InputStream is)
    throws IOException
    {    	
    	ByteQueue q = new ByteQueue();
        byte[] buf = new byte[1024];
        for (;;) {
        	int bytesRead = is.read(buf); 
        	if (bytesRead==-1) break;
        	q.put(buf, 0, bytesRead);
        }  
        byte[] result = new byte[ (int) q.getBytesWritten() ];
        q.get(result);
        return result;
    }        

    
}
