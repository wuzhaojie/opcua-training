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

package com.prosysopc.ua.encoding;

import com.prosysopc.ua.builtintypes.ByteString;
import com.prosysopc.ua.builtintypes.DataValue;
import com.prosysopc.ua.builtintypes.DateTime;
import com.prosysopc.ua.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.builtintypes.Enumeration;
import com.prosysopc.ua.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.builtintypes.ExtensionObject;
import com.prosysopc.ua.builtintypes.LocalizedText;
import com.prosysopc.ua.builtintypes.NodeId;
import com.prosysopc.ua.builtintypes.QualifiedName;
import com.prosysopc.ua.builtintypes.StatusCode;
import com.prosysopc.ua.builtintypes.Structure;
import com.prosysopc.ua.builtintypes.UnsignedByte;
import com.prosysopc.ua.builtintypes.UnsignedInteger;
import com.prosysopc.ua.builtintypes.UnsignedLong;
import com.prosysopc.ua.builtintypes.UnsignedShort;
import com.prosysopc.ua.builtintypes.Variant;
import com.prosysopc.ua.builtintypes.XmlElement;
import com.prosysopc.ua.encoding.binary.BinaryEncoder;
import com.prosysopc.ua.encoding.xml.XmlEncoder;
import java.util.Collection;
import java.util.UUID;

/**
 * Abstract base implementation for encoders
 *
 * @see XmlEncoder
 * @see BinaryEncoder
 * @see IDecoder
 */
public interface IEncoder {
	
	/**
	 * <p>putBoolean.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Boolean} object.
	 * @throws EncodingException if any.
	 */
	void putBoolean(String fieldName, Boolean v)
    throws EncodingException;	
	
	/**
	 * <p>putBooleanArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Boolean} objects.
	 * @throws EncodingException if any.
	 */
	void putBooleanArray(String fieldName, Boolean[] v)
    throws EncodingException;	
	
	/**
	 * <p>putBooleanArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putBooleanArray(String fieldName, Collection<Boolean> v)
    throws EncodingException;	
	
	/**
	 * <p>putSByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Byte} object.
	 * @throws EncodingException if any.
	 */
	void putSByte(String fieldName, Byte v)
    throws EncodingException;	
	
	/**
	 * <p>putSByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a byte.
	 * @throws EncodingException if any.
	 */
	void putSByte(String fieldName, byte v)
    throws EncodingException;	
	
	/**
	 * <p>putSByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a int.
	 * @throws EncodingException if any.
	 */
	void putSByte(String fieldName, int v)
    throws EncodingException;
	
	/**
	 * <p>putSByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Byte} objects.
	 * @throws EncodingException if any.
	 */
	void putSByteArray(String fieldName, Byte[] v)
    throws EncodingException;	
	
	/**
	 * <p>putSByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putSByteArray(String fieldName, Collection<Byte> v)
    throws EncodingException;	
	
	/**
	 * <p>putByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link UnsignedByte} object.
	 * @throws EncodingException if any.
	 */
	void putByte(String fieldName, UnsignedByte v)
    throws EncodingException;	
	
	/**
	 * <p>putByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link UnsignedByte} objects.
	 * @throws EncodingException if any.
	 */
	void putByteArray(String fieldName, UnsignedByte[] v)
    throws EncodingException;	
	
	/**
	 * <p>putByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putByteArray(String fieldName, Collection<UnsignedByte> v)
    throws EncodingException;	
	
	/**
	 * <p>putInt16.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Short} object.
	 * @throws EncodingException if any.
	 */
	void putInt16(String fieldName, Short v)
    throws EncodingException;	
	
	/**
	 * <p>putInt16.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a short.
	 * @throws EncodingException if any.
	 */
	void putInt16(String fieldName, short v)
    throws EncodingException;	
	
	/**
	 * <p>putInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Short} objects.
	 * @throws EncodingException if any.
	 */
	void putInt16Array(String fieldName, Short[] v)
    throws EncodingException;	
	
