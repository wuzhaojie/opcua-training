/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.EnumSet;
import java.util.UUID;

import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedByte;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.UnsignedLong;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.builtintypes.XmlElement;
import com.prosysopc.ua.stack.core.Identifiers;

/**
 * This enum will be used to construct StaticData folder.
 *
 */
public enum StaticData implements CommonComplianceInfo {
  VARIANT("Variant", Identifiers.BaseDataType, Boolean.TRUE),

  BOOLEAN("Boolean", Identifiers.Boolean, Boolean.TRUE),

  BYTE("Byte", Identifiers.Byte, UnsignedByte.valueOf(0)),

  BYTE_STRING("ByteString", Identifiers.ByteString, ByteString.valueOf((byte) 0)),

  DATE_TIME("DateTime", Identifiers.DateTime, DateTime.currentTime()),

  DOUBLE("Double", Identifiers.Double, Double.valueOf(0)),

  FLOAT("Float", Identifiers.Float, Float.valueOf(0)),

  GUID("GUID", Identifiers.Guid, UUID.randomUUID()),

  INT16("Int16", Identifiers.Int16, Short.valueOf((short) 0)),

  INT32("Int32", Identifiers.Int32, Integer.valueOf(0)),

  INT64("Int64", Identifiers.Int64, Long.valueOf(0)),

  SBYTE("SByte", Identifiers.SByte, Byte.valueOf((byte) 0)),

  STRING("String", Identifiers.String, "TestString"),

  UINT16("UInt16", Identifiers.UInt16, UnsignedShort.valueOf(0)),

  UINT32("UInt32", Identifiers.UInt32, UnsignedInteger.valueOf(0)),

  UINT64("UInt64", Identifiers.UInt64, UnsignedLong.valueOf(0)),

  XML_ELEMENT("XmlElement", Identifiers.XmlElement, new XmlElement("<testElement />")),

  // some additional nodes needed..
  DURATION("Duration", Identifiers.Duration, Double.valueOf(0.0)),

  QUALIFIED_NAME("QualifiedName", Identifiers.QualifiedName, QualifiedName.DEFAULT_BINARY_ENCODING),

  LOCALIZED_TEXT("LocalizedText", Identifiers.LocalizedText, LocalizedText.english("Test Text")),

  NODE_ID("NodeId", Identifiers.NodeId, Identifiers.NodeId),

  // even more additional nodes needed
  IMAGE("Image", Identifiers.Image, null),

  IMAGE_JPG("ImageJPG", Identifiers.ImageJPG, null),

  IMAGE_BMP("ImageBMP", Identifiers.ImageBMP, null),

  IMAGE_PNG("ImagePNG", Identifiers.ImagePNG, null),

  IMAGE_GIF("ImageGIF", Identifiers.ImageGIF, null),

  INTEGER("Integer", Identifiers.Integer, 1),

  LOCALE_ID("LocaleId", Identifiers.LocaleId, "en"),

  NUMBER("Number", Identifiers.Number, 1),

  UINTEGER("UInteger", Identifiers.UInteger, UnsignedInteger.valueOf(0)),

  TIME("Time", Identifiers.Time, "10:10:10.100"),

  UTC_TIME("UtcTime", Identifiers.UtcTime, DateTime.currentTime()),

  ENUMERATION("Enumeration", Identifiers.Enumeration, null),

  // "Array" postfix should be added when created..
  VARIANT_ARRAY("Variant", Identifiers.BaseDataType,
      new Variant[] {new Variant(Boolean.TRUE), new Variant(Boolean.TRUE), new Variant(Boolean.TRUE),
          new Variant(Boolean.TRUE), new Variant(Boolean.TRUE), new Variant(Boolean.TRUE)}),

  LOCALIZED_TEXT_ARRAY("LocalizedText", Identifiers.LocalizedText,
      new LocalizedText[] {LocalizedText.english("Text1"), LocalizedText.english("Text2"),
          LocalizedText.english("Text3"), LocalizedText.english("Text4"), LocalizedText.english("Text5"),
          LocalizedText.english("Text6")}),

  QUALIFIED_NAME_ARRAY("QualifiedName", Identifiers.QualifiedName,
      new QualifiedName[] {QualifiedName.DEFAULT_BINARY_ENCODING, QualifiedName.DEFAULT_XML_ENCODING,
          QualifiedName.DEFAULT_BINARY_ENCODING, QualifiedName.DEFAULT_XML_ENCODING,
          QualifiedName.DEFAULT_BINARY_ENCODING, QualifiedName.DEFAULT_XML_ENCODING}),

  BOOLEAN_ARRAY("Boolean", Identifiers.Boolean, new Boolean[] {true, false, true, false, false}),

  BYTE_ARRAY("Byte", Identifiers.Byte, new UnsignedByte[] {UnsignedByte.valueOf(1), UnsignedByte.valueOf(2),
      UnsignedByte.valueOf(3), UnsignedByte.valueOf(4), UnsignedByte.valueOf(5)}),

  BYTE_STRING_ARRAY("ByteString", Identifiers.ByteString,
      new ByteString[] {ByteString.valueOf(), ByteString.valueOf((byte) 1, (byte) 2, (byte) 3),
          ByteString.valueOf((byte) 2, (byte) 3, (byte) 4), ByteString.valueOf((byte) 3, (byte) 4, (byte) 5),
          ByteString.valueOf((byte) 4, (byte) 5, (byte) 6), ByteString.valueOf((byte) 5, (byte) 6, (byte) 7)}),

