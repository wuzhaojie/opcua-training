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

import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.NodeClass;

public class AttributesUtil {

    //Copied from earlier project
    public static boolean isValid(UnsignedInteger attributeId) {
		return (attributeId.compareTo(Attributes.NodeId) >= 0 && attributeId.compareTo(Attributes.UserExecutable) <= 0);
	}
    
    //TODO Mikko added some attributeIds to this function..please check that those are correct..the information has been taken from spec part 3 v. 1.01.13 from chapter 5.9
    /**
	 * Tests if the attribute is valid for at least one of the node classes specified in the mask.
	 * 
	 * @param nodeClass
	 * @param attributeId
	 * @return true if valid
	 */
    public static boolean isValid(NodeClass nodeClass, UnsignedInteger attributeId) {
    	
    	int ordinalNodeClass = nodeClass.ordinal();
    	//NodeId,NodeClass,DisplayName&BrowseName are mandatory for all node types..Description,UserWriteMask&EriteMask are optional for all nodeTypes
    	if (attributeId.equals(Attributes.NodeId) || attributeId.equals(Attributes.NodeClass) || attributeId.equals(Attributes.BrowseName) || 
    			attributeId.equals(Attributes.DisplayName) || attributeId.equals(Attributes.Description) ||
    			attributeId.equals(Attributes.WriteMask) || attributeId.equals(Attributes.UserWriteMask)) {
    		return true;
		}
    	
    	if (attributeId.equals(Attributes.Value) || attributeId.equals(Attributes.DataType) || attributeId.equals(Attributes.ValueRank)) {
    		return (ordinalNodeClass & (org.opcfoundation.ua.core.NodeClass.VariableType.ordinal()| org.opcfoundation.ua.core.NodeClass.Variable.ordinal())) != 0;
			
		}
    	
    	if (attributeId.equals(Attributes.IsAbstract)) {
			return (ordinalNodeClass & (org.opcfoundation.ua.core.NodeClass.VariableType.ordinal() | 
					org.opcfoundation.ua.core.NodeClass.ObjectType.ordinal() | org.opcfoundation.ua.core.NodeClass.DataType.ordinal() |org.opcfoundation.ua.core.NodeClass.ReferenceType.ordinal())) != 0;
		}

    	if (attributeId.equals(Attributes.Symmetric) || attributeId.equals(Attributes.InverseName)) {
			return (ordinalNodeClass & org.opcfoundation.ua.core.NodeClass.ReferenceType.ordinal()) != 0;
		}

    	if (attributeId.equals(Attributes.ContainsNoLoops)) {
			return (ordinalNodeClass & org.opcfoundation.ua.core.NodeClass.View.ordinal()) != 0;
		}
    	
    	if (attributeId.equals(Attributes.EventNotifier)) {
			return (ordinalNodeClass & (org.opcfoundation.ua.core.NodeClass.Object.ordinal() | org.opcfoundation.ua.core.NodeClass.View.ordinal())) != 0;
		}
                
    	if (attributeId.equals(Attributes.AccessLevel) || attributeId.equals(Attributes.UserAccessLevel) || attributeId.equals(Attributes.MinimumSamplingInterval) || 
    			attributeId.equals(Attributes.Historizing)) {
			return (ordinalNodeClass & org.opcfoundation.ua.core.NodeClass.Variable.ordinal()) != 0;
		}
    	if( attributeId.equals(Attributes.ArrayDimensions)){
    		return (ordinalNodeClass & (org.opcfoundation.ua.core.NodeClass.Variable.ordinal() | 
					org.opcfoundation.ua.core.NodeClass.VariableType.ordinal())) != 0; 
    	}
                
    	if (attributeId.equals(Attributes.Executable) || attributeId.equals(Attributes.UserExecutable)) {
			return (ordinalNodeClass & org.opcfoundation.ua.core.NodeClass.Method.ordinal()) != 0;
		}

        return false;
	}

    
	public static String toString(UnsignedInteger value) {
		try {
		  return attributeNames.get(value);
		} catch (NullPointerException e) {
			return "<InvalidAttributeValue " + value + ">";
		}
	}
	
    final static Map<UnsignedInteger, String> attributeNames = new HashMap<UnsignedInteger, String>();
    static {
        
        attributeNames.put(Attributes.NodeId , "NodeId");
        attributeNames.put(Attributes.NodeClass , "NodeClass");
        attributeNames.put(Attributes.BrowseName , "BrowseName");
        attributeNames.put(Attributes.DisplayName , "DisplayName");
        attributeNames.put(Attributes.Description , "Description");
        attributeNames.put(Attributes.WriteMask , "WriteMask");
        attributeNames.put(Attributes.UserWriteMask , "UserWriteMask");
        attributeNames.put(Attributes.IsAbstract , "IsAbstract");
        attributeNames.put(Attributes.Symmetric , "Symmetric");
        attributeNames.put(Attributes.InverseName , "InverseName");
        attributeNames.put(Attributes.ContainsNoLoops , "ContainsNoLoops");
        attributeNames.put(Attributes.EventNotifier , "EventNotifier");
        attributeNames.put(Attributes.Value , "Value");
        attributeNames.put(Attributes.DataType , "DataType");
        attributeNames.put(Attributes.ValueRank , "ValueRank");
        attributeNames.put(Attributes.ArrayDimensions , "ArrayDimensions");
        attributeNames.put(Attributes.AccessLevel , "AccessLevel");
        attributeNames.put(Attributes.UserAccessLevel , "UserAccessLevel");
        attributeNames.put(Attributes.MinimumSamplingInterval , "MinimumSamplingInterval");
        attributeNames.put(Attributes.Historizing , "Historizing");
        attributeNames.put(Attributes.Executable , "Executable");
        attributeNames.put(Attributes.UserExecutable , "UserExecutable");
    }
}
