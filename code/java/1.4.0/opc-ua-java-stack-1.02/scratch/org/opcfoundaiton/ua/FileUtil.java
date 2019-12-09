package org.opcfoundaiton.ua;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.regex.Pattern;

public class FileUtil {

	/**
	 * Copy file or directory recursively.
	 * 
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
    public static void copy(File src, File dst) throws IOException
    {
    	// doc.isv doesn't need .svn folders for anything
    	if ( src.getName().equals(".svn") ) return;
    	
    	if (src.isDirectory()) {    		 
    		if(!dst.exists()) dst.mkdir();
 
    		for (String file : src.list()) {
    		   File srcFile = new File(src, file);
    		   File dstFile = new File(dst, file);
    		   copy(srcFile,dstFile);
    		}
    	}else{
    		if ( !src.exists() ) return;
            if (!dst.exists()) dst.createNewFile();

            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(src).getChannel();
                destination = new FileOutputStream(dst).getChannel();

                long count = 0;
                long size = source.size();
                while (count < size) {
                    count += destination.transferFrom(source, count, size - count);
                }
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
    	}
    }
    
    public static File findFile(File root, Pattern filePattern)
    {
    	if ( !root.exists() ) return null;
    	for (File file : root.listFiles())
    	{
    		if ( file.isDirectory() ) {
    			File f = findFile(file, filePattern);
    			if ( f!=null ) return f;
    		}
    		if ( file.isFile() ) {
    			if (filePattern.matcher(file.getName()).matches()) return file;
    		}
    	}
    	return null;
    }
	
    public static void copyStream(InputStream is, OutputStream out)
    throws IOException
    {
    	ReadableByteChannel ic = Channels.newChannel(is);
    	WritableByteChannel oc = Channels.newChannel(out);
    	try {    		
    	    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    	    while (ic.read(buffer) != -1) {
    	    	buffer.flip();
    	    	oc.write(buffer);
    	    	buffer.compact();
    	    }
    	    buffer.flip();
    	    while (buffer.hasRemaining()) {
    	    	oc.write(buffer);
    	    }
    	} finally {
    		ic.close();
    		oc.close();
    	}
    }
    
    public static void writeFile(File file, InputStream src)
    throws IOException
    {
    	if ( file.exists() ) file.delete();
    	file.createNewFile();
    	FileOutputStream fos = new FileOutputStream( file );
    	try {
    		copyStream(src, fos);
    	} finally {
    		fos.close();
    	}
    }

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
     * Delete a directory incl. all files and sub-directories.
     * 
     * @param dir
     * @boolean if true all files were successfully deleted
     */
    public static boolean deleteDir(File dir) {
        boolean result = true;

        if (!dir.isDirectory()) return false;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) result &= deleteDir(f);
            if (f.isFile()) result &= f.delete();
        }
        boolean ok = dir.delete();
//		if (!ok) dir.deleteOnExit();
        result &= ok;
        return result;
    }    
    
}
