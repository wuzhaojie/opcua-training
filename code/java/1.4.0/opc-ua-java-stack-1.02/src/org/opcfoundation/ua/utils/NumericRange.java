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

package org.opcfoundation.ua.utils;

import java.lang.reflect.Array;
import java.util.Collection;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;

/**
 * A helper class for defining index ranges according to the OPC UA
 * specification. The index ranges are transferred as string values, but are
 * easier to use with the NumericRange.
 * 
 */
public class NumericRange {

	private int[] begin;
	private int[] end;

	// constructors
	public NumericRange() {
		//initialize with default values
		init(1);
	}

	private void init(int[]... indexes) {
		int dimensions = indexes.length;
		init(dimensions);
		for(int i=0;i<dimensions;i++) {
			if (indexes[i].length > 0)
				begin[i] = indexes[i][0];
			if (indexes[i].length > 1)
				end[i] = indexes[i][1];
		}
	}

	/**
	 * Initializes the object with a begin index.
	 * @param begin
	 */
	public NumericRange(int begin) {
		init(new int[]{begin, -1});
	}
	/**
	 * Initializes the object with a begin and end index.
	 */
	public NumericRange(int begin, int end) {
		init(new int[]{begin, end});

	}

	/**
	 * Initializes the range with the default indexes. The dimensions will be
	 * initialized to the length of indexes.
	 * 
	 * @param indexes
	 *            the indexes to use for initializing the range. Each element is
	 *            expected to define an array of [begin, end] indexes or just
	 *            [begin].
	 */
	public NumericRange(int[]... indexes) {
		init(indexes);
	}

	/**
	 * Ensures the bounds are valid values for the object passed in.<p>
	 * Returns false if the object is not indexable or if the numeric range is out of bounds.
	 */
	public boolean ensureValid(Object value) {
		int count = -1;
		
		// Check for collections and lists.
		try {
			Collection<?> collection = (Collection<?>) value;
			count = collection.size();
		} catch (Exception e) {
			try 
			{
				count = Array.getLength(value);
			}
			catch (IllegalArgumentException e2)
			{
				
			}
		}
		
		// Ensure bounds are less than count.
		return ensureValid(count);
	}
	
	/**
	 * Tests the bounds are valid values for a collection with the specified length.<p>
	 * Returns false if the numeric range is out of bounds.
	 * 
	 * @param count
	 * @return true if valid  
	 */
	public boolean ensureValid(int count) {
		
		// Object not indexable
		if (count == -1) {
			return false;
		}
		
		// Check bounds.
		if (begin[0] > count || end[0] >= count) {
			return false;
		}
		
		// Set begin.
		if (begin[0] < 0) {
			begin[0] = 0;
		}
		
		// Set end 
		if (end[0] < 0) {
			end[0] = count;
		}
		
		return true;
	}
	
	/**
	 * @return the begin index of the range for the first dimension.
	 */
	public int getBegin() {
		return getBegin(0);
	}


	/**
	 * @return the end index of the range for the first dimension.
	 */
	public int getEnd() {
		return getEnd(0);
	}

	/**
	 * Get the beginning of the range for the specified dimension.
	 * @param dim the dimension, minimum 0
	 * @return the begin index of the range for the specified dimension.
	 */
	public int getBegin(int dim) {
		if (dim < 0)
			throw new IllegalArgumentException("Dim");
		else if (dim >= getDimensions())
			return -1;
		return begin[dim];
	}


	/**
	 * Get the end of the range for the specified dimension.
	 * @param dim the dimension, minimum 0
	 * @return the end index of the range for the specified dimension.
	 */
	public int getEnd(int dim) {
		if (dim < 0) 
			throw new IllegalArgumentException("Dim");
		else if (dim >= getDimensions())
			return -1;
		return end[dim] < 0 ? begin[dim] : end[dim];
	}

	/**
	 * Define the begin index of the range for the first dimension.
	 * @param value the begin index
	 */
	public void setBegin(int value) {
		setBegin(1, value);
	}

	/**
	 * Define the end index of the range for the first dimension.
	 * @param value the end index
	 */
	public void setEnd(int value) {
		setEnd(1, value);
	}

	/**
	 * Define the begin index of the range for the specified dimension. Note
	 * that {@link #setDimensions(int)} must be called first to define the
	 * number of dimensions in the range.
	 * 
	 * @param dim
	 *            the dimension, minimum 0
	 * @param value
	 *            the begin index
	 */
	public void setBegin(int dim, int value) {
		if (value < -1) {
			throw new IllegalArgumentException("Begin");
		}
		if (dim < 0 || dim >= getDimensions()) {
			throw new IllegalArgumentException("Dim");
		}

		int end = this.end[dim];
		if (end != -1 && (value > end || value < 0)) {
			throw new IllegalArgumentException("Begin < End");
		}

		this.begin[dim] = value;
	}


