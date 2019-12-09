package org.opcfoundation.ua.transport.security;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import org.bouncycastle.util.encoders.Base64;
import org.opcfoundation.ua.utils.CryptoUtil;

public class BcJceCryptoProvider extends JceCryptoProvider implements CryptoProvider {

	public BcJceCryptoProvider() {
		super();
		CryptoUtil.setSecurityProviderName("BC");
		this.provider = Security.getProvider("BC");
	}

	@Override
	public byte[] base64Decode(String string) {
		return Base64.decode(string);
	}

	@Override
	public String base64Encode(byte[] bytes) {
		try {
			return new String(Base64.encode(bytes), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
