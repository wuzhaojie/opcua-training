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

import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;

/**
 * Variant wraps an arbitrary builtin variable, an array of builtin variables or
 * a multi-dimension array of builtin variable. Variant is equals-comparable.
 * <p>
 * An example: Variant v = new Variant( new UnsignedInteger[4][5][6] );
 * <p> 
 * The value may be builtin primitive or a {@link Structure}. 
 *  e.g. new Variant( new NotificationData() );
 * 
 * Encoders write a structure as an {@link ExtensionObject}. 
 * 
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public class Variant {
	private static Logger logger = LoggerFactory.getLogger(Variant.class);

	public static final Variant NULL = new Variant(null);
	Object value;
	Class<?> compositeClass;

	/**
	 * Create variant.
	 * 
	 * @param value
	 *            scalar, array or multi-dimension array
	 */
	public Variant(Object value) {
		if (value instanceof Enumeration)
			value = ((Enumeration) value).getValue();
		this.value = value;
		if (value != null) {
			compositeClass = value.getClass();
			
			if(compositeClass == Variant.class) {
				throw new IllegalArgumentException("Variant cannot be "
						+ compositeClass.getCanonicalName());
			}
			 
			while (compositeClass.isArray()
					&& !compositeClass.equals(byte[].class))
				compositeClass = compositeClass.getComponentType();
			
			assertValidClass(compositeClass);
		}
	}

	void assertValidClass(Class<?> clazz) {
		if (clazz.equals(byte[].class))
			return;
		if (clazz.equals(Boolean.class))
			return;
		if (clazz.equals(Byte.class))
			return;
		if (clazz.equals(UnsignedByte.class) && !clazz.isArray())
			return;
		if (clazz.equals(Short.class))
			return;
		if (clazz.equals(UnsignedShort.class))
			return;
		if (clazz.equals(Integer.class))
			return;
		if (clazz.equals(UnsignedInteger.class))
			return;
		if (clazz.equals(Long.class))
			return;
		if (clazz.equals(UnsignedLong.class))
			return;
		if (clazz.equals(Float.class))
			return;
		if (clazz.equals(Double.class))
			return;
		if (clazz.equals(String.class))
			return;
		if (clazz.equals(DateTime.class))
			return;
		if (clazz.equals(UUID.class))
			return;
		if (clazz.equals(XmlElement.class))
			return;
		if (clazz.equals(NodeId.class))
			return;
		if (clazz.equals(ExpandedNodeId.class))
			return;
		if (clazz.equals(StatusCode.class))
			return;
		if (clazz.equals(QualifiedName.class))
			return;
		if (clazz.equals(LocalizedText.class))
			return;
		if (clazz.equals(ExtensionObject.class))
			return;
		if (clazz.equals(DataValue.class))
			return;
		if (clazz.equals(DiagnosticInfo.class))
			return;
		if (clazz.equals(Variant.class))
			return;
		if (Structure.class.isAssignableFrom(clazz))
			return;
		if (Enumeration.class.isAssignableFrom(clazz))
			return;
		throw new IllegalArgumentException("Variant cannot be "
				+ clazz.getCanonicalName());
	}

	public boolean isEmpty() {
		return value == null;
	}

	public boolean isArray() {
		if (value == null)
			return false;
		Class<?> c = value.getClass();
		return c.isArray() && !c.equals(byte[].class);
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Convert the value to string, including the compositeClass.
	 * @return
	 */
	public String toStringWithType() {
		return toString(true);
	}
	
	/**
	 * Convert the value to string, optionally including the compositeClass.
	 * 
	 * @param includeCompositeClass whether to also add the name of the compositeClass
	 * @return the value as string
	 */
	public String toString(boolean includeCompositeClass) {
		if (value == null)
			return "(null)";
		String classStr = "";
		String valueStr;
		if (isArray()) {
			if (includeCompositeClass) {
				int[] d = getArrayDimensions();
				StringBuilder sb = new StringBuilder();
				sb.append(d[0]);
				for (int i = 1; i < getDimension(); i++)
					sb.append(",").append(d[i]);
				classStr = String.format("(%s[%s]) ", compositeClassToString(),
						sb.toString());
			}
			valueStr = MultiDimensionArrayUtils.toString(value);
		} else {
			if (includeCompositeClass)
				classStr = String.format("(%s) ", compositeClassToString());
			valueStr = MultiDimensionArrayUtils.toString(value); // works for scalars too (byte[] as ByteString is "scalar")
		}
		return classStr + valueStr;
	}

	/**
	 * @return
	 */
	protected String compositeClassToString() {
		String className = compositeClass.getSimpleName();
		return className;
	}
	/**
	 * The class type of the variant value. If the value is an array, the type
	 * may be an ExtensionObject, in which case the array elements must be
	 * decoded to find out the actual type of each.
	 * 
	 * @return the class of Value
	 */
	public Class<?> getCompositeClass() {
		return compositeClass;
	}

	public int[] getArrayDimensions() {
		int dim = getDimension();
		int result[] = new int[dim];
		if (dim == 0)
			return result;

		Object o = value;
		for (int i = 0; i < dim; i++) {
			Object[] array = (Object[]) o;
			result[i] = array.length;
			if (array.length == 0)
				break;
			o = array[0];
		}

		return result;
	}

	public int getDimension() {
		int dim = MultiDimensionArrayUtils.getDimension(value);
		if (compositeClass.isArray())
			dim--;
		return dim;
	}

	@Override
	public int hashCode() {
		if (value == null)
			return 0;
		if (!isArray())
			return value.hashCode();
		if (value instanceof byte[])
			return Arrays.hashCode((byte[]) value);
		return Arrays.deepHashCode((Object[]) value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Variant))
			return false;
		Variant o = (Variant) obj;
		if (value == null && o.value == null)
			return true;
		if (value == null && o.value != null)
			return false;
		if (value != null && o.value == null)
			return false;

		Class<?> c = value.getClass();
		if (!c.equals(o.value.getClass()))
			return false;

		if (c == byte[].class)
			return Arrays.equals((byte[]) o.value, (byte[]) value);

		if (!isArray())
			return value.equals(o.value);
		return Arrays.deepEquals((Object[]) value, (Object[]) o.value);
	}

	/**
	 * Convert the variant value to any class. If it cannot be converted returns defaultValue. 
	 * @param clazz The class type to convert to.
	 * @param defaultValue A default value to return, if the conversion cannot be done
	 * @return Value as the requested class or defaultValue, if it cannot be converted.
	 */
	public <T> T asClass(Class<T> clazz, T defaultValue) {
		if (value == null)
			return defaultValue;
		try {
			return clazz.cast(value);
		} catch (ClassCastException e) {
			return defaultValue;
		}
	}

	/**
	 * Returns the value of the specified Variant as a <code>boolean</code>
	 * 
	 * @return the Variant value as boolean, if it can be cast to such
	 * @throws ClassCastException if the value cannot be cast to boolean
	 */
	public boolean booleanValue() {
		if (value instanceof Boolean)
			return (Boolean) value;
		if (isNumber())
			return longValue() != 0;
		if(isEmpty())
			throw new ClassCastException("Variant null cannot be cast to boolean");
		if (getCompositeClass().equals(String.class)) {
			// The following values are defined for ContentFilter conversions
			// Spec. Part 4, v1.01 p.113
			String s = ((String) getValue()).toLowerCase();
			if (s.equals("true") || s.equals("1"))
				return true;
			if (s.equals("false") || s.equals("0"))
				return false;
			throw new ClassCastException("Variant String cannot be cast to boolean: "+ s);
		}
		return asClass(Boolean.class, false);
	}

	/**
	 * Returns the value of the specified Variant as a <code>Number</code>
	 * 
	 * @return the Variant value as Number, if it can be cast to such
	 * @throws ClassCastException if the value cannot be cast to Number
	 */
	public Number toNumber() {
		if (value instanceof Boolean)
			return booleanValue() ? 1 : 0;
		if (isNumber())
			return (Number) value;
		throw new ClassCastException("Variant is not a Number; CompositeClass="
				+ compositeClass);
	}


	/**
	 * @return true if the current value is an instance of Number or Boolean (which can be used as Integer)
	 */
	public boolean isNumber() {
		return value instanceof Number || value instanceof Boolean;
	}

	/**
	 * @return
	 */
	public boolean isComparable() {
		return value instanceof Comparable<?>;
	}
	
    /**
     * Returns the value of the specified Variant as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
	 * @throws ClassCastException if the value cannot be cast to Number
     */
    public int intValue() {
		return toNumber().intValue();
	}

    /**
     * Returns the value of the specified Variant as a <code>long</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
	 * @throws ClassCastException if the value cannot be cast to Number
     */
    public long longValue() {
		return toNumber().longValue();
	}

    /**
     * Returns the value of the specified Variant as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
	 * @throws ClassCastException if the value cannot be cast to Number
     */
    public float floatValue() {
		return toNumber().floatValue();
	}

    /**
     * Returns the value of the specified Variant as a <code>double</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
 	 * @throws ClassCastException if the value cannot be cast to Number
    */
    public double doubleValue() {
		return toNumber().doubleValue();
	}

    /**
     * Returns the value of the specified Variant as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
 	 * @throws ClassCastException if the value cannot be cast to Number
    */
	public byte byteValue() {
		return toNumber().byteValue();
	}

    /**
     * Returns the value of the specified Variant as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
 	 * @throws ClassCastException if the value cannot be cast to Number
     */
	public short shortValue() {
		return toNumber().shortValue();
	}

	/**
	 * Compares the value of a variant with another variant. If both variants
	 * {@link #isNumber()}, compares the {@link #floatValue()} of each.
	 * <p>
	 * Otherwise, it will check if {@link #isComparable()}, in which case it
	 * will compare the values as {@link Comparable}s. If the values are of
	 * different class, it will try to cast the values to the same type.
	 * 
	 * @param value2
	 *            the value to compare to
	 * @return true if the values of the variants are equal
	 * @throws ClassCastException
	 *             if the values cannot be compared
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compareTo(Variant value2) {
		if (value2 == null)
			throw new NullPointerException("cannot compare to null");
		if (isEmpty() && value2.isEmpty())
			return 0;
		if (isEmpty())
			return -1;
		if (value2.isEmpty())
			return 1;
		// if both are numbers, compare as float
		if (isNumber() && value2.isNumber()) {
			float diff = floatValue() - value2.floatValue();
			return (int) Math.signum(diff);
		}
		if (isComparable()) {
			Object v2;
			if (getCompositeClass().equals(value2.getCompositeClass()))
				v2 = value2.getValue();
			else
				v2 = value2.asClass(getCompositeClass(), null);
			if (v2 == null)
				throw new ClassCastException(String.format(
						"Cannot compare the variants of types %s vs. %s",
						getCompositeClass(), value2.getCompositeClass()));
			return ((Comparable) getValue()).compareTo(v2);
		}
		if (value2.isComparable()) {
			Object v1;
			if (getCompositeClass().equals(value2.getCompositeClass()))
				v1 = getValue();
			else
				v1 = asClass(value2.getCompositeClass(), null);
			if (v1 == null)
				throw new ClassCastException(String.format(
						"Cannot compare the variants of types %s vs. %s",
						getCompositeClass(), value2.getCompositeClass()));
			return ((Comparable) value2.getValue()).compareTo(v1);
		}
		throw new ClassCastException(String.format(
				"Cannot compare the variants of types %s vs. %s",
				getCompositeClass(), value2.getCompositeClass()));
	}

	/**
	 * Compares the value of a variant with another variant. If the values are
	 * of the same class, {@link #equals(Object)} is called. If both variants
	 * {@link #isNumber()}, compares the {@link #floatValue()} of each.
	 * 
	 * @param value2 the value to compare to
	 * @return true if the values of the variants are equal 
	 */
	public boolean valueEquals(Variant value2) {
		if (value2 == null)
			return false;
		if (isEmpty() || value2.isEmpty())
			return false;
		// #0, if the classes are the same, use equals
		if (getCompositeClass().equals(value2.getCompositeClass()))
			return equals(value2);
		// #1, if both are numbers, compare as float
		if (isNumber() && value2.isNumber())
			return floatValue() == value2.floatValue();
		// #2 try to cast value2 to the class of this
		final Object castValue = value2.asClass(getCompositeClass(), null);
		if (castValue != null)
			return equals(new Variant(castValue));
		// #3 cast this to the class of value2
		return value2.equals(asClass(value2.getCompositeClass(), null));
	}

	
}
