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


import java.io.FileReader;
import java.io.StringWriter;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.CapitalizeNameMapper;
import org.opcfoundation.ua.stacktest.TestLog;
//import org.apache.commons.betwixt.strategy.PropertySuppressionStrategy;

/**
 * This is a test class for testing Jakarta Betwixt with the
 * TestLog.xml document. Reading in does not work properly
 * and also writing has some drawback. 
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class BetwixtTestLog {
	public TestLog testLog;
	
    public BetwixtTestLog() {
		super();
	}

	public static void main(String[] args) {

    	BetwixtTestLog betwixtTestLog = new BetwixtTestLog();
    	System.out.println("read()");
    	betwixtTestLog.read();
    	System.out.println("write()");
    	betwixtTestLog.write();
    }

    private void write() {
        try {
            // Start by preparing the writer
            // We'll write to a string 
            StringWriter outputWriter = new StringWriter(); 
            
            // Betwixt just writes out the bean as a fragment
            // So if we want well-formed xml, we need to add the prolog
            outputWriter.write("<?xml version='1.0' ?>\n");
            
			// create write and set basic properties
        	BeanWriter beanWriter = new BeanWriter(outputWriter);
        	//writer.getXMLIntrospector().setAttributesForPrimitives(true);
        	beanWriter.enablePrettyPrint();
        	beanWriter.setInitialIndentLevel(0);
        	beanWriter.getBindingConfiguration().setMapIDs(false);

        	beanWriter.getBindingConfiguration().setObjectStringConverter(new DateConverter());
/*        			new ObjectStringConverter() {
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
*/        	
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
        	System.out.println(outputWriter.toString());

        	outputWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	private void read() {
	        try {
	            FileReader inputReader = new FileReader("resources/TestLog.xml"); 

	            // Now convert this to a bean using betwixt
	            // Create BeanReader
	            BeanReader beanReader  = new BeanReader();
	            
	            // Configure the reader
	            // If you're round-tripping, make sure that the configurations are compatible!
	            //beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(true);
	        	// set a custom name mapper for attributes
	            beanReader.getXMLIntrospector().getConfiguration().setAttributeNameMapper(new CapitalizeNameMapper());
	        	// set a custom name mapper for elements
	            beanReader.getXMLIntrospector().getConfiguration().setElementNameMapper(new CapitalizeNameMapper());
	            beanReader.getBindingConfiguration().setMapIDs(false);

	        	beanReader.getBindingConfiguration().setObjectStringConverter(new DateConverter());
/*	        			new ObjectStringConverter() {
	        				public String objectToString(Object object, Class type, Context context) {
	        					if (object == null)
	        						return "";
	        					else if (Date.class.equals(type))
	        						return ((Date) object).toString();
	        					else
	        						return object.toString();
	        				}
	        				public Object stringToObject(String value, Class type, Context context) {
	        					if (value == "")
	        						return null;
	        					else if (Date.class.equals(type))
	        					{
//	        						ConvertUtilsObjectStringConverter conv = new ConvertUtilsObjectStringConverter();
	        						ObjectStringConverter conv = new ObjectStringConverter();
	        						GregorianCalendar cal = (GregorianCalendar) conv.stringToObject(value, GregorianCalendar.class, context);
	        						return cal.getTime();
	        						//return new SimpleDateFormat("yyyy-dd-MM'T'ddHH:mm:ss.zzzzz'Z'").parse(value);
	        					}
	        					else
	        						return null;
	        				}
	        			}
	        			);
*/	        	
	            
	            // Register beans so that betwixt knows what the xml is to be converted to
	            beanReader.registerBeanClass("TestLog", TestLog.class);
	            //beanReader.registerBeanClass("TestLog/TestCase", TestCase.class);
	            //beanReader.registerBeanClass("TestLog/TestCase/TestParameter", TestParameter.class);
	            
	            // Now we parse the xml
	            testLog = (TestLog) beanReader.parse(inputReader);
	            
	            // send bean to system out
	            System.out.println(testLog);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
}
