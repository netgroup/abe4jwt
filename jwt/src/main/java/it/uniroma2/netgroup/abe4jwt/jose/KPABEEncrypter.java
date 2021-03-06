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
public class KPABEEncrypter extends Encrypter {

	public KPABEEncrypter(String attributes) {
		super(attributes);
	}

	public JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText) throws JOSEException {
		if ("KP-ABE".equals(header.getAlgorithm().getName())) {
				try {
					return AbeCryptoFactory.get().encrypt(header, clearText, encInput);
				} catch (Exception e) {
					throw new JOSEException(e.getMessage(),e);
				}
		}
		throw new JOSEException("Algorithm not supported. Must be KP-ABE.");
	}

}