/*

 * Copyright 
 *
 */

package it.uniroma2.netgroup.abe4jwt.jose;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWECryptoParts;
import com.nimbusds.jose.JWEHeader;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import net.jcip.annotations.ThreadSafe;


@ThreadSafe
public class CPABEEncrypter extends Encrypter {

	public CPABEEncrypter(String policy) {
		super(policy);
	}

	public JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText) throws JOSEException {
		if ("CP-ABE".equals(header.getAlgorithm().getName())) {
				try {
					return AbeCryptoFactory.get().encrypt(header, clearText, encInput);
				} catch (Exception e) {
					throw new JOSEException(e.getMessage(),e);
				}
		}
		throw new JOSEException("Algorithm not supported. Must be CP-ABE.");
	}
	
}