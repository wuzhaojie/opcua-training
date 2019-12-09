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

import java.io.*;
import java.util.*;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.CapitalizeNameMapper;
import org.apache.commons.betwixt.strategy.ObjectStringConverter;
import org.apache.commons.betwixt.expression.Context;
import org.opcfoundation.ua.stacktest.TestLog;
//import org.apache.commons.betwixt.strategy.ConvertUtilsObjectStringConverter;
//import org.apache.commons.betwixt.strategy.PropertySuppressionStrategy;

/**
 * Writer class for writing the TestLog.xml
 * 
 * The reader uses Jakarta Betwist to read the objects from the xml
 * document. The rules for each object type are defined in the .betwixt
 * files.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestLogWriter {
    public TestLogWriter() {
		super();
	}

	public void write(TestLog testLog, String filePath) {
        try {
            // Start by preparing the writer
            // We'll write to a string 
            FileWriter outputWriter = new FileWriter(filePath); 
            
            // Betwixt just writes out the bean as a fragment
            // So if we want well-formed xml, we need to add the prolog
            outputWriter.write("<?xml version='1.0' ?>\n");
            
			// create write and set basic properties
        	BeanWriter beanWriter = new BeanWriter(outputWriter);
        	//writer.getXMLIntrospector().setAttributesForPrimitives(true);
        	beanWriter.enablePrettyPrint();
        	beanWriter.setInitialIndentLevel(0);
        	beanWriter.getBindingConfiguration().setMapIDs(false);

        	beanWriter.getBindingConfiguration().setObjectStringConverter(
        			new ObjectStringConverter() {
        				public String objectToString(Object object, Class type, Context context) {
        					if (object == null)
        						return "";
        					else if (GregorianCalendar.class.equals(context.getBean().getClass()))
        						return ((GregorianCalendar) context.getBean()).getTime().toString();
        					else if (GregorianCalendar.class.equals(type))
        						return ((GregorianCalendar) object).getTime().toString();
        					else
        						return object.toString();
        				}
        			}
        			);
        	
        	// set a custom name mapper for attributes
        	beanWriter.getXMLIntrospector().getConfiguration().setAttributeNameMapper(new CapitalizeNameMapper());
        	// set a custom name mapper for elements
        	beanWriter.getXMLIntrospector().getConfiguration().setElementNameMapper(new CapitalizeNameMapper());

/*       	beanWriter.getXMLIntrospector().getConfiguration().setPropertySuppressionStrategy(
        	        new PropertySuppressionStrategy() {
        	             public boolean suppressProperty(Class clazz, Class type, String name) {
        	                 return "class".equals(name) || GregorianCalendar.class.equals(type);
        	             }
        	        });
*/        	// write out the bean
        	beanWriter.write(testLog);
        	outputWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
