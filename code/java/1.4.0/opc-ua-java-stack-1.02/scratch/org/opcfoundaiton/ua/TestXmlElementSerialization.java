package org.opcfoundaiton.ua;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import org.opcfoundation.ua.builtintypes.XmlElement;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.encoding.EncodingException;
import org.opcfoundation.ua.encoding.binary.BinaryDecoder;
import org.opcfoundation.ua.encoding.binary.BinaryEncoder;
import org.w3c.dom.Document;

public class TestXmlElementSerialization {

	public static void main(String[] args) throws Exception {
		
		byte[] data1 = FileUtil.readFile( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Turbine_section.pdf" ) );
		byte[] data2 = FileUtil.readFile( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Control_system.pdf" ) );
		byte[] data3 = FileUtil.readFile( new File( "C:\\scratch\\boiler_runs.pdf" ) );
		
		Document doc1 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Feedwater_section.xml" ) );
		Document doc2 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal\\Turbine_section.xml" ) );
		Document doc3 = XMLTool.readDocument( new File( "C:\\scratch\\boiler_example-AprosTransmittal-3\\boiler_example.xml" ) );
		
		String str1 = XMLTool.printXMLDocument( doc1 );
		String str2 = XMLTool.printXMLDocument( doc2 );
		String str3 = XMLTool.printXMLDocument( doc3 );
		
		XmlElement xml1 = new XmlElement( data1 );
		XmlElement xml2 = new XmlElement( data2 );
		XmlElement xml3 = new XmlElement( data3 );
		XmlElement xml4 = new XmlElement( doc1 );
		XmlElement xml5 = new XmlElement( doc2 );
		XmlElement xml6 = new XmlElement( doc3 );
		
		testXmlElement( xml1 );
		testXmlElement( xml2 );
		testXmlElement( xml3 );
		
		testXmlElement( xml4 );
		testXmlElement( xml5 );
		testXmlElement( xml6 );
		
	}
	
	static void testXmlElement( XmlElement xe ) throws EncodingException, DecodingException {

		EncoderContext ctx = EncoderContext.getDefault();
		System.out.println( ctx );
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BinaryEncoder enc = new BinaryEncoder( baos );
		enc.setEncoderContext( ctx );
		enc.putXmlElement( "1", xe );
		
		ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
		BinaryDecoder dec = new BinaryDecoder( bais, baos.size() );
		dec.setEncoderContext( ctx );
		XmlElement xe2 = dec.getXmlElement( "1" );
		
		if ( !Arrays.equals( xe.getData(), xe2.getData() ) ) {
			throw new AssertionError("X X X");
		}

		if ( !xe.getValue().equals( xe2.getValue() ) ) {
			throw new AssertionError("X X X");
		}
		
	}
	
}
