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


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.util.Base64URL;

import it.uniroma2.netgroup.abe4jwt.crypto.AbeCryptoFactory;
import net.jcip.annotations.ThreadSafe;


@ThreadSafe
public class CPABEDecrypter extends Decrypter {
	
	public CPABEDecrypter(final Base64URL privateKey) {
		super(privateKey);
	}
	
	public byte[] decrypt(final JWEHeader header, final Base64URL encryptedKey, final Base64URL iv, final Base64URL cipherText, final Base64URL authTag) throws JOSEException {
		if ("CP-ABE".equals(header.getAlgorithm().getName())) {
			try {
				return AbeCryptoFactory.get().decrypt(header, privateKey, encryptedKey, cipherText);
			} catch (Exception e) {
				throw new JOSEException(e.getMessage(),e);
			}
		}
		throw new JOSEException("Algorithm not supported. Must be CP-ABE.");
	}
}

