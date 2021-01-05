/*

 * Copyright 
 *
 */

package it.uniroma2.netgroup.abe4jwt.jose;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWECryptoParts;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.jca.JWEJCAContext;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import net.jcip.annotations.ThreadSafe;


@ThreadSafe
public class KPABEEncrypter implements JWEEncrypter {
	private final JWEJCAContext jcaContext = new JWEJCAContext();
	private static final Set<JWEAlgorithm> supportedJWE;
	private static final Set<EncryptionMethod> supportedEncs;
	static {
		Set<JWEAlgorithm> algs = new LinkedHashSet<JWEAlgorithm>();
		algs.add(new JWEAlgorithm("KP-ABE", Requirement.OPTIONAL));
		supportedJWE = Collections.unmodifiableSet(algs);
		Set<EncryptionMethod> encs = new LinkedHashSet<EncryptionMethod>();
		encs.add(new EncryptionMethod("NONE", Requirement.OPTIONAL, 0));
		supportedEncs = Collections.unmodifiableSet(encs);
	}
	
	private final String attributes;

	public KPABEEncrypter(String attributes) {
		this.attributes=attributes;
	}


	public JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText) throws JOSEException {
		if ("KP-ABE".equals(header.getAlgorithm().getName())) {
				try {
					return AbeCryptoFactory.get().encrypt(header, clearText, attributes);
				} catch (Exception e) {
					throw new JOSEException(e.getMessage(),e);
				}
		}
		throw new JOSEException("Algorithm not supported. Must be KP-ABE.");
	}


	public Set<JWEAlgorithm> supportedJWEAlgorithms() {
		return supportedJWE;
	}


	public Set<EncryptionMethod> supportedEncryptionMethods() {
		return supportedEncs;
	}


	public JWEJCAContext getJCAContext() {
		return jcaContext;
	}
	
	
//	public static JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText) throws JOSEException {
//		// Apply compression if instructed
//		final byte[] plainText = DeflateHelper.applyCompression(header, clearText);
//		return new JWECryptoParts(
//			null,
//			null,
//			null,
//			Base64URL.encode(plainText),
//			null);
//	}
//
//
//	public static byte[] decrypt(final JWEHeader header, final Base64URL encryptedKey, final Base64URL cipherText) throws JOSEException {
//		byte[] plainText=cipherText.decode();
//		// Apply decompression if requested
//		return DeflateHelper.applyDecompression(header, plainText);
//	}
}