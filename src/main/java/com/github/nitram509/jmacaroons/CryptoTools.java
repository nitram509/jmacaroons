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

class CryptoTools {

  private static final String MAGIC_KEY = "macaroons-key-generator";


  static byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MAGIC_KEY.getBytes(), variableKey);
  }

  static byte[] macaroon_hmac(byte[] key, String text) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(text.getBytes());
  }
}
