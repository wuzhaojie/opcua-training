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
import org.opcfoundation.ua.utils.ObjectUtils;



public class DataValue implements Cloneable {

	public static final NodeId ID = Identifiers.DataValue;
	
	Variant value;
    StatusCode statusCode;
    DateTime sourceTimestamp;
    UnsignedShort sourcePicoseconds;
    DateTime serverTimestamp;
    UnsignedShort serverPicoseconds;

	public DataValue() {
      this(StatusCode.GOOD);
    }
    
    public DataValue(Variant value, StatusCode statusCode, DateTime sourceTimestamp, UnsignedShort sourcePicoseconds, DateTime serverTimestamp, UnsignedShort serverPicoseconds) {
        super();
        this.statusCode = statusCode;
        this.sourceTimestamp = sourceTimestamp;
        this.serverTimestamp = serverTimestamp;
		this.sourcePicoseconds = sourcePicoseconds == null ? UnsignedShort.ZERO
				: sourcePicoseconds;
		this.serverPicoseconds = serverPicoseconds == null ? UnsignedShort.ZERO
				: serverPicoseconds;
        setValue(value);
    }

    public DataValue(Variant value, StatusCode statusCode, DateTime sourceTimestamp, DateTime serverTimestamp) {
		this(value, statusCode, sourceTimestamp, null, serverTimestamp, null);
    }

    public DataValue(StatusCode statusCode) {
    	this(Variant.NULL, statusCode); 
    }

    public DataValue(Variant value, StatusCode statusCode) {
		this(value, statusCode, null, null, null, null);
	}

	public DataValue(Variant variant) {
		this(variant, StatusCode.GOOD);
	}

	public DateTime getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(DateTime serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public DateTime getSourceTimestamp() {
        return sourceTimestamp;
    }

    public void setSourceTimestamp(DateTime sourceTimestamp) {
        this.sourceTimestamp = sourceTimestamp;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

	public void setStatusCode(UnsignedInteger value) {
		setStatusCode(new StatusCode(value));
	}

	public Variant getValue() {
        return value;
    }
	
	/**
	 * Check if Value is null.
	 * 
	 * @return true if Value is null or the Variant returned by it contains a null.
	 */
	public boolean isNull() {
		return value.getValue() == null;
	}

    public void setValue(Variant value) {
    	if (value == null)
			this.value = Variant.NULL;
		else
			this.value = value;
    }
    
    @Override
    public int hashCode() {
    	return 
		ObjectUtils.hashCode(value) |
		ObjectUtils.hashCode(statusCode) |
		ObjectUtils.hashCode(sourceTimestamp) |
		ObjectUtils.hashCode(sourcePicoseconds) |
		ObjectUtils.hashCode(serverTimestamp) |
		ObjectUtils.hashCode(serverPicoseconds);
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof DataValue)) return false;
    	DataValue o = (DataValue) obj;
    	return 
			ObjectUtils.objectEquals(o.value, value) &&
			ObjectUtils.objectEquals(o.statusCode, statusCode) &&
			ObjectUtils.objectEquals(o.sourceTimestamp, sourceTimestamp) &&
			ObjectUtils.objectEquals(o.serverTimestamp, serverTimestamp) &&
			ObjectUtils.objectEquals(o.sourcePicoseconds, sourcePicoseconds) &&
			ObjectUtils.objectEquals(o.serverPicoseconds, serverPicoseconds);		
    }
    
    public UnsignedShort getSourcePicoseconds() {
		return sourcePicoseconds;
	}

	public void setSourcePicoseconds(UnsignedShort sourcePicoseconds) {
		this.sourcePicoseconds = sourcePicoseconds;
	}

	public UnsignedShort getServerPicoseconds() {
		return serverPicoseconds;
	}

	public void setServerPicoseconds(UnsignedShort serverPicoseconds) {
		this.serverPicoseconds = serverPicoseconds;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DataValue(");
		sb.append("value="+value);
		sb.append(", statusCode="+statusCode);
		sb.append(", sourceTimestamp="+sourceTimestamp);
		sb.append(", sourcePicoseconds="+sourcePicoseconds);
		sb.append(", serverTimestamp="+serverTimestamp);
		sb.append(", serverPicoseconds="+serverPicoseconds);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Object clone() {
		return new DataValue(getValue(),
				getStatusCode(), getSourceTimestamp(),
				getServerPicoseconds(),
				getServerTimestamp(),
				getServerPicoseconds());	}
	   
}
