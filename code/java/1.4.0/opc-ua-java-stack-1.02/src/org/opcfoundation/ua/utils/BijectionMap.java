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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Bijection map is a 1:1 binding of 2-tuples.
 * For each binding one value has role "left" and the other one the role "right". 
 *
 * @author Toni Kalajainen
 */
public class BijectionMap<L, R> {

	/** The keys of this map are lefts and values rights */
	private Map<L, R> tableLeft = new HashMap<L, R>();
	/** The keys of this map are rights and values lefts */
	private Map<R, L> tableRight = new HashMap<R, L>();
	
	public void addAll(BijectionMap<L, R> map)
	{
		for (Entry<L, R> e : map.getEntries())
			map(e.getKey(), e.getValue());
	}
	
    public boolean retainAllLeft(Collection<L> values)
    {
        boolean result = false;
        for (L lValue : values)
            if ( !tableLeft.containsKey(lValue) ) {
                removeWithLeft(lValue);
                result = true;
            }
        return result;
    }
    
    public boolean retainAllRight(Collection<R> values)
    {
        boolean result = false;
        for (R rValue : values)
            if ( !tableRight.containsKey(rValue) ) {
                removeWithRight(rValue);
                result = true;
            }
        return result;
    }
    
	public Set<Entry<L, R>> getEntries()
	{
		return tableLeft.entrySet();
	}
	
	public boolean containsLeft(L leftValue)
	{
		return tableLeft.containsKey(leftValue);
	}
	
	public boolean containsRight(R rightValue)
	{
		return tableRight.containsKey(rightValue);
	}
	
	/**
	 * Contains binding
	 * 
	 * @param leftValue
	 * @param rightValue
	 * @return true if there is a mapping between left and right value
	 */
	public boolean contains(L leftValue, R rightValue)
	{
		if (leftValue==rightValue) return true;
		if (leftValue==null || rightValue==null) return false;
		R right = tableLeft.get(leftValue);
		if (right==rightValue) return true;
		return tableLeft.get(leftValue).equals(right);
	}
	
	/**
	 * Add value to the map
	 * 
	 * @param leftValue
	 * @param rightValue
	 */
	public void map(L leftValue, R rightValue)
	{
        // Remove possible old mapping
        R oldRight = tableLeft.remove(leftValue);
        if (oldRight != null) {
            tableRight.remove(oldRight);
        } else {
            L oldLeft = tableRight.remove(rightValue);
            if (oldLeft != null) {
                tableLeft.remove(oldLeft);
            }
        }
        
		tableLeft.put(leftValue, rightValue);
		tableRight.put(rightValue, leftValue);
	}
	
	public boolean isEmpty() {
	    return tableLeft.isEmpty();
	}
    
	/**
	 * Get the number of mappings
	 * 
	 * @return the number of mappings
	 */
    public int size() 
    {
        return tableLeft.size();
    }
	
    /**
     * Get left value with right key
     * 
     * @param rightValue
     * @return left value
     */
	public L getLeft(R rightValue) {
		return tableRight.get(rightValue);		
	}

	/**
	 * Get right value with left key
	 * 
	 * @param leftValue
	 * @return right vlaue
	 */
	public R getRight(L leftValue) {
		return tableLeft.get(leftValue);		
	}
	
	/**
	 * Remove a binding with left key
	 * 
	 * @param leftValue
	 * @return old right value
	 */
	public R removeWithLeft(L leftValue) {
		R rightValue = tableLeft.remove(leftValue);
		if (rightValue!=null)
			tableRight.remove(rightValue);
		return rightValue;
	}

	/**
	 * Remove a binding with right key
	 * 
	 * @param rightValue
	 * @return old left value
	 */
	public L removeWithRight(R rightValue) {
		L leftValue = tableRight.remove(rightValue);
		if (leftValue!=null)
			tableLeft.remove(leftValue);
		return leftValue;
	}
    
	/**
	 * Get all left values
	 * 
	 * @return all left values
	 */
    public Set<L> getLeftSet() {
        return Collections.unmodifiableSet( tableLeft.keySet() ); 
    }
    
    /**
     * Get all right values. 
     * 
     * @return all right values
     */
    public Set<R> getRightSet() {
        return Collections.unmodifiableSet( tableRight.keySet() );
    }    
    
    /**
     * Clear all bindings
     */
    public void clear() {
        tableLeft.clear();
        tableRight.clear();
    }
    
    @Override
    public String toString() {
    	int count = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	for (Entry<L, R> e : tableLeft.entrySet())
    	{
    		if (count++>0) sb.append(", ");
    		sb.append(e.getKey().toString());
    		sb.append("=");
    		sb.append(e.getValue().toString());    		
    	}
    	sb.append("]");
    	return sb.toString();
    }
}
