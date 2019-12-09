/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
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

package org.opcfoundation.ua.stacktest.random;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import org.opcfoundation.ua.builtintypes.BuiltinsMap;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedLong;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.common.RuntimeServiceResultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ArrayTestType;
import org.opcfoundation.ua.core.CompositeTestType;
import org.opcfoundation.ua.core.EnumeratedTestType;
import org.opcfoundation.ua.core.IdType;
import org.opcfoundation.ua.core.ScalarTestType;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.stacktest.exception.RandomException;
import org.opcfoundation.ua.stacktest.random.library.RandomGenerator;
import org.opcfoundation.ua.stacktest.random.library.UARandomLibException;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * PseudoRandom provides the random data for the stack tests.
 * 
 * It uses the org.opcfoundation.ua.stacktest.random.library.RandomGenerator for
 * retrieving random values, which it uses for generating random values for
 * different UA data types.
 * 
 * @author jouni.aro@prosys.fi
 * 
 */
public class PseudoRandom {
	public RandomGenerator random = null;

	private long step = 0;
	private UnsignedByte boundaryValueRate = new UnsignedByte(64);
	private int currentDepth;

	private String randomFile = "";

	public PseudoRandom() {
		// random = new RandomGenerator();
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public String getRandomFile() {
		return randomFile;
	}

	public void setRandomFile(String randomFile) {
		this.randomFile = randomFile;
	}

	/**
	 * Opens random file
	 */
	public void start(int iteration, int seed) throws RandomException {
		int errorCode;
		if (random != null) { // (random.getRandom() != 0) {
			// return;
			errorCode = random.randomDestroy();
			if (errorCode > 0)
				throw new RandomException(String.format(
						"Cannot close random generator) errorCode=%d",
						errorCode));

		}
		if (step == 0)
			throw new RandomException("Step=0, cannot start random generator!");
		random = new RandomGenerator();
		errorCode = random
				.randomCreate(getRandomFile(), seed + iteration, step);
		if (errorCode > 0)
			throw new RandomException(
					String
							.format(
									"Cannot open random file '%s' seed=%d, step=%d) errorCode=%d",
									getRandomFile(), seed + iteration, step,
									errorCode));
	}

	public Variant getScalarVariant(int maxStringLength, int maxDepth) {
		Class<?> clazz = getVariantType(maxDepth, BuiltinsMap.SCALAR_LIST);

		if (clazz == Boolean.class) {
			return new Variant(getBoolean());
		}
		if (clazz == Byte.class) {
			return new Variant(getSByte());
		}
		if (clazz == UnsignedByte.class) {
			return new Variant(getByte());
		}
		if (clazz == Short.class) {
			return new Variant(getInt16());
		}
		if (clazz == UnsignedShort.class) {
			return new Variant(getUInt16());
		}
		if (clazz == Integer.class) {
			return new Variant(getInt32());
		}
		if (clazz == UnsignedInteger.class) {
			return new Variant(getUInt32());
		}
		if (clazz == Long.class) {
			return new Variant(getInt64());
		}
		if (clazz == UnsignedLong.class) {
			return new Variant(getUInt64());
		}
		if (clazz == Float.class) {
			return new Variant(getFloat());
		}
		if (clazz == Double.class) {
			return new Variant(getDouble());
		}
		if (clazz == String.class) {
			return new Variant(getString(maxStringLength));
		}
		if (clazz == DateTime.class) {
			return new Variant(getDateTime());
		}
		if (clazz == UUID.class) {
			return new Variant(getGuid());
		}
		if (clazz == byte[].class) {
			return new Variant(getByteString(maxStringLength));
		}
		if (clazz == XmlElement.class) {
			return new Variant(getXmlElement());
		}
		if (clazz == NodeId.class) {
			return new Variant(getNodeId(maxStringLength));
		}
		if (clazz == ExpandedNodeId.class) {
			return new Variant(getExpandedNodeId(maxStringLength));
		}
		if (clazz == StatusCode.class) {
			return new Variant(getStatusCode());
		}
		if (clazz == DiagnosticInfo.class) {
			return new Variant(getDiagnosticInfo(maxDepth));
		}
		if (clazz == QualifiedName.class) {
			return new Variant(getQualifiedName(maxStringLength));
		}
		if (clazz == LocalizedText.class) {
			return new Variant(getLocalizedText(maxStringLength));
		}
		if (clazz == ExtensionObject.class) {
			return new Variant(getExtensionObject(maxStringLength, maxDepth));
		}
		if (clazz == DataValue.class) {
			return new Variant(getDataValue(1, maxStringLength, maxDepth));
		}

		return new Variant(null);
	}

	private Class<?> getVariantType(int maxDepth, ArrayList<Class<?>> typeList) {
		int last = typeList.size() - 1;

		if (currentDepth < maxDepth) {
			last = typeList.indexOf(DataValue.class);
			if (last < 0)
				last = typeList.indexOf(DataValue[].class); // TODO added
			// brackets to use
			// correct list
		} else {
			last = typeList.indexOf(LocalizedText.class);
			if (last < 0)
				last = typeList.indexOf(LocalizedText[].class);
		}

		int type = getInt32Range(0, last);

		Class<?> clazz = typeList.get(type);
		return clazz;
	}

	// / <summary>
	// / Returns a random boolean.
	// / </summary>
	public Boolean getRandomBoolean() {
		return getRandomSByte() >= 0;
	}

	// / <summary>
	// / Returns a boolean.
	// / </summary>
	public Boolean getBoolean() {
		return getRandomSByte() >= 0;
	}

	// / <summary>
	// / Returns a boolean array.
	// / </summary>
	public Boolean[] getBooleanArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Boolean[] values = new Boolean[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getBoolean();
		}

		return values;
	}

