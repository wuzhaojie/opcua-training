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

package org.opcfoundation.ua.encoding.binary;

import org.opcfoundation.ua.core.IdType;

/**
 * NodeId binary encoding byte.
 */
public enum NodeIdEncoding {

	  TwoByte((byte) 0x00, IdType.Numeric), 
	  FourByte((byte) 0x01, IdType.Numeric), 
	  Numeric((byte) 0x02, IdType.Numeric), 
	  String((byte) 0x03, IdType.String), 
	  Guid((byte) 0x04, IdType.Guid), 
	  ByteString((byte) 0x05, IdType.String);

	  private final byte bits;
	  private final IdType identifierType;

	  private NodeIdEncoding(byte bits, IdType identifierType) {
	    this.bits = bits;
	    this.identifierType = identifierType;
	  }

	  public byte getBits() {
	    return bits;
	  }
	  
	  public IdType toIdentifierType()
	  {
	      return identifierType; 
	  }

	  public static NodeIdEncoding getNodeIdEncoding(int bits) {
	    if (bits == TwoByte.getBits()) {
	      return TwoByte;
	    } else if (bits == FourByte.getBits()) {
	      return FourByte;
	    } else if (bits == Numeric.getBits()) {
	      return Numeric;
	    } else if (bits==String.getBits()) {
	    	return String;
	    } else if (bits == Guid.getBits()) {
	      return Guid;
	    } else if (bits == ByteString.getBits()) {
	      return ByteString;
	    } else
	      return null;
	  }

}
