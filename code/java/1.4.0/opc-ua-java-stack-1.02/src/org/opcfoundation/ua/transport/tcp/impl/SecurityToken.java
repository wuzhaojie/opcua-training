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

package org.opcfoundation.ua.transport.tcp.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.GregorianCalendar;

import javax.crypto.Mac;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.security.SecurityConfiguration;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.CryptoUtil;

/**
 * Security Token of a tcp connection
 * 
 */
public class SecurityToken {

	private static final Charset UTF8 = Charset.forName("utf-8");
	
	private SecurityConfiguration securityConfiguration;	
	private int tokenId;
	private int secureChannelId;
	private long creationTime;
	private long lifetime;
	
	//TODO: Mikon lisayksia
	private byte[] localNonce;
	private byte[] remoteNonce;
	private byte[] localSigningKey;
    private byte[] localEncryptingKey;
    private byte[] localInitializationVector;
    private byte[] remoteSigningKey;
    private byte[] remoteEncryptingKey;
    private byte[] remoteInitializationVector;
    
	/**
	 * Create new security token.
	 * 
	 * @param securityProfile
	 * @param secureChannelId
	 * @param tokenId
	 * @param creationTime
	 * @param lifetime
	 * @param localNonce
	 * @param remoteNonce
	 * @throws ServiceResultException
	 */
	public SecurityToken(SecurityConfiguration securityProfile, 
			int secureChannelId, int tokenId, 
			long creationTime, long lifetime,
			byte[] localNonce, byte[] remoteNonce) 
	throws ServiceResultException
	{
		if (securityProfile==null)
			throw new IllegalArgumentException("null arg");
		this.secureChannelId = secureChannelId;
		this.securityConfiguration = securityProfile;
		this.tokenId = tokenId;
		this.lifetime = lifetime;
		this.creationTime = creationTime;
		
		this.localNonce = localNonce;
		this.remoteNonce = remoteNonce;
		
        boolean isNone = securityProfile.getMessageSecurityMode() == MessageSecurityMode.None;
       	//Calculate and set keys       
		int sks = getSecurityPolicy().getSignatureKeySize();
		int eks = getSecurityPolicy().getEncryptionKeySize();
		int ebs = getSecurityPolicy().getEncryptionBlockSize();
		localSigningKey = isNone? null : PSHA(getRemoteNonce(), null, getLocalNonce(), 0, sks);
		localEncryptingKey = isNone? null : PSHA(getRemoteNonce(), null, getLocalNonce(), sks, eks);
		localInitializationVector = isNone? null : PSHA(getRemoteNonce(), null, getLocalNonce(), sks + eks, ebs);
		remoteSigningKey = isNone? null : PSHA(getLocalNonce(), null, getRemoteNonce(), 0, sks);
		remoteEncryptingKey = isNone? null : PSHA(getLocalNonce(), null, getRemoteNonce(), sks, eks);
		remoteInitializationVector = isNone? null : PSHA(getLocalNonce(), null, getRemoteNonce(), sks + eks, ebs);
	}	
	
	/**
     * Generates a Pseudo random sequence of bits using the P_SHA1 or P_SHA256 alhorithm.
     * 
     * This function conforms with C#-implementation, so that keys returned 
     * from this function matches the C#-implementation keys.
     * 
     * @param secret
     * @param label
     * @param data
     * @param offset
     * @param length
     * @return the pseudo random bytes
     */
	private byte[] PSHA(byte[] secret, String label, byte[] data,
			int offset, int length) throws ServiceResultException {
        //test parameters
    	if (secret == null) throw new IllegalArgumentException("ArgumentNullException: secret");
        if (offset < 0)     throw new IllegalArgumentException("ArgumentOutOfRangeException: offset");
        if (length < 0)     throw new IllegalArgumentException("ArgumentOutOfRangeException: offset");

        // convert label to UTF-8 byte sequence.
        byte[] seed = label != null && !label.isEmpty() ? label.getBytes(UTF8) : null;

        // append data to label.
        if (data != null && data.length > 0){
            if (seed != null){
            	ByteBuffer buf = ByteBuffer.allocate(seed.length+data.length);
            	buf.put(seed);
            	buf.put(data);
            	buf.rewind();
                seed = buf.array();
            }
            else
            {
                seed = data;
            }
        }

        // check for a valid seed.
        if (seed == null)
        {
           throw new ServiceResultException(StatusCodes.Bad_UnexpectedError, "The PSHA algorithm requires a non-null seed.");
        }

        // create the hmac.
        SecurityPolicy policy = securityConfiguration.getSecurityPolicy();
		Mac hmac = CryptoUtil.createMac(policy.getKeyDerivationAlgorithm(), secret);
        //update data to mac and compute it
        hmac.update(seed);
        byte[] keySeed = hmac.doFinal();
       
        byte[] prfSeed = new byte[hmac.getMacLength() + seed.length];
        
        //Copy keyseed to prfseed from starting point to keyseed.lenght
        System.arraycopy(keySeed, 0, prfSeed, 0, keySeed.length);
        //Copy seed to prfseed, put it after keyseed
        System.arraycopy(seed, 0, prfSeed, keySeed.length, seed.length);
        
                    
        // create buffer with requested size.
        byte[] output = new byte[length];

        int position = 0;

        
        do {
        	//because Mac.doFinal reseted hmac, we must update it again
        	hmac.update(prfSeed);
        	//compute always new hash from prfseed
        	byte[] hash = hmac.doFinal();

            if (offset < hash.length)
            {
                for (int ii = offset; position < length && ii < hash.length; ii++)
                {
                    output[position++] = hash[ii];
                }
            }

            if (offset > hash.length)
            {
                offset -= hash.length;
            }
            else
            {
                offset = 0;
            }

            //calculate hmac from keySeed
            hmac.update(keySeed);
            keySeed = hmac.doFinal();
            System.arraycopy(keySeed, 0, prfSeed, 0, keySeed.length);
            
        }
        while (position < length);

        // return random data.
        return output;
    }

