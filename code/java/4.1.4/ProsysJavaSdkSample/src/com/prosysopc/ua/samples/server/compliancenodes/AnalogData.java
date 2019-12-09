/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import java.util.EnumSet;

import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedByte;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.UnsignedLong;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.Range;

public enum AnalogData implements CommonComplianceInfo {

  BYTE("Byte", Identifiers.Byte, UnsignedByte.valueOf(0), new Range(U.LOW, U.HIGH)),

  BYTE_ARRAY("Byte", Identifiers.Byte, Util.unsignedByteArray(0, 1, 2, 3, 4, 5), new Range(U.LOW, U.HIGH)),

  DOUBLE("Double", Identifiers.Double, Double.valueOf(0), new Range(S.LOW, S.HIGH)),

  DOUBLE_ARRAY("Double", Identifiers.Double, Util.doubleArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  FLOAT("Float", Identifiers.Float, Float.valueOf(0), new Range(S.LOW, S.HIGH)),

  FLOAT_ARRAY("Float", Identifiers.Float, Util.floatArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  INSTRUMENT_INT32("Sample Instrument Int32", Identifiers.Int32, Integer.valueOf(25), new Range(20.0, 40.0),
      new Range(0.0, 100.0)),

  INT16("Int16", Identifiers.Int16, Short.valueOf((short) 0), new Range(S.LOW, S.HIGH)),

  INT16_ARRAY("Int16", Identifiers.Int16, Util.signedShortArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  INT32("Int32", Identifiers.Int32, Integer.valueOf(0), new Range(S.LOW, S.HIGH)),

  INT32_ARRAY("Int32", Identifiers.Int32, Util.signedIntegerArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  INT64("Int64", Identifiers.Int64, Long.valueOf(0), new Range(S.LOW, S.HIGH)),

  INT64_ARRAY("Int64", Identifiers.Int64, Util.signedLongArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  SBYTE("SByte", Identifiers.SByte, Byte.valueOf((byte) 0), new Range(S.LOW, S.HIGH)),

  SBYTE_ARRAY("SByte", Identifiers.SByte, Util.signedByteArray(0, 1, 2, 3, 4, 5), new Range(S.LOW, S.HIGH)),

  UINT16("UInt16", Identifiers.UInt16, UnsignedShort.valueOf(0), new Range(U.LOW, U.HIGH)),

  UINT16_ARRAY("UInt16", Identifiers.UInt16, Util.unsignedShortArray(0, 1, 2, 3, 4, 5), new Range(U.LOW, U.HIGH)),

  UINT32("UInt32", Identifiers.UInt32, UnsignedInteger.valueOf(0), new Range(U.LOW, U.HIGH)),

  UINT32_ARRAY("UInt32", Identifiers.UInt32, Util.unsignedIntegerArray(0, 1, 2, 3, 4, 5), new Range(U.LOW, U.HIGH)),

  UINT64("UInt64", Identifiers.UInt64, UnsignedLong.valueOf(0), new Range(U.LOW, U.HIGH)),

  UINT64_ARRAY("UInt64", Identifiers.UInt64, Util.unsignedLongArray(0, 1, 2, 3, 4, 5), new Range(U.LOW, U.HIGH));

  private class S {
    public static final double HIGH = 50.0;
    public static final double LOW = -50.0;
  }

  private class U {
    public static final double HIGH = 100.0;
    public static final double LOW = 0.0;
  }

  public static final EnumSet<AnalogData> ANALOG_ITEM_ARRAYS = EnumSet.of(BYTE_ARRAY, UINT16_ARRAY, UINT32_ARRAY,
      UINT64_ARRAY, SBYTE_ARRAY, INT16_ARRAY, INT32_ARRAY, INT64_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY);

  public static final EnumSet<AnalogData> ANALOG_ITEMS =
      EnumSet.of(BYTE, UINT16, UINT32, UINT64, FLOAT, DOUBLE, SBYTE, INT16, INT32, INT64, INSTRUMENT_INT32);

  private final NodeId dataTypeId;

  private final String dataTypeName;

  private final Range euRange;

  private final Object initialValue;

  private final Range instrumentRange;

  private AnalogData(String dataTypeName, NodeId dataTypeId, Object initialValue, Range euRange) {
    this(dataTypeName, dataTypeId, initialValue, euRange, null);
  }

  private AnalogData(String dataTypeName, NodeId dataTypeId, Object initialValue, Range euRange,
      Range instrumentRange) {
    this.dataTypeName = dataTypeName;
    this.dataTypeId = dataTypeId;
    this.initialValue = initialValue;
    this.euRange = euRange;
    this.instrumentRange = instrumentRange;
  }

  @Override
  public String getBaseName() {
    return dataTypeName;
  }

  @Override
  public NodeId getDataTypeId() {
    return dataTypeId;
  }

  public String getDataTypeName() {
    return dataTypeName;
  }

  public Range getEURange() {
    return euRange;
  }

  @Override
  public Object getInitialValue() {
    return initialValue;
  }

  public Range getInstrumentRange() {
    return instrumentRange;
  }
}
