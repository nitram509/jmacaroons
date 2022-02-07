/*
 * Copyright 2014 Martin W. Kirst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nitram509.jmacaroons;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.IDENTIFIER_CHARSET;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_HASH_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_SECRET_NONCE_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.STRING_KEY_CHARSET;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.VID_NONCE_KEY_SZ;

class CryptoTools {
  private static final String HMAC_SHA_256_ALGO = "HmacSHA256";
  private static final String MACAROONS_MAGIC_KEY = "macaroons-key-generator";

  private static final Mac HMACSHA256_PROTOTYPE;

  static {
    try {
      HMACSHA256_PROTOTYPE = Mac.getInstance(HMAC_SHA_256_ALGO);
    } catch (NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  static byte[] generate_derived_key(byte[] variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MACAROONS_MAGIC_KEY.getBytes(IDENTIFIER_CHARSET), variableKey);
  }

  static byte[] macaroon_hmac(byte[] key, String message) throws NoSuchAlgorithmException, InvalidKeyException {
    return macaroon_hmac(key, message.getBytes(IDENTIFIER_CHARSET));
  }

  static byte[] string_to_bytes(String message) {
    return message == null
        ? null
        : message.getBytes(STRING_KEY_CHARSET);
  }

  static byte[] macaroon_hmac(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256HMAC = createNewHmacInstance();
    SecretKeySpec secret_key = new SecretKeySpec(key, HMAC_SHA_256_ALGO);
    sha256HMAC.init(secret_key);
    return sha256HMAC.doFinal(message);
  }

  static byte[] macaroon_hash2(byte[] key, byte[] message1, byte[] message2) throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] tmp = new byte[2 * MACAROON_HASH_BYTES];
    System.arraycopy(macaroon_hmac(key, message1), 0, tmp, 0, MACAROON_HASH_BYTES);
    System.arraycopy(macaroon_hmac(key, message2), 0, tmp, MACAROON_HASH_BYTES, MACAROON_HASH_BYTES);
    return macaroon_hmac(key, tmp);
  }

  static ThirdPartyPacket macaroon_add_third_party_caveat_raw(byte[] old_sig, byte[] key, String identifier) throws InvalidKeyException, NoSuchAlgorithmException {
    final SecretBox box = new SecretBox(old_sig);
    final byte[] nonce = box.nonce(key);
    final byte[] ciphertext = box.seal(nonce, key);

    byte[] vid = new byte[VID_NONCE_KEY_SZ];
    System.arraycopy(nonce, 0, vid, 0, MACAROON_SECRET_NONCE_BYTES);
    System.arraycopy(ciphertext, 0, vid, MACAROON_SECRET_NONCE_BYTES, VID_NONCE_KEY_SZ - MACAROON_SECRET_NONCE_BYTES);

    byte[] new_sig = macaroon_hash2(old_sig, vid, identifier.getBytes(IDENTIFIER_CHARSET));
    return new ThirdPartyPacket(new_sig, vid);
  }

  static byte[] macaroon_bind(byte[] Msig, byte[] MPsig) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] key = new byte[MACAROON_HASH_BYTES];
    return macaroon_hash2(key, Msig, MPsig);
  }

  private static Mac createNewHmacInstance() throws NoSuchAlgorithmException {
    try {
      Mac clonedMac = (Mac) HMACSHA256_PROTOTYPE.clone();
      clonedMac.reset();
      return clonedMac;
    } catch (CloneNotSupportedException e) {
      return Mac.getInstance(HMAC_SHA_256_ALGO);
    }
  }

  static class ThirdPartyPacket {
    final byte[] signature;
    final byte[] vid_data;

    ThirdPartyPacket(byte[] signature, byte[] vid_data) {
      this.signature = signature;
      this.vid_data = vid_data;
    }
  }

  /**
   * Use constant time approach, to compare two byte arrays
   * See also
   * <a href="https://codahale.com/a-lesson-in-timing-attacks">A Lesson In Timing Attacks (or, Don’t use MessageDigest.isEquals)</a>
   *
   * @param a an array
   * @param b an array
   * @return true if both have same length and content
   */
  static boolean safeEquals(byte[] a, byte[] b) {
    if (a.length != b.length) {
      return false;
    }

    int result = 0;
    for (int i = 0; i < a.length; i++) {
      result |= a[i] ^ b[i];
    }
    return result == 0;
  }
}
