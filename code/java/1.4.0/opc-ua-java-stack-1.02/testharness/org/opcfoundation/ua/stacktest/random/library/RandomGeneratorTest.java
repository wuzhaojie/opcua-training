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

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.opcfoundation.ua.builtintypes.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the RandomGenerator
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class RandomGeneratorTest {
	private final String randomFile = "resources\\Random.bin";
	private final long seed=2;
	private final long step=4;
	private RandomGenerator generator=null;
	
	@Before
	public void setUp() throws Exception {
		generator = new RandomGenerator();	
		int retValue = generator.randomCreate(randomFile, seed, step);
		assertEquals("Failed to initialize random generator with randomFile '" + randomFile+ "'",
				0, retValue);
		
	}
		
	@After
	public void tearDown() throws Exception {
		if (generator != null)
			generator.randomDestroy();
	}
	

	@Test
	public void testRandomGetValue() {
		try {
			byte[] bytes = generator.randomGetValue(100);
			int i=0;
			for (byte b : bytes) {
				System.out.println("byte "+i+": "+b);
				i++;
			}
		} catch (UARandomLibException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testGetValueInt8() {
		for (int i=0; i<10; i++){
			byte b = generator.getValueInt8();
			System.out.println(i+", byte: "+b);
		}
	}
	
	@Test
	public void testGetValueInt16() {
		for (int i=0; i<10; i++){
			short s = generator.getValueInt16();
			System.out.println(i+", short: "+s);
		}
	}
	
	@Test
	public void testGetValueInt32() {
		for (int i=0; i<10; i++){
			int i32 = generator.getValueInt32();
			System.out.println(i+", I32: "+i32);
		}
	}

	@Test
	public void testGetValueInt64() {
		for (int i=0; i<10; i++){
			long i64 = generator.getValueInt64();
			System.out.println(i+", I64: "+i64);
		}
	}

	
	@Test
	public void testGetValueUInt8() {
		for (int i=0; i<10; i++){
			UnsignedByte ui8 = generator.getValueUInt8();
			System.out.println(i+", UI8: "+ui8.getValue());
		}
	}
	
	@Test
	public void testGetValueUInt16() {
		for (int i=0; i<10; i++){
			UnsignedShort ui16 = generator.getValueUInt16();
			System.out.println(i+", UI16: "+ui16.intValue());
		}
	}
	
	@Test
	public void testGetValueUInt32() {
		for (int i=0; i<10; i++){
			UnsignedInteger ui32 = generator.getValueUInt32();
			System.out.println(i+", UI32: "+ui32.longValue());
		}
	}
	
	@Test
	public void testGetValueUInt64() {
		for (int i=0; i<10; i++){
			UnsignedLong ul = generator.getValueUInt64();
			System.out.println(i+", UI64: "+ul.toString());
		}
	}
	
	@Test
	public void testGetValueFloat() {
		for (int i=0; i<10; i++){
			float f = generator.getValueFloat();
			System.out.println(i+", F: "+f);
		}
	}

	@Test
	public void testGetValueDouble() {
		for (int i=0; i<10; i++){
			double f = generator.getValueDouble();
			System.out.println(i+", D: "+f);
		}
	}

	@Test
	public void testGetValueDateTime() {
		final Calendar m_MinDateTime = new GregorianCalendar(1601, 1, 1);
		final long offset = -m_MinDateTime.getTimeInMillis();

		for (int i=0; i<10; i++){
			long time = generator.getValueDateTime();
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis((long) ((time / 10000) - offset));
			System.out.println(i+", T: "+time+" = " + DateFormat.getDateTimeInstance().format(cal.getTime()));
		}
	}

	@Test
	public void testGetValueString() {
		final int MAX_LENGTH = 256;
		for (int i=0; i<10; i++){
			String s = generator.getValueString(MAX_LENGTH);
			System.out.println(i+", S[" + s.length() + "]: "+s);
		}
	}
		
}
