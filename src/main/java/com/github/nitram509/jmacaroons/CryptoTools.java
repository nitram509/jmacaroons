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

import com.github.nitram509.jmacaroons.util.Base64;
import com.github.nitram509.jmacaroons.util.StringUtil;
import com.neilalexander.jnacl.crypto.xsalsa20poly1305;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.StringUtil.asString;
import static com.github.nitram509.jmacaroons.util.StringUtil.getBytes;

class CryptoTools {

  private static final String MAGIC_KEY = "macaroons-key-generator";

  static byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(getBytes(MAGIC_KEY), variableKey);
  }

  static byte[] macaroon_hmac(byte[] key, String message) throws NoSuchAlgorithmException, InvalidKeyException {
    return macaroon_hmac(key, getBytes(message));
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

  static ThirdPartyPacket macaroon_add_third_party_caveat_raw(byte[] hash, String key, String identifier) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] derived_key = generate_derived_key(key);

    byte[] enc_plaintext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    System.arraycopy(derived_key, 0, enc_plaintext, MACAROON_SECRET_TEXT_ZERO_BYTES, MACAROON_HASH_BYTES);

    byte[] enc_nonce = new byte[MACAROON_SECRET_NONCE_BYTES]; /* XXX get some random bytes instead */
    byte[] enc_ciphertext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    macaroon_secretbox(hash, enc_nonce, enc_plaintext, enc_ciphertext);

    byte[] vid = new byte[VID_NONCE_KEY_SZ];
    System.arraycopy(enc_nonce, 0, vid, 0, MACAROON_SECRET_NONCE_BYTES);
    System.arraycopy(enc_ciphertext, MACAROON_SECRET_BOX_ZERO_BYTES, vid, MACAROON_SECRET_NONCE_BYTES, VID_NONCE_KEY_SZ - MACAROON_SECRET_NONCE_BYTES);
    byte[] vidAsBase64 = Base64.encodeToByte(vid, 0, VID_NONCE_KEY_SZ, false);

    byte[] hashNew = macaroon_hash2(hash, vidAsBase64, getBytes(identifier));
    return new ThirdPartyPacket(hashNew, asString(vidAsBase64));
  }

  private static void macaroon_secretbox(byte[] key, byte[] nonce, byte[] plaintext, byte[] ciphertext) throws GeneralSecurityRuntimeException {
    int err_code = xsalsa20poly1305.crypto_secretbox(ciphertext, plaintext, plaintext.length, nonce, key);
    if (err_code != 0) {
      throw new GeneralSecurityRuntimeException("Error while creating secret box. err_code=" + err_code);
    }
  }

  static byte[] macaroon_bind(byte[] Msig, byte[] MPsig) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] key = new byte[MACAROON_HASH_BYTES];
    return CryptoTools.macaroon_hash2(key, Msig, MPsig);
  }

  static class ThirdPartyPacket {
    final byte[] hash;
    final String vid;

    ThirdPartyPacket(byte[] hash, String vid) {
      this.hash = hash;
      this.vid = vid;
    }
  }
}
