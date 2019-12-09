/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
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

package org.opcfoundation.ua.stacktest.random.library;

import org.opcfoundation.ua.builtintypes.*;

/**
 * 
 * JNI wrapper for Opc.Ua.Random.dll. See <i>RandomGenerator.h</i> file to get
 * more information about parameters and return values of the functions. 
 * 
 * @author jouni.aro@prosys.fi
 */
public class RandomGenerator {
	/**
	 * Holds the handle for generator.
	 */
	@SuppressWarnings({"unused" }) // Used in C++ code
	private long random=0;
	/**
	 * 
	 * @param pathToFile File path to random data file. 
	 * Usually named random.bin
	 * @param nSeed 
	 * @param nStep
	 * @return Error code, see RandomGenerator.h  
	 */
	public native int randomCreate(String pathToFile, long nSeed, long nStep);
	/**
	 * 
	 * @return
	 * Returns error codes from Opc.Ua.Random.dll. Wrapper specific
	 * codes:
	 * <li> 1001 = Generator file was not opened
	 */
	public native int randomDestroy();
	
	private native int randomGetValue(byte[] data, long count);
	
	public byte[] randomGetValue(int count) throws UARandomLibException{
		byte[] result = new byte[count];
		int genResult = randomGetValue(result, count);
		if (genResult != 0) {
			throw new UARandomLibException(
					"Error while calling random generator library: "+genResult);
		}
		return result;
	}
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native byte getValueInt8();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native short getValueInt16();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native int getValueInt32();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native long getValueInt64();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native UnsignedByte getValueUInt8();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native UnsignedShort getValueUInt16();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native UnsignedInteger getValueUInt32();
	/**
	 * @see See <i>RandomGenerator.h</i> 
	 */
	public native UnsignedLong getValueUInt64();

	/**
	 * Raw memory interprets a got 32 - bit signed value independent of platform to float.
	 * @param pRandom [in] Pointer to a RANDOM variable.
	 * @return A random 32-bit floating point number.
	 */
	public native float getValueFloat();

	/**
	 * Raw memory interprets a got 64 - bit signed value independent of platform to double.
	 * @param pRandom [in] Pointer to a RANDOM variable.
	 * @return A random 64-bit floating point number.
	 */
	public native double getValueDouble();

	/**
	 * Returns DateTime between 1900/1/1 and 2099/12/31 (as number of 100ns ticks since 1601/1/1).
	 * @param pRandom [in] Pointer to a RANDOM variable.
	 * @return A random DateTime value.
	 */
	public native long getValueDateTime();

	/**
	 * Fills a string with random characters. 
	 * Range for characters of this string are hardcoded into range 0x0001 .. 0x33ff.
	 * The string generated is null terminated an will have a minimum length of 1 and a maximum length of nSize-1
	 * The length of the string calculated by generating an random UInt32 and the modulus of the min and max size.
	 * @param pRandom [in] Pointer to a RANDOM variable.
	 * @param [in/out] pString A pointer to a buffer allocated by the caller.
	 * @param [in] nSize The number of characters in the string buffer.
	 * @return pString address
	 */
	public native String getValueString(int maxStringLength);
	
    static {
        System.loadLibrary("testharness/RandomGenerator");
        System.loadLibrary("testharness/Opc.Ua.Random.JNI");
    }
    /**
     * Handle to the random generator. Can not be changed. 
     * @return 
     * The handle for the generator. If it is zero then a generator
     * is not opened.
     * 
     */
	public long getRandom() {
		return random;
	}

}
