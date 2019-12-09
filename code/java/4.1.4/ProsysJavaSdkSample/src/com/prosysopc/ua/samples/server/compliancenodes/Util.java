/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.compliancenodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.stack.builtintypes.UnsignedByte;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.UnsignedLong;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;

class Util {

  public static final Logger logger = LoggerFactory.getLogger(Util.class);

  public static Double[] doubleArray(int... values) {
    Double[] r = new Double[values.length];
    for (int i : values) {
      r[i] = Double.valueOf(i);
    }
    return r;
  }

  public static Float[] floatArray(int... values) {
    Float[] r = new Float[values.length];
    for (int i : values) {
      r[i] = Float.valueOf(i);
    }
    return r;
  }

  public static Byte[] signedByteArray(int... values) {
    Byte[] r = new Byte[values.length];
    for (int i : values) {
      r[i] = Byte.valueOf((byte) i);
    }
    return r;
  }

  public static Integer[] signedIntegerArray(int... values) {
    Integer[] r = new Integer[values.length];
    for (int i : values) {
      r[i] = Integer.valueOf(i);
    }
    return r;
  }

  public static Long[] signedLongArray(int... values) {
    Long[] r = new Long[values.length];
    for (int i : values) {
      r[i] = Long.valueOf(i);
    }
    return r;
  }

  public static Short[] signedShortArray(int... values) {
    Short[] r = new Short[values.length];
    for (int i : values) {
      r[i] = Short.valueOf((short) i);
    }
    return r;
  }

  public static UnsignedByte[] unsignedByteArray(int... values) {
    UnsignedByte[] r = new UnsignedByte[values.length];
    for (int i : values) {
      r[i] = UnsignedByte.valueOf(i);
    }
    return r;
  }

  public static UnsignedInteger[] unsignedIntegerArray(int... values) {
    UnsignedInteger[] r = new UnsignedInteger[values.length];
    for (int i : values) {
      r[i] = UnsignedInteger.valueOf(i);
    }
    return r;
  }

  public static UnsignedLong[] unsignedLongArray(int... values) {
    UnsignedLong[] r = new UnsignedLong[values.length];
    for (int i : values) {
      r[i] = UnsignedLong.valueOf(i);
    }
    return r;
  }

  public static UnsignedShort[] unsignedShortArray(int... values) {
    UnsignedShort[] r = new UnsignedShort[values.length];
    for (int i : values) {
      r[i] = UnsignedShort.valueOf(i);
    }
    return r;
  }

  private Util() {}

}