	// / <summary>
	// / This method returns a random unsigned byte.
	// / </summary>
	public UnsignedByte getRandomByte() {
		return random.getValueUInt8();
	}

	// / <summary>
	// / This method returns an unsigned byte.
	// / </summary>
	public UnsignedByte getByte() {
		if (useBoundaryValue()) {
			return ByteValues[getRandomIndex(ByteValues)];
		}

		return getRandomByte();
	}

	// Boundary values to use when generating a Byte.
	private static final UnsignedByte[] ByteValues = new UnsignedByte[] {
			UnsignedByte.MAX_VALUE, UnsignedByte.MIN_VALUE };

	// / <summary>
	// / This method returns an unsigned byte array.
	// / </summary>
	public UnsignedByte[] getByteArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UnsignedByte[] values = new UnsignedByte[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getByte();
		}

		return values;
	}

	// / <summary>
	// / This method returns a random signed byte.
	// / </summary>
	public byte getRandomSByte() {
		return random.getValueInt8();
	}

	// / <summary>
	// / This method returns a signed byte.
	// / </summary>
	public byte getSByte() {
		if (useBoundaryValue()) {
			return SByteValues[getRandomIndex(SByteValues)];
		}

		return getRandomSByte();
	}

	// Boundary values to use when generating an SByte.
	private static final Byte[] SByteValues = new Byte[] { 0, -1,
			Byte.MAX_VALUE, Byte.MIN_VALUE };

	// / <summary>
	// / This method returns a signed byte array.
	// / </summary>
	public Byte[] getSByteArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Byte[] values = new Byte[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getSByte();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 16-bit integer.
	// / </summary>
	public short getRandomInt16() {
		return random.getValueInt16();
	}

	// / <summary>
	// / Returns a 16-bit integer.
	// / </summary>
	public short getInt16() {
		if (useBoundaryValue()) {
			return Int16Values[getRandomIndex(Int16Values)];
		}

		return getRandomInt16();
	}

	// Boundary values to use when generating an Int16.
	private static final Short[] Int16Values = new Short[] { 0, -1,
			Short.MAX_VALUE, Short.MIN_VALUE };

	// / <summary>
	// / Returns a 16-bit integer array.
	// / </summary>
	public Short[] getInt16Array(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Short[] values = new Short[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getInt16();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 16-bit unsigned integer.
	// / </summary>
	public UnsignedShort getRandomUInt16() {
		return random.getValueUInt16();
	}

	// / <summary>
	// / Returns a 16-bit unsigned integer.
	// / </summary>
	public UnsignedShort getUInt16() {
		if (useBoundaryValue()) {
			return UInt16Values[getRandomIndex(UInt16Values)];
		}

		return getRandomUInt16();
	}

	// Boundary values to use when generating a UInt16.
	private static final UnsignedShort[] UInt16Values = new UnsignedShort[] {
			UnsignedShort.MAX_VALUE, UnsignedShort.MIN_VALUE };

	// / <summary>
	// / Returns a 16-bit unsigned integer array.
	// / </summary>
	public UnsignedShort[] getUInt16Array(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UnsignedShort[] values = new UnsignedShort[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getUInt16();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 32-bit integer.
	// / </summary>
	public int getRandomInt32() {
		return random.getValueInt32();
	}

	// / <summary>
	// / Returns a 32-bit integer.
	// / </summary>
	public int getInt32() {
		if (useBoundaryValue()) {
			return Int32Values[getRandomIndex(Int32Values)];
		}

		return getRandomInt32();
	}

	// Boundary values to use when generating a Int32.
	private static final Integer[] Int32Values = new Integer[] { 0, -1,
			Integer.MAX_VALUE, Integer.MIN_VALUE };

	// / <summary>
	// / Returns a 32-bit integer array.
	// / </summary>
	public Integer[] getInt32Array(Integer maxArrayLength) {
		Integer length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Integer[] values = new Integer[length];

		for (Integer ii = 0; ii < values.length; ii++) {
			values[ii] = getInt32();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 32-bit unsigned integer.
	// / </summary>
	public UnsignedInteger getRandomUInt32() {
		return random.getValueUInt32();
	}

	// / <summary>
	// / Returns a 32-bit unsigned integer.
	// / </summary>
	public UnsignedInteger getUInt32() {
		if (useBoundaryValue()) {
			return UInt32Values[getRandomIndex(UInt32Values)];
		}

		return getRandomUInt32();
	}

	// Boundary values to use when generating a UInt32.
	private static final UnsignedInteger[] UInt32Values = new UnsignedInteger[] {
			UnsignedInteger.MAX_VALUE, UnsignedInteger.MIN_VALUE };

	// / <summary>
	// / Returns a 32-bit unsigned integer array.
	// / </summary>
	public UnsignedInteger[] getUInt32Array(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UnsignedInteger[] values = new UnsignedInteger[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getUInt32();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 64-bit integer.
	// / </summary>
	public Long getRandomInt64() {
		return random.getValueInt64();
	}

	// / <summary>
	// / Returns a 64-bit integer.
	// / </summary>
	public Long getInt64() {
		if (useBoundaryValue()) {
			return Int64Values[getRandomIndex(Int64Values)];
		}

		return getRandomInt64();
	}

	// Boundary values to use when generating a Int64.
	private static final Long[] Int64Values = new Long[] { new Long(0),
			new Long(-1), Long.MAX_VALUE, Long.MIN_VALUE };

	// / <summary>
	// / Returns a 64-bit integer array.
	// / </summary>
	public Long[] getInt64Array(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Long[] value = new Long[length];

		for (int ii = 0; ii < value.length; ii++) {
			value[ii] = getInt64();
		}

		return value;
	}

	// / <summary>
	// / Returns a random 64-bit unsigned integer.
	// / </summary>
	public UnsignedLong getRandomUInt64() {
		// return random.getValueUInt64();
		return UnsignedLong.getFromBits(random.getValueInt64());
	}

	// / <summary>
	// / Returns a 64-bit integer.
	// / </summary>
	public UnsignedLong getUInt64() {
		if (useBoundaryValue()) {
			return UInt64Values[getRandomIndex(UInt64Values)];
		}

		return getRandomUInt64();
	}

	// Boundary values to use when generating a UInt64.
	private static final UnsignedLong[] UInt64Values = new UnsignedLong[] {
			UnsignedLong.MAX_VALUE, UnsignedLong.MIN_VALUE };

	// / <summary>
	// / Returns a 64-bit unsigned integer array.
	// / </summary>
	public UnsignedLong[] getUInt64Array(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UnsignedLong[] values = new UnsignedLong[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getUInt64();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 32-bit floating point value.
	// / </summary>
	public Float getRandomFloat() {
		return random.getValueFloat();
	}

	// / <summary>
	// / Returns a 32-bit floating point value.
	// / </summary>
	public Float getFloat() {
		if (useBoundaryValue()) {
			return FloatValues[getRandomIndex(FloatValues)];
		}

		return getRandomFloat();
	}

	// Boundary values to use when generating a Float.
	private static final Float[] FloatValues = new Float[] {
	/* new Float(1.40129846E-45), */Float.MIN_VALUE, Float.NaN,
			-Float.MAX_VALUE, Float.MAX_VALUE, new Float(0), new Float(-1) };

	// / <summary>
	// / Returns a 32-bit floating point array.
	// / </summary>
	public Float[] getFloatArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Float[] values = new Float[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getFloat();
		}

		return values;
	}

	// / <summary>
	// / Returns a random 64-bit floating point value.
	// / </summary>
	public Double getRandomDouble() {
		return random.getValueDouble();
	}

	// / <summary>
	// / Returns a 64-bit floating point value.
	// / </summary>
	public Double getDouble() {
		if (useBoundaryValue()) {
			return DoubleValues[getRandomIndex(DoubleValues)];
		}

		return getRandomDouble();
	}

	// Boundary values to use when generating a Double.
	private static final Double[] DoubleValues = new Double[] {
			Double.MIN_VALUE, Double.NaN, -Double.MAX_VALUE, Double.MAX_VALUE,
			new Double(0), new Double(-1) };

	// / <summary>
	// / Returns a 32-bit floating point array.
	// / </summary>
	public Double[] getDoubleArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Double[] values = new Double[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getDouble();
		}

		return values;
	}

	// / <summary>
	// / Returns a random date time value.
	// / </summary>
	public DateTime getRandomDateTime() {
		long ticks = random.getValueDateTime();
		return new DateTime(ticks);
	}

	// / <summary>
	// / Returns a date time value.
	// / </summary>
	public DateTime getDateTime() {
		if (useBoundaryValue()) {
			return DateTimeValues[getRandomIndex(DateTimeValues)];
		}

		return getRandomDateTime();
	}

	// Boundary values to use when generating a DateTime.
	private static final DateTime[] DateTimeValues = new DateTime[] {
			DateTime.MIN_VALUE,
			DateTime.MAX_VALUE,
			// new DateTime(125007732000000000L),
			// new DateTime(126438084000000000L),
			new DateTime(1997, Calendar.FEBRUARY, 18, 21, 0, 0, 0, TimeZone
					.getDefault()),
			new DateTime(2001, Calendar.SEPTEMBER, 1, 9, 0, 0, 0), };

	// / <summary>
	// / Returns a date time array.
	// / </summary>
	public DateTime[] getDateTimeArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		DateTime[] values = new DateTime[length];

		for (int ii = 0; ii < values.length; ii++) {
			if (ii == 26) {
				int temp = 0;
			}
			values[ii] = getDateTime();
		}

		return values;
	}

	// / <summary>
	// / Returns a random UUID value.
	// / </summary>
	public UUID getRandomGuid() {
		// To match the reference implementation, the two longs must be
		// constructed
		// from 16 bytes
		long firstBytes = 0;
		long secondBytes = 0;

		byte[] bits = Next(16);
		byte[] enc = new byte[16];
		
		enc[0] = bits[3];
		enc[1] = bits[2];
		enc[2] = bits[1];
		enc[3] = bits[0];
		enc[4] = bits[5];
		enc[5] = bits[4];
		enc[6] = bits[7];
		enc[7] = bits[6];
		for (int i = 8; i < 16; i++)
			enc[i] = bits[i];

		long hi = 0;
		long lo = 0;
		for (int i= 0; i < 8; i++)
			hi = (hi << 8) | (enc[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lo = (lo << 8) | (enc[i] & 0xff);
		return new UUID(hi, lo);
	}

	// / <summary>
	// / Returns a UUID value.
	// / </summary>
	public UUID getGuid() {
		return getRandomGuid();
	}

	// / <summary>
	// / Returns a UUID array.
	// / </summary>
	public UUID[] getGuidArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UUID[] values = new UUID[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getGuid();
		}

		return values;
	}

	// / <summary>
	// / Returns a UUID array.
	// / </summary>
	public UUID[] getUuidArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		UUID[] values = new UUID[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getGuid();
		}

		return values;
	}

	// / <summary>
	// / Returns a random String value.
	// / </summary>
	public String getRandomString(int maxStringLength) {
		return random.getValueString(maxStringLength + 1);
	}

	// / <summary>
	// / Returns a String value.
	// / </summary>
	public String getString(int maxStringLength) {
		if (useBoundaryValue()) {
			return StringValues[getRandomIndex(StringValues)];
		}

		return getRandomString(maxStringLength);
	}

	// Boundary values to use when generating a String.
	private static final String[] StringValues = new String[] { null, "" };

	// / <summary>
	// / Returns a String array.
	// / </summary>
	public String[] getStringArray(int maxArrayLength, int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		String[] values = new String[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getString(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random byte String value.
	// / </summary>
	public byte[] getRandomByteString(int maxStringLength) {
		int length = getInt32Range(1, maxStringLength);
		return Next(length);
	}

	// / <summary>
	// / Returns a byte String value.
	// / </summary>
	public byte[] getByteString(int maxStringLength) {
		if (useBoundaryValue()) {
			return ByteStringValues[getRandomIndex(ByteStringValues)];
		}

		return getRandomByteString(maxStringLength);
	}

	// Boundary values to use when generating a ByteString.
	private static final byte[][] ByteStringValues = new byte[][] { null,
			new byte[0] };

	// / <summary>
	// / Returns a String array.
	// / </summary>
	public byte[][] getByteStringArray(int maxArrayLength, int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		byte[][] values = new byte[length][];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getByteString(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random node id value.
	// / </summary>
	public NodeId getRandomNodeId(int maxStringLength) {
		IdType idType = (IdType) getEnum(IdType.class);

		switch (idType) {
		default:
		case Numeric: {
			UnsignedInteger value = getUInt32();
			int namespaceIndex = getUInt16().intValue();
			return new NodeId(namespaceIndex, value);
		}

		case String: {
			String value = getString(maxStringLength);
			int namespaceIndex = getUInt16().intValue();
			return new NodeId(namespaceIndex, value);
		}

		case Guid: {
			UUID value = getGuid();
			int namespaceIndex = getUInt16().intValue();
			return new NodeId(namespaceIndex, value);
		}

		case Opaque: {
			byte[] value = getByteString(maxStringLength);
			int namespaceIndex = getUInt16().intValue();
			return new NodeId(namespaceIndex, value);
		}
		}
	}

	// / <summary>
	// / Returns a node id value.
	// / </summary>
	public NodeId getNodeId(int maxStringLength) {
		if (useBoundaryValue()) {
			int temp = getRandomIndex(NodeIdValues);
			return NodeIdValues[temp];
			// return NodeIdValues[getRandomIndex(NodeIdValues)]; TODO
		}

		return getRandomNodeId(maxStringLength);
	}

	// Boundary values to use when generating a NodeId.
	private static final NodeId[] NodeIdValues = new NodeId[] { null,
			NodeId.NULL_NUMERIC, NodeId.NULL_STRING, NodeId.NULL_GUID,
			NodeId.NULL_OPAQUE };

	// / <summary>
	// / Returns a node id array.
	// / </summary>
	public NodeId[] getNodeIdArray(int maxArrayLength, int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		NodeId[] values = new NodeId[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getNodeId(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random expanded node id value.
	// / </summary>
	public ExpandedNodeId getRandomExpandedNodeId(int maxStringLength) {
		NodeId nodeId = getRandomNodeId(maxStringLength);

		if (getRandomSByte() < 0) {
			return new ExpandedNodeId(nodeId);
		}

		// UnsignedInteger serverIndex = getRandomUInt32();
		// return new ExpandedNodeId(serverIndex, nodeId.getNamespaceIndex(),
		// nodeId.getValue());

		String uri = getRandomString(maxStringLength);
		UnsignedInteger serverIndex = getRandomUInt32();
		return new ExpandedNodeId(serverIndex, uri, nodeId.getValue());
	}

	// / <summary>
	// / Returns a expanded node id value.
	// / </summary>
	public ExpandedNodeId getExpandedNodeId(int maxStringLength) {
		if (useBoundaryValue()) {
			return ExpandedNodeIdValues[getRandomIndex(ExpandedNodeIdValues)];
		}

		return getRandomExpandedNodeId(maxStringLength);
	}

	// Boundary values to use when generating an ExpandedNodeId.
	private static final ExpandedNodeId[] ExpandedNodeIdValues = new ExpandedNodeId[] {
			null,
			ExpandedNodeId.NULL_NUMERIC,
			ExpandedNodeId.NULL_STRING,
			ExpandedNodeId.NULL_GUID,
			ExpandedNodeId.NULL_OPAQUE};

	// / <summary>
	// / Returns a expanded node id array.
	// / </summary>
	public ExpandedNodeId[] getExpandedNodeIdArray(int maxArrayLength,
			int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		ExpandedNodeId[] values = new ExpandedNodeId[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getExpandedNodeId(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random qualified name value.
	// / </summary>
	public QualifiedName getRandomQualifiedName(int maxStringLength) {
		String name = getRandomString(maxStringLength);

		// if (name.length()>512)
		// name = name.substring(0, 512);

		if (getRandomSByte() < 0) {
			return new QualifiedName(name);
		}

		return new QualifiedName(getUInt16().intValue(), name);
	}

	// / <summary>
	// / Returns a expanded node id value.
	// / </summary>
	public QualifiedName getQualifiedName(int maxStringLength) {
		if (useBoundaryValue()) {
			return QualifiedNameValues[getRandomIndex(QualifiedNameValues)];
		}

		return getRandomQualifiedName(maxStringLength);
	}

	// Boundary values to use when generating a QualifiedName.
	private static final QualifiedName[] QualifiedNameValues = new QualifiedName[] {
			null, QualifiedName.NULL };

	// / <summary>
	// / Returns a expanded node id array.
	// / </summary>
	public QualifiedName[] getQualifiedNameArray(int maxArrayLength,
			int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		QualifiedName[] values = new QualifiedName[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getQualifiedName(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random qualified name value.
	// / </summary>
	public LocalizedText getRandomLocalizedText(int maxStringLength) {
		String text = getRandomString(maxStringLength);

		if (getRandomSByte() < 0) {
			return new LocalizedText(text, LocalizedText.NULL_LOCALE);
		}

		return new LocalizedText(getRandomString(maxStringLength), text); // constructor
		// arguments
		// are
		// in
		// the
		// wrong
		// order
		// to
		// work
		// with
		// .NET
		// Stack
		// test
	}

	// / <summary>
	// / Returns a expanded node id value.
	// / </summary>
	public LocalizedText getLocalizedText(int maxStringLength) {
		if (useBoundaryValue()) {
			return LocalizedTextValues[getRandomIndex(LocalizedTextValues)];
		}

		return getRandomLocalizedText(maxStringLength);
	}

	// Boundary values to use when generating a LocalizedText.
	private static final LocalizedText[] LocalizedTextValues = new LocalizedText[] {
			null, new LocalizedText(null, LocalizedText.NULL_LOCALE) };

	// / <summary>
	// / Returns a expanded node id array.
	// / </summary>
	public LocalizedText[] getLocalizedTextArray(int maxArrayLength,
			int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		LocalizedText[] values = new LocalizedText[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getLocalizedText(maxStringLength);
		}

		return values;
	}

	// / <summary>
	// / Returns a random StatusCode value.
	// / </summary>
	public StatusCode getRandomStatusCode() {
		// create a random code without the severity.
		UnsignedInteger code = getRandomUInt32().and(0x7FFFFFFF);

		// select the severity.
		UnsignedByte type = getRandomByte();

		// return uncertain code.
		if (type.getValue() < 85) {
			return new StatusCode(code.or(0x40000000));
		}

		// return bad code.
		if (type.getValue() < 170) {
			return new StatusCode(code.or(0xC0000000));
		}

		// return good code.
		return new StatusCode(code);
	}

	// / <summary>
	// / Returns a StatusCode.
	// / </summary>
	public StatusCode getStatusCode() {
		if (useBoundaryValue()) {
			return StatusCodeValues[getRandomIndex(StatusCodeValues)];
		}

		return getRandomStatusCode();
	}

	// Boundary values to use when generating a StatusCode.
	private static StatusCode[] StatusCodeValues = new StatusCode[] { new StatusCode(
			UnsignedInteger.getFromBits(StatusCode.SEVERITY_GOOD)) };

	// / <summary>
	// / Returns a StatusCode array.
	// / </summary>
	public StatusCode[] getStatusCodeArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		StatusCode[] values = new StatusCode[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getStatusCode();
		}

		return values;
	}

	// / <summary>
	// / Returns a random DiagnosticInfo value.
	// / </summary>
	public DiagnosticInfo getDiagnosticInfo(int maxDepth) {
		int mask = getRandomByte().getValue();

		if (mask == 0) {
			return null;
		}

		DiagnosticInfo diagnosticInfo = new DiagnosticInfo();
		if ((mask & 0x01) != 0)
			diagnosticInfo.setSymbolicId(getUInt16().intValue());
		if ((mask & 0x02) != 0)
			diagnosticInfo.setNamespaceUri(getUInt16().intValue());
		if ((mask & 0x04) != 0)
			diagnosticInfo.setLocalizedText(getUInt16().intValue());
		if ((mask & 0x08) != 0)
			diagnosticInfo.setInnerStatusCode(getStatusCode());
		if ((mask & 0x10) != 0) {
			if (currentDepth < maxDepth) {
				currentDepth++;
				diagnosticInfo
						.setInnerDiagnosticInfo(getDiagnosticInfo(maxDepth));
				currentDepth--;
			}
		}
		return diagnosticInfo;
	}

	// / <summary>
	// / Returns a DiagnosticInfo array.
	// / </summary>
	public DiagnosticInfo[] getDiagnosticInfoArray(int maxArrayLength,
			int maxDepth) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		DiagnosticInfo[] values = new DiagnosticInfo[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getDiagnosticInfo(maxDepth);
		}

		return values;
	}

	// / <summary>
	// / Returns XmlElement.
	// / </summary>
	public XmlElement getRandomXmlElement() {
		if (XmlElements == null) {
			final String fileName = "testharness/SampleXmlData.xml";
			// System.getProperty("user.dir")
			// +
			// "\\testharness\\org\\opcfoundation\\ua\\stacktest\\client\\SampleXmlData.xml";
			try {
				FileInputStream fileInputStream = new FileInputStream(fileName);
				DOMParser parser = new DOMParser();
				parser.parse(new InputSource(fileInputStream));
				fileInputStream.close();

				XmlElements = parser.getDocument().getElementsByTagName("*");
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Cannot read sample Xml data", e);
			} catch (SAXException e) {
				throw new RuntimeException("Cannot parse sample Xml data", e);
			} catch (IOException e) {
				throw new RuntimeException(
						"IO error while reading sample Xml data" + fileName, e);
			}
		}

		int index = getInt32Range(-1, XmlElements.getLength() - 1);
		if (index < 0)
			return null;

		Node node = XmlElements.item(index);
		XmlElement xe = new XmlElement( node );
//		String xmlValue = CollectElements(node, new String(), true);

		// removing excess newspaces
		return new XmlElement( node );
	}

	// Array of type XML Element.
	private static NodeList XmlElements = null;

	private static Object element;

	// / <summary>
	// / Returns a expanded node id value.
	// / </summary>
	public XmlElement getXmlElement() {
		return getRandomXmlElement();
	}

	// / <summary>
	// / Collect all the elements from a node and return a string
	// representation.
	// / </summary>
	private static String CollectElements(Node node, String rep,
			boolean isFirstLevel) {
		if (node != null) {
			if (node.getNodeName() != "#text") {
				rep = rep + "<" + node.getNodeName();
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					rep = rep + " " + attributes.item(i).getNodeName() + "=\""
							+ attributes.item(i).getTextContent() + "\"";
				}
			}
			if (node.getChildNodes().getLength() != 0) {
				if (isFirstLevel)
					rep = rep + " xmlns=\"" + node.getNamespaceURI() + "\"";
				rep = rep + ">";
				for (Node child = node.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child != null)
						rep = CollectElements(child, rep, false);
				}
				if (node.getNodeName() != "#text")
					rep = rep + "</" + node.getNodeName() + ">";
			} else {
				if (node.getTextContent() != null
						&& node.getTextContent() != "") {
					rep = rep + node.getTextContent();
				} else {
					if (isFirstLevel)
						rep = rep + " xmlns=\"" + node.getNamespaceURI() + "\"";
					rep = rep + " />";
				}
			}
		}

		return rep;
	}

	// / <summary>
	// / Returns a XmlElement array.
	// / </summary>
	public XmlElement[] getXmlElementArray(int maxArrayLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		XmlElement[] values = new XmlElement[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getXmlElement();
		}

		return values;
	}

	// / <summary>
	// / Returns a DataValue.
	// / </summary>
	public DataValue getDataValue(int maxArrayLength, int maxStringLength,
			int maxDepth) {
		DataValue value = new DataValue(getVariant(maxArrayLength,
				maxStringLength, maxDepth), getStatusCode(), getDateTime(),
				getUInt16(), getDateTime(), getUInt16());

		return value;
	}

	// / <summary>
	// / Returns a DataValue array.
	// / </summary>
	public DataValue[] getDataValueArray(int maxArrayLength,
			int maxStringLength, int maxDepth) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		DataValue[] values = new DataValue[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getDataValue(maxArrayLength, maxStringLength, maxDepth);
		}

		return values;
	}

	// / <summary>
	// / Returns a ExtensionObject.
	// / </summary>
	public ExtensionObject getExtensionObject(int maxStringLength, int maxDepth) {
		try {
			currentDepth++;

			Class clazz = ExtensionObjectTypes[getRandomIndex(ExtensionObjectTypes)];

			if (clazz == byte[].class) {
				return new ExtensionObject(getRandomExpandedNodeId(maxStringLength),
						getRandomByteString(maxStringLength));
			}

			if (clazz == XmlElement.class) {
				return new ExtensionObject(getRandomExpandedNodeId(maxStringLength),
						getXmlElement());
			}

			/*
			 * if (clazz == typeof(Driver)) { return new
			 * ExtensionObject(getDriver()); }
			 */

			// if (clazz == AcmeWidget.class)
			// {
			// return new
			// ExtensionObject(getAcmeWidget(maxStringLength).encode(new
			// IEncoder));
			// }
			//
			// if (clazz ==CoyoteGadget.class)
			// {
			// return new ExtensionObject(getCoyoteGadget(maxStringLength));
			// }
			/*
			 * if (clazz == typeof(SkyNetRobot)) { return new
			 * ExtensionObject(getSkyNetRobot()); }
			 * 
			 * if (clazz == typeof(S88Batch)) { return new
			 * ExtensionObject(getS88Batch()); }
			 * 
			 * if (clazz == typeof(S88UnitProcedure)) { return new
			 * ExtensionObject(getS88UnitProcedure()); }
			 * 
			 * if (clazz == typeof(S88Operation)) { return new
			 * ExtensionObject(getS88Operation()); }
			 * 
			 * if (clazz == typeof(S88Phase)) { return new
			 * ExtensionObject(getS88Phase()); }
			 */
			return null;
		} finally {
			currentDepth--;
		}
	}

	/*
	 * private AcmeWidget getAcmeWidget(int maxStringLength) { AcmeWidget widget
	 * = new AcmeWidget();
	 * 
	 * widget.color = getString(maxStringLength); widget.quantity = getInt32();
	 * widget.buildDate = getDateTime();
	 * 
	 * return widget; }
	 */

	// Boundary values to use when generating an ExtensionObject.
	private static final Class[] ExtensionObjectTypes = new Class[] {
			byte[].class, XmlElement.class,
	/*
	 * TODO)
	 * 
	 * Driver.class, AcmeWidget.class, CoyoteGadget.class, /* SkyNetRobot.class,
	 * S88Batch.class, S88UnitProcedure.class, S88Operation.class,
	 * S88Phase.class
	 */};

	// / <summary>
	// / Returns a ExtensionObject array.
	// / </summary>
	public ExtensionObject[] getExtensionObjectArray(int maxArrayLength,
			int maxStringLength, int maxDepth) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		ExtensionObject[] values = new ExtensionObject[length];

		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = getExtensionObject(maxStringLength, maxDepth);
		}

		return values;
	}

	// / <summary>
	// / Returns a Variant.
	// / </summary>
	public Variant getVariant(int maxArrayLength, int maxStringLength,
			int maxDepth) {
		try {
			currentDepth++;

			if (getBoolean()) {
				return getScalarVariant(maxStringLength, maxDepth);
			} else {
				return getArrayVariant(maxArrayLength, maxStringLength,
						maxDepth);
			}
		} finally {
			currentDepth--;
		}
	}

	// / <summary>
	// / This method returns ScalarTestType.
	// / </summary>
	public ScalarTestType getScalarTestType(int maxStringLength, int maxDepth) {
		return new ScalarTestType(getBoolean(), getSByte(), getByte(),
				getInt16(), getUInt16(), getInt32(), getUInt32(), getInt64(),
				getUInt64(), getFloat(), getDouble(),
				getString(maxStringLength), getDateTime(), getGuid(),
				getByteString(maxStringLength), getXmlElement(),
				getNodeId(maxStringLength), getExpandedNodeId(maxStringLength),
				getStatusCode(), getDiagnosticInfo(maxDepth),
				getQualifiedName(maxStringLength),
				getLocalizedText(maxStringLength), getExtensionObject(
						maxStringLength, maxDepth), getDataValue(1,
						maxStringLength, maxDepth),
				getEnum(EnumeratedTestType.class));
	}

	// / <summary>
	// / This method returns ArrayTestType.
	// / </summary>
	public ArrayTestType getArrayTestType(int maxDepth, int maxArrayLength,
			int maxStringLength) {
		return new ArrayTestType(getBooleanArray(maxArrayLength),
				getSByteArray(maxArrayLength), getInt16Array(maxArrayLength),
				getUInt16Array(maxArrayLength), getInt32Array(maxArrayLength),
				getUInt32Array(maxArrayLength), getInt64Array(maxArrayLength),
				getUInt64Array(maxArrayLength), getFloatArray(maxArrayLength),
				getDoubleArray(maxArrayLength), getStringArray(maxArrayLength,
						maxStringLength), getDateTimeArray(maxArrayLength),
				getGuidArray(maxArrayLength), getByteStringArray(
						maxArrayLength, maxStringLength),
				getXmlElementArray(maxArrayLength), getNodeIdArray(
						maxArrayLength, maxStringLength),
				getExpandedNodeIdArray(maxArrayLength, maxStringLength),
				getStatusCodeArray(maxArrayLength), getDiagnosticInfoArray(
						maxArrayLength, maxDepth), getQualifiedNameArray(
						maxArrayLength, maxStringLength),
				getLocalizedTextArray(maxArrayLength, maxStringLength),
				getExtensionObjectArray(maxArrayLength, maxStringLength,
						maxDepth), getDataValueArray(maxArrayLength,
						maxStringLength, maxDepth), getVariantArray(
						maxArrayLength, maxStringLength, maxDepth),
				getEnumArray(EnumeratedTestType.class, maxArrayLength));
	}

	// / <summary>
	// / Returns a variant containing a array value.
	// / </summary>
	public Variant getArrayVariant(int maxArrayLength, int maxStringLength,
			int maxDepth) {
		Class<?> clazz = getVariantType(maxDepth, BuiltinsMap.ARRAY_LIST);
		Object[] array = null;
		if (clazz == Boolean[].class) {
			array = getBooleanArray(maxArrayLength);
		} else if (clazz == Byte[].class) {
			array = getSByteArray(maxArrayLength);
		} else if (clazz == UnsignedByte[].class) {
			array = getByteArray(maxArrayLength);
		} else if (clazz == Short[].class) {
			array = getInt16Array(maxArrayLength);
		} else if (clazz == UnsignedShort[].class) {
			array = getUInt16Array(maxArrayLength);
		} else if (clazz == Integer[].class) {
			array = getInt32Array(maxArrayLength);
		} else if (clazz == UnsignedInteger[].class) {
			array = getUInt32Array(maxArrayLength);
		} else if (clazz == Long[].class) {
			array = getInt64Array(maxArrayLength);
		} else if (clazz == UnsignedLong[].class) {
			array = getUInt64Array(maxArrayLength);
		} else if (clazz == Float[].class) {
			array = getFloatArray(maxArrayLength);
		} else if (clazz == Double[].class) {
			array = getDoubleArray(maxArrayLength);
		} else if (clazz == String[].class) {
			array = getStringArray(maxArrayLength, maxStringLength);
		} else if (clazz == DateTime[].class) {
			array = getDateTimeArray(maxArrayLength);
		} else if (clazz == UUID[].class) {
			array = getGuidArray(maxArrayLength);
		} else if (clazz == byte[][].class) {
			array = getByteStringArray(maxArrayLength, maxStringLength);
		} else if (clazz == XmlElement[].class) {
			array = getXmlElementArray(maxArrayLength);
		} else if (clazz == NodeId[].class) {
			array = getNodeIdArray(maxArrayLength, maxStringLength);
		} else if (clazz == ExpandedNodeId[].class) {
			array = getExpandedNodeIdArray(maxArrayLength, maxStringLength);
		} else if (clazz == StatusCode[].class) {
			array = getStatusCodeArray(maxArrayLength);
		} else if (clazz == DiagnosticInfo[].class) {
			array = getDiagnosticInfoArray(maxArrayLength, maxDepth);
		} else if (clazz == QualifiedName[].class) {
			array = getQualifiedNameArray(maxArrayLength, maxStringLength);
		} else if (clazz == LocalizedText[].class) {
			array = getLocalizedTextArray(maxArrayLength, maxStringLength);
		} else if (clazz == ExtensionObject[].class) {
			array = getExtensionObjectArray(maxArrayLength, maxStringLength,
					maxDepth);
		} else if (clazz == DataValue[].class) {
			array = getDataValueArray(maxArrayLength, maxStringLength, maxDepth);
		}

		if (array == null) {
			return new Variant(array);
		}

		if (array.length == 0) {
			return new Variant(array);
		}

		// Create a multi-dim array?
		if (getRandomByte().intValue() > 128) {
			int[] dimensions = new int[getRandomByte().intValue() % 3 + 2];

			int length = array.length;

			for (int jj = 0; jj < dimensions.length - 1; jj++) {
				if (length > 3) {
					Boolean prime = true;

					for (int ii = 2; ii <= length / 2; ii++) {
						if (length % ii == 0) {
							dimensions[jj] = ii;
							length = length / ii;
							prime = false;
							break;
						}
					}

					if (prime) {
						dimensions[jj] = length;
						length = 1;
					}
				} else if (length > 1) {
					dimensions[jj] = length;
					length = 1;
				} else {
					dimensions[jj] = 1;
				}
			}

			dimensions[dimensions.length - 1] = length;

			return new Variant(MultiDimensionArrayUtils.demuxArray(array,
					dimensions));
		}

		return new Variant(array);
	}

	public Variant[] getVariantArray(int maxDepth, int maxArrayLength,
			int maxStringLength) {
		int length = getInt32Range(-1, maxArrayLength);

		if (length < 0) {
			return null;
		}

		Variant[] values = new Variant[length];

		for (int ii = 0; ii < values.length; ii++) {
			if (ii == 91)
				;
			values[ii] = getVariant(maxArrayLength, maxStringLength, maxDepth);
		}

		return values;
	}

	// / <summary>
	// / This method returns CompositeTestType.
	// / </summary>
	public CompositeTestType getCompositeTestType(int maxArrayLength,
			int maxStringLength, int maxDepth) {
		return new CompositeTestType(getScalarTestType(maxStringLength,
				maxDepth), getArrayTestType(maxDepth, maxArrayLength,
				maxStringLength));
	}

	// / <summary>
	// / This method returns a random index in an array.
	// / </summary>
	// / <param name="value">Array</param>
	// / <returns></returns>
	public int getRandomIndex(Object value) {
		return getInt32Range(0, Array.getLength(value) - 1);
	}

	// / <summary>
	// / Check is a boundary value should be generated.
	// / </summary>
	public Boolean useBoundaryValue() {
		// return getRandomSByte() <= getBoundaryValueRate(); testi 14.4.2009
		// UnsignedByte boundaryValueRate = new UnsignedByte(64);
		return getRandomByte().getValue() <= getBoundaryValueRate().getValue();
	}

	// / <summary>
	// / This method returns a Int32 value that is greater than or equal to
	// first and less than or equal to last.
	// / </summary>
	// / <param name="first">Minimum range value.</param>
	// / <param name="last">Maximum range value.</param>
	// / <returns>Int32 value.</returns>

	private Integer getInt32Range(Integer first, Integer last) {
		if (last == first) {
			return first;
		}

		UnsignedInteger sample = getRandomUInt32();

//		 System.out.println("sample=" + sample);
//		 System.out.println("sample.getValue()=" + sample.getValue());
//		 System.out.println("first=" + first);
//		 System.out.println("last=" + last);

		if (first < last) {
			long value = sample.getValue() % (last - first + 1);
//			 System.out.println("value=" + value);
			return (int) (value + first);
		} else {
			long value = sample.getValue() % (first - last + 1);
			return (int) (value + last);
		}
	}

	// / <summary>
	// / This method returns a Int64 value that is greater than or equal to
	// first and less than or equal to last.
	// / </summary>
	// / <param name="first">Minimum range value.</param>
	// / <param name="last">Maximum range value.</param>
	// / <returns>Int64 value.</returns>
	public long getInt64Range(long first, long last) {
		if (last == first) {
			return first;
		}

		if (first < last) {
			return (getRandomInt64() % (last - first + 1)) + first;
		} else {
			return (getRandomInt64() % (first - last + 1)) + last;
		}
	}

	// / <summary>
	// / This method returns a random enumerated value.
	// / </summary>
	// / <param name="enumType">Enum Type.</param>
	// / <returns>Random enumerated value</returns>
	public <T extends Enum<T>> T getEnum(Class<T> enumType) {
		T[] values = enumType.getEnumConstants();
		int index = getInt32Range(0, values.length - 1);
		return values[index];
	}

	// / <summary>
	// / This method returns an Enum array.
	// / </summary>
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T[] getEnumArray(Class<T> enumType,
			int maxArrayLength) {
		ArrayList<T> values = new ArrayList<T>();
		int length = getInt32Range(-1, maxArrayLength);

		for (int i = 0; i < length; i++)
			values.add(getEnum(enumType));
		return values.toArray((T[]) Array.newInstance(enumType, length));
	}

	// / <summary>
	// / This method returns a 32-bit integer Timeout Value.
	// / </summary>
	public int getTimeout(int minTimeout, int maxTimeout) {
		return getInt32Range(minTimeout, maxTimeout);
	}

	public UnsignedByte getBoundaryValueRate() {
		return boundaryValueRate;
	}

	public void setBoundaryValueRate(UnsignedByte boundaryValueRate) {
		this.boundaryValueRate = boundaryValueRate;
	}

	// / <summary>
	// / This method returns an array of random bytes.
	// / </summary>
	// / <param name="length">Size of the array.</param>
	// / <returns></returns>
	private byte[] Next(int length) {
		try {
			return random.randomGetValue(length);
		} catch (UARandomLibException e) {
			throw new RuntimeServiceResultException(new ServiceResultException(
					StatusCodes.Bad_ConfigurationError, e.getMessage()));
		}

		/*
		 * byte[] buffer = new byte[length]; Marshal.Copy(buffer, buffer, 0,
		 * length); return buffer;
		 */}

	public enum RandomSeedType {
		RequestSeed, ResponseSeed
	};
}