  DATA_TIME_ARRAY("DateTime", Identifiers.DateTime, new DateTime[] {DateTime.currentTime(), DateTime.currentTime(),
      DateTime.currentTime(), DateTime.currentTime(), DateTime.currentTime()}),

  DOUBLE_ARRAY("Double", Identifiers.Double, new Double[] {(double) 1, (double) 2, (double) 3, (double) 4, (double) 5}),

  DURATION_ARRAY("Duration", Identifiers.Duration,
      new Double[] {(double) 1, (double) 2, (double) 3, (double) 4, (double) 5}),

  FLOAT_ARRAY("Float", Identifiers.Float, new Float[] {(float) 1, (float) 2, (float) 3, (float) 4, (float) 5}),

  GUID_ARRAY("GUIDArray", Identifiers.Guid,
      new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()}),

  INT16_ARRAY("Int16", Identifiers.Int16, new Short[] {(short) 1, (short) 2, (short) 3, (short) 4, (short) 5}),

  INT32_ARRAY("Int32", Identifiers.Int32, new Integer[] {1, 2, 3, 4, 5}),

  INT64_ARRAY("Int64", Identifiers.Int64, new Long[] {(long) 1, (long) 2, (long) 3, (long) 4, (long) 5}),

  SBYTE_ARRAY("SByte", Identifiers.SByte, new Byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5}),

  STRING_ARRAY("String", Identifiers.String,
      new String[] {"testString1", "testString2", "testString3", "testString4", "testString5"}),

  UINT16_ARRAY("UInt16", Identifiers.UInt16, new UnsignedShort[] {UnsignedShort.valueOf(1), UnsignedShort.valueOf(2),
      UnsignedShort.valueOf(3), UnsignedShort.valueOf(4), UnsignedShort.valueOf(5)}),

  UINT32_ARRAY("UInt32", Identifiers.UInt32, new UnsignedInteger[] {UnsignedInteger.valueOf(1),
      UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(3), UnsignedInteger.valueOf(4), UnsignedInteger.valueOf(5)}),

  UINT64_ARRAY("UInt64", Identifiers.UInt64, new UnsignedLong[] {UnsignedLong.valueOf(1), UnsignedLong.valueOf(2),
      UnsignedLong.valueOf(3), UnsignedLong.valueOf(4), UnsignedLong.valueOf(5)}),

  XML_ELEMENT_ARRAY("XmlElement", Identifiers.XmlElement,
      new XmlElement[] {new XmlElement("<testElement1 />"), new XmlElement("<testElement2 />"),
          new XmlElement("<testElement3 />"), new XmlElement("<testElement4 />"), new XmlElement("<testElement5 />")});

  public static final EnumSet<StaticData> DATA_ITEMS = EnumSet.of(BOOLEAN, BYTE, BYTE_STRING, DATE_TIME, DOUBLE, FLOAT,
      GUID, INT16, INT32, INT64, SBYTE, STRING, UINT16, UINT32, UINT64, XML_ELEMENT);

  public static final EnumSet<StaticData> STATIC_DATAS =
      EnumSet.of(VARIANT, BOOLEAN, BYTE, UINT16, UINT32, UINT64, SBYTE, INT16, INT32, INT64, FLOAT, DOUBLE, DURATION,
          STRING, BYTE_STRING, LOCALIZED_TEXT, QUALIFIED_NAME, GUID, NODE_ID, DATE_TIME, XML_ELEMENT, IMAGE, IMAGE_JPG,
          IMAGE_PNG, IMAGE_BMP, IMAGE_GIF, LOCALE_ID, NUMBER, INTEGER, UINTEGER, TIME, UTC_TIME, ENUMERATION);

  public static final EnumSet<StaticData> ANALOG_ARRAY_ITEMS = EnumSet.of(BOOLEAN_ARRAY, BYTE_ARRAY, UINT16_ARRAY,
      UINT32_ARRAY, UINT64_ARRAY, SBYTE_ARRAY, INT16_ARRAY, INT32_ARRAY, INT64_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY,
      DURATION_ARRAY, STRING_ARRAY, BYTE_STRING_ARRAY, DATA_TIME_ARRAY, GUID_ARRAY, XML_ELEMENT_ARRAY);

  public static final EnumSet<StaticData> STATIC_DATA_ARRAYS =
      EnumSet.of(VARIANT_ARRAY, BOOLEAN_ARRAY, BYTE_ARRAY, BYTE_STRING_ARRAY, DATA_TIME_ARRAY, DOUBLE_ARRAY,
          FLOAT_ARRAY, GUID_ARRAY, INT16_ARRAY, INT32_ARRAY, INT64_ARRAY, SBYTE_ARRAY, STRING_ARRAY,
          LOCALIZED_TEXT_ARRAY, QUALIFIED_NAME_ARRAY, UINT16_ARRAY, UINT32_ARRAY, UINT64_ARRAY, XML_ELEMENT_ARRAY);

  private String dataTypeName;
  private NodeId dataType;
  private Object initialValue;

  private StaticData(String dataTypeName, NodeId dataType, Object initialValue) {
    this.dataTypeName = dataTypeName;
    this.dataType = dataType;
    this.initialValue = initialValue;
  }

  @Override
  public String getBaseName() {
    return dataTypeName;
  }

  @Override
  public NodeId getDataTypeId() {
    return dataType;
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  @Override
  public Object getInitialValue() {
    return initialValue;
  }

}
