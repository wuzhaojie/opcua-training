/* ========================================================================
 * Copyright (c) 2005-2014 The OPC Foundation, Inc. All rights reserved.
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
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.ObjectUtils;
import org.opcfoundation.ua.utils.EndpointUtil;
_imports_

/**
 * Endpoint Description
 * 
 * @See {@link EndpointUtil} for utility methods
 */
@Description("_description_")
public class _ClassName_ extends _SuperType_ implements Structure, Cloneable {

	public static final ExpandedNodeId ID = new ExpandedNodeId(Identifiers._ClassName_);
	public static final ExpandedNodeId BINARY = new ExpandedNodeId(Identifiers._ClassName__Encoding_DefaultBinary);
	public static final ExpandedNodeId XML = new ExpandedNodeId(Identifiers._ClassName__Encoding_DefaultXml);
	
_Content_
	
	/**
	 * Tests whether the stack and the endpoint supports given token type.
	 * This verifies that the stack knows the encryption algorithms of the
	 * token type. 
	 *  
	 * @param endpoint
	 * @param type
	 * @return true, if token type is supported
	 */
	public boolean supportsUserTokenType(EndpointDescription endpoint, UserTokenType type)
	{
		return findUserTokenPolicy(type) != null;
	}

	/**
	 * Finds UserTokenPolicy of given type that this stack can encrypt
	 * 
	 * @param endpoint
	 * @param type
	 * @return user token policy or null 
	 */
	public UserTokenPolicy findUserTokenPolicy(UserTokenType type)
	{
		if (UserIdentityTokens==null) return null;
		for (UserTokenPolicy p : UserIdentityTokens)
		{
		
			// Ensure the stack knows the policy
			try {
				String securityPolicyUri = p.getSecurityPolicyUri();
				SecurityPolicy.getSecurityPolicy(securityPolicyUri);
			} catch (ServiceResultException e) {
				continue;
			}

			if (p.getTokenType() != type) continue;
		
			return p;
		}
		return null;
	}

    /**
     * Finds the user token policy with the specified id.
     * 
     * @return user token policy or null
     */
    public UserTokenPolicy findUserTokenPolicy(String policyId)
    {
		if (UserIdentityTokens==null) return null;
    	//TODO how to determine right policyId's? Now policyId == Token name
		for (UserTokenPolicy policy : UserIdentityTokens)
			if (policy != null) {
				final String p = policy.getPolicyId();
				if (p != null && p.equals(policyId))
					return policy;
			}
        return null;
    }

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

	public boolean needsCertificate() {
		return getSecurityMode().hasSigning() ||
			EndpointUtil.containsSecureUserTokenPolicy(getUserIdentityTokens());
	}
    
}
