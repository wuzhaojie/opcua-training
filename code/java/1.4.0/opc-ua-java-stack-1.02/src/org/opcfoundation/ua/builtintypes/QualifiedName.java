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

import org.opcfoundation.ua.core.Identifiers;


/**
 * A name qualified by a namespace.
 *
 */
public final class QualifiedName {
	
	public static final NodeId ID = Identifiers.QualifiedName;
	public static final QualifiedName NULL = new QualifiedName(UnsignedShort.valueOf(0), null); 
	public static final QualifiedName DEFAULT_BINARY_ENCODING = new QualifiedName("Default Binary"); 
	public static final QualifiedName DEFAULT_XML_ENCODING = new QualifiedName("Default XML");
	private int namespaceIndex;
	private String name;

	/**
	 * Initializes the object with default values.
	 * @param namespaceIndex
	 * @param name  
	 */
	public QualifiedName(UnsignedShort namespaceIndex, String name)
	{		
//		if (name == null)  //changed 21.4. TODO
//			throw new IllegalArgumentException("name argument must not be null");
		// Part 3: 8.3, table 20
		// doesn't apply according to Randy, application layer must enforce this
//		if (name!=null){
//			if (name.length()>512)
//				throw new IllegalArgumentException("The length of name is restricted to 512 characters");
//		}
			
		this.namespaceIndex = namespaceIndex.intValue();
		this.name           = name;
	}

	/**
	 * Initializes the object with default values.
	 * Convenience method. 
	 * 
	 * @param namespaceIndex int bits of an unsigned short
	 * @param name 
	 */
	public QualifiedName(int namespaceIndex, String name)
	{
//		if (name == null)
//			throw new IllegalArgumentException("name argument must not be null");
		if (namespaceIndex<UnsignedShort.MIN_VALUE.intValue() || namespaceIndex>UnsignedShort.MAX_VALUE.intValue())
			throw new IllegalArgumentException("namespace index out of bounds");
		// Part 3: 8.3, table 20
		// doesn't apply according to Randy, application layer must enforce this
//		if (name.length()>512)
//			throw new IllegalArgumentException("The length of name is restricted to 512 characters");
		
		this.namespaceIndex = namespaceIndex;
		this.name           = name;
	}	
		
	//TODO Added by Mikko 3.11.2008, this was needed in Session
	/** 
	 * Initializes the object with a name.
	 * In this convenience method the namespaceIndex is 0.
	 *
	 * @param name name
	 * 
	 */
	public QualifiedName(String name)
	{
		if (name == null)
			throw new IllegalArgumentException("name argument must not be null");
		// Part 3: 8.3, table 20
		// doesn't apply according to Randy, application layer must enforce this
//		if (name.length()>512)
//			throw new IllegalArgumentException("The length of name is restricted to 512 characters");
		namespaceIndex = 0;
		this.name           = name;
	}

	public String toString() {
		if (namespaceIndex > 0) 
			return namespaceIndex+":"+name;	
		return name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the namespaceIndex
	 */
	public int getNamespaceIndex() {
		return namespaceIndex;
	}
	
	/**
	 * Returns true if the value is null.
	 * @param value
	 * @return true if value is null or equal to NULL
	 */
	public static boolean isNull(QualifiedName value) {
		return value==null || value.equals(NULL);
	}
	
	/**
	 * Return true if the value is null, or name part is empty string
	 * @param value
	 * @return true if isNull(value) is true or the name part is empty string
	 */
	public static boolean isNullOrEmpty(QualifiedName value){
		if(isNull(value)){
			return true;
		} else if("".equals(value.name)){
			return true;
		} else {
			return false;
		}		
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + namespaceIndex;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return isNull(this);
		if (getClass() != obj.getClass())
			return false;
		QualifiedName other = (QualifiedName) obj;
		if (namespaceIndex != other.namespaceIndex)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	/**
	 * Parse the QualifiedName from a string.
	 * <p>
	 * The string is supposed to be in format "[NameSpaceIndex]:[Name]". or just "[Name]"
	 * @param value the string
	 * @return the new QualifiedName
	 */
	public static QualifiedName parseQualifiedName(String value) {
		String[] parts = value.split(":");
		UnsignedShort namespaceIndex = UnsignedShort.ZERO;
		String name = value;
		if (parts.length > 1)
			try {
				namespaceIndex = UnsignedShort.parseUnsignedShort(parts[0]);
				name = value.substring(parts[0].length()+1);
			} catch (NumberFormatException e) {
			} catch (IllegalArgumentException e) {
			}
		return new QualifiedName(namespaceIndex, name);
	}

}
