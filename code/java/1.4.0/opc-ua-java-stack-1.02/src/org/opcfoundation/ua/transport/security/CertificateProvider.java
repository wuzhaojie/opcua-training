package org.opcfoundation.ua.transport.security;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CertificateProvider {

	public X509Certificate generateCertificate(String domainName,
			PublicKey publicKey, PrivateKey privateKey, KeyPair issuerKeys,
			Date from, Date to, BigInteger serialNumber, String applicationUri,
			String... hostNames) throws GeneralSecurityException, IOException;

	public X509Certificate generateIssuerCert(PublicKey publicKey,
			PrivateKey privateKey, KeyPair issuerKeys, String domainName,
			BigInteger serialNumber, Date startDate, Date expiryDate)
					throws GeneralSecurityException, IOException;

	public Collection<List<?>> getSubjectAlternativeNames(X509Certificate cert)
			throws CertificateParsingException;

	public void writeToPem(X509Certificate key, File savePath, String password,
			String algorithm) throws IOException;

}
