/* Copyright (c) 1996-2015, OPC Foundation. All rights reserved.
   The source code in this file is covered under a dual-license scenario:
     - RCL: for OPC Foundation members in good-standing
     - GPL V2: everybody else
   RCL license terms accompanied with this source code. See http://opcfoundation.org/License/RCL/1.00/
   GNU General Public License as published by the Free Software Foundation;
   version 2 of the License are accompanied with this source code. See http://opcfoundation.org/License/GPLv2
   This source code is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/

package com.prosysopc.ua.transport.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import com.prosysopc.ua.common.ServiceResultException;
import com.prosysopc.ua.core.StatusCodes;
import com.prosysopc.ua.utils.CertificateUtils;
import com.prosysopc.ua.utils.CryptoUtil;
import com.prosysopc.ua.utils.FileUtil;
import com.prosysopc.ua.utils.StringUtils;

/**
 * Cert is a X509 certificate that contains a public key.
 * The instance is valid and encodedable.
 * Wrapper to {@link java.security.cert.Certificate}.
 * <p>
 * To Create a new certificate See {@link CertificateUtils}
 */
public class Cert {
	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----" + StringUtils.lineSeparator() ;
	private static final String END_CERT = StringUtils.lineSeparator() + "-----END CERTIFICATE-----";

	public final X509Certificate certificate;
	public final byte[] encodedCertificate; 
	public final byte[] encodedCertificateThumbprint;
	
	/**
	 * Load X.509 Certificate from an url
	 *
	 * @param url a {@link java.net.URL} object.
	 * @return Certificate
	 * @throws java.io.IOException if any.
	 * @throws java.security.cert.CertificateException In case the certificate is not valid
	 */
	public static Cert load(URL url) 
	throws IOException, CertificateException
	{
		X509Certificate cert = CertificateUtils.readX509Certificate(url);
		return new Cert(cert);
	}
	
	/**
	 * Load X.509 Certificate from a file
	 *
	 * @param file a {@link java.io.File} object.
	 * @return Certificate
	 * @throws java.io.IOException if any.
	 * @throws java.security.cert.CertificateException In case the certificate is not valid
	 */
	public static Cert load(File file) 
	throws IOException, CertificateException
	{
		return load(file.toURI().toURL());
	}
	
	
	/**
	 * <p>save.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 * @throws java.io.IOException if any.
	 */
	public void save(File file)
	throws IOException
	{
		FileUtil.writeFile(file, encodedCertificate);
	}
	
	/**
	 * <p>saveToPem.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 * @throws java.io.IOException if any.
	 */
	public void saveToPem(File file) throws IOException
	{
	    FileWriter fw = new FileWriter(file);
		try {
			fw.append(BEGIN_CERT);
			fw.append(StringUtils.addLineBreaks(CryptoUtil.base64Encode(getEncoded()), 72));
			fw.append(END_CERT);
		} finally {
			fw.close();
		}		
		//CertificateUtils.writeToPem(certificate, file);
	}
	
	/**
	 * <p>getKeySize.</p>
	 *
	 * @return a int.
	 */
	public int getKeySize() {
		PublicKey key = certificate.getPublicKey();
		if ( key instanceof RSAPublicKey == false ) return -1;
		RSAPublicKey rsaKey = (RSAPublicKey) key;
		return rsaKey.getModulus().bitLength();
	}
	
	/**
	 * Create Certificate from encoded data, if the data contains more than one certificate, only the first one is read.
	 *
	 * @param data encoded Certificate
	 * @throws ServiceResultException if any.
	 */
	public Cert(byte[] data) throws ServiceResultException {
		try {
			certificate = CertificateUtils.decodeX509Certificate(data);
			encodedCertificate = certificate.getEncoded();
			encodedCertificateThumbprint = CertificateUtils.createThumbprint(encodedCertificate);
		} catch (CertificateNotYetValidException ce) {
			throw new ServiceResultException(StatusCodes.Bad_CertificateTimeInvalid, ce);
		} catch (CertificateExpiredException ce) {
			throw new ServiceResultException(StatusCodes.Bad_CertificateTimeInvalid, ce);
		} catch (CertificateParsingException ce) {
			throw new ServiceResultException(StatusCodes.Bad_CertificateInvalid, ce);
		} catch (CertificateException ce) {
			throw new ServiceResultException(StatusCodes.Bad_CertificateInvalid, ce);
		}
	}

	/**
	 * Create Certificate from a X509Certificate certificate
	 *
	 * @param certificate a X509Certificate certificate
	 * @throws java.security.cert.CertificateEncodingException if any.
	 */
	public Cert(X509Certificate certificate) throws CertificateEncodingException {
		this.certificate = certificate;
		this.encodedCertificate = certificate.getEncoded();
		this.encodedCertificateThumbprint = CertificateUtils.createThumbprint(encodedCertificate);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Arrays.hashCode(encodedCertificate);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cert other = (Cert) obj;
		if (!Arrays.equals(encodedCertificate, other.encodedCertificate))
			return false;
		return true;
	}

	/**
	 * <p>Getter for the field <code>certificate</code>.</p>
	 *
	 * @return a {@link java.security.cert.X509Certificate} object.
	 */
	public X509Certificate getCertificate() {
		return certificate;
	}

	/**
	 * <p>getEncoded.</p>
	 *
	 * @return an array of byte.
	 */
	public byte[] getEncoded() {
		return encodedCertificate;
	}
	
	/**
	 * <p>getEncodedThumbprint.</p>
	 *
	 * @return an array of byte.
	 */
	public byte[] getEncodedThumbprint() {
		return encodedCertificateThumbprint;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return certificate.toString();
	}
	
}