    /**
     * Return security token validity. Security token is still valid if it has expired
     * up to 25% after its lifetime. (See Part 6, 5.5.2.1/3)
     * 
     * @return true if less than 125% of tokens life time has elapsed. 
     */
	public boolean isValid()
	{
		return System.currentTimeMillis() < creationTime + lifetime + (lifetime / 4);
	}
	
	/**
	 * Return security token time to renew status. 
	 * True if 75% of security tokens life-time has elapsed.
	 *  
	 * @return true if 75% of tokens life-time has passed
	 */
	public boolean isTimeToRenew()
	{
		return creationTime + (lifetime * 3) / 4 < System.currentTimeMillis();
	}
	
	/**
	 * Return security tokens expired status.
	 * Token is expired if its 100% of its life time has elapsed. Note, the token
	 * is valid for use until 125% of its life time has passed.  
	 * 
	 * @return true if 100% of security tokens life time has elapsed. 
	 */
	public boolean isExpired()
	{
		return System.currentTimeMillis() >= creationTime + lifetime;
	}

	public SecurityPolicy getSecurityPolicy() {
		return securityConfiguration.getSecurityPolicy();
	}
	
	public SecurityConfiguration getSecurityConfiguration() {
		return securityConfiguration;
	}
	
	public MessageSecurityMode getMessageSecurityMode() {
		return securityConfiguration.getMessageSecurityMode();
	}
	
	public byte[] getLocalSigningKey() {
		return localSigningKey;
	}

	public void setLocalSigningKey(byte[] localSigningKey) {
		this.localSigningKey = localSigningKey;
	}

	public byte[] getLocalEncryptingKey() {
		return localEncryptingKey;
	}

	public void setLocalEncryptingKey(byte[] localEncryptingKey) {
		this.localEncryptingKey = localEncryptingKey;
	}

	public byte[] getLocalInitializationVector() {
		return localInitializationVector;
	}

	public void setLocalInitializationVector(byte[] localInitializationVector) {
		this.localInitializationVector = localInitializationVector;
	}

	public byte[] getRemoteSigningKey() {
		return remoteSigningKey;
	}

	public void setRemoteSigningKey(byte[] remoteSigningKey) {
		this.remoteSigningKey = remoteSigningKey;
	}

	public byte[] getRemoteEncryptingKey() {
		return remoteEncryptingKey;
	}

	public void setRemoteEncryptingKey(byte[] remoteEncryptingKey) {
		this.remoteEncryptingKey = remoteEncryptingKey;
	}

	public byte[] getRemoteInitializationVector() {
		return remoteInitializationVector;
	}

	public void setRemoteInitializationVector(byte[] remoteInitializationVector) {
		this.remoteInitializationVector = remoteInitializationVector;
	}

	/**
	 * Crate new remoteHmac 
	 * 
	 * @return hmac
	 * @throws ServiceResultException 
	 */
	public Mac createRemoteHmac() throws ServiceResultException 
	{
		return createHmac(getRemoteSigningKey());
	}
	
	/**
	 * Create new localHmac 
	 * 
	 * @return hmac
	 * @throws ServiceResultException 
	 */
	public Mac createLocalHmac() throws ServiceResultException 
	{
		return createHmac(getLocalSigningKey());
	}

	/**
	 * @param keySpec
	 * @return
	 * @throws ServiceResultException
	 */
	protected Mac createHmac(byte[] secret) throws ServiceResultException {
		SecurityPolicy policy = securityConfiguration.getSecurityPolicy();
		return CryptoUtil.createMac(policy.getSymmetricSignatureAlgorithm(), secret);
	}
	

	public byte[] getLocalNonce() {
		return localNonce;
	}

	public byte[] getRemoteNonce() {
		return remoteNonce;
	}

	public int getSecureChannelId() {
		return secureChannelId;
	}

	public int getTokenId() {
		return tokenId;
	}
	
	public long getCreationTime()
	{
		return creationTime;
	}
	
	public long getLifeTime()
	{
		return lifetime;
	}
	
	public long getRenewTime()
	{
		return creationTime + ((lifetime *3)/4);
	}
	
	@Override
	public String toString() {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(creationTime);
		return "SecurityToken(Id="+tokenId+", secureChannelId="+secureChannelId+", creationTime="+DateFormat.getDateTimeInstance().format(cal.getTime())+", lifetime="+lifetime+")";
	}
	
}
