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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.CryptoTools.*;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.ArrayTools.appendToArray;
import static com.github.nitram509.jmacaroons.util.ArrayTools.containsElement;

public class MacaroonsVerifier {

  private String[] predicates = new String[0];
  private List<Macaroon> boundMacaroons = new ArrayList<>(3);
  private GeneralCaveatVerifier[] generalCaveatVerifiers = new GeneralCaveatVerifier[0];
  private final Macaroon macaroon;

  public MacaroonsVerifier(Macaroon macaroon) {
    this.macaroon = macaroon;
  }

  /**
   * @param secret secret secret this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @throws com.github.nitram509.jmacaroons.MacaroonValidationException     when the macaroon isn't valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException when the runtime doesn't provide sufficient crypto support
   */
  public void assertIsValid(String secret) throws MacaroonValidationException, GeneralSecurityRuntimeException {
    assertIsValid(string_to_bytes(secret));
  }

  /**
   * @param secret secret this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @throws com.github.nitram509.jmacaroons.MacaroonValidationException     when the macaroon isn't valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException when the runtime doesn't provide sufficient crypto support
   */
  public void assertIsValid(byte[] secret) throws MacaroonValidationException, GeneralSecurityRuntimeException {
    try {
      VerificationResult result = isValid_verify_raw(macaroon, generate_derived_key(secret));
      if (result.fail) {
        String msg = result.failMessage != null ? result.failMessage : "This macaroon isn't valid.";
        throw new MacaroonValidationException(msg, macaroon);
      }
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @param secret secret this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @return true/false if the macaroon is valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public boolean isValid(String secret) throws GeneralSecurityRuntimeException {
    return isValid(string_to_bytes(secret));
  }

  /**
   * @param secret secret this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @return true/false if the macaroon is valid
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public boolean isValid(byte[] secret) throws GeneralSecurityRuntimeException {
    try {
      return !isValid_verify_raw(macaroon, generate_derived_key(secret)).fail;
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  private VerificationResult isValid_verify_raw(Macaroon M, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
    VerificationResult vresult = macaroon_verify_inner(M, secret);
    if (!vresult.fail) {
      vresult.fail = !safeEquals(vresult.csig, getMacaroon().signatureBytes);
      if (vresult.fail) {
        vresult = new VerificationResult("Verification failed. Signature doesn't match. Maybe the key was wrong OR some caveats aren't satisfied.");
      }
    }
    return vresult;
  }

  private VerificationResult macaroon_verify_inner(Macaroon M, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] csig = macaroon_hmac(key, M.identifier);
    if (M.caveatPackets != null) {
      CaveatPacket[] caveatPackets = M.caveatPackets;
      for (int i = 0; i < caveatPackets.length; i++) {
        CaveatPacket caveat = caveatPackets[i];
        if (caveat == null) continue;
        if (caveat.type == Type.cl) continue;
        if (!(caveat.type == Type.cid && caveatPackets[Math.min(i + 1, caveatPackets.length - 1)].type == Type.vid)) {
          if (containsElement(predicates, caveat.getValueAsText()) || verifiesGeneral(caveat.getValueAsText())) {
            csig = macaroon_hmac(csig, caveat.rawValue);
          }
        } else {
          i++;
          CaveatPacket caveat_vid = caveatPackets[i];
          Macaroon boundMacaroon = findBoundMacaroon(caveat.getValueAsText());
          if (boundMacaroon == null) {
            String msg = "Couldn't verify 3rd party macaroon, because no discharged macaroon was provided to the verifier.";
            return new VerificationResult(msg);
          }
          if (!macaroon_verify_inner_3rd(boundMacaroon, caveat_vid, csig)) {
            String msg = "Couldn't verify 3rd party macaroon, identifier= " + boundMacaroon.identifier;
            return new VerificationResult(msg);
          }
          byte[] data = caveat.rawValue;
          byte[] vdata = caveat_vid.rawValue;
          csig = macaroon_hash2(csig, vdata, data);
        }
      }
    }
    return new VerificationResult(csig);
  }

  private boolean macaroon_verify_inner_3rd(Macaroon M, CaveatPacket C, byte[] sig) throws InvalidKeyException, NoSuchAlgorithmException {
    if (M == null) return false;
    byte[] enc_plaintext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] enc_ciphertext = new byte[MACAROON_SECRET_BOX_ZERO_BYTES + MACAROON_HASH_BYTES + SECRET_BOX_OVERHEAD];

    byte[] vid_data = C.rawValue;
    assert vid_data.length == VID_NONCE_KEY_SZ;
    /**
     * the nonce is in the first MACAROON_SECRET_NONCE_BYTES
     * of the vid; the ciphertext is in the rest of it.
     */
    byte[] enc_nonce = new byte[MACAROON_SECRET_NONCE_BYTES];
    System.arraycopy(vid_data, 0, enc_nonce, 0, MACAROON_SECRET_NONCE_BYTES);

    /* fill in the ciphertext */
    System.arraycopy(vid_data, MACAROON_SECRET_NONCE_BYTES, enc_ciphertext, MACAROON_SECRET_BOX_ZERO_BYTES, vid_data.length - MACAROON_SECRET_NONCE_BYTES);
    boolean valid = 0 == macaroon_secretbox_open(sig, enc_nonce, enc_ciphertext, enc_plaintext);

    byte[] key = new byte[MACAROON_HASH_BYTES];
    System.arraycopy(enc_plaintext, MACAROON_SECRET_TEXT_ZERO_BYTES, key, 0, MACAROON_HASH_BYTES);
    VerificationResult vresult = macaroon_verify_inner(M, key);

    byte[] data = getMacaroon().signatureBytes;
    byte[] csig = macaroon_bind(data, vresult.csig);

    return valid && safeEquals(csig, M.signatureBytes);
  }

  private Macaroon findBoundMacaroon(String identifier) {
    for (Macaroon boundMacaroon : boundMacaroons) {
      if (identifier.equals(boundMacaroon.identifier)) {
        return boundMacaroon;
      }
    }
    return null;
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
   * the list of satisfied caveats provided to satisfyExact(String).
   * When it finds a match, it knows that the caveat holds and it can move onto the next caveat in
   * the macaroon.
   *
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsVerifier}
   */
  public MacaroonsVerifier satisfyExact(String caveat) {
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
  public MacaroonsVerifier satisfy3rdParty(Macaroon preparedMacaroon) {
    if (preparedMacaroon != null) {
      this.boundMacaroons.add(preparedMacaroon);
    }
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

  private static class VerificationResult {
    byte[] csig = null;
    boolean fail = false;
    String failMessage = null;

    private VerificationResult(byte[] csig) {
      this.csig = csig;
    }

    private VerificationResult(String failMessage) {
      this.failMessage = failMessage;
      this.fail = true;
    }
  }
}
