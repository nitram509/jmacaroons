/*
 * Copyright © 2017 Coda Hale (coda.hale@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.nitram509.jmacaroons;

import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

/**
 * Encryption and decryption using XSalsa20Poly1305.
 *
 * <p>Compatible with NaCl's {@code box} and {@code secretbox} constructions.
 */
class SecretBox {
  static final int KEY_LEN = 32;
  static final int NONCE_SIZE = 24;
  private final byte[] key;

  /**
   * Create a new {@link SecretBox} instance with the given secret key.
   *
   * @param secretKey a 32-byte secret key
   */
  public SecretBox(byte[] secretKey) {
    if (secretKey.length != KEY_LEN) {
      throw new IllegalArgumentException("secretKey must be 32 bytes long");
    }
    this.key = Arrays.copyOf(secretKey, secretKey.length);
  }

  /**
   * Encrypt a plaintext using the given key and nonce.
   *
   * @param nonce     a 24-byte nonce (cf. {@link #nonce(byte[])})
   * @param plaintext an arbitrary message
   * @return the ciphertext
   */
  public byte[] seal(byte[] nonce, byte[] plaintext) {
    final XSalsa20Engine xsalsa20 = new XSalsa20Engine();
    final Poly1305 poly1305 = new Poly1305();

    // initialize XSalsa20
    xsalsa20.init(true, new ParametersWithIV(new KeyParameter(key), nonce));

    // generate Poly1305 subkey
    final byte[] sk = new byte[KEY_LEN];
    xsalsa20.processBytes(sk, 0, KEY_LEN, sk, 0);

    // encrypt plaintext
    final byte[] out = new byte[plaintext.length + poly1305.getMacSize()];
    xsalsa20.processBytes(plaintext, 0, plaintext.length, out, poly1305.getMacSize());

    // hash ciphertext and prepend mac to ciphertext
    poly1305.init(new KeyParameter(sk));
    poly1305.update(out, poly1305.getMacSize(), plaintext.length);
    poly1305.doFinal(out, 0);

    return out;
  }

  /**
   * Decrypt a ciphertext using the given key and nonce.
   *
   * @param nonce      a 24-byte nonce
   * @param ciphertext the encrypted message
   * @return an {@link Optional} of the original plaintext, or if either the key, nonce, or
   * ciphertext was modified, an empty {@link Optional}
   * @see #nonce(byte[])
   */
  public Optional<byte[]> open(byte[] nonce, byte[] ciphertext) {
    final XSalsa20Engine xsalsa20 = new XSalsa20Engine();
    final Poly1305 poly1305 = new Poly1305();

    // initialize XSalsa20
    xsalsa20.init(false, new ParametersWithIV(new KeyParameter(key), nonce));

    // generate mac subkey
    final byte[] sk = new byte[KEY_LEN];
    xsalsa20.processBytes(sk, 0, sk.length, sk, 0);

    // hash ciphertext
    poly1305.init(new KeyParameter(sk));
    final int len = Math.max(ciphertext.length - poly1305.getMacSize(), 0);
    poly1305.update(ciphertext, poly1305.getMacSize(), len);
    final byte[] calculatedMAC = new byte[poly1305.getMacSize()];
    poly1305.doFinal(calculatedMAC, 0);

    // extract mac
    final byte[] presentedMAC = new byte[poly1305.getMacSize()];
    System.arraycopy(
        ciphertext, 0, presentedMAC, 0, Math.min(ciphertext.length, poly1305.getMacSize()));

    // compare macs
    if (!MessageDigest.isEqual(calculatedMAC, presentedMAC)) {
      return Optional.empty();
    }

    // decrypt ciphertext
    final byte[] plaintext = new byte[len];
    xsalsa20.processBytes(ciphertext, poly1305.getMacSize(), plaintext.length, plaintext, 0);
    return Optional.of(plaintext);
  }

  /**
   * Generates a random nonce which is guaranteed to be unique even if the process's PRNG is
   * exhausted or compromised.
   *
   * <p>Internally, this creates a Blake2b instance with the given key, a random 16-byte salt, and a
   * random 16-byte personalization tag. It then hashes the message and returns the resulting
   * 24-byte digest as the nonce.
   *
   * <p>In the event of a broken or entropy-exhausted {@link SecureRandom} provider, the nonce is
   * essentially equivalent to a synthetic IV and should be unique for any given key/message pair.
   * The result will be deterministic, which will allow attackers to detect duplicate messages.
   *
   * <p>In the event of a compromised {@link SecureRandom} provider, the attacker would need a
   * complete second-preimage attack against Blake2b in order to produce colliding nonces.
   *
   * @param message the message to be encrypted
   * @return a 24-byte nonce
   */
  public byte[] nonce(byte[] message) {
    final byte[] n1 = new byte[16];
    final byte[] n2 = new byte[16];
    final SecureRandom random = new SecureRandom();
    random.nextBytes(n1);
    random.nextBytes(n2);

    final Blake2bDigest blake2b = new Blake2bDigest(key, NONCE_SIZE, n1, n2);
    blake2b.update(message, message.length, 0);

    final byte[] nonce = new byte[NONCE_SIZE];
    blake2b.doFinal(nonce, 0);
    return nonce;
  }
}
