package org.opcfoundaiton.ua;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLTool {
	
	public static Document readDocument(URL url) throws ParserConfigurationException, IOException, SAXException
	{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream is = url.openStream();
        try {
        	return db.parse( is );
        } finally {
        	is.close();
        }
	}
	
	public static Document readDocument(InputStream is) throws ParserConfigurationException, IOException, SAXException
	{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        try {
        	return db.parse( is );
        } finally {
        	is.close();
        }
	}
	
	public static Document readDocument(File file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse( file );
	}

	public static Document readHtmlDocument(File file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//    	dbf.setValidating(false);
//    	//dbf.setNamespaceAware(false);
//    	dbf.setIgnoringComments(false);
    	dbf.setIgnoringElementContentWhitespace(true);
//    	dbf.setExpandEntityReferences(false);
    	
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse( file );
	}
	
	public static String printHtmlDocument(Node document) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
		Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "systmId");
        
		DOMSource source = new DOMSource( document );
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult( sw );
		transformer.transform(source, result);
		return sw.toString();
	}
	
	public static String printDocument(Node document) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
		Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");        
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
		DOMSource source = new DOMSource( document );
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult( sw );
		transformer.transform(source, result);
		return sw.toString();
	}
	
	public static String printXMLDocument(Document document) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
		Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                
		DOMSource source = new DOMSource( document );
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult( sw );
		transformer.transform(source, result);
		return sw.toString();
	}

	
	public static void writeDocument(Document document, File toFile) throws TransformerException, IOException {		
		// Indent
		String xml = printDocument(document);
		writeFile(toFile, xml.getBytes());		
	}
	

    /**
     * Creates and writes a binary file
     * @param file file
     * @param data data
     * @throws IOException on i/o problems
     */
    public static void writeFile(File file, byte[] data)
    throws IOException
    {
//        file.createNewFile();
//        file.setWritable(true);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(data.length);
            raf.seek(0);
            raf.write(data);
        } finally {
            raf.close();
        }
    }    
    
    public static Node getChildByName(Node parent, String name)
    {
    	NodeList nl = parent.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++) {
    		Node n = nl.item(i);
    		if ( name.equals(n.getNodeName()) ) return n; 
    	}
    	return null;
    }

    public static Node getChildByName(Node parent, String...names)
    {
    	return getChildByName(parent, 0, names);
    }

    private static Node getChildByName(Node parent, int index, String[] names)
    {
    	NodeList nl = parent.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++) {
    		Node n = nl.item(i);
    		String name = n.getNodeName();
    		if ( name==null ) continue;
    		if ( names[index].equals(name) ) {
    			if ( index+1==names.length ) return n;
    			Node r = getChildByName(n, index+1, names);
    			if ( r!=null ) return r;
    		}
    	}
    	return null;
    }
    
    
    public static List<Node> getChildrenByName(Node parent, String name)
    {
    	List<Node> result = new ArrayList<Node>();
    	
    	NodeList nl = parent.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++) {
    		Node n = nl.item(i);
    		if ( name.equals(n.getNodeName()) ) result.add( n );
    	}
    	return result;
    }
    
    public static String escapeName(String name) {
        char[] chars = name.toCharArray();
        boolean modified = false;
        for(int i=0;i<chars.length;++i)
            if(!Character.isJavaIdentifierPart(chars[i])) {
                chars[i] = '_';
                modified = true;
            }
        if(modified)
            return new String(chars);
        else
            return name;
    }
    
    public static void visit(Node node, NodeVisitor visitor)
    {
    	visitor.visit(node);
    	
    	if ( node.hasAttributes() ) {
    		NamedNodeMap attrib = node.getAttributes();
    		for ( int i=0; i<attrib.getLength(); i++) {
    			Node n = attrib.item(i);
    			visit(n, visitor);
    		}
    	}
    	
    	NodeList nl = node.getChildNodes();
    	if ( nl != null ) {
    		for (int i=0; i<nl.getLength(); i++) {
    			Node n = nl.item(i);
    			visit(n, visitor);
    		}
    	}
    	
    	visitor.leave(node);
    }
    
    public interface NodeVisitor {
    	void visit(Node node);
    	void leave(Node node);
    }
    
    /**
     * Copy DOM-Tree
     * 
     * @param src node to copy
     * @param doc document to construct nodes
     * @param lens (optional) lens for adjusting values while copying
     * @return copied DOM tree
     */
    public static Node copyNode(Node src, Document doc, DOMLens lens)
    {
    	if (lens == null) lens = DEFAULT_LENS;
    	return _copy(src, null, doc, lens);
    }
    
	private static Node _copy(Node src, Node dstParent, Document doc, DOMLens lens)
	{
		int type = lens.getNodeType(src);
		String name = lens.getNodeName(src);
		String value = lens.getNodeValue(src);

		Node dn = dstParent;
		switch (type) {
		case Node.COMMENT_NODE:
			dn = doc.createComment(src.getNodeValue());
			if ( dstParent != null ) dstParent.appendChild( dn );
			return dn;
		case Node.TEXT_NODE:
			dn = doc.createTextNode(src.getNodeValue());
			if ( dstParent != null ) dstParent.appendChild( dn );
			return dn;
		case Node.CDATA_SECTION_NODE:
			dn = doc.createCDATASection(src.getNodeValue());
			if ( dstParent != null ) dstParent.appendChild( dn );
			return dn;
		case Node.ATTRIBUTE_NODE:
			//dn = doc.createAttribute( name );			
			//dn.setNodeValue(value); 
			//if ( dstParent != null ) dstParent.appendChild( dn );
			Element e = (Element) dstParent;
			e.setAttribute(name, value);
			return dn;
		case Node.ELEMENT_NODE:
			dn = doc.createElement(name);
			if ( dstParent != null ) dstParent.appendChild( dn );
			break;
		case Node.DOCUMENT_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
		case Node.DOCUMENT_TYPE_NODE:			
		case Node.ENTITY_REFERENCE_NODE:
			dn = doc.createEntityReference(src.getNodeName());
			if ( dstParent != null ) dstParent.appendChild( dn );
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			dn = doc.createProcessingInstruction(src.getNodeName(), src.getNodeValue());
			if ( dstParent != null ) dstParent.appendChild( dn );
			break;
		case Node.ENTITY_NODE:			
			break;
		case Node.NOTATION_NODE:			
		}
		if ( dn==null ) return null;
		
    	if ( src.hasAttributes() ) {
    		NamedNodeMap attrib = src.getAttributes();
    		for ( int i=0; i<attrib.getLength(); i++) {
    			Node nn = attrib.item(i);
    			_copy(nn, dn, doc, lens);
    		}
    	}
		
		// Recursion
		NodeList nl = src.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node nn = nl.item(i);			
			_copy(nn, dn, doc, lens);						
		}
		return dn;
	}
	
	/** 
	 * An interface that allows mutation of DOM objects while tree is copied
	 */
	public interface DOMLens {
		
		int getNodeType( Node src );
		String getNodeName( Node src );
		String getNodeValue( Node src );
		
	}
	
	public static final DOMLens DEFAULT_LENS = new DOMLens() {
		@Override
		public int getNodeType(Node src) {
			return src.getNodeType();
		}

		@Override
		public String getNodeName(Node src) {
			return src.getNodeName();
		}

		@Override
		public String getNodeValue(Node src) {
			return src.getNodeValue();
		}
		
	};

}
