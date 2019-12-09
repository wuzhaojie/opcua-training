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

package org.opcfoundation.ua.encoding.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.UUID;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.Enumeration;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Structure;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedLong;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.IDecoder;
import org.opcfoundation.ua.encoding.IEncodeable;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;
import org.opcfoundation.ua.utils.bytebuffer.ByteBufferReadable;
import org.opcfoundation.ua.utils.bytebuffer.IBinaryReadable;
import org.opcfoundation.ua.utils.bytebuffer.InputStreamReadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes builtinTypes, structures, enumerations and messages from byte buffer.
 * + Arrays
 * 
 * 
 * @see IDecoder
 * @see BinaryEncoder
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class BinaryDecoder implements IDecoder {

	private static final Charset UTF8 = Charset.forName("utf-8");
	private static final Logger logger = LoggerFactory.getLogger(BinaryDecoder.class);
	
	IBinaryReadable in;
	EncoderContext ctx; 

	public BinaryDecoder(IBinaryReadable in)
	{
		setReadable(in);
	}
	
	public BinaryDecoder(InputStream is, int limit)
	{
		InputStreamReadable isr = new InputStreamReadable(is, limit);
		isr.order(ByteOrder.LITTLE_ENDIAN);
		setReadable(isr);
	}
	
	/**
	 * Create byte buffer decoder. Byte buffer must be in little-endian order.
	 * @param buf
	 */
	public BinaryDecoder(ByteBuffer buf)
	{
		setReadable(new ByteBufferReadable(buf));		
	}
	
	public BinaryDecoder(byte[] buf)
	{
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		setReadable( new ByteBufferReadable(bb) );
	}
	
	public BinaryDecoder(byte[] buf, int off, int len)
	{
		ByteBuffer bb = ByteBuffer.wrap(buf, off, len);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		setReadable( new ByteBufferReadable(bb) );
	}
	
	public IBinaryReadable getReadable()
	{
		return in;
	}
	
	public void setReadable(IBinaryReadable in)
	{
		if (in.order() != ByteOrder.LITTLE_ENDIAN)
			throw new IllegalArgumentException("Readable must be in Little-Ending byte order");		
		this.in = in;
	}
	
	public EncoderContext getEncoderContext() {
		return ctx;
	}

	public void setEncoderContext(EncoderContext ctx) {
		this.ctx = ctx;
	}
	
	protected long remaining() 
	throws DecodingException
	{
		try {
			return in.limit()-in.position();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	/**
	 * Assert array length is within restrictions
	 * @param len
	 * @throws DecodingException
	 */
	private void assertArrayLength(int len)
	throws DecodingException
	{
		if (len<-1) throw new DecodingException(StatusCodes.Bad_DecodingError, "Illegal array length "+len);
		int maxLen = ctx.getMaxArrayLength();
		if (maxLen>0 && len>maxLen) 
			throw new DecodingException(StatusCodes.Bad_EncodingLimitsExceeded, "MaxArrayLength="+maxLen+" < "+len);
	}
	
	/**
	 * Assert string length is within restrictions
	 * @param len
	 * @throws DecodingException
	 */
	private void assertStringLength(int len)
	throws DecodingException
	{
		if (len<-1) throw new DecodingException(StatusCodes.Bad_DecodingError, "Unexpected string length "+len);
		int maxLen = ctx.getMaxStringLength();
		if (maxLen>0 && len>maxLen) 
			throw new DecodingException(StatusCodes.Bad_EncodingLimitsExceeded, "MaxStringLength "+maxLen+" < "+len);
	}
	
	/**
	 * Assert bytestring length is within restrictions
	 * @param len
	 * @throws DecodingException
	 */
	private void assertByteStringLength(int len)
	throws DecodingException
	{
		if (len<-1) throw new DecodingException(StatusCodes.Bad_DecodingError, "Unexpected byte string length "+len);
		int maxLen = ctx.getMaxByteStringLength();
		if (maxLen>0 && len>maxLen) 
			throw new DecodingException(StatusCodes.Bad_EncodingLimitsExceeded, "MaxByteStringLength "+maxLen+" < "+len);
	}	
	
	private static DecodingException toDecodingException(IOException e) 
	{
		if (e instanceof ClosedChannelException)
			return new DecodingException(StatusCodes.Bad_ConnectionClosed, e);
		if (e instanceof EOFException)
			return new DecodingException(StatusCodes.Bad_EndOfStream, e);
		if (e instanceof ConnectException)
			return new DecodingException(StatusCodes.Bad_ConnectionRejected, e);
		if (e instanceof SocketException)
			return new DecodingException(StatusCodes.Bad_UnexpectedError, e);
		return new DecodingException(StatusCodes.Bad_UnexpectedError, e);		
	}
	
	public Boolean getBoolean(String fieldName)
    throws DecodingException	
	{
		try {
			if (in.get()==0) return Boolean.FALSE;
			return Boolean.TRUE;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Boolean[] getBooleanArray(String fieldName)
    throws DecodingException	
	{
		try{
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Boolean[] result = new Boolean[len];
			for (int i=0; i<len; i++)
				result[i] = getBoolean(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Byte getSByte(String fieldName)
    throws DecodingException	
	{
		try {
			return in.get();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Byte[] getSByteArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Byte[] result = new Byte[len];
			for (int i=0; i<len; i++)
				result[i] = getSByte(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public UnsignedByte getByte(String fieldName)
    throws DecodingException	
	{
		try {
			return UnsignedByte.getFromBits( (byte) in.get() );		
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public UnsignedByte[] getByteArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			UnsignedByte[] result = new UnsignedByte[len];
			for (int i=0; i<len; i++)
				result[i] = getByte(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Short getInt16(String fieldName)
    throws DecodingException	
	{
		try {
			return in.getShort();
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public Short[] getInt16Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*2>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Short[] result = new Short[len];
			for (int i=0; i<len; i++)
				result[i] = getInt16(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public UnsignedShort getUInt16(String fieldName)
    throws DecodingException	
	{
		try {
			return UnsignedShort.getFromBits( (short) in.getShort() );
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public UnsignedShort[] getUInt16Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*2>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			UnsignedShort[] result = new UnsignedShort[len];
			for (int i=0; i<len; i++)
				result[i] = getUInt16(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	public Integer getInt32(String fieldName)
    throws DecodingException	
	{
		try {
			return in.getInt();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Integer[] getInt32Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Integer[] result = new Integer[len];
			for (int i=0; i<len; i++)
				result[i] = getInt32(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}		
	
	public int[] getInt32Array_(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			int[] result = new int[len];
			for (int i=0; i<len; i++)
				result[i] = getInt32(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}		
	
	public UnsignedInteger getUInt32(String fieldName)
    throws DecodingException	
	{
		try {
			return UnsignedInteger.getFromBits( in.getInt() );
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public UnsignedInteger[] getUInt32Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			UnsignedInteger[] result = new UnsignedInteger[len];
			for (int i=0; i<len; i++)
				result[i] = getUInt32(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Long getInt64(String fieldName)
    throws DecodingException	
	{
		try {
			return in.getLong();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Long[] getInt64Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*8>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Long[] result = new Long[len];
			for (int i=0; i<len; i++)
				result[i] = getInt64(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}	
	
	public UnsignedLong getUInt64(String fieldName)
    throws DecodingException	
	{
		try {
			return UnsignedLong.getFromBits( in.getLong() );
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public UnsignedLong[] getUInt64Array(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*8>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			UnsignedLong[] result = new UnsignedLong[len];
			for (int i=0; i<len; i++)
				result[i] = getUInt64(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Float getFloat(String fieldName)
    throws DecodingException	
	{
		try {
			return in.getFloat();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Float[] getFloatArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Float[] result = new Float[len];
			for (int i=0; i<len; i++)
				result[i] = getFloat(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public Double getDouble(String fieldName)
    throws DecodingException	
	{
		try {
			return in.getDouble();
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public Double[] getDoubleArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*8>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Double[] result = new Double[len];
			for (int i=0; i<len; i++)
				result[i] = getDouble(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public String getString(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertStringLength(len);			
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			byte dada[] = new byte[len];
			in.get(dada);
			return new String(dada, UTF8);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public String[] getStringArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			String[] result = new String[len];
			for (int i=0; i<len; i++)
				result[i] = getString(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}	
	
	public DateTime getDateTime(String fieldName)
    throws DecodingException	
	{
		try {
			long v = in.getLong();
			DateTime t = new DateTime(v);
			if(t.compareTo(DateTime.MAX_VALUE) > 0) {
				return DateTime.MAX_VALUE;
			} else if (t.compareTo(DateTime.MIN_VALUE) < 0) {
				return DateTime.MIN_VALUE;
			} else {
				return t;
			}
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public DateTime[] getDateTimeArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*8>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			DateTime[] result = new DateTime[len];
			for (int i=0; i<len; i++)
				result[i] = getDateTime(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public UUID getGuid(String fieldName)
    throws DecodingException	
	{
		try {
			byte[] enc = new byte[16];
			
			enc[3] = in.get();
			enc[2] = in.get();
			enc[1] = in.get();
			enc[0] = in.get();
			enc[5] = in.get();
			enc[4] = in.get();
			enc[7] = in.get();
			enc[6] = in.get();
			for (int i = 8; i < 16; i++)
				enc[i] = in.get();;

			long hiBits = 0;
			long loBits = 0;
			for (int i = 0; i < 8; i++)
				hiBits = (hiBits << 8) | (enc[i] & 0xff);
			for (int i = 8; i < 16; i++)
				loBits = (loBits << 8) | (enc[i] & 0xff);
			
			return new UUID(hiBits, loBits);
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public UUID[] getGuidArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*16>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			UUID[] result = new UUID[len];
			for (int i=0; i<len; i++)
				result[i] = getGuid(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}	
	
	public byte[] getByteString(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertByteStringLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			byte dada[] = new byte[len];
			in.get(dada);
			return dada;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	public byte[][] getByteStringArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			byte[][] result = new byte[len][];
			for (int i=0; i<len; i++)
				result[i] = getByteString(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	public XmlElement getXmlElement(String fieldName)
    throws DecodingException	
	{
		byte[] data = getByteString(fieldName);
		if ( data == null ) return null;
		return new XmlElement( data );
//		String str = getString(null);
//		if (str==null) return null;
//		return new XmlElement(str);
	}
	
	public XmlElement[] getXmlElementArray(String fieldName)
    throws DecodingException	
	{	
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			XmlElement[] result = new XmlElement[len];
			for (int i=0; i<len; i++)
				result[i] = getXmlElement(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	

	public NodeId getNodeId(String fieldName)
    throws DecodingException	
	{
		try {
			byte encodingByte = in.get();		
			NodeIdEncoding encoding = NodeIdEncoding.getNodeIdEncoding( encodingByte );
			if (encoding == null) 
				throw new DecodingException("Unexpected NodeId Encoding Byte "+encodingByte);
		
			NodeId result;
			int namespaceIndex = 0;
		
			if (encoding == NodeIdEncoding.TwoByte)
			{
				namespaceIndex = 0;
				UnsignedInteger id = UnsignedInteger.getFromBits( in.get() & 0xff );
				result = new NodeId(namespaceIndex, id);
			}
			else
			if (encoding == NodeIdEncoding.FourByte)
			{
				namespaceIndex = in.get() & 0xff;
				UnsignedInteger id = UnsignedInteger.getFromBits( in.getShort() & 0xffff);
				result = new NodeId(namespaceIndex, id);
			}
			else
			if (encoding == NodeIdEncoding.Numeric)
			{
				namespaceIndex = in.getShort() & 0xffff;
				UnsignedInteger id = getUInt32(null);
				result = new NodeId(namespaceIndex, id);
			}
			else
			if (encoding == NodeIdEncoding.String)
			{
				namespaceIndex = in.getShort() & 0xffff;
				String id = getString(null);
				result = new NodeId(namespaceIndex, id);
			}
			else
			if (encoding == NodeIdEncoding.ByteString)
			{
				namespaceIndex = in.getShort() & 0xffff;
				byte[] id = getByteString(null);
				result = new NodeId(namespaceIndex, id);
			}
			else
			if (encoding == NodeIdEncoding.Guid)
			{
				namespaceIndex = in.getShort() & 0xffff;
				UUID id = getGuid(null);
				result = new NodeId(namespaceIndex, id);
			}
			else
				throw new DecodingException("Unsupported NodeId Encoding byte "+encoding);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public NodeId[] getNodeIdArray(String fieldName)
    throws DecodingException	
	{		
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*2>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			NodeId[] result = new NodeId[len];
			for (int i=0; i<len; i++)
				result[i] = getNodeId(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	public ExpandedNodeId getExpandedNodeId(String fieldName)
    throws DecodingException	
	{
		try {
			byte encodingByte = in.get();
			boolean hasServerIndex		= (encodingByte & 0x40) == 0x40;
			boolean hasNamespaceUri		= (encodingByte & 0x80) == 0x80;
		
			NodeIdEncoding encoding		= NodeIdEncoding.getNodeIdEncoding( encodingByte & 0x3f );
			if (encoding == null) 
				throw new DecodingException("Unexpected NodeId Encoding Byte "+encodingByte);
			
			Object id					= null;
			int namespaceIndex			= 0;
			String namespaceUri			= null;
			UnsignedInteger serverIndex	= null;
		
			if (encoding == NodeIdEncoding.TwoByte)
			{
				namespaceIndex = 0;
				id = new UnsignedInteger( in.get() );
			}
		
			if (encoding == NodeIdEncoding.FourByte)
			{
				namespaceIndex = in.get() & 0xff;
				id = new UnsignedInteger( in.getShort() & 0xffff );
			}
			
			if (encoding == NodeIdEncoding.Numeric)
			{
				namespaceIndex = in.getShort() & 0xffff;
				id = UnsignedInteger.getFromBits( in.getInt() );
			}
		
			if (encoding == NodeIdEncoding.String)
			{
				namespaceIndex = in.getShort() & 0xffff;
				id = getString(null);
			}
		
			if (encoding == NodeIdEncoding.ByteString)
			{
				namespaceIndex = in.getShort() & 0xffff;
				id = getByteString(null);
			}
		
			if (encoding == NodeIdEncoding.Guid)
			{
				namespaceIndex = in.getShort() & 0xffff;
				id = getGuid(null);
			}
		
			if (hasNamespaceUri) namespaceUri = getString(null);				
			if (hasServerIndex) serverIndex = getUInt32(null);  		
		
			if (namespaceUri!=null)
				return new ExpandedNodeId(serverIndex, namespaceUri, id);
			else
				return new ExpandedNodeId(serverIndex, namespaceIndex, id);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public ExpandedNodeId[] getExpandedNodeIdArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*2>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			ExpandedNodeId[] result = new ExpandedNodeId[len];
			for (int i=0; i<len; i++)
				result[i] = getExpandedNodeId(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}		
	
	public StatusCode getStatusCode(String fieldName)
    throws DecodingException	
	{
		return new StatusCode( getUInt32(null) );
	}
	
	public StatusCode[] getStatusCodeArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			StatusCode[] result = new StatusCode[len];
			for (int i=0; i<len; i++)
				result[i] = getStatusCode(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public QualifiedName getQualifiedName(String fieldName)
    throws DecodingException	
	{
		UnsignedShort namespaceIndex	= getUInt16(null);
		String name						= getString(null);
		//if (name==null) return null; //test 21.5.
		if (name==null) return new QualifiedName( namespaceIndex, null );
		return new QualifiedName( namespaceIndex, name );
	}
	
	public QualifiedName[] getQualifiedNameArray(String fieldName)
    throws DecodingException	
	{	
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*6>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			QualifiedName[] result = new QualifiedName[len];
			for (int i=0; i<len; i++)
				result[i] = getQualifiedName(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}			
	
	public LocalizedText getLocalizedText(String fieldName)
    throws DecodingException	
	{
		try {
			int encodingMask	= in.get();
			String locale		= null;
			String text			= null;
			if ((encodingMask & 1)==1) locale = getString(null);
			if ((encodingMask & 2)==2) text = getString(null);				
			return new LocalizedText(text, locale);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public LocalizedText[] getLocalizedTextArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			LocalizedText[] result = new LocalizedText[len];
			for (int i=0; i<len; i++)
				result[i] = getLocalizedText(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}		
	
	@Override
	public Structure getStructure(String fieldName) 
	throws DecodingException 
	{
		try {
			NodeId typeId = getNodeId(null);
			int encodingByte = in.get();
			if (encodingByte==0) return null;
			if (encodingByte==1) {						
				Class<? extends IEncodeable> clazz = ctx.getEncodeableClass(typeId);
				getInt32(null);			 // length
				return (Structure) getEncodeable(fieldName, clazz);
			}
			if (encodingByte==2) {
				throw new DecodingException("XML Decoder is not implemented");			
			}
			throw new DecodingException("Unexpected encoding byte ("+encodingByte+") in ExtensionObject");
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	@Override
	public Structure[] getStructureArray(String fieldName) 
	throws DecodingException 
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Structure[] result = new Structure[len];
			for (int i=0; i<len; i++)
				result[i] = getStructure(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public ExtensionObject getExtensionObject(String fieldName)
    throws DecodingException	
	{
		try {
			NodeId typeId = getNodeId(null);
			ExpandedNodeId expandedNodeId = ctx.getNamespaceTable().toExpandedNodeId(typeId);
			int encodingByte = in.get();
			if (encodingByte==0) {
				//			if (typeId.equals(NodeId.NULL)) return null;
				if ((typeId == null) || typeId.equals(NodeId.NULL)) return null;
				return new ExtensionObject(expandedNodeId);
			}
			if (encodingByte==1) return new ExtensionObject(expandedNodeId, getByteString(null));
			if (encodingByte==2) return new ExtensionObject(expandedNodeId, getXmlElement(null));
			throw new DecodingException("Unexpected encoding byte ("+encodingByte+") in ExtensionObject");
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public ExtensionObject[] getExtensionObjectArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*3>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			ExtensionObject[] result = new ExtensionObject[len];
			for (int i=0; i<len; i++)
				result[i] = getExtensionObject(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	public DataValue getDataValue(String fieldName)
    throws DecodingException	
	{
		try {
			int encodingMask = in.get();
			Variant value					= (encodingMask &    1)!=0 ? getVariant(null) : null;
			StatusCode status				= (encodingMask &    2)!=0 ? getStatusCode(null) : StatusCode.GOOD;
			DateTime sourceTimeStamp		= (encodingMask &    4)!=0 ? getDateTime(null) : null;//DateTime.MIN_VALUE;
			UnsignedShort sourcePicoSeconds	= (encodingMask & 0x10)!=0 ? getUInt16(null) : UnsignedShort.MIN_VALUE;
			DateTime serverTimeStamp		= (encodingMask &    8)!=0 ? getDateTime(null) : null;//DateTime.MIN_VALUE;
			UnsignedShort serverPicoSeconds	= (encodingMask & 0x20)!=0 ? getUInt16(null) : UnsignedShort.MIN_VALUE;		
			return new DataValue(value, status, sourceTimeStamp, sourcePicoSeconds, serverTimeStamp, serverPicoSeconds);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public DataValue[] getDataValueArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			DataValue[] result = new DataValue[len];
			for (int i=0; i<len; i++)
				result[i] = getDataValue(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	public Variant getVariant(String fieldName)
    throws DecodingException	
	{
		try {
			int encodingMask				= in.get();
			int builtinType					= encodingMask & 0x3f;
			boolean isArray					= (encodingMask & 0x80) == 0x80;
			boolean hasDimensionLengths		= (encodingMask & 0x40) == 0x40;
			boolean isNull					= builtinType == 0; // XXX Assumption. null in specs, not specified how.  
			Object value					= isNull ? null : isArray ? getArrayObject(null, builtinType) : getScalarObject(null, builtinType);
			int[] dims						= hasDimensionLengths ? getInt32Array_(null) : null;
			boolean multiDimension			= isArray && dims != null && dims.length>1;

			if (value instanceof ExtensionObject) {
				ExtensionObject extobj = (ExtensionObject) value;
				try {
					value = extobj.decode(ctx);
				} catch (DecodingException e) {
					logger.info("Failed to decode ExtensionObject", e);
					value = extobj;
				}
			}
		
			if (isArray)
				try {
					if (multiDimension)
						// Build multi-dimension
						value = MultiDimensionArrayUtils.demuxArray((Object[]) value, dims);
					if (value instanceof ExtensionObject[]) {
						ExtensionObject[] values = (ExtensionObject[]) value;
						try {
							value = ctx.decode(values);
						} catch (Exception e) {
							value = values;
						}
					}
				} catch (IllegalArgumentException e) {
					throw new DecodingException("The length of ArrayDimensions-field does not match Value-field");
				}
			return new Variant( value );
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}

	public Variant[] getVariantArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len = in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Variant[] result = new Variant[len];
			for (int i=0; i<len; i++)
				result[i] = getVariant(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}		
	
	public DiagnosticInfo getDiagnosticInfo(String fieldName)
    throws DecodingException	
	{
		try {
			int encodingMask = in.get();
			Integer symbolicId					= (encodingMask &    1)!=0 ? getInt32(null) : null;
			Integer namespaceUri				= (encodingMask &    2)!=0 ? getInt32(null) : null;
			Integer localizedText				= (encodingMask &    4)!=0 ? getInt32(null) : null;
			Integer locale						= (encodingMask &    8)!=0 ? getInt32(null) : null;
			String additionalInfo				= (encodingMask & 0x10)!=0 ? getString(null) : null;
			StatusCode innerStatusCode			= (encodingMask & 0x20)!=0 ? getStatusCode(null) : null;
			DiagnosticInfo innerDiagnosticInfo	= (encodingMask & 0x40)!=0 ? getDiagnosticInfo(null) : null;
				
			return new DiagnosticInfo(additionalInfo, innerDiagnosticInfo, innerStatusCode, locale, localizedText, namespaceUri, symbolicId);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}
	
	public DiagnosticInfo[] getDiagnosticInfoArray(String fieldName)
    throws DecodingException	
	{
		try {
			int len 					= in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			DiagnosticInfo[] result = new DiagnosticInfo[len];
			for (int i=0; i<len; i++)
				result[i] = getDiagnosticInfo(null);
			return result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}	
	
	@SuppressWarnings("unchecked")
	public <T extends Enumeration> T[] getEnumerationArray(String fieldName, Class<T> enumerationClass)
    throws DecodingException	
	{
		try {
			int len					= in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*4>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Object result = Array.newInstance(enumerationClass, len);
			for (int i=0; i<len; i++)
			{
				Object value = getEnumeration(null, enumerationClass);
				Array.set(result, i, value);
			}
			return (T[]) result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Enumeration> T getEnumeration(String fieldName, Class<T> enumerationClass)
    throws DecodingException	
	{
		try {
			int value 					= in.getInt();
			Method m = enumerationClass.getMethod("valueOf", int.class);
			return (T) m.invoke(null, value);
		} catch (SecurityException e) {
			throw new DecodingException(e, "cannot decode "+enumerationClass);
		} catch (NoSuchMethodException e) {
			throw new DecodingException(e, "cannot decode "+enumerationClass);
		} catch (IllegalArgumentException e) {
			throw new DecodingException(e, "cannot decode "+enumerationClass);
		} catch (IllegalAccessException e) {
			throw new DecodingException(e, "cannot decode "+enumerationClass);
		} catch (InvocationTargetException e) {
			throw new DecodingException(e, "cannot decode "+enumerationClass);
		} catch (IOException e) {
			throw toDecodingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends IEncodeable> T[] getEncodeableArray(String fieldName, Class<? extends T> encodeableClass)
    throws DecodingException	
	{
		try {
			int len			= in.getInt();
			if (len==-1) return null;
			assertArrayLength(len);
			if (len*2>remaining()) throw new DecodingException(StatusCodes.Bad_EndOfStream, "Buffer underflow");
			Object result = Array.newInstance(encodeableClass, len);
			for (int i=0; i<len; i++)
			{
				Object value = ctx.getEncodeableSerializer().getEncodeable(encodeableClass, this);
				Array.set(result, i, value);
			}
			return (T[]) result;
		} catch (IOException e) {
			throw toDecodingException(e);
		}			
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IEncodeable> T getEncodeable(String fieldName, Class<? extends T> encodeableClass)
    throws DecodingException	
	{
		return (T) ctx.getEncodeableSerializer().getEncodeable(encodeableClass, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String fieldName, Class<T> clazz) throws DecodingException {
		if (clazz.equals(Boolean.class)) {
			return (T) getBoolean(fieldName);
		}
		if (clazz.equals(Byte.class)) {
			return (T) getSByte(fieldName);
		}
		if (clazz.equals(UnsignedByte.class)) {
			return (T) getByte(fieldName);
		}
		if (clazz.equals(Short.class)) {
			return (T) getInt16(fieldName);
		}
		if (clazz.equals(UnsignedShort.class)) {
			return (T) getUInt16(fieldName);
		}
		if (clazz.equals(Integer.class)) {
			return (T) getInt32(fieldName);
		}
		if (clazz.equals(UnsignedInteger.class)) {
			return (T) getUInt32(fieldName);
		}
		if (clazz.equals(Long.class)) {
			return (T) getInt64(fieldName);
		}
		if (clazz.equals(UnsignedLong.class)) {
			return (T) getUInt64(fieldName);
		}
		if (clazz.equals(Float.class)) {
			return (T) getFloat(fieldName);
		}
		if (clazz.equals(Double.class)) {
			return (T) getDouble(fieldName);
		}
		if (clazz.equals(String.class)) {
			return (T) getString(fieldName);
		}
		if (clazz.equals(DateTime.class)) {
			return (T) getDateTime(fieldName);
		}
		if (clazz.equals(UUID.class)) {
			return (T) getGuid(fieldName);
		}
		if (clazz.equals(byte[].class)) {
			return (T) getByteString(fieldName);
		}
		if (clazz.equals(XmlElement.class)) {
			return (T) getXmlElement(fieldName);
		}
		if (clazz.equals(NodeId.class)) {
			return (T) getNodeId(fieldName);
		}
		if (clazz.equals(ExpandedNodeId.class)) {
			return (T) getExpandedNodeId(fieldName);
		}
		if (clazz.equals(StatusCode.class)) {
			return (T) getStatusCode(fieldName);
		}
		if (clazz.equals(QualifiedName.class)) {
			return (T) getQualifiedName(fieldName);
		}
		if (clazz.equals(LocalizedText.class)) {
			return (T) getLocalizedText(fieldName);
		}
		if (clazz.equals(ExtensionObject.class)) {
			return (T) getExtensionObject(fieldName);
		}
		if (Structure.class.isAssignableFrom(clazz)) {
			return (T) getEncodeable(fieldName, (Class<? extends IEncodeable>) clazz);
		}
		if (clazz.equals(DataValue.class)) {
			return (T) getDataValue(fieldName);
		}
		if (clazz.equals(Variant.class)) {
			return (T) getVariant(fieldName);
		}
		if (clazz.equals(Object.class)) {
			return (T) getVariant(fieldName).getValue();
		}
		if (clazz.equals(DiagnosticInfo.class)) {
			return (T) getDiagnosticInfo(fieldName);
		}
		if (clazz.equals(Boolean[].class)) {
			return (T) getBooleanArray(fieldName);
		}
		if (clazz.equals(Byte[].class)) {
			return (T) getSByteArray(fieldName);
		}
		if (clazz.equals(UnsignedByte[].class)) {
			return (T) getByteArray(fieldName);
		}
		if (clazz.equals(Short[].class)) {
			return (T) getInt16Array(fieldName);
		}
		if (clazz.equals(UnsignedShort[].class)) {
			return (T) getUInt16Array(fieldName);
		}
		if (clazz.equals(Integer[].class)) {
			return (T) getInt32Array(fieldName);
		}
		if (clazz.equals(UnsignedInteger[].class)) {
			return (T) getUInt32Array(fieldName);
		}
		if (clazz.equals(Long[].class)) {
			return (T) getInt64Array(fieldName);
		}
		if (clazz.equals(UnsignedLong[].class)) {
			return (T) getUInt64Array(fieldName);
		}
		if (clazz.equals(Float[].class)) {
			return (T) getFloatArray(fieldName);
		}
		if (clazz.equals(Double[].class)) {
			return (T) getDoubleArray(fieldName);
		}
		if (clazz.equals(String[].class)) {
			return (T) getStringArray(fieldName);
		}
		if (clazz.equals(DateTime[].class)) {
			return (T) getDateTimeArray(fieldName);
		}
		if (clazz.equals(UUID[].class)) {
			return (T) getGuidArray(fieldName);
		}
		if (clazz.equals(byte[][].class)) {
			return (T) getByteStringArray(fieldName);
		}
		if (clazz.equals(XmlElement[].class)) {
			return (T) getXmlElementArray(fieldName);
		}
		if (clazz.equals(NodeId[].class)) {
			return (T) getNodeIdArray(fieldName);
		}
		if (clazz.equals(ExpandedNodeId[].class)) {
			return (T) getExpandedNodeIdArray(fieldName);
		}
		if (clazz.equals(StatusCode[].class)) {
			return (T) getStatusCodeArray(fieldName);
		}
		if (clazz.equals(QualifiedName[].class)) {
			return (T) getQualifiedNameArray(fieldName);
		}
		if (clazz.equals(LocalizedText[].class)) {
			return (T) getLocalizedTextArray(fieldName);
		}
		if (clazz.equals(ExtensionObject[].class)) {
			return (T) getExtensionObjectArray(fieldName);
		}
		if (clazz.getComponentType() != null && Structure.class.isAssignableFrom(clazz.getComponentType())) {
			return (T) getEncodeableArray(fieldName, (Class<? extends Structure>) clazz.getComponentType());
		}
		if (clazz.equals(DataValue[].class)) {
			return (T) getDataValueArray(fieldName);
		}
		if (clazz.equals(Variant[].class)) {
			return (T) getVariantArray(fieldName);
		}
		if (clazz.equals(Object[].class)) {
			Variant[] varArray = getVariantArray(fieldName);
			Object[] objArray = new Object[varArray.length];
			for (int i = 0; i < varArray.length; i++)
				objArray[i] = varArray[i].getValue();
			return (T) objArray;
		}
		if (clazz.equals(DiagnosticInfo[].class)) {
			return (T) getDiagnosticInfoArray(fieldName);
		}
		if (Enumeration.class.isAssignableFrom(clazz)) {
			return (T) getEnumeration(fieldName, (Class<? extends Enumeration>) clazz);
		}
		if (clazz.getComponentType() != null && Enumeration.class.isAssignableFrom(clazz.getComponentType())) {
			return (T) getEnumerationArray(fieldName, (Class<? extends Enumeration>) clazz);
		}
		throw new DecodingException("Cannot decode "+clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends IEncodeable> T getMessage()
    throws DecodingException	
	{
		NodeId	id 					= getNodeId(null);
		if (id==null) throw new DecodingException("Cannot decode "+id);
		Class<T> clazz = (Class<T>) ctx.getEncodeableClass(id);
		if (clazz==null) throw new DecodingException("Cannot decode "+id);
		return (T) ctx.getEncodeableSerializer().getEncodeable(clazz, this);
	}

//	public IEncodeable getStructure()
//    throws DecodingException	
//	{
//		NodeId	id 					= getNodeId();
//		EncodeableDesc info				= encodeableTable.get(id);
//		if (info==null)
//			throw new DecodingException("Cannot decode "+id);
//		Byte encodeByte  			= getSByte();
//		if (encodeByte == 2)
//			throw new DecodingException("XML Decoding not supported");
//		if (encodeByte != 1)
//			throw new DecodingException("Unexpected encode type "+encodeByte);		
//		int length					= getInt32();
//		if (length<0)
//			throw new DecodingException("Unexpected length "+length);
//		
//		return getEncodeable(info);
//	}

	public Object getScalarObject(String fieldName, int builtinTypeId)
    throws DecodingException	
	{
		switch (builtinTypeId) {
		case 1: return getBoolean(null);
		case 2: return getSByte(null);
		case 3: return getByte(null);
		case 4: return getInt16(null);
		case 5: return getUInt16(null);
		case 6: return getInt32(null);
		case 7: return getUInt32(null);
		case 8: return getInt64(null);
		case 9: return getUInt64(null);
		case 10: return getFloat(null);
		case 11: return getDouble(null);
		case 12: return getString(null);
		case 13: return getDateTime(null);
		case 14: return getGuid(null);
		case 15: return getByteString(null);
		case 16: return getXmlElement(null);
		case 17: return getNodeId(null);
		case 18: return getExpandedNodeId(null);
		case 19: return getStatusCode(null);
		case 20: return getQualifiedName(null);
		case 21: return getLocalizedText(null);
		case 22: return getExtensionObject(null);
		case 23: return getDataValue(null);
		case 24: return getVariant(null);		
		case 25: return getDiagnosticInfo(null);
		}
		throw new DecodingException("Cannot decode builtin type id "+builtinTypeId);
	}
	
	public Object getArrayObject(String fieldName, int builtinTypeId)
    throws DecodingException	
	{
		switch (builtinTypeId) {
		case 1: return getBooleanArray(null);
		case 2: return getSByteArray(null);
		case 3: return getByteArray(null);
		case 4: return getInt16Array(null);
		case 5: return getUInt16Array(null);
		case 6: return getInt32Array(null);
		case 7: return getUInt32Array(null);
		case 8: return getInt64Array(null);
		case 9: return getUInt64Array(null);
		case 10: return getFloatArray(null);
		case 11: return getDoubleArray(null);
		case 12: return getStringArray(null);
		case 13: return getDateTimeArray(null);
		case 14: return getGuidArray(null);
		case 15: return getByteStringArray(null);
		case 16: return getXmlElementArray(null);
		case 17: return getNodeIdArray(null);
		case 18: return getExpandedNodeIdArray(null);
		case 19: return getStatusCodeArray(null);
		case 20: return getQualifiedNameArray(null);
		case 21: return getLocalizedTextArray(null);
		case 22: return getExtensionObjectArray(null);
		case 23: return getDataValueArray(null);
		case 24: return getVariantArray(null);		
		case 25: return getDiagnosticInfoArray(null);
		}
		throw new DecodingException("Cannot decode builtin type id "+builtinTypeId);
	}

		
	
}
