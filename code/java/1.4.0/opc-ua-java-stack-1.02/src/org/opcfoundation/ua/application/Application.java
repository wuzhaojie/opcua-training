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

package org.opcfoundation.ua.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.common.NamespaceTable;
import org.opcfoundation.ua.common.ServerTable;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.SignedSoftwareCertificate;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.encoding.EncoderContext;
import org.opcfoundation.ua.transport.EndpointServer;
import org.opcfoundation.ua.transport.UriUtil;
import org.opcfoundation.ua.transport.https.HttpsServer;
import org.opcfoundation.ua.transport.https.HttpsSettings;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.tcp.io.OpcTcpSettings;
import org.opcfoundation.ua.transport.tcp.nio.OpcTcpServer;
import org.opcfoundation.ua.utils.CryptoUtil;
import org.opcfoundation.ua.utils.StackUtils;

/**
 * This class contains the mechanisms that are commong for both client and server
 * application.
 * 
 * @see Client OPC UA Client Application
 * @see Server OPC UA Server Application
 */
public class Application {
	private final static Logger logger = LoggerFactory.getLogger(Application.class);		

	/** Application description */
	ApplicationDescription applicationDescription = new ApplicationDescription();
	/** Application Instance Certificates */
	List<KeyPair> applicationInstanceCertificates = new CopyOnWriteArrayList<KeyPair>();
	/** Software Certificates */
	List<SignedSoftwareCertificate> softwareCertificates = new CopyOnWriteArrayList<SignedSoftwareCertificate>();
	/** Locales */
	List<Locale> locales = new CopyOnWriteArrayList<Locale>();
	/** Https Settings for Https Endpoint Servers */
	HttpsSettings httpsSettings = new HttpsSettings();
	/** OpcTcp Settings for OpcTcp Endpoint Servers */
	OpcTcpSettings opctcpSettings = new OpcTcpSettings();
	/** Https Server */
	HttpsServer httpsServer;
	/** OpcTcp Server */
	OpcTcpServer opctcpServer;

	private EncoderContext encoderContext = new EncoderContext(new NamespaceTable(), new ServerTable(), StackUtils.getDefaultSerializer());
	
	public Application()
	{
		// Create application name
		String publicHostname = "";
		try {
			publicHostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		
		applicationDescription.setApplicationUri( "urn:"+publicHostname+":"+UUID.randomUUID() );
		
		getOpctcpSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
		getHttpsSettings().setCertificateValidator( CertificateValidator.ALLOW_ALL );
	}

	public EncoderContext getEncoderContext() {
		return encoderContext;
	}

	public HttpsSettings getHttpsSettings() {
		return httpsSettings;
	}

	public void setHttpsSettings(HttpsSettings httpsSettings) {
		this.httpsSettings = httpsSettings;
	}

	public synchronized EndpointServer getOrCreateEndpointServer(String scheme) throws ServiceResultException {
		if ( scheme.equals( UriUtil.SCHEME_OPCTCP ) ) {
			return getOrCreateOpcTcpServer();
		} else 
		if ( scheme.equals( UriUtil.SCHEME_HTTP ) || scheme.equals( UriUtil.SCHEME_HTTPS )) {
			return getOrCreateHttpsServer();
		} else throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "Cannot find EndpointServer for scheme "+scheme);
	}
	
	public synchronized HttpsServer getOrCreateHttpsServer() throws ServiceResultException {
		if ( httpsServer == null ) {
			httpsServer = new HttpsServer(this);
		}
		return httpsServer;
	}
	
	public synchronized OpcTcpServer getOrCreateOpcTcpServer() throws ServiceResultException {
		if ( opctcpServer == null ) {
			opctcpServer = new OpcTcpServer( this );
		}
		return opctcpServer;
	}

	public OpcTcpSettings getOpctcpSettings() {
		return opctcpSettings;
	}

	public void setOpctcpSettings(OpcTcpSettings opctcpSettings) {
		this.opctcpSettings = opctcpSettings;
	}
	
	public ApplicationDescription getApplicationDescription()
	{
		return applicationDescription;
	}
	
	public SignedSoftwareCertificate[] getSoftwareCertificates()
	{
		return softwareCertificates.toArray( new SignedSoftwareCertificate[softwareCertificates.size()] );
	}
	
	public void addSoftwareCertificate(SignedSoftwareCertificate cert)
	{
		if (cert==null) throw new IllegalArgumentException("null arg");
		softwareCertificates.add(cert);
	}
	
	public KeyPair[] getApplicationInstanceCertificates()
	{
		return applicationInstanceCertificates.toArray( new KeyPair[applicationInstanceCertificates.size()] );
	}
	
	public void addApplicationInstanceCertificate(KeyPair cert)
	{
		if (cert==null) throw new IllegalArgumentException("null arg");
		applicationInstanceCertificates.add(cert);
	}

	public void removeApplicationInstanceCertificate(KeyPair applicationInstanceCertificate)
	{
		applicationInstanceCertificates.remove( applicationInstanceCertificate );
	}

	public KeyPair getApplicationInstanceCertificate(byte[] thumb) 
	{
		logger.debug("getApplicationInstanceCertificate: expected={}", CryptoUtil.toHex(thumb));
		if (thumb != null) {
			int i = 0;
			for (KeyPair cert : applicationInstanceCertificates) {
				byte[] encodedThumbprint = cert.getCertificate()
						.getEncodedThumbprint();
				logger.debug("getApplicationInstanceCertificate: cert[{}]={}", i++, CryptoUtil.toHex(encodedThumbprint));
				if (Arrays.equals(encodedThumbprint, thumb))
					return cert;
			}
		}
		return null;
	}
		
	public KeyPair getApplicationInstanceCertificate()
	{
		final int index = applicationInstanceCertificates.size()-1;
		if (index < 0)
			return null;
		return applicationInstanceCertificates.get(index);
	}
	
	public String getApplicationUri()
	{
		return applicationDescription.getApplicationUri();
	}
	
	public void setApplicationUri(String applicationUri)
	{
		applicationDescription.setApplicationUri(applicationUri);
	}
	
	public void setApplicationName(LocalizedText applicationName)
	{
		applicationDescription.setApplicationName(applicationName);
	}

	public String getProductUri() 
	{
		return applicationDescription.getProductUri();
	}

	public void setProductUri(String productUri) 
	{
		applicationDescription.setProductUri( productUri );
	}
	
	public void addLocale(Locale locale)
	{
		if (locale==null)
			throw new IllegalArgumentException("null arg");
		locales.add(locale);
	}
	
	public void removeLocale(Locale locale)
	{
		locales.remove(locale);
	}
	
	public Locale[] getLocales()
	{
		return locales.toArray( new Locale[0] );
	}
	
	public String[] getLocaleIds()
	{
		ArrayList<String> result = new ArrayList<String>(locales.size());
		for (Locale l : locales)
			result.add( LocalizedText.toLocaleId(l) );
		return result.toArray( new String[ result.size() ] );
	}
	
	public void close() {
		if ( httpsServer !=null ) {
			httpsServer.close();
			httpsServer = null;
		}
		if ( opctcpServer != null ) {
			opctcpServer.close();
			opctcpServer = null;
		}
	}
	
}
