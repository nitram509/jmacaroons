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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;
import static com.github.nitram509.jmacaroons.util.ArrayTools.appendToArray;
import static com.github.nitram509.jmacaroons.util.ArrayTools.containsElement;

public class MacaroonsVerifier {

  private String[] predicates = new String[0];
  private GeneralCaveatVerifier[] generalCaveatVerifiers = new GeneralCaveatVerifier[0];
  private final Macaroon macaroon;

  public MacaroonsVerifier(Macaroon macaroon) {
    this.macaroon = macaroon;
  }

  /**
   * @param secret secret
   * @throws com.github.nitram509.jmacaroons.MacaroonValidationException     when the macaroon isn't valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public void assertIsValid(String secret) throws MacaroonValidationException, GeneralSecurityRuntimeException {
    if (!isValid(secret)) {
      throw new MacaroonValidationException("This macaroon isn't valid.", macaroon);
    }
  }

  /**
   * @param secret secret
   * @return true/false if the macaroon is valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public boolean isValid(String secret) throws GeneralSecurityRuntimeException {
    try {
      byte[] key = generate_derived_key(secret);
      byte[] hmac = macaroon_hmac(key, macaroon.identifier);
      if (macaroon.caveatPackets != null) {
        for (CaveatPacket caveat : macaroon.caveatPackets) {
          if (caveat != null) {
            if (containsElement(predicates, caveat.value) || verifiesGeneral(caveat.value)) {
              hmac = macaroon_hmac(hmac, caveat.value);
            }
          }
        }
      }
      return Arrays.equals(hmac, macaroon.signatureBytes);
    } catch (InvalidKeyException e) {
      throw new GeneralSecurityRuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  private boolean verifiesGeneral(String caveat) {
    boolean found = false;
    for (GeneralCaveatVerifier verifier : this.generalCaveatVerifiers) {
      found |= verifier.verifyCaveat(caveat);
    }
    return found;
  }

  /**
   * Caveats like these are called "exact caveats" because there is exactly one way
   * to satisfy them.  Either the given caveat matches, or it doesn't.  At
   * verification time, the verifier will check each caveat in the macaroon against
   * the list of satisfied caveats provided to satisfyExcact(String).
   * When it finds a match, it knows that the caveat holds and it can move onto the next caveat in
   * the macaroon.
   *
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsVerifier}
   */
  public MacaroonsVerifier satisfyExcact(String caveat) {
    if (caveat != null) {
      this.predicates = appendToArray(this.predicates, caveat);
    }
    return this;
  }

  /**
   * Binds a prepared macaroon.
   *
   * @param preparedMacaroon preparedMacaroon
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsVerifier}
   */
  public MacaroonsVerifier bind(Macaroon preparedMacaroon) {
    return this;
  }

  /**
   * Another technique for informing the verifier that a caveat is satisfied
   * allows for expressive caveats. Whereas exact caveats are checked
   * by simple byte-wise equality, general caveats are checked using
   * an application-provided callback that returns true if and only if the caveat
   * is true within the context of the request.
   * There's no limit on the contents of a general caveat,
   * so long as the callback understands how to determine whether it is satisfied.
   * This technique is called "general caveats".
   *
   * @param verifier verifier
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsVerifier}
   */
  public MacaroonsVerifier satisfyGeneral(GeneralCaveatVerifier verifier) {
    if (verifier != null) {
      this.generalCaveatVerifiers = appendToArray(this.generalCaveatVerifiers, verifier);
    }
    return this;
  }

  public Macaroon getMacaroon() {
    return macaroon;
  }
}
