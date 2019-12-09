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
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.CapitalizeNameMapper;
import org.opcfoundation.ua.stacktest.TestSequence;

/**
 * A class for writing TestSequence to an xml file.
 * 
 * Not actually used in the Stack Tester.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestSequenceWriter {

    public TestSequenceWriter() {
		super();
	}

	public String write(TestSequence testSequence) {
        try {
            // Start by preparing the writer
            // We'll write to a string 
            StringWriter outputWriter = new StringWriter(); 
            try {          
            	// Betwixt just writes out the bean as a fragment
            	// So if we want well-formed xml, we need to add the prolog
            	outputWriter.write("<?xml version='1.0' ?>\n");

            	// create write and set basic properties
            	BeanWriter beanWriter = new BeanWriter(outputWriter);
            	//beanWriter.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
            	beanWriter.enablePrettyPrint();
            	beanWriter.setInitialIndentLevel(0);
            	beanWriter.getBindingConfiguration().setMapIDs(false);

            	// set a custom name mapper for attributes
            	beanWriter.getXMLIntrospector().getConfiguration().setAttributeNameMapper(new CapitalizeNameMapper());
            	// set a custom name mapper for elements
            	beanWriter.getXMLIntrospector().getConfiguration().setElementNameMapper(new CapitalizeNameMapper());

            	// write out the bean
            	beanWriter.write(testSequence);
            	return outputWriter.toString();
            } finally {
            	outputWriter.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Failed.";
        }
    }
}
