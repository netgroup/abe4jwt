/*
 * nimbus-jose-jwt
 *
 * Copyright 2012-2016, Connect2id Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package it.uniroma2.netgroup.abe4jwt.jose;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.jca.JWEJCAContext;
import com.nimbusds.jose.util.Base64URL;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import net.jcip.annotations.ThreadSafe;


@ThreadSafe
public class KPABEDecrypter implements JWEDecrypter {
	
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
	
//	private PrivateKey privateKey;
//	public KPABEDecrypter(final PrivateKey privateKey) {
//		this.privateKey=privateKey;
//	}
//	WORKAROUND: uncomment above when a PrivateKey interface will be available
//	for the time now just use a key encoded as Base64URL 
	private Base64URL privateKey;
	public KPABEDecrypter(final Base64URL privateKey) {
		this.privateKey=privateKey;
	}
	
	public byte[] decrypt(final JWEHeader header, final Base64URL encryptedKey, final Base64URL iv, final Base64URL cipherText, final Base64URL authTag) throws JOSEException {
		if ("KP-ABE".equals(header.getAlgorithm().getName())) {
			try {
				return AbeCryptoFactory.get().decrypt(header, privateKey, encryptedKey, cipherText);
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
}