	/**
	 * <p>putInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putInt16Array(String fieldName, Collection<Short> v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt16.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link UnsignedShort} object.
	 * @throws EncodingException if any.
	 */
	void putUInt16(String fieldName, UnsignedShort v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link UnsignedShort} objects.
	 * @throws EncodingException if any.
	 */
	void putUInt16Array(String fieldName, UnsignedShort[] v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putUInt16Array(String fieldName, Collection<UnsignedShort> v)
    throws EncodingException;	
	
	/**
	 * <p>putInt32.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Integer} object.
	 * @throws EncodingException if any.
	 */
	void putInt32(String fieldName, Integer v)
    throws EncodingException;	
	
	/**
	 * <p>putInt32.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a int.
	 * @throws EncodingException if any.
	 */
	void putInt32(String fieldName, int v)
    throws EncodingException;	
	
	/**
	 * <p>putInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of int.
	 * @throws EncodingException if any.
	 */
	void putInt32Array(String fieldName, int[] v)
    throws EncodingException;	
	
	/**
	 * <p>putInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putInt32Array(String fieldName, Collection<Integer> v)
    throws EncodingException;	
	
	/**
	 * <p>putInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Integer} objects.
	 * @throws EncodingException if any.
	 */
	void putInt32Array(String fieldName, Integer[] v)
    throws EncodingException;	

	/**
	 * <p>putUInt32.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link UnsignedInteger} object.
	 * @throws EncodingException if any.
	 */
	void putUInt32(String fieldName, UnsignedInteger v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link UnsignedInteger} objects.
	 * @throws EncodingException if any.
	 */
	void putUInt32Array(String fieldName, UnsignedInteger[] v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putUInt32Array(String fieldName, Collection<UnsignedInteger> v)
    throws EncodingException;	

	/**
	 * <p>putInt64.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Long} object.
	 * @throws EncodingException if any.
	 */
	void putInt64(String fieldName, Long v)
    throws EncodingException;	
	
	/**
	 * <p>putInt64.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a long.
	 * @throws EncodingException if any.
	 */
	void putInt64(String fieldName, long v)
    throws EncodingException;	
	
	/**
	 * <p>putInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Long} objects.
	 * @throws EncodingException if any.
	 */
	void putInt64Array(String fieldName, Long[] v)
    throws EncodingException;	
	
	/**
	 * <p>putInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putInt64Array(String fieldName, Collection<Long> v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt64.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link UnsignedLong} object.
	 * @throws EncodingException if any.
	 */
	void putUInt64(String fieldName, UnsignedLong v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link UnsignedLong} objects.
	 * @throws EncodingException if any.
	 */
	void putUInt64Array(String fieldName, UnsignedLong[] v)
    throws EncodingException;	
	
	/**
	 * <p>putUInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putUInt64Array(String fieldName, Collection<UnsignedLong> v)
    throws EncodingException;	
	
	/**
	 * <p>putFloat.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Float} object.
	 * @throws EncodingException if any.
	 */
	void putFloat(String fieldName, Float v)
    throws EncodingException;	
	
	/**
	 * <p>putFloat.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a float.
	 * @throws EncodingException if any.
	 */
	void putFloat(String fieldName, float v)
    throws EncodingException;	
	
	/**
	 * <p>putFloatArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Float} objects.
	 * @throws EncodingException if any.
	 */
	void putFloatArray(String fieldName, Float[] v)
    throws EncodingException;	
	
	/**
	 * <p>putFloatArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putFloatArray(String fieldName, Collection<Float> v)
    throws EncodingException;	
	
	/**
	 * <p>putDouble.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.Double} object.
	 * @throws EncodingException if any.
	 */
	void putDouble(String fieldName, Double v)
    throws EncodingException;	
	
	/**
	 * <p>putDouble.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a double.
	 * @throws EncodingException if any.
	 */
	void putDouble(String fieldName, double v)
    throws EncodingException;	
	
	/**
	 * <p>putDoubleArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.Double} objects.
	 * @throws EncodingException if any.
	 */
	void putDoubleArray(String fieldName, Double[] v)
    throws EncodingException;	
	
	/**
	 * <p>putDoubleArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putDoubleArray(String fieldName, Collection<Double> v)
    throws EncodingException;	
	
	/**
	 * <p>putString.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.lang.String} object.
	 * @throws EncodingException if any.
	 */
	void putString(String fieldName, String v)
    throws EncodingException;	
	
	/**
	 * <p>putStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putStringArray(String fieldName, Collection<String> v)
	throws EncodingException;
	
	/**
	 * <p>putStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.lang.String} objects.
	 * @throws EncodingException if any.
	 */
	void putStringArray(String fieldName, String[] v)
    throws EncodingException;	
	
	/**
	 * <p>putDateTime.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link DateTime} object.
	 * @throws EncodingException if any.
	 */
	void putDateTime(String fieldName, DateTime v)
    throws EncodingException;	
	
	/**
	 * <p>putDateTimeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link DateTime} objects.
	 * @throws EncodingException if any.
	 */
	void putDateTimeArray(String fieldName, DateTime[] v)
    throws EncodingException;	
	
	/**
	 * <p>putDateTimeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putDateTimeArray(String fieldName, Collection<DateTime> v)
    throws EncodingException;	
	
	/**
	 * <p>putGuid.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.UUID} object.
	 * @throws EncodingException if any.
	 */
	void putGuid(String fieldName, UUID v)
    throws EncodingException;	
	
	/**
	 * <p>putGuidArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link java.util.UUID} objects.
	 * @throws EncodingException if any.
	 */
	void putGuidArray(String fieldName, UUID[] v)
    throws EncodingException;	
	
	/**
	 * <p>putGuidArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putGuidArray(String fieldName, Collection<UUID> v)
    throws EncodingException;	
	
	/**
	 * <p>putByteString.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of byte.
	 * @throws EncodingException if any.
	 */
	void putByteString(String fieldName, ByteString v)
    throws EncodingException;	
	
	/**
	 * <p>putByteStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of byte.
	 * @throws EncodingException if any.
	 */
	void putByteStringArray(String fieldName, ByteString[] v)
    throws EncodingException;	
	
	/**
	 * <p>putByteStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putByteStringArray(String fieldName, Collection<ByteString> v)
    throws EncodingException;	
	
	/**
	 * <p>putXmlElement.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link XmlElement} object.
	 * @throws EncodingException if any.
	 */
	void putXmlElement(String fieldName, XmlElement v)
    throws EncodingException;	
	
	/**
	 * <p>putXmlElementArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link XmlElement} objects.
	 * @throws EncodingException if any.
	 */
	void putXmlElementArray(String fieldName, XmlElement[] v)
    throws EncodingException;	
	
	/**
	 * <p>putXmlElementArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putXmlElementArray(String fieldName, Collection<XmlElement> v)
    throws EncodingException;	
	
	/**
	 * <p>putNodeId.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link NodeId} object.
	 * @throws EncodingException if any.
	 */
	void putNodeId(String fieldName, NodeId v)
    throws EncodingException;	
	
	/**
	 * <p>putNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link NodeId} objects.
	 * @throws EncodingException if any.
	 */
	void putNodeIdArray(String fieldName, NodeId[] v)
    throws EncodingException;	
	
	/**
	 * <p>putNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putNodeIdArray(String fieldName, Collection<NodeId> v)
    throws EncodingException;
	
	/**
	 * <p>putExpandedNodeId.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link ExpandedNodeId} object.
	 * @throws EncodingException if any.
	 */
	void putExpandedNodeId(String fieldName, ExpandedNodeId v)
    throws EncodingException;	
	
	/**
	 * <p>putExpandedNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link ExpandedNodeId} objects.
	 * @throws EncodingException if any.
	 */
	void putExpandedNodeIdArray(String fieldName, ExpandedNodeId[] v)
    throws EncodingException;	
	
	/**
	 * <p>putExpandedNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putExpandedNodeIdArray(String fieldName, Collection<ExpandedNodeId> v)
    throws EncodingException;	
	
	/**
	 * <p>putStatusCode.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link StatusCode} object.
	 * @throws EncodingException if any.
	 */
	void putStatusCode(String fieldName, StatusCode v)
    throws EncodingException;	
	
	/**
	 * <p>putStatusCodeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link StatusCode} objects.
	 * @throws EncodingException if any.
	 */
	void putStatusCodeArray(String fieldName, StatusCode[] v)
    throws EncodingException;	
	
	/**
	 * <p>putStatusCodeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putStatusCodeArray(String fieldName, Collection<StatusCode> v)
    throws EncodingException;	
	
	/**
	 * <p>putQualifiedName.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link QualifiedName} object.
	 * @throws EncodingException if any.
	 */
	void putQualifiedName(String fieldName, QualifiedName v)
    throws EncodingException;	
	
	/**
	 * <p>putQualifiedNameArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link QualifiedName} objects.
	 * @throws EncodingException if any.
	 */
	void putQualifiedNameArray(String fieldName, QualifiedName[] v)
    throws EncodingException;	
	
	/**
	 * <p>putQualifiedNameArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putQualifiedNameArray(String fieldName, Collection<QualifiedName> v)
    throws EncodingException;	
	
	/**
	 * <p>putLocalizedText.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link LocalizedText} object.
	 * @throws EncodingException if any.
	 */
	void putLocalizedText(String fieldName, LocalizedText v)
    throws EncodingException;	
	
	/**
	 * <p>putLocalizedTextArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link LocalizedText} objects.
	 * @throws EncodingException if any.
	 */
	void putLocalizedTextArray(String fieldName, LocalizedText[] v)
    throws EncodingException;	
	
	/**
	 * <p>putLocalizedTextArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putLocalizedTextArray(String fieldName, Collection<LocalizedText> v)
    throws EncodingException;	
	
	/**
	 * <p>putStructure.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link Structure} object.
	 * @throws EncodingException if any.
	 */
	void putStructure(String fieldName, Structure v)
    throws EncodingException;		

	/**
	 * <p>putStructureArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link Structure} objects.
	 * @throws EncodingException if any.
	 */
	void putStructureArray(String fieldName, Structure[] v)
    throws EncodingException;		

	/**
	 * <p>putStructureArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putStructureArray(String fieldName, Collection<Structure> v)
    throws EncodingException;		
	
	/**
	 * <p>putExtensionObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link ExtensionObject} object.
	 * @throws EncodingException if any.
	 */
	void putExtensionObject(String fieldName, ExtensionObject v)
    throws EncodingException;	
	
	/**
	 * <p>putExtensionObjectArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link ExtensionObject} objects.
	 * @throws EncodingException if any.
	 */
	void putExtensionObjectArray(String fieldName, ExtensionObject[] v)
    throws EncodingException;	
	
	/**
	 * <p>putExtensionObjectArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putExtensionObjectArray(String fieldName, Collection<ExtensionObject> v)
    throws EncodingException;	
	
	/**
	 * <p>putDataValue.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link DataValue} object.
	 * @throws EncodingException if any.
	 */
	void putDataValue(String fieldName, DataValue v)
    throws EncodingException;	
	
	/**
	 * <p>putDataValueArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link DataValue} objects.
	 * @throws EncodingException if any.
	 */
	void putDataValueArray(String fieldName, DataValue[] v)
    throws EncodingException;	
	
	/**
	 * <p>putDataValueArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putDataValueArray(String fieldName, Collection<DataValue> v)
    throws EncodingException;	
	
	/**
	 * <p>putVariant.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link Variant} object.
	 * @throws EncodingException if any.
	 */
	void putVariant(String fieldName, Variant v)
    throws EncodingException;	
	
	/**
	 * <p>putVariantArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link Variant} objects.
	 * @throws EncodingException if any.
	 */
	void putVariantArray(String fieldName, Variant[] v)
    throws EncodingException;	
	
	/**
	 * <p>putVariantArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putVariantArray(String fieldName, Collection<Variant> v)
    throws EncodingException;	
	
	/**
	 * <p>putDiagnosticInfoArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v an array of {@link DiagnosticInfo} objects.
	 * @throws EncodingException if any.
	 */
	void putDiagnosticInfoArray(String fieldName, DiagnosticInfo[] v)
    throws EncodingException;
	
	/**
	 * <p>putDiagnosticInfoArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link java.util.Collection} object.
	 * @throws EncodingException if any.
	 */
	void putDiagnosticInfoArray(String fieldName, Collection<DiagnosticInfo> v)
    throws EncodingException;
	
	/**
	 * <p>putDiagnosticInfo.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link DiagnosticInfo} object.
	 * @throws EncodingException if any.
	 */
	void putDiagnosticInfo(String fieldName, DiagnosticInfo v)
    throws EncodingException;	
	
	/**
	 * <p>putEnumerationArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param array a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putEnumerationArray(String fieldName, Object array)
    throws EncodingException;	
	
	/**
	 * <p>putEnumeration.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param v a {@link Enumeration} object.
	 * @throws EncodingException if any.
	 */
	void putEnumeration(String fieldName, Enumeration v)
    throws EncodingException;	
	
	/**
	 * <p>putObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param o a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putObject(String fieldName, Object o)
    throws EncodingException;	

	/**
	 * <p>putObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param c a {@link java.lang.Class} object.
	 * @param o a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putObject(String fieldName, Class<?> c, Object o)
    throws EncodingException;	
	
	/**
	 * <p>putScalar.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param builtinType a int.
	 * @param o a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putScalar(String fieldName, int builtinType, Object o)
    throws EncodingException;	
	
	/**
	 * <p>putArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param builtinType a int.
	 * @param o a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putArray(String fieldName, int builtinType, Object o)
    throws EncodingException;	
	
	/**
	 * <p>putEncodeableArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @param array a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void putEncodeableArray(String fieldName, Class<? extends IEncodeable> clazz, Object array)
    throws EncodingException;	
	
	/**
	 * <p>putEncodeable.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param s a {@link IEncodeable} object.
	 * @throws EncodingException if any.
	 */
	void putEncodeable(String fieldName, IEncodeable s)
    throws EncodingException;	
	
	/**
	 * <p>putEncodeable.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @param s a {@link IEncodeable} object.
	 * @throws EncodingException if any.
	 */
	void putEncodeable(String fieldName, Class<? extends IEncodeable> clazz, IEncodeable s)
    throws EncodingException;	
	
	/**
	 * <p>putMessage.</p>
	 *
	 * @param s a {@link IEncodeable} object.
	 * @throws EncodingException if any.
	 */
	void putMessage(IEncodeable s)
    throws EncodingException;

	/**
	 * <p>put.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param o a {@link java.lang.Object} object.
	 * @throws EncodingException if any.
	 */
	void put(String fieldName, Object o)
	throws EncodingException;

	/**
	 * <p>put.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param o a {@link java.lang.Object} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @throws EncodingException if any.
	 */
	void put(String fieldName, Object o, Class<?> clazz)
	throws EncodingException;
}
