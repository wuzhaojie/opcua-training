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

package org.opcfoundation.ua.stacktest;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;


/*
 * Configuration data for stack test client and server.
 * 
 * Includes the file paths to TestCases.xml. Random.bin & TestLog.xml
 * and to the security certificates.
 * 
 * @Author Jouni Aro <jouni.aro@prosys.fi>
 */
public class TestConfiguration {
	
	public Cert ClientCertificate;
	public PrivKey ClientPrivateKey;
	public KeyPair ClientApplicationInstanceCertificate;
	
	public Cert ServerCertificate;
	public PrivKey ServerPrivateKey;
	public KeyPair ServerApplicationInstanceCertificate; 

	// Test case file path.        
    private String testFilePath;
    
    // Random number file path.        
    private String randomFilePath;
    
    // Log file path.        
    private String logFilePath;
    
    // Random data step size.        
    private Integer randomDataStepSize;
    
    // Server endpoint.        
    private EndpointDescription endpoint;

	public TestConfiguration(String testCases) throws CertificateException,
			IOException, UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
		// default values
		if (testCases != null)
			testFilePath = testCases;
		else
			testFilePath = "testharness/TestCases.xml";
		randomFilePath = "testharness/Random.bin";
		logFilePath = "testharness/TestLog.xml";
		randomDataStepSize = 1;

		ServerCertificate = Cert.load(TestConfiguration.class
				.getResource("ServerCert.der"));
		ServerPrivateKey = PrivKey.loadFromKeyStore(
				TestConfiguration.class.getResource("UAServerCert.pfx"),
				"Opc.Sample.Ua.Server");
		ServerApplicationInstanceCertificate = new KeyPair(ServerCertificate,
				ServerPrivateKey);
		ClientCertificate = Cert.load(TestConfiguration.class
				.getResource("ClientCert.der"));
		ClientPrivateKey = PrivKey.loadFromKeyStore(
				TestConfiguration.class.getResource("ClientCert.pfx"),
				"Opc.Sample.Ua.Client");
		ClientApplicationInstanceCertificate = new KeyPair(ClientCertificate,
				ClientPrivateKey);
	}

	public EndpointDescription getEndpoint() {
		return endpoint;
	}

	public String getLogFilePath() {
		return logFilePath;
	}

	public Integer getRandomDataStepSize() {
		return randomDataStepSize;
	}

	public String getRandomFilePath() {
		return randomFilePath;
	}

	public String getTestFilePath() {
		return testFilePath;
	}

	public void setLogFilePath(String value) {
		logFilePath = value;
		
	}

}
