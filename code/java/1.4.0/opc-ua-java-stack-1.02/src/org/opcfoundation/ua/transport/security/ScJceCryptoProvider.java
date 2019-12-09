package org.opcfoundation.ua.transport.security;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import org.opcfoundation.ua.utils.CryptoUtil;
import org.spongycastle.util.encoders.Base64;

public class ScJceCryptoProvider extends JceCryptoProvider implements CryptoProvider {

	public ScJceCryptoProvider() {
		super();
		CryptoUtil.setSecurityProviderName("SC");
		this.provider = Security.getProvider("SC");
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
