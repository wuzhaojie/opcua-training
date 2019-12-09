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
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.strategy.CapitalizeNameMapper;
import org.opcfoundation.ua.stacktest.TestSequence;
import org.opcfoundation.ua.stacktest.io.UnicodeReader;

/**
 * Class for reading TestSequence from TestCases.xml.
 * 
 * The reader uses Jakarta Betwist to read the objects from the xml
 * document. The rules for each object type are defined in the .betwixt
 * files.
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class TestSequenceReader {

	public TestSequenceReader() {
		super();
	}

	public TestSequence read(String fileName) {
        try {
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

            // Register beans so that betwixt knows what the xml is to be converted to
            beanReader.registerBeanClass("TestSequence", TestSequence.class);
            
            // Now we parse the xml
            FileInputStream is = new FileInputStream(fileName);      
            UnicodeReader reader = new UnicodeReader(is, "UTF-8");
            TestSequence sequence = (TestSequence) beanReader.parse(reader);
            return sequence.convertToDerivedTestCases();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
