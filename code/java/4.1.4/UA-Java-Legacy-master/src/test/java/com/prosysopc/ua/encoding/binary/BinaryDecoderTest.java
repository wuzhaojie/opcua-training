package com.prosysopc.ua.encoding.binary;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import com.prosysopc.ua.builtintypes.ByteString;
import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.builtintypes.ExtensionObject;
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.Variant;
import com.prosysopc.ua.common.NamespaceTable;
import com.prosysopc.ua.core.Identifiers;
import com.prosysopc.ua.encoding.EncoderContext;
import com.prosysopc.ua.utils.CryptoUtil;
import com.prosysopc.ua.utils.MultiDimensionArrayUtils;

public class BinaryDecoderTest {
	
	@Test
	public void testExpandedNodeIdTwoByte() throws Exception{
		ExpandedNodeId data = new ExpandedNodeId(null, 0, 128);
		
		EncoderContext ctx = EncoderContext.getDefaultInstance();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		BinaryEncoder enc = new BinaryEncoder(buf);
		enc.setEncoderContext(ctx);
		enc.putExpandedNodeId(null, data);
		
		BinaryDecoder sut = new BinaryDecoder(buf.toByteArray());
		sut.setEncoderContext(ctx);
		ExpandedNodeId actual = sut.getExpandedNodeId(null);
		assertEquals(data, actual);
		
	}
	
	@Test
	public void testExpandedNodeIdFourByte() throws Exception{
		ExpandedNodeId data = new ExpandedNodeId(null, 0, 33000);
		
		EncoderContext ctx = EncoderContext.getDefaultInstance();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		BinaryEncoder enc = new BinaryEncoder(buf);
		enc.setEncoderContext(ctx);
		enc.putExpandedNodeId(null, data);
		
		BinaryDecoder sut = new BinaryDecoder(buf.toByteArray());
		sut.setEncoderContext(ctx);
		ExpandedNodeId actual = sut.getExpandedNodeId(null);
		assertEquals(data, actual);
	}
	
	@Test
	public void testNodeIdTwoByte() throws Exception{
		NodeId data = new NodeId(0, 128);
		
		EncoderContext ctx = EncoderContext.getDefaultInstance();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		BinaryEncoder enc = new BinaryEncoder(buf);
		enc.setEncoderContext(ctx);
		enc.putNodeId(null, data);
		
		BinaryDecoder sut = new BinaryDecoder(buf.toByteArray());
		sut.setEncoderContext(ctx);
		NodeId actual = sut.getNodeId(null);
		assertEquals(data, actual);
	}
	
	@Test
	public void objectClassDecoderGet() throws Exception {
		final Integer expected = Integer.valueOf(1237891223);
		byte[] data = binaryEncode(new Variant(expected));
		BinaryDecoder sut = new BinaryDecoder(data);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		Object actual = sut.get(null, Object.class);
		assertEquals(expected, actual);
	}
	
