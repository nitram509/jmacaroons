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

import com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.xsalsa20poly1305;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.xsalsa20poly1305.crypto_secretbox_open;

class CryptoTools {

  private static final String HMAC_SHA_256_ALGO = "HmacSHA256";
  private static final String MACAROONS_MAGIC_KEY = "macaroons-key-generator";

  private static final Mac HMACSHA256_PROTOTYPE;
  private static final SecureRandom SECURE_RANDOM;

  static {
    try {
      HMACSHA256_PROTOTYPE = Mac.getInstance(HMAC_SHA_256_ALGO);
      SECURE_RANDOM = new SecureRandom();
      SECURE_RANDOM.setSeed(System.currentTimeMillis());
    } catch (NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  static byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MACAROONS_MAGIC_KEY.getBytes(IDENTIFIER_CHARSET), variableKey);
  }

  static byte[] macaroon_hmac(byte[] key, String message) throws NoSuchAlgorithmException, InvalidKeyException {
    return macaroon_hmac(key, message.getBytes(IDENTIFIER_CHARSET));
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

  static ThirdPartyPacket macaroon_add_third_party_caveat_raw(byte[] old_sig, String key, String identifier) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] derived_key = generate_derived_key(key);

    byte[] enc_nonce = new byte[MACAROON_SECRET_NONCE_BYTES];
    SECURE_RANDOM.nextBytes(enc_nonce);
    byte[] enc_plaintext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] enc_ciphertext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    /* now encrypt the key to give us vid */
    System.arraycopy(derived_key, 0, enc_plaintext, MACAROON_SECRET_TEXT_ZERO_BYTES, MACAROON_HASH_BYTES);

    macaroon_secretbox(old_sig, enc_nonce, enc_plaintext, enc_ciphertext);

    byte[] vid = new byte[VID_NONCE_KEY_SZ];
    System.arraycopy(enc_nonce, 0, vid, 0, MACAROON_SECRET_NONCE_BYTES);
    System.arraycopy(enc_ciphertext, MACAROON_SECRET_BOX_ZERO_BYTES, vid, MACAROON_SECRET_NONCE_BYTES, VID_NONCE_KEY_SZ - MACAROON_SECRET_NONCE_BYTES);

    byte[] new_sig = macaroon_hash2(old_sig, vid, identifier.getBytes(IDENTIFIER_CHARSET));
    return new ThirdPartyPacket(new_sig, vid);
  }

  static byte[] macaroon_bind(byte[] Msig, byte[] MPsig) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] key = new byte[MACAROON_HASH_BYTES];
    return CryptoTools.macaroon_hash2(key, Msig, MPsig);
  }

  private static void macaroon_secretbox(byte[] key, byte[] nonce, byte[] plaintext, byte[] ciphertext) throws GeneralSecurityRuntimeException {
    int err_code = xsalsa20poly1305.crypto_secretbox(ciphertext, plaintext, plaintext.length, nonce, key);
    if (err_code != 0) {
      throw new GeneralSecurityRuntimeException("Error while creating secret box. err_code=" + err_code);
    }
  }

  static int macaroon_secretbox_open(byte[] enc_key, byte[] enc_nonce, byte[] ciphertext, byte[] plaintext) {
    return crypto_secretbox_open(plaintext, ciphertext, ciphertext.length, enc_nonce, enc_key);
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
}
