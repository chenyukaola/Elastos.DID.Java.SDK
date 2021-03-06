/*
 * Copyright (c) 2019 Elastos Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.elastos.did.jwt;

import java.security.Key;
import java.security.PublicKey;
import java.util.Date;

import org.elastos.did.DID;
import org.elastos.did.DIDDocument;
import org.elastos.did.DIDURL;
import org.elastos.did.exception.InvalidKeyException;
import org.elastos.did.exception.MalformedDIDException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;

/**
 * A builder for parsing JWT.
 */
public class JwtParserBuilder {
	private io.jsonwebtoken.JwtParserBuilder impl;
	private KeyProvider keyProvider;

	/**
	 * Constructs a default JwtParserBuilder instance.
	 */
	public JwtParserBuilder() {
		this(null);
	}

	/**
	 * Constructs a JwtParserBuilder instance with the specific KeyProvider.
	 *
	 * @param keyProvider the KeyProvider interface
	 */
	public JwtParserBuilder(KeyProvider keyProvider) {
		this.keyProvider = keyProvider;
		this.impl = Jwts.parserBuilder();

		this.impl.setSigningKeyResolver(new SigningKeyResolver() {
			@SuppressWarnings("rawtypes")
			@Override
			public Key resolveSigningKey(io.jsonwebtoken.JwsHeader header,
					io.jsonwebtoken.Claims claims) {
				String keyid = header.getKeyId();

				try {
					if (keyProvider != null)
						return keyProvider.getPublicKey(keyid);
					else {
						DID did = new DID(claims.getIssuer());
						DIDDocument doc = did.resolve();
						if (doc == null)
							throw new DIDResolveException("Can not resolve the iss's DID");

						return doc.getKeyPair(DIDURL.valueOf(doc.getSubject(), keyid)).getPublic();
					}
				} catch (InvalidKeyException e) {
					throw new InvalidSignKeyException("Invalid sign key", e);
				} catch (MalformedDIDException e) {
					throw new IllegalArgumentException("iss field is not a valid DID", e);
				} catch (org.elastos.did.exception.DIDResolveException e) {
					throw new DIDResolveException("Failed to resolve the iss's DID", e);
				}
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Key resolveSigningKey(io.jsonwebtoken.JwsHeader header,
					String plaintext) {
				try {
					if (keyProvider != null)
						return keyProvider.getPublicKey(header.getKeyId());
					else
						return null;
				} catch (Exception e) {
					return null;
				}
			}
		});
	}

	/**
	 * Ensures that the specified {@code jti} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param id the required id string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireId(String id) {
		impl.requireId(id);
		return this;
	}

	/**
	 * Ensures that the specified {@code sub} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param subject the require subject string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireSubject(String subject) {
		impl.requireSubject(subject);
		return this;
	}

	/**
	 * Ensures that the specified {@code aud} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param audience the audience string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireAudience(String audience) {
		impl.requireAudience(audience);
		return this;
	}

	/**
	 * Ensures that the specified {@code iss} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param issuer the issuer string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireIssuer(String issuer) {
		impl.requireIssuer(issuer);
		return this;
	}

	/**
	 * Ensures that the specified {@code iat} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param issuedAt the issued place string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireIssuedAt(Date issuedAt) {
		impl.requireIssuedAt(issuedAt);
		return this;
	}

	/**
	 * Ensures that the specified {@code exp} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param expiration the expires time
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder requireExpiration(Date expiration) {
		impl.requireExpiration(expiration);
		return this;
	}

	/**
	 * Ensures that the specified {@code nbf} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param notBefore the time not before
	 * @return the parser builder for method chaining
	 */
	public JwtParserBuilder requireNotBefore(Date notBefore) {
		impl.requireNotBefore(notBefore);
		return this;
	}

	/**
	 * Ensures that the specified {@code claimName} exists in the parsed JWT. If
	 * missing or if the parsed value does not equal the specified value, an
	 * exception will be thrown indicating that the JWT is invalid and may not
	 * be used.
	 *
	 * @param claimName the claim name string
	 * @param value the claim value string
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder require(String claimName, Object value) {
		impl.require(claimName, value);
		return this;
	}

	/**
	 * Sets the amount of clock skew in seconds to tolerate when verifying the
	 * local time against the {@code exp} and {@code nbf} claims.
	 *
	 * @param seconds the number of seconds to tolerate for clock skew when
	 *                verifying {@code exp} or {@code nbf} claims.
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder setAllowedClockSkewSeconds(long seconds) {
		impl.setAllowedClockSkewSeconds(seconds);
		return this;
	}

	/**
	 * Sets the signing key id used to verify any discovered JWS digital
	 * signature. If the specified JWT string is not a JWS (no signature), this
	 * key is not used.
	 *
	 * Note that this key <em>MUST</em> be a valid key for the signature
	 * algorithm found in the JWT header (as the {@code alg} header parameter).
	 *
	 * This method overwrites any previously set key.
	 *
	 * @param key the algorithm-specific signature verification key id to use to
	 *            validate any discovered JWS digital signature.
	 * @return the parser builder for method chaining.
	 */
	public JwtParserBuilder setSigningKey(String key) {
		PublicKey pk = keyProvider.getPublicKey(key);
		impl.setSigningKey(pk);
		return this;
	}

	/**
	 * Returns an immutable/thread-safe {@link JwtParser} created from the
	 * configuration from this JwtParserBuilder.
	 *
	 * @return an immutable/thread-safe JwtParser created from the configuration
	 *         from this JwtParserBuilder.
	 */
	public JwtParser build() {
		return new JwtParser(impl.build());
	}
}
