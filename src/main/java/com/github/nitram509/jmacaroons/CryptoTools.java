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

import com.neilalexander.jnacl.crypto.xsalsa20poly1305;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_HASH_BYTES;

class CryptoTools {

  private static final String MAGIC_KEY = "macaroons-key-generator";

  private static final Charset UTF8 = Charset.forName("UTF-8");

  static byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MAGIC_KEY.getBytes(UTF8), variableKey);
  }

  static byte[] macaroon_hmac(byte[] key, String message) throws NoSuchAlgorithmException, InvalidKeyException {
    return macaroon_hmac(key, message.getBytes(UTF8));
  }

  static byte[] macaroon_hmac(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(message);
  }

  static byte[] macaroon_hash2(byte[] key, byte[] message1, byte[] message2) throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] tmp = new byte[2 * MACAROON_HASH_BYTES];
    System.arraycopy(macaroon_hmac(key, message1), 0, tmp, 0, MACAROON_HASH_BYTES);
    System.arraycopy(macaroon_hmac(key, message2), 0, tmp, MACAROON_HASH_BYTES, MACAROON_HASH_BYTES);
    return macaroon_hmac(key, tmp);
  }

  public static void macaroon_secretbox(byte[] key, byte[] nonce, byte[] plaintext, byte[] ciphertext) throws GeneralSecurityRuntimeException {
    int err_code = xsalsa20poly1305.crypto_secretbox(ciphertext, plaintext, plaintext.length, nonce, key);
    if (err_code != 0) {
      throw new GeneralSecurityRuntimeException("Error while creating secret box. err_code=" + err_code);
    }
  }
}
