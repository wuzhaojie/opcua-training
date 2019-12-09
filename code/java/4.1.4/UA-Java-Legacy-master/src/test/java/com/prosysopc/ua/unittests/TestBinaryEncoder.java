 /* ========================================================================
 * Copyright (c) 2005-2015 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================*/

package com.prosysopc.ua.unittests;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.UUID;

import junit.framework.TestCase;

import com.prosysopc.ua.builtintypes.DataValue;
import com.prosysopc.ua.builtintypes.DateTime;
import com.prosysopc.ua.builtintypes.LocalizedText;
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.StatusCode;
import com.prosysopc.ua.builtintypes.Structure;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import com.prosysopc.ua.builtintypes.Variant;
import com.prosysopc.ua.builtintypes.XmlElement;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.common.ServerTable;
import com.prosysopc.ua.core.BuildInfo;
import com.prosysopc.ua.core.ServerState;
import com.prosysopc.ua.core.ServerStatusDataType;
import com.prosysopc.ua.encoding.EncoderContext;
import com.prosysopc.ua.encoding.IEncodeable;
import com.prosysopc.ua.encoding.binary.BinaryDecoder;
import com.prosysopc.ua.encoding.binary.BinaryEncoder;
import com.prosysopc.ua.utils.StackUtils;

public class TestBinaryEncoder extends TestCase {

	EncoderContext ctx;
	BinaryEncoder enc;
	BinaryDecoder dec;
	BinaryEncoder calc;
	ByteBuffer buf;
	ByteArrayOutputStream calcBuf;
	
	public void setUp() throws Exception {
		
		buf = ByteBuffer.allocate(1024*1024);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		enc = new BinaryEncoder(buf);		
		ctx = new EncoderContext(new NamespaceTable(), new ServerTable(), StackUtils.getDefaultSerializer(), Integer.MAX_VALUE);
//		ctx.setMaxArrayLength(5);
		enc.setEncoderContext( ctx );
		
		dec = new BinaryDecoder(buf);		
		dec.setEncoderContext( ctx );
		
		calcBuf = new ByteArrayOutputStream();
		calc = new BinaryEncoder(calcBuf);
		calc.setEncoderContext( ctx );		
	}
	
	public void tearDown() throws Exception {
		ctx = null;
		enc = null;
		dec = null;
		calc = null;
		buf = null;
	}

	public void testNodeId() throws Exception
	{
		tstNodeId( new NodeId(7, "x") );
		tstNodeId( new NodeId(6, UUID.randomUUID()) );
		tstNodeId( new NodeId(5, (String) null) );
		tstNodeId( new NodeId(4, (byte[]) null) );
		tstNodeId( new NodeId(3, new byte[] {0,1,43,56,7,7,4}) );
		tstNodeId( new NodeId(2, UnsignedInteger.valueOf(324L)) );		
	}
	
	public void testVariant() 
	throws Exception
	{
		tstVariant( null );
		tstVariant( new Variant(UnsignedInteger.valueOf( 0L )) );
		final Variant NULL_NODE = new Variant(NodeId.NULL);
		tstVariant( new Variant( new DataValue(NULL_NODE) ) );
		tstVariant( new Variant( new XmlElement("<abu/>")));
		BuildInfo buildInfo = new BuildInfo();
		buildInfo.setBuildDate(DateTime.MIN_VALUE);
		ServerStatusDataType status = new ServerStatusDataType(DateTime.currentTime(), DateTime.currentTime(), ServerState.Running, buildInfo, UnsignedInteger.ZERO, LocalizedText.NULL);
		tstVariant( new Variant( status));
		tstVariant( new Variant( new ServerStatusDataType[] {
				status,
				status
		}));
	}
	
	public void testStructures() throws Exception
	{
		ArrayList<Class<? extends IEncodeable>> clazzes = new ArrayList<Class<? extends IEncodeable>>(); 
		ctx.getEncodeableSerializer().getSupportedClasses(clazzes);
		for (Class<? extends IEncodeable> c : clazzes)
		{
			IEncodeable o = c.newInstance();
			if (!( o instanceof Structure)) continue;
			tstStructure( (Structure) o );
			calcBuf.reset();
			buf.rewind();
		}
		
	}
	
	public void testStructureArrays() throws Exception
	{
		ArrayList<Class<? extends IEncodeable>> clazzes = new ArrayList<Class<? extends IEncodeable>>(); 
		ctx.getEncodeableSerializer().getSupportedClasses(clazzes);
		for (Class<? extends IEncodeable> c : clazzes)
		{
			IEncodeable[] o = (IEncodeable[]) Array.newInstance(c, 1);
			if (!( o instanceof Structure[])) continue;
			tstStructureArray( (Structure[]) o );
			calcBuf.reset();
			buf.rewind();
		}		
	}
	
	public void tstVariant(Variant v) 
	throws Exception
	{
		calcBuf.reset();
		buf.rewind();
		calc.putVariant(null, v);
		int len = calcBuf.size();

		buf.rewind();
		enc.putVariant(null, v);
		assertEquals(len, buf.position());
		
		buf.rewind();
		Variant value = dec.getVariant(null);
		assertEquals(len, buf.position());
		if (v == null)
			assertEquals(Variant.NULL, value);
		else
			assertEquals(v, value);
	}
	
	public void testDataValue()
	throws Exception
	{
		DataValue v = new DataValue(StatusCode.getFromBits( StatusCode.HISTORIANBITS_INTERPOLATED ));

		calcBuf.reset();
		buf.rewind();
		calc.putDataValue(null, v);
		int len = calcBuf.size();

		buf.rewind();
		enc.putDataValue(null, v);
		assertEquals(len, buf.position());
		
		buf.rewind();
		DataValue value = dec.getDataValue(null);		
		assertEquals(len, buf.position());
		
	}
	
	public void tstStructure(Structure o) throws Exception 
	{		
//		System.out.println(o.getClass().getName());
		calcBuf.reset();
		buf.rewind();
		calc.putStructure(null, o);
		int len = calcBuf.size();

		buf.rewind();
		enc.putStructure(null, o);
		assertEquals(len, buf.position());
		
		buf.rewind();
		Structure value = dec.getStructure(null);		
		assertEquals(len, buf.position());

		assertSame(o.getClass(), value.getClass());		
	}

	public void tstStructureArray(Structure[] o) throws Exception 
	{		
		Class<? extends Structure> componentType = (Class<? extends Structure>) o.getClass().getComponentType();

		//		System.out.println(o.getClass().getName());
		calcBuf.reset();
		buf.rewind();
		calc.putEncodeableArray(null, componentType, o);
		int len = calcBuf.size();

		buf.rewind();
		enc.putEncodeableArray(null, componentType, o);
		assertEquals(len, buf.position());
		
		buf.rewind();
		Structure[] value = dec.getEncodeableArray(null, componentType);		
		//assertEquals(len, buf.position());
		
		
		
		assertSame(o.getClass(), value.getClass());
	}
	
	public void tstNodeId(NodeId id) throws Exception 
	{	
		calcBuf.reset();
		buf.rewind();
		calc.putNodeId(null, id);
		int len = calcBuf.size();

		buf.rewind();
		enc.putNodeId(null, id);
		assertEquals(len, buf.position());
		
		buf.rewind();
		NodeId id2 = dec.getNodeId(null);		
		assertEquals(len, buf.position());
		assertEquals(id, id2);
	}
	
}
