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
package org.opcfoundation.ua.common;

import java.util.Arrays;

import org.opcfoundation.ua.utils.BijectionMap;

public class UriTable {

	BijectionMap<Integer, String> indexUriMap = new BijectionMap<Integer, String>();

	public UriTable() {
		super();
	}

	public synchronized String[] toArray() {
		int len = 0;
		for (Integer i : indexUriMap.getLeftSet())
			if (i > len)
				len = i;
		len++;
		String result[] = new String[len];
		for (int i = 0; i < len; i++)
			result[i] = indexUriMap.getRight(i);
		return result;
	}

	/**
	 * Finds the URI with index in the table
	 * 
	 * @param index
	 *            the index you are looking for
	 * @return the URI with the index or null, if there is no such
	 *         index
	 */
	public String getUri(int index) {
		return indexUriMap.getRight(index);
	}

	/**
	 * Finds the index of the namespace URI in the table
	 * 
	 * @param namespaceUri
	 *            the URI of the namespace you are looking for
	 * @return the index of the URI or -1, if it is not in the table
	 */
	public int getIndex(String namespaceUri) {
		Integer i = indexUriMap.getLeft(namespaceUri);
		if (i == null)
			return -1;
		return i;
	}

	/**
	 * Add a new uri to the table. The uri will be added with a new index, unless it is in the table already, in which case the index is returned.
	 * 
	 * @param uri
	 *            The URI.
	 */
	public int add(String uri) {
		return add(-1, uri);
	}

	private int nextIndex() {
		int result = -1;
		for (int i : indexUriMap.getLeftSet())
			if (i > result)
				result = i;
		return result + 1;
	}

	/**
	 * Remove the entry for the specified index
	 * 
	 * @param index
	 */
	public void remove(int index) {
		indexUriMap.removeWithLeft(index);
	}

	/**
	 * Remove the entry for the specified uri
	 * 
	 * @param uri
	 */
	public void remove(String uri) {
		indexUriMap.removeWithRight(uri);
	}

	/**
	 * Add a new uri to the table.
	 * 
	 * @param index
	 *            The new index (use -1 to automatically use the next unused
	 *            index)
	 * @param uri
	 *            The URI.
	 * @throws IllegalArgumentException
	 *             if the index is already in use
	 */
	public synchronized int add(int index, String uri) {
		// check if namespaceIndex already exists
		int i = getIndex(uri);
		if (i >= 0)
			return i;
		if (index < 0)
			index = nextIndex();
		else if (getUri(index) != null)
			throw new IllegalArgumentException(
					"namespaceTable already has namespaceIndex " + index);
		// in other case we are able to add new namespaceIndex with value
		indexUriMap.map(index, uri);
		return index;
	}

	/**
	 * @param namespaceArray
	 * @param result
	 */
	public void addAll(String[] namespaceArray) {
		for (int i = 0; i < namespaceArray.length; i++)
			add(i, namespaceArray[i]);
	}

	public String toString() {
		return Arrays.toString(toArray());
	}

	public int size() {
		return indexUriMap.size();
	}

}