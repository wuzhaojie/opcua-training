package org.opcfoundation.ua.transport.security;

import java.io.IOException;
import java.security.Security;

import org.opcfoundation.ua.utils.CryptoUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class SunJceCryptoProvider extends JceCryptoProvider implements CryptoProvider {

	public SunJceCryptoProvider() {
		CryptoUtil.setSecurityProviderName("SunJCE");
		this.provider = Security.getProvider("SunJCE");
	}

	@Override
	public byte[] base64Decode(String string) {
		// Probably better, but not available on the default jars!
		// return javax.xml.bind.DatatypeConverter.parseBase64Binary(string);
		BASE64Decoder bd = new BASE64Decoder();
		try {
			return bd.decodeBuffer(string);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String base64Encode(byte[] bytes) {
		// See above: base64Decode
		// return javax.xml.bind.DatatypeConverter.printBase64Binary(bytes);
		BASE64Encoder bd = new BASE64Encoder();
		return bd.encode(bytes);
	}

}
