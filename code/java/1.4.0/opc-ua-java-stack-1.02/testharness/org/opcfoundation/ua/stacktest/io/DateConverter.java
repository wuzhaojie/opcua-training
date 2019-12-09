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

package org.opcfoundation.ua.stacktest.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.betwixt.strategy.ConvertUtilsObjectStringConverter;
import org.opcfoundation.ua.stacktest.TestEvent;

/**
 * ObjectStringConverter used with the Betwixt BeanReader & BeanWriter.
 *   
 * Does not seem to do the trick that we would need, though...  
 *   
 * @author jaro
 *
 */
public class DateConverter extends ConvertUtilsObjectStringConverter {
	private static final long serialVersionUID = 4431544120477199705L;
	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ss.SSSZ");

	public String objectToString(Object object, Class type, Context context) {
		if (object != null) {
			if (object instanceof java.util.Date) {
				return formatter.format((java.util.Date) object);
			}
		}
		return super.objectToString(object, type, context);
	}

	@Override
	public Object stringToObject(String str, Class type, Context context) {
		if (type == java.util.Date.class)
			try {
				return (java.util.Date) formatter.parse(str);
			} catch (ParseException e) {
				GregorianCalendar g = new GregorianCalendar();
				try {
				g = (GregorianCalendar) stringToObject(str, GregorianCalendar.class, context);
				return g.getTime();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			else if (type == TestEvent.TestEventType.class) {
				if (str == "Started")
					return TestEvent.TestEventType.Started;
				if (str == "Completed")
					return TestEvent.TestEventType.Completed;
				if (str == "NotValidated")
					return TestEvent.TestEventType.NotValidated;
				if (str == "Failed")
					return TestEvent.TestEventType.Failed;
				if (str == "StackEvents")
					return TestEvent.TestEventType.StackEvents;
			}
				
		return super.stringToObject(str, type, context);
	}
}
