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

package org.opcfoundation.ua.builtintypes;

import java.math.BigInteger;

import org.opcfoundation.ua.core.Identifiers;

/**
 * Unsigned Long represents an integer number value 
 * between 0 .. and 0xFFFFFFFFFFFFFFFF.
 * <p>
 * There is a static instance for values between 0..1023 which can be accessed using
 * static methods {@link UnsignedLong#valueOf(long)} or {@link UnsignedLong#getFromBits(long)} 
 * <p>
 * This class is immutable - once it has been constructed its value cannot be changed. 
 * <p>
 * To use int as backend use {@link UnsignedLong#toLongBits()} and {@link UnsignedLong#getFromBits(long)}.
 */
public final class UnsignedLong extends Number implements Comparable<Number> {
		
    private static final UnsignedLong CACHE[] = new UnsignedLong[1024];
	
	private static final long serialVersionUID = 1L;
	public static final NodeId ID = Identifiers.UInt64;
	
	public static final int SIZE = 64;
	private static final long L_MAX_VALUE = Long.MAX_VALUE;
	private static final long L_HI_BIT = Long.MIN_VALUE;
	
	private static final BigInteger BI_L_MAX_VALUE = new BigInteger(Long.toString(L_MAX_VALUE));
	private static final BigInteger BI_MAX_VALUE = new BigInteger("2").pow(SIZE).add(new BigInteger("-1"));
	private static final BigInteger BI_MIN_VALUE = new BigInteger("0");
	private static final BigInteger BI_MID_VALUE = new BigInteger("2").pow(SIZE-1);
	private static final double D_MID_VALUE = BI_MID_VALUE.doubleValue(); 
	private static final float F_MID_VALUE = BI_MID_VALUE.floatValue(); 
	public static final UnsignedLong MAX_VALUE = new UnsignedLong(BI_MAX_VALUE); 
	public static final UnsignedLong MIN_VALUE = new UnsignedLong(BI_MIN_VALUE);

	public static final UnsignedLong ZERO = MIN_VALUE;
	public static final UnsignedLong ONE = new UnsignedLong(1);

	private long value;

	static {
		CACHE[0] = ZERO;
		CACHE[1] = ONE;
    	for (int i=2; i<CACHE.length; i++)
    		CACHE[i] = new UnsignedLong(i);
    }
	
	/**
	 * Create unsigned long from 64 bits
	 * 
	 * @param bits
	 * @return new or cached instance
	 */
	public static UnsignedLong getFromBits(long bits)
	{
		if (bits>=0 && bits<CACHE.length)
			return CACHE[(int)bits];		
		UnsignedLong result = new UnsignedLong(0);
		result.value = bits;
		return result;
	}

	/**
	 * Get cached or create new instance
	 * 
	 * @param value
	 * @return new or cached instance
	 */
	public static UnsignedLong valueOf(long value)
	{
		if (value>=0 && value<CACHE.length)
			return CACHE[(int)value];		
		return new UnsignedLong(value);
	}
	
	public UnsignedLong(BigInteger value) throws IllegalArgumentException {
		if (value.compareTo(BI_MIN_VALUE)<0) throw new IllegalArgumentException("Value underflow");
		if (value.compareTo(BI_MAX_VALUE)>0) throw new IllegalArgumentException("Value overflow");
		
		if (value.compareTo(BI_L_MAX_VALUE)<=0) 
		{
			this.value = value.longValue();
		} else {
			this.value = value.subtract(BI_MID_VALUE).longValue() | L_HI_BIT;	
		}	
	}

	public UnsignedLong(int value) {
		if (value < 0)
			throw new IllegalArgumentException("Value underflow");
		this.value = value;
	}

	/**
	 * Construct UnsignedLong from long. If long is negative, its upper bit is 
	 * intrepreted as 0x8000000000000000.
	 * @param value
	 */
	public UnsignedLong(long value) {
		if (value < 0)
			throw new IllegalArgumentException("Value underflow");
		this.value = value;
	}

	public UnsignedLong(String value) {
		BigInteger bi = new BigInteger(value);
		if (bi.compareTo(BI_MIN_VALUE)<0) throw new IllegalArgumentException("Value underflow");
		if (bi.compareTo(BI_MAX_VALUE)>0) throw new IllegalArgumentException("Value overflow");
		
		if (bi.compareTo(BI_L_MAX_VALUE)<0) 
		{
			this.value = bi.longValue();
		} else {
			this.value = bi.subtract(BI_MID_VALUE).longValue() | L_HI_BIT;	
		}		
	}

	public BigInteger bigIntegerValue() {
		// negative value
		if ((value & L_HI_BIT) == L_HI_BIT)		
			return BigInteger.valueOf(value & L_MAX_VALUE).add(BI_MID_VALUE);		
		return BigInteger.valueOf(value);
	}

