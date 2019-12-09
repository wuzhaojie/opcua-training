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
import com.prosysopc.ua.encoding.binary.BinaryDecoder;
import java.util.UUID;

import com.prosysopc.ua.encoding.xml.XmlDecoder;


/**
 * <p>IDecoder interface.</p>
 *
 * @see IEncoder
 * @see BinaryDecoder
 * @see XmlDecoder
 */
public interface IDecoder {
	
	/**
	 * <p>getBoolean.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Boolean} object.
	 * @throws DecodingException if any.
	 */
	public Boolean getBoolean(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getBooleanArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Boolean} objects.
	 * @throws DecodingException if any.
	 */
	public Boolean[] getBooleanArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getSByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Byte} object.
	 * @throws DecodingException if any.
	 */
	public Byte getSByte(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getSByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Byte} objects.
	 * @throws DecodingException if any.
	 */
	public Byte[] getSByteArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getByte.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link UnsignedByte} object.
	 * @throws DecodingException if any.
	 */
	public UnsignedByte getByte(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getByteArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link UnsignedByte} objects.
	 * @throws DecodingException if any.
	 */
	public UnsignedByte[] getByteArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt16.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Short} object.
	 * @throws DecodingException if any.
	 */
	public Short getInt16(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Short} objects.
	 * @throws DecodingException if any.
	 */
	public Short[] getInt16Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt16.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link UnsignedShort} object.
	 * @throws DecodingException if any.
	 */
	public UnsignedShort getUInt16(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt16Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link UnsignedShort} objects.
	 * @throws DecodingException if any.
	 */
	public UnsignedShort[] getUInt16Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt32.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws DecodingException if any.
	 */
	public Integer getInt32(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Integer} objects.
	 * @throws DecodingException if any.
	 */
	public Integer[] getInt32Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt32Array_.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of int.
	 * @throws DecodingException if any.
	 */
	public int[] getInt32Array_(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt32.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link UnsignedInteger} object.
	 * @throws DecodingException if any.
	 */
	public UnsignedInteger getUInt32(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt32Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link UnsignedInteger} objects.
	 * @throws DecodingException if any.
	 */
	public UnsignedInteger[] getUInt32Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt64.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Long} object.
	 * @throws DecodingException if any.
	 */
	public Long getInt64(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Long} objects.
	 * @throws DecodingException if any.
	 */
	public Long[] getInt64Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt64.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link UnsignedLong} object.
	 * @throws DecodingException if any.
	 */
	public UnsignedLong getUInt64(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getUInt64Array.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link UnsignedLong} objects.
	 * @throws DecodingException if any.
	 */
	public UnsignedLong[] getUInt64Array(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getFloat.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Float} object.
	 * @throws DecodingException if any.
	 */
	public Float getFloat(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getFloatArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Float} objects.
	 * @throws DecodingException if any.
	 */
	public Float[] getFloatArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDouble.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 * @throws DecodingException if any.
	 */
	public Double getDouble(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDoubleArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.Double} objects.
	 * @throws DecodingException if any.
	 */
	public Double[] getDoubleArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getString.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws DecodingException if any.
	 */
	public String getString(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 * @throws DecodingException if any.
	 */
	public String[] getStringArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDateTime.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link DateTime} object.
	 * @throws DecodingException if any.
	 */
	public DateTime getDateTime(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDateTimeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link DateTime} objects.
	 * @throws DecodingException if any.
	 */
	public DateTime[] getDateTimeArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getGuid.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link java.util.UUID} object.
	 * @throws DecodingException if any.
	 */
	public UUID getGuid(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getGuidArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link java.util.UUID} objects.
	 * @throws DecodingException if any.
	 */
	public UUID[] getGuidArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getByteString.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of byte.
	 * @throws DecodingException if any.
	 */
	public ByteString getByteString(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getByteStringArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of byte.
	 * @throws DecodingException if any.
	 */
	public ByteString[] getByteStringArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getXmlElement.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link XmlElement} object.
	 * @throws DecodingException if any.
	 */
	public XmlElement getXmlElement(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getXmlElementArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link XmlElement} objects.
	 * @throws DecodingException if any.
	 */
	public XmlElement[] getXmlElementArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getNodeId.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link NodeId} object.
	 * @throws DecodingException if any.
	 */
	public NodeId getNodeId(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link NodeId} objects.
	 * @throws DecodingException if any.
	 */
	public NodeId[] getNodeIdArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getExpandedNodeId.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link ExpandedNodeId} object.
	 * @throws DecodingException if any.
	 */
	public ExpandedNodeId getExpandedNodeId(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getExpandedNodeIdArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link ExpandedNodeId} objects.
	 * @throws DecodingException if any.
	 */
	public ExpandedNodeId[] getExpandedNodeIdArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getStatusCode.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link StatusCode} object.
	 * @throws DecodingException if any.
	 */
	public StatusCode getStatusCode(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getStatusCodeArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link StatusCode} objects.
	 * @throws DecodingException if any.
	 */
	public StatusCode[] getStatusCodeArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getQualifiedName.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link QualifiedName} object.
	 * @throws DecodingException if any.
	 */
	public QualifiedName getQualifiedName(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getQualifiedNameArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link QualifiedName} objects.
	 * @throws DecodingException if any.
	 */
	public QualifiedName[] getQualifiedNameArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getLocalizedText.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link LocalizedText} object.
	 * @throws DecodingException if any.
	 */
	public LocalizedText getLocalizedText(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getLocalizedTextArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link LocalizedText} objects.
	 * @throws DecodingException if any.
	 */
	public LocalizedText[] getLocalizedTextArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getStructure.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link Structure} object.
	 * @throws DecodingException if any.
	 */
	public Structure getStructure(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getStructureArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link Structure} objects.
	 * @throws DecodingException if any.
	 */
	public Structure[] getStructureArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getExtensionObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link ExtensionObject} object.
	 * @throws DecodingException if any.
	 */
	public ExtensionObject getExtensionObject(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getExtensionObjectArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link ExtensionObject} objects.
	 * @throws DecodingException if any.
	 */
	public ExtensionObject[] getExtensionObjectArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDataValue.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link DataValue} object.
	 * @throws DecodingException if any.
	 */
	public DataValue getDataValue(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDataValueArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link DataValue} objects.
	 * @throws DecodingException if any.
	 */
	public DataValue[] getDataValueArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getVariant.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link Variant} object.
	 * @throws DecodingException if any.
	 */
	public Variant getVariant(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getVariantArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link Variant} objects.
	 * @throws DecodingException if any.
	 */
	public Variant[] getVariantArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getDiagnosticInfo.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return a {@link DiagnosticInfo} object.
	 * @throws DecodingException if any.
	 */
	public DiagnosticInfo getDiagnosticInfo(String fieldName)
    throws DecodingException;
	
	/**
	 * <p>getDiagnosticInfoArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @return an array of {@link DiagnosticInfo} objects.
	 * @throws DecodingException if any.
	 */
	public DiagnosticInfo[] getDiagnosticInfoArray(String fieldName)
    throws DecodingException;	
	
	/**
	 * <p>getEnumerationArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param enumerationClass a {@link java.lang.Class} object.
	 * @param <T> a T object.
	 * @return an array of T objects.
	 * @throws DecodingException if any.
	 */
	public <T extends Enumeration> T[] getEnumerationArray(String fieldName, Class<T> enumerationClass)
    throws DecodingException;	
	
	/**
	 * <p>getEnumeration.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param enumerationClass a {@link java.lang.Class} object.
	 * @param <T> a T object.
	 * @return a T object.
	 * @throws DecodingException if any.
	 */
	public <T extends Enumeration> T getEnumeration(String fieldName, Class<T> enumerationClass)
    throws DecodingException;	
	
	/**
	 * <p>getEncodeableArray.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param encodeableClass a {@link java.lang.Class} object.
	 * @param <T> a T object.
	 * @return an array of T objects.
	 * @throws DecodingException if any.
	 */
	public <T extends IEncodeable> T[] getEncodeableArray(String fieldName, Class<? extends T> encodeableClass)
    throws DecodingException;	
	
	/**
	 * <p>getEncodeable.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param encodeableClass a {@link java.lang.Class} object.
	 * @param <T> a T object.
	 * @return a T object.
	 * @throws DecodingException if any.
	 */
	public <T extends IEncodeable> T getEncodeable(String fieldName, Class<? extends T> encodeableClass)
    throws DecodingException;

	/**
	 * <p>get.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @param <T> a T object.
	 * @return a T object.
	 * @throws DecodingException if any.
	 */
	public <T> T get(String fieldName, Class<T> clazz)
	throws DecodingException;
	
	/**
	 * <p>getMessage.</p>
	 *
	 * @param <T> a T object.
	 * @return a T object.
	 * @throws DecodingException if any.
	 */
	public <T extends IEncodeable> T getMessage()
    throws DecodingException;	
	
	/**
	 * <p>getScalarObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param builtinTypeId a int.
	 * @return a {@link java.lang.Object} object.
	 * @throws DecodingException if any.
	 */
	public Object getScalarObject(String fieldName, int builtinTypeId)
    throws DecodingException;	
	
	/**
	 * <p>getArrayObject.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param builtinTypeId a int.
	 * @return a {@link java.lang.Object} object.
	 * @throws DecodingException if any.
	 */
	public Object getArrayObject(String fieldName, int builtinTypeId)
    throws DecodingException;	
	
}
