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

import net.jcip.annotations.ThreadSafe;


@ThreadSafe
public abstract class Encrypter implements JWEEncrypter {
	private final JWEJCAContext jcaContext = new JWEJCAContext();
	private static final Set<JWEAlgorithm> supportedJWE;
	private static final Set<EncryptionMethod> supportedEncs;
	static {
		Set<JWEAlgorithm> algs = new LinkedHashSet<JWEAlgorithm>();
		algs.add(new JWEAlgorithm("CP-ABE", Requirement.OPTIONAL));
		algs.add(new JWEAlgorithm("KP-ABE", Requirement.OPTIONAL));
		supportedJWE = Collections.unmodifiableSet(algs);
		Set<EncryptionMethod> encs = new LinkedHashSet<EncryptionMethod>();
		encs.add(new EncryptionMethod("NONE", Requirement.OPTIONAL, 0));
		supportedEncs = Collections.unmodifiableSet(encs);
	}
	
	//MUST be: a policy for CP-ABE; a set of attributes for KP-ABE. See OpenABE command line interface manual:
	// https://github.com/zeutro/openabe/blob/master/docs/libopenabe-v1.0.0-cli-doc.pdf
	protected final String encInput;
	public Encrypter(String input) {
		this.encInput=input;
	}

	public abstract JWECryptoParts encrypt(final JWEHeader header, final byte[] clearText) throws JOSEException;


	public Set<JWEAlgorithm> supportedJWEAlgorithms() {
		return supportedJWE;
	}


	public Set<EncryptionMethod> supportedEncryptionMethods() {
		return supportedEncs;
	}


	public JWEJCAContext getJCAContext() {
		return jcaContext;
	}
}