	@Override
	public double doubleValue() {
		if ((value & L_HI_BIT) == L_HI_BIT)		
			return (double)(value & L_MAX_VALUE) + D_MID_VALUE;
		return (double)value;
	}

	@Override
	public float floatValue() {
		if ((value & L_HI_BIT) == L_HI_BIT)		
			return (float)(value & L_MAX_VALUE) + F_MID_VALUE;
		return (float)value;
	}

	@Override
	public int intValue() {
		if ((value & L_HI_BIT) == L_HI_BIT)		
			return (int)(value & Integer.MAX_VALUE) | Integer.MIN_VALUE;
		return (int) value;
	}

	@Override
	public long longValue() {
		return value;
	}

	public int compareTo(Number o) {
		//if just one has high bit
		if (((value & L_HI_BIT) == L_HI_BIT) ^ ((o.longValue() & L_HI_BIT) == L_HI_BIT)) {
			if ((value & L_HI_BIT) == L_HI_BIT) { return 1; } else { return -1; }
		} else {
			//else, a comparison needs to be done
			long x = longValue();
			long y = o.longValue();
		    return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj.getClass().equals(UnsignedLong.class))) return false;
		UnsignedLong other = (UnsignedLong) obj;
		return value == other.value;	
	}
	
	@Override
	public int hashCode() {
	    return (int)value | (int)(value>>32);
	}	
    
    @Override
    public String toString() {
		if ((value & L_HI_BIT) == L_HI_BIT)
			return bigIntegerValue().toString();
		return Long.toString(value);
    }
    
    public long toLongBits() {
    	return value;
    }

    /**
	 * Parse an UnsignedLong value from a string
	 * @param s the string to parse, assumed to contain a positive Long value
	 * @return the respective UnsignedInteger
	 */
	public static UnsignedLong parseUnsignedLong(String s) {
		try {
			return valueOf(Long.parseLong(s));
		} catch (NumberFormatException e) {
			return new UnsignedLong(s);
		}
	}
    
	/**
	 * Increase the value by one. Note that this object is not changed, but a new one is created.
	 * @return a new UnsignedLong, increased by 1 from this one.
	 */
	public UnsignedLong inc() {
		return valueOf(getValue()+1);
	}
	
	/**
	 * Decrease the value by one. Note that this object is not changed, but a new one is created.
	 * @return a new UnsignedLong, decreased by 1 from this one.
	 * @throws IllegalArgumentException if the value was 0 before the call
	 */
	public UnsignedLong dec() {
		return valueOf(getValue()-1);
	}
	
	/**
	 * Add a value. Note that this object is not changed, but a new one is created.
	 * @param increment the value to add to the current value
	 * @return a new UnsignedLong, increased by increment from this one.
	 */
	public UnsignedLong add(long increment) {
		long v = getValue()+increment;
		if (increment > 0 && (getValue() < 0 || v < getValue()))
			return new UnsignedLong(bigIntegerValue().add(BigInteger.valueOf(increment)));
		return valueOf(v);
	}
	
	/**
	 * Add a value. Note that this object is not changed, but a new one is created.
	 * @param increment the value to add to the current value
	 * @return a new UnsignedLong, increased by increment from this one.
	 */
	public UnsignedLong add(UnsignedLong increment) {
		long v = getValue()+increment.getValue();
		if (increment.getValue() > 0 && v < getValue())
			return new UnsignedLong(BigInteger.valueOf(getValue()).add(BigInteger.valueOf(increment.getValue())));
		return valueOf(v);
	}
	
	/**
	 * Subtract a value from this value. Note that this object is not changed, but a new one is created.
	 * @param decrement the value to subtract from the current value
	 * @return a new UnsignedLong, decreased by decrement from this one.
	 * @throws IllegalArgumentException if the decrement is bigger than the current value
	 */
	public UnsignedLong subtract(long decrement) {
		if (getValue() >= 0 && getValue() > decrement)
			return valueOf(getValue()-decrement);
		BigInteger bi = bigIntegerValue();
		bi = bi.subtract(BigInteger.valueOf(decrement));
		return new UnsignedLong(bi);
	}
	/**
	 * Subtract a value from this value. Note that this object is not changed, but a new one is created.
	 * @param decrement the value to subtract from the current value
	 * @return a new UnsignedLong, decreased by decrement from this one.
	 * @throws IllegalArgumentException if the decrement is bigger than the current value
	 */
	public UnsignedLong subtract(UnsignedLong decrement) {
		if (getValue() < 0 || decrement.getValue() < 0)
			return new UnsignedLong(bigIntegerValue().subtract(decrement.bigIntegerValue()));
		return valueOf(getValue()-decrement.getValue());
	}

	private long getValue() {
		return value;
	}
	}
