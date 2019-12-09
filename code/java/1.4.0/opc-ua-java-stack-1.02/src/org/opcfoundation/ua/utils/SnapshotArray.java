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

/*
 * 12.6.2007
 */
package org.opcfoundation.ua.utils;

import java.lang.reflect.Array;

/**
 * SnapshotArray is synchronized list that provides copy-on-write-arraylist
 * of its contents. 
 * 
 * @author Toni Kalajainen
 * @param <T> 
 */
public class SnapshotArray<T> {
    
    /**
     * Array of listeners
     */
    private volatile T [] array;
    
    /** 
     * The class of T
     */
    private final Class<T> componentType;
           
    /**
     * Construct new Listener List
     * @param componentType the class of the listener type
     */
    public SnapshotArray(Class<T> componentType)
    {
        this.componentType = componentType;
        array = createArray(0);
    }
    
    /**
     * Get a snapshot of the contents. This method exposes an internal state
     * which must not be modified. 
     * 
     * @return an array. 
     */
    public T[] getArray()
    {
        return array;
    }
    
    public synchronized void add(T item)
    {
        int oldLength = array.length;
        int newLength = oldLength + 1;
        T newArray[] = createArray(newLength);
        System.arraycopy(array, 0, newArray, 0, oldLength);
        newArray[oldLength] = item;
        array = newArray;
    }
    
    /**
     * Removes the first occurance of the item.
     * If the item is added multiple times, then it must be removed
     * as many times.
     * 
     * @param item an item
     * @return true if the item was removed from the list
     */
    public synchronized boolean remove(T item)
    {
        int pos = getPos(item);
        if (pos<0) return false;
        
        int oldLength = array.length;
        int newLength = oldLength -1;
        T newArray[] = createArray(newLength);
        
        // Copy beginning
        if (pos>0)
            System.arraycopy(array, 0, newArray, 0, pos);
        
        // Copy ending
        if (pos<newLength)
            System.arraycopy(array, pos+1, newArray, pos, newLength-pos);
        
        array = newArray;
        return true;
    }        
    
    private synchronized int getPos(T listener)
    {
        for (int i=0; i<array.length; i++)
            if (array[i] == listener)
                return i;
        return -1;
    }
    
    public int size()
    {
        return array.length;
    }
    
    public boolean isEmpty()
    {
        return array.length == 0;
    }
    
    public void clear()
    {
        array = createArray(0);
    }
    
    @SuppressWarnings("unchecked")
    private T[] createArray(int size)
    {
        return (T[]) Array.newInstance(componentType, size);
    }
    
}
