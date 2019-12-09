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

package org.opcfoundation.ua.builtintypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.ObjectUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An XML element is a container for XML DOM documents.
 *
 */
public final class XmlElement {

	public static final NodeId ID = Identifiers.XmlElement;
	
	private static final Charset UTF8 = Charset.forName("utf-8");
	
	// The content is held in one or more formats: 
	// XML node
	private Node node;
	// XXL Document in text notation
	private String document;
	// UTF8 Encoded document
	private byte[] encoded;
	
	// Hash value, hash value exists with node
	int hash;

	/**
	 * Create new XML Element from XML Node
	 * 
	 * @param node xml node
	 */
	public XmlElement(Node node)
	{
		if (node==null)
			throw new IllegalArgumentException("value is null");
		this.node = node;
		this.hash = makeHash(node);
	}
	
	/**
	 * Create new XML Element from XML document
	 * 
	 * @param document
	 */
	public XmlElement(String document) {
		if (document==null)
			throw new IllegalArgumentException("value is null");
		this.document = document;
	}
	
	/**
	 * Create XML Element with UTF8 encoded XML document. 
	 * 
	 * @param encodedDocument
	 */
	public XmlElement(byte[] encodedDocument) {
		if (encodedDocument==null)
			throw new IllegalArgumentException("value is null");
		this.encoded = encodedDocument.clone();
	}
	
	/**
	 * Get the XmlElement as UTF8 encoded document
	 * 
	 * @return UTF8 encoded document
	 */
	public synchronized byte[] getData() {
		if ( encoded != null ) return encoded;
		
		if (document!=null) {			
			encoded = getValue().getBytes(UTF8);
			return encoded;
		}
		
		if (node!=null) {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = null;
			try {
				t = tf.newTransformer();
//					t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, );
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.setOutputProperty(OutputKeys.METHOD, "xml");
				t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			} catch (TransformerConfigurationException tce) {
				assert (false);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			DOMSource doms = new DOMSource(node);
			StreamResult sr = new StreamResult(os);				
			try {
				t.transform(doms, sr);
			} catch (TransformerException te) {
				throw new RuntimeException(te);
			}
			encoded = os.toByteArray();
		} 
		
		return encoded;
	}	
	
	public synchronized String getValue() {
		if ( document != null ) return document;
		
		if (encoded!=null) {
			document = new String(encoded, UTF8);
			return document;
		} 
		
		if (node!=null) {
			try {
				document = nodeToString(node);
			} catch (TransformerException te) {
				throw new RuntimeException(te);
			}			
		} 
		
		return document;
	}
	
	static String nodeToString(Node node) throws TransformerException
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
//			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, );
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		} catch (TransformerConfigurationException tce) {
			assert (false);
		}
		StringWriter sw = new StringWriter();
		DOMSource doms = new DOMSource(node);
		StreamResult sr = new StreamResult(sw);				
		t.transform(doms, sr);
		return sw.toString();		
	}
	
	// FEFF because this is the Unicode char represented by the UTF-8 byte order
	// mark (EF BB BF).
	public static final String UTF8_BOM = "\uFEFF";
	
	public synchronized Node getNode() {
		if (node==null) {
			if (encoded!=null) {
				try {
					InputStream is = new ByteArrayInputStream(encoded); 
					InputStreamReader reader = new InputStreamReader(is, UTF8);
					char[] cbuf = new char[2];
					reader.read(cbuf, 0, reader.getEncoding().equals("UTF8") ? 1
							: 2);
					//Ignoring possible BOM in the data.  
					if (cbuf[0] != UTF8_BOM.charAt(0)) {
						is = new ByteArrayInputStream(encoded);
						reader = new InputStreamReader(is, UTF8);
					}
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					//Using factory get an instance of document builder
					DocumentBuilder parser = dbf.newDocumentBuilder();
					node = parser.parse(new InputSource(reader));
					this.hash = makeHash(node);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				}
			} else if (document!=null) {
				try {
					StringReader reader = new StringReader(document);
					char[] cbuf = new char[2];
					reader.read(cbuf, 0, 1);
					//Ignoring possible BOM in the data.  
					if (cbuf[0] != UTF8_BOM.charAt(0)) {
						reader = new StringReader(document);
					}
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder parser = dbf.newDocumentBuilder();
					node = parser.parse(new InputSource(reader));
					this.hash = makeHash(node);
				} catch (SAXException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return node;
	}
	
	@Override
	public String toString() {
		try {
			return nodeToString(getNode());
		} catch (Exception e) {
			return getValue();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj.getClass().equals(XmlElement.class))) return false;
		XmlElement other = (XmlElement) obj;
		return areNodesEqual(this.getNode(), other.getNode() );	
	}
	
	static boolean areNodesEqual(Node n1, Node n2)
	{
        if (n2 == n1) return true;
        if (!ObjectUtils.objectEquals(n1.getNodeType(), n2.getNodeType())) return false;
        if (!ObjectUtils.objectEquals(n1.getNodeName(), n2.getNodeName())) return false;
        if (!ObjectUtils.objectEquals(n1.getLocalName(), n2.getLocalName())) return false;
        if (!ObjectUtils.objectEquals(n1.getNamespaceURI(), n2.getNamespaceURI())) return false;
        
        String v1 = n1.getNodeValue();
        String v2 = n2.getNodeValue();
        if ( (v1==null && v2!=null) || (v1!=null && v2==null) ) return false;
        if ( v1!=null && !v1.trim().equals(v2.trim())) return false;
        
        NodeList nl1 = n1.getChildNodes();
        NodeList nl2 = n2.getChildNodes();
        if ( (nl1==null && nl2!=null) || (nl1!=null && nl2==null) ) return false;
        if ( nl1!=null ) {
        	
        	int len = nl1.getLength();
        	if (nl2.getLength() != nl1.getLength()) return false;

        	// Make order-insensitive compare of values
        	/// (Pair comparison)
        	for (int i=0; i<len; i++) {
        		boolean ok = false;
        		Node sb1 = nl1.item(i);
        		
        		for (int j=0; j<len; j++) {
            		Node sb2 = nl2.item(j);
        			if (areNodesEqual(sb1, sb2)) {
        				ok = true;
        				break;
        			}
        		}
        		if (!ok) 
        			return false;
        	}
        	
        }        
        return true;
	}
	
	@Override
	public int hashCode() {
		if (node==null) {
			getNode();
		}
		return hash;
	}
	
	static int makeHash(Node n)
	{
		int hash = 123;		
        hash = hash*13 + n.getNodeType();
        hash = h(hash, n.getNodeName());
        hash = h(hash, n.getLocalName());
        hash = h(hash, n.getNamespaceURI());
//        hash = h(hash, n.getPrefix());
        hash = h(hash, n.getNodeValue());		
		return hash; 
	}

	static int h(int hash, Object o)
	{
		if (o==null) return hash*13;
		return hash*13 + o.hashCode();
	}
	
}