	@Test
	public void unknownBuiltInTypeId() throws Exception {
		ByteString expected = ByteString.valueOf((byte)1,(byte)2,(byte)3);
		byte[] data = binaryEncode(new Variant(expected));
		
		//EncodingMask should look like 0+0+011010 (array+dimensions+typeid)
		for(int i = 26; i<64;i++) {
			data[0] = (byte)i;
			BinaryDecoder sut = new BinaryDecoder(data);
			sut.setEncoderContext(EncoderContext.getDefaultInstance());
			ByteString actual = (ByteString) sut.getVariant(null).getValue();
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void unknownBuiltInTypeIdArrays() throws Exception {
		ByteString[] expected = new ByteString[] {ByteString.valueOf((byte)1,(byte)2,(byte)3), 
				ByteString.valueOf((byte)4,(byte)5,(byte)6)};
		
		byte[] data = binaryEncode(new Variant(expected));

		//EncodingMask should look like 1+0+011010 (array+dimensions+typeid)
		for(int i = 26; i<64;i++) {
			data[0] = (byte) (i | 0x80);
			BinaryDecoder sut = new BinaryDecoder(data);
			sut.setEncoderContext(EncoderContext.getDefaultInstance());
			ByteString[] actual = (ByteString[]) sut.getVariant(null).getValue();
			assertArrayEquals(expected, actual);
		}
	}
	
	@Test
	public void multidimBooleanArray() throws Exception {
		//TODO refactor to test vs. already encoded data
		Boolean[][] expected = (Boolean[][]) MultiDimensionArrayUtils.demuxArray(new Boolean[] {true,  true,  true, true}, new int[] {2,2}, Boolean.class);
		byte[] encoded = binaryEncode(expected);
		BinaryDecoder sut = new BinaryDecoder(encoded);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		Boolean[][] actual = sut.get(null, Boolean[][].class);
		assertTrue(Arrays.deepEquals(expected, actual));
	}
	
	@Test
	public void testNodeIdFourByte() throws Exception{
		NodeId data = new NodeId(0, 33000);
		
		EncoderContext ctx = EncoderContext.getDefaultInstance();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		BinaryEncoder enc = new BinaryEncoder(buf);
		enc.setEncoderContext(ctx);
		enc.putNodeId(null, data);
		
		BinaryDecoder sut = new BinaryDecoder(buf.toByteArray());
		sut.setEncoderContext(ctx);
		NodeId actual = sut.getNodeId(null);
		assertEquals(data, actual);
	}
	
	@Test
	public void decimalDecoding() throws Exception{
		long value = 1518632738243L; //random number
		short scale  = 4;
		
		//encoded form per spec
		//1.TypeId, NodeId, the NodeId for Decimal DataType node
		//2. Encoding, byte, always 1 as this fakes Structure with binary encodings
		//3. Lenght, Int32, "lenght of the Decimal", i.e. encoded body in fake Structures
		//4. Scale, Int16, included in lenght
		//5. Value, Byte array, size len - 2 bytes (of the scale)
		
		byte[] scalebytes = binaryEncode(scale);
		byte[] valuebytes = binaryEncode(value);
		System.out.println("valuebits:" + CryptoUtil.toHex(valuebytes));
		byte[] combinedbytes = ByteUtils.concat(scalebytes, valuebytes);
		System.out.println("combined bytes len: "+combinedbytes.length);
		ExpandedNodeId id = new ExpandedNodeId(NamespaceTable.OPCUA_NAMESPACE, Identifiers.Decimal.getValue());
		ExtensionObject eo = new ExtensionObject(id, ByteString.valueOf(combinedbytes));
		byte[] completebytes = binaryEncode(eo);
		byte[] completedexpected = CryptoUtil.hexToBytes("0032010a0000000400c39d909561010000");
		assertArrayEquals(completedexpected, completebytes);
		
		//Decoding
		BinaryDecoder sut = new BinaryDecoder(completebytes);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		BigDecimal bd = sut.get(null, BigDecimal.class);
		BigDecimal expected = BigDecimal.valueOf(value, scale);
		assertEquals(expected, bd);
	}
	
	@Test
	public void decimalWithinVariantDecoding() throws Exception {
		long value = 1518632738243L;
		short scale = 5;
		BigDecimal expected = BigDecimal.valueOf(value, scale);
		ExpandedNodeId id = new ExpandedNodeId(NamespaceTable.OPCUA_NAMESPACE, Identifiers.Decimal.getValue());
		ExtensionObject eo = new ExtensionObject(id, createDecimalAsEncodedBytes(value, scale));
		Variant veo = new Variant(eo);
		byte[] datainput = binaryEncode(veo);
		
		//Decoding
		BinaryDecoder sut = new BinaryDecoder(datainput);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		Variant v = sut.get(null, Variant.class);
		BigDecimal actual = (BigDecimal) v.getValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void decimalArrayWithinVariantDecoding() throws Exception {
		long value = 1518632738243L;
		ExpandedNodeId id = new ExpandedNodeId(NamespaceTable.OPCUA_NAMESPACE, Identifiers.Decimal.getValue());
		ArrayList<ExtensionObject> eos = new ArrayList<ExtensionObject>();
		ArrayList<BigDecimal> expecteds = new ArrayList<BigDecimal>();
		for(short i=0;i<10;i++) {
			eos.add((new ExtensionObject(id, createDecimalAsEncodedBytes(value, i))));
			expecteds.add(BigDecimal.valueOf(value, i));
		}
		ExtensionObject[] eoarr = eos.toArray(new ExtensionObject[0]);
		byte[] datainput = binaryEncode(new Variant(eoarr));
		
		//Decoding
		BinaryDecoder sut = new BinaryDecoder(datainput);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		Variant output = sut.get(null, Variant.class);
		Variant expected = new Variant(expecteds.toArray(new BigDecimal[0]));
		assertEquals(expected, output);
	}
	
	@Test
	public void decimalArrayDecoding() throws Exception {
		long value = 1518632738243L;
		ExpandedNodeId id = new ExpandedNodeId(NamespaceTable.OPCUA_NAMESPACE, Identifiers.Decimal.getValue());
		ArrayList<ExtensionObject> eos = new ArrayList<ExtensionObject>();
		ArrayList<BigDecimal> expecteds = new ArrayList<BigDecimal>();
		for(short i=0;i<10;i++) {
			eos.add(new ExtensionObject(id, createDecimalAsEncodedBytes(value, i)));
			expecteds.add(BigDecimal.valueOf(value, i));
		}
		ExtensionObject[] eosarr = eos.toArray(new ExtensionObject[0]);
		byte[] datainput = binaryEncode(eosarr);
		
		//Decoding
		BinaryDecoder sut = new BinaryDecoder(datainput);
		sut.setEncoderContext(EncoderContext.getDefaultInstance());
		BigDecimal[] output = sut.get(null, BigDecimal[].class);
		assertArrayEquals(expecteds.toArray(), output);
	}
	
	private ByteString createDecimalAsEncodedBytes(long valueraw, short scale) throws Exception{
		byte[] scalebytes = binaryEncode(scale);
		byte[] valuebytes = binaryEncode(valueraw);
		return ByteString.valueOf(ByteUtils.concat(scalebytes, valuebytes));
	}

	
	private byte[] binaryEncode(Object o) throws Exception{
		ByteArrayOutputStream r = new ByteArrayOutputStream();
		BinaryEncoder enc = new BinaryEncoder(r);
		enc.setEncoderContext(EncoderContext.getDefaultInstance());
		enc.put(null, o);
		return r.toByteArray();
	}
	
}
