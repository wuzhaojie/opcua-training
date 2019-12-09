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

package org.opcfoundation.ua.transport.tcp.io;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;

public class TcpMessageLimits {

    /** The size of the message type and size prefix in each message. */
    public static final int MessageTypeAndSize = 8;

    /** The minimum send or receive buffer size. */
    public static final int MinBufferSize = 8192;

    /** The maximum send or receive buffer size. */
    public static final int MaxBufferSize = 8192*18;

    /** The maximum length for the reason in an error message. */
    public static final int MaxErrorReasonLength = 4096;
    
    /** The maximum length for the endpoint url in the hello message. */
    public static final int MaxEndpointUrlLength = 4096;

    /** The maximum length for an x509 certificate. */
    public static final int MaxCertificateSize = 7500;

    /** The maximum length for an a security policy uri. */
    public static final int MaxSecurityPolicyUriSize = 256;
            
    /** The length of the base message header. */
    public static final int BaseHeaderSize = 12;
            
    /** The length of the message header use with symmetric cryptography. */
    public static final int SymmetricHeaderSize = 16;
            
    /** The length of the sequence message header. */
    public static final int SequenceHeaderSize = 8;
            
    /** The length a X509 certificate thumbprint. */
    public static final int CertificateThumbprintSize = 20;
            
    /** The number of bytes required to specify the length of an encoding string or bytestring. */
    public static final int StringLengthSize = 4;

    /** Sequence numbers may only rollover if they are larger than this value. */
    public static final UnsignedInteger MinSequenceNumber = UnsignedInteger.valueOf( UnsignedInteger.L_MAX_VALUE - UnsignedShort.L_MAX_VALUE );

    /** The first sequence number after a rollover must be less than this value. */
    public static final UnsignedInteger MaxRolloverSequenceNumber = UnsignedInteger.valueOf( UnsignedShort.L_MAX_VALUE );
    
    /** The default buffer size to use for communication. */
    public static final int DefaultMaxBufferSize = 65535;

    /** The default maximum message size. */
    public static final int DefaultMaxMessageSize = 16*65535;

    /** How long a connection will remain in the server after it goes into a faulted state. */
    public static final int DefaultChannelLifetime = 60000;        

    /** How long a security token lasts before it needs to be renewed. */
    public static final int DefaultSecurityTokenLifeTime = 3600000;

    /** The minimum lifetime for a security token lasts before it needs to be renewed. */
    public static final int MinSecurityTokenLifeTime = 60000;
            
    /** The minimum time interval between reconnect attempts. */
    public static final int MinTimeBetweenReconnects = 0;
    
    /** The maximum time interval between reconnect attempts. */
    public static final int MaxTimeBetweenReconnects = 120000;

    /** The fraction of the lifetime to wait before renewing a token. */
    public static final double TokenRenewalPeriod = 0.75;	
    
}
