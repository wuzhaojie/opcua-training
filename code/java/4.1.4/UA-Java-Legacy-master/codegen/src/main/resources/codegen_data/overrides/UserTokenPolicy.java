/* ========================================================================
 * Copyright (c) 2005-2015 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================*/

package _PackageName_;

import org.opcfoundation.ua.builtintypes.Structure;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.ObjectUtils;
_imports_

@Description("_description_")
public class _ClassName_ extends _SuperType_ implements Structure, Cloneable {
	
	public static final UserTokenPolicy ANONYMOUS = new UserTokenPolicy("anonymous", UserTokenType.Anonymous, null, null, null);
	public static final UserTokenPolicy SECURE_USERNAME_PASSWORD = new UserTokenPolicy("username_basic128", UserTokenType.UserName, null, null, SecurityPolicy.BASIC128RSA15.getPolicyUri());
	public static final UserTokenPolicy SECURE_USERNAME_PASSWORD_BASIC256 = new UserTokenPolicy("username_basic256", UserTokenType.UserName, null, null, SecurityPolicy.BASIC256.getPolicyUri());
	public static final UserTokenPolicy SECURE_CERTIFICATE = new UserTokenPolicy("certificate_basic128", UserTokenType.Certificate, null, null, SecurityPolicy.BASIC128RSA15.getPolicyUri());
	public static final UserTokenPolicy SECURE_CERTIFICATE_BASIC256 = new UserTokenPolicy("certificate_basic256", UserTokenType.Certificate, null, null, SecurityPolicy.BASIC256.getPolicyUri());

	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers._ClassName_);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers._ClassName__Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers._ClassName__Encoding_DefaultXml);
	
_Content_

	public ExpandedNodeId getTypeId() {
		return ID;
	}

	public ExpandedNodeId getXmlEncodeId() {
		return XML;
	}

	public ExpandedNodeId getBinaryEncodeId() {
		return BINARY;
	}
	
	public String toString() {
		return "_ClassName_: "+ObjectUtils.printFieldsDeep(this);
	}

}
