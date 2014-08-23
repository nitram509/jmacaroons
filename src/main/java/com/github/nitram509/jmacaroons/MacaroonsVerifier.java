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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;

public class MacaroonsVerifier {

  /**
   * @param macaroon
   * @param secret
   * @return
   */
  public boolean verify(Macaroon macaroon, String secret) {
    try {
      byte[] key = generate_derived_key(secret);
      byte[] hmac = macaroon_hmac(key, macaroon.identifier);
      for (String caveat : macaroon.caveats) {
        hmac = macaroon_hmac(hmac, caveat);
      }
      return Arrays.equals(hmac, macaroon.signatureBytes);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
