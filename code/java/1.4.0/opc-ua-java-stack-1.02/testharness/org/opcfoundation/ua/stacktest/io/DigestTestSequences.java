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

import org.apache.commons.digester.*;
import org.opcfoundation.ua.stacktest.TestCase;
import org.opcfoundation.ua.stacktest.TestParameter;
import org.opcfoundation.ua.stacktest.TestSequence;

/**
 * This is a test class for testing Jakarta Digester with the
 * TestLog.xml document. Reading in does not work properly
 * and also writing has some drawback. 
 * 
 * @author jouni.aro@prosys.fi
 *
 */
public class DigestTestSequences {
	public TestSequence testSequence;
	
    public DigestTestSequences() {
		super();
		testSequence = new TestSequence();
	}

	public static void main(String[] args) {
    	DigestTestSequences digestTestSequences = new DigestTestSequences();
    	digestTestSequences.digest();
    }

    private void digest() {
        try {
            Digester digester = new Digester();
            //Logger logger = Logger.getLogger("log.txt");
            //Log log = new Log(logger);
            //digester.setLogger(log);
            //Push the current object onto the stack
            digester.push(testSequence);

            //Set the attribute values as properties
            digester.addSetProperties("TestSequence", 
            		new String[] { "HaltOnError" , "LogDetailLevel"},
            		new String[] { "haltOnError" , "logDetailLevel"});

            //Creates a new instance of the TestSequence class
            digester.addObjectCreate( "TestSequence/TestCase", TestCase.class );

            //Uses setter methods of the TestCase instance
            //Uses tag name as the property name
            digester.addBeanPropertySetter( "TestSequence/TestCase/Name", "name");
            digester.addBeanPropertySetter( "TestSequence/TestCase/Count", "count");
            digester.addBeanPropertySetter( "TestSequence/TestCase/Seed", "seed");
            digester.addBeanPropertySetter( "TestSequence/TestCase/ResponseSeed", "responseSeed");
            digester.addBeanPropertySetter( "TestSequence/TestCase/Start", "start");

            //Creates a new instance of the TestSequence class
            digester.addObjectCreate( "TestSequence/TestCase/Parameter", TestParameter.class );
            digester.addBeanPropertySetter( "TestSequence/TestCase/Parameter/Name", "name");
            digester.addBeanPropertySetter( "TestSequence/TestCase/Parameter/Value", "value");

            digester.addSetNext( "TestSequence/TestCase/Parameter", "addParameter" );

            //Move to next TestSequence
            digester.addSetNext( "TestSequence/TestCase", "addTestCase" );

//            InputStream input = new FileInputStream("TestCases.xml");
//            digester.parse(input);
            /// DigestTestSequences ds = (DigestTestSequences) 
            digester.parse(this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("TestCases.xml"));

            //Print the contents of the Vector
            System.out.println("TestSequences: "+testSequence.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