	/**
	 * Define the end index of the range for the specified dimension. Note that
	 * {@link #setDimensions(int)} must be called first to define the number of
	 * dimensions in the range.
	 * 
	 * @param dim
	 *            the dimension, minimum 0
	 * @param value
	 *            the end index
	 */
	public void setEnd(int dim, int value) {
		if (value < -1) {
			throw new IllegalArgumentException("End");
		}
		if (dim < 0 || dim >= getDimensions()) {
			throw new IllegalArgumentException("Dim");
		}

		int begin = getBegin(dim);
		if (value != -1 && (begin > value || begin < 0)) {
			throw new IllegalArgumentException("Begin > End");
		}

		this.end[dim] = value;
	}


	public static NumericRange getEmpty() {
		return new NumericRange();
	}

	/**
	 * Parses a string representing a numeric range.
	 * 
	 * @param textToParse
	 * @return numeric range
	 * @throws ServiceResultException in case the range is not in proper format
	 */
	public static NumericRange parse(String textToParse) 
	throws ServiceResultException {
		if (textToParse == null || textToParse.length() == 0) {
			return NumericRange.getEmpty();
		}

		NumericRange range = new NumericRange();
		
		String[] dims = textToParse.split(",");

		range.setDimensions(dims.length);
		for (int d = 0; d < dims.length; d++)
			try {
				String dimStr = dims[d];
				String[] indexes = dimStr.split(":");

				if (indexes.length > 1) {
					range.setBegin(d,
							Integer.parseInt(indexes[0]));
					range.setEnd(d,
							Integer.parseInt(indexes[1]));


					if (range.getBegin(d) == range.getEnd(d))
						throw new IllegalArgumentException("Begin = End");

				} else {
					range.setBegin(d, Integer.parseInt(indexes[0]));
				}

			} catch (Exception e) {
				throw new ServiceResultException(
						StatusCodes.Bad_IndexRangeInvalid, e,
						"Cannot parse numeric range: " + textToParse + ".");
			}

		return range;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getDimensions(); i++) {
			if (i > 0)
				sb.append(",");
			if (getBegin(i) >= 0) {
				if (getEnd(i) <= getBegin(i))
					sb.append(String.valueOf(getBegin(i)));
				else
					sb.append(String.format("%d:%d", getBegin(i), getEnd(i)));
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof NumericRange))
			return false;
		else {
			NumericRange other = (NumericRange)obj;
			if (other.getDimensions() != getDimensions())
				return false;
			for (int d = 0; d < getDimensions(); d++) {
				if (other.getBegin(d) != getBegin(d))
					return false;
				if (other.getEnd(d) != getEnd(d))
					return false;
			}
		}
		return true;
	}

	/**
	 * Define the number of dimensions for the NumericRange. By default the
	 * range is 1-dimensional, but you can define a multidimensional range as
	 * well.
	 * 
	 * @param dimensions
	 *            the number of dimensions for the range; must be greater than 0
	 */
	public void setDimensions(int dimensions) {
		if (dimensions < 1)
			throw new IllegalArgumentException("Dimensions must be greater than 0. Was:" + dimensions);
		if (dimensions != getDimensions()) {
		   init(dimensions);
		}
		
	}

	/**
	 * @param dimensions
	 */
	private void init(int dimensions) {
		begin = new int[dimensions];
		end = new int[dimensions];
		for(int i=0;i<dimensions;i++) {
			begin[i] = -1;
			end[i] = -1;
		}
	}

	/**
	 * The number of dimensions in the range.
	 * @return the dimensions
	 */
	public int getDimensions() {
		return begin.length;
	}

	/**
	 * Checks if the defined range is empty.
	 * @return true, if no indexes have been defined and Dimensions==1.
	 */
	public boolean isEmpty() {
		return getDimensions() == 1 && getBegin() == -1;
	}
	
	/**
	 * Checks if the defined range for the specified dimension is empty.
	 * @param dim the dimension to check
	 * @return true, if no range is defined for the dimension.
	 */
	public boolean isEmpty(int dim) {
		return getBegin(dim) == -1;
	}

	/**
	 * Check if the range is an empty range. Adds a null check, in addition to the standard {@link #isEmpty()} check. 
	 * @param indexRange the range to check.
	 * @return true if indexRange is null or it is an empty range definition
	 * @see #isEmpty()
	 */
	public static boolean isNullOrEmpty(NumericRange indexRange) {
		return indexRange == null || indexRange.isEmpty();
	}

	public static String toString(NumericRange indexRange) {
		return indexRange == null ? null : indexRange.toString();
	}
}
