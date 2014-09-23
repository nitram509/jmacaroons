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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hash2;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.ArrayTools.appendToArray;
import static com.github.nitram509.jmacaroons.util.ArrayTools.containsElement;
import static com.neilalexander.jnacl.crypto.xsalsa20poly1305.crypto_secretbox_open;
import static java.util.Arrays.fill;

public class MacaroonsVerifier {

  private String[] predicates = new String[0];
  private List<Macaroon> boundMacaroons = new ArrayList<Macaroon>(3);
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
      return isValid_verify_raw(macaroon, key, this.boundMacaroons.toArray(new Macaroon[boundMacaroons.size()]));
    } catch (InvalidKeyException e) {
      throw new GeneralSecurityRuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  private boolean isValid_verify_raw(Macaroon M, byte[] key, Macaroon[] MS) throws NoSuchAlgorithmException, InvalidKeyException {
    int MS_sz = MS.length;
    int[] tree = new int[MS_sz];
    fill(tree, MS_sz);
    byte[] hmac = macaroon_verify_inner(M, M, key, MS, tree, 0);
    return Arrays.equals(hmac, M.signatureBytes);
  }

  private byte[] macaroon_verify_inner(Macaroon M, Macaroon TM, byte[] key, Macaroon[] MS, int[] tree, int tree_idx) throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] hmac = macaroon_hmac(key, M.identifier);
    if (M.caveatPackets != null) {
      CaveatPacket[] caveatPackets = M.caveatPackets;
      for (int i = 0; i < caveatPackets.length; i++) {
        CaveatPacket caveat = caveatPackets[i];
        if (caveat == null) continue;
        if (caveat.type == Type.cl) continue; // todo: 99.9% yes --> make 100% sure ;-)
        if (!(caveat.type == Type.cid && caveatPackets[Math.min(i + 1, caveatPackets.length - 1)].type == Type.vid)) {
          if (containsElement(predicates, caveat.value) || verifiesGeneral(caveat.value)) {
            hmac = macaroon_hmac(hmac, caveat.value);
          }
        } else {
          i++;
          CaveatPacket caveat_vid = caveatPackets[i];
          /*fail |= */macaroon_verify_inner_3rd(caveat, caveat_vid, hmac, TM, MS, tree, tree_idx);

          byte[] tmp = hmac;
          String data = caveat.value;
          String vdata = caveat_vid.value;
          hmac = macaroon_hash2(tmp, vdata.getBytes(IDENTIFIER_CHARSET), data.getBytes(IDENTIFIER_CHARSET));
        }
      }
    }

    if (tree_idx > 0) {
      byte[] tmp = hmac;
      byte[] data = TM.signatureBytes;
      hmac = macaroon_bind(data, tmp, hmac);
    }

    return hmac;
  }

  private byte[] macaroon_bind(byte[] Msig, byte[] MPsig, byte[] bound) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hash2(bound, MPsig, MPsig);
  }

  private byte[] macaroon_verify_inner_3rd(CaveatPacket C_cid, CaveatPacket C_vid, byte[] sig, Macaroon TM, Macaroon[] MS, int[] tree, int tree_idx) throws InvalidKeyException, NoSuchAlgorithmException {


    byte[] enc_key = new byte[MACAROON_SECRET_KEY_BYTES];
    byte[] enc_nonce = new byte[MACAROON_SECRET_NONCE_BYTES];
    byte[] enc_plaintext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] enc_ciphertext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] b64_enc = new byte[VID_NONCE_KEY_SZ * 2 + 1];
    byte[] b64_dec = new byte[VID_NONCE_KEY_SZ];

    int fail = 0;

    String cav = C_cid.value;

    int MS_sz = MS.length;
    for (int midx = 0; midx < MS_sz; ++midx) {
      String mac = MS[midx].identifier;
//      int sz = cav.length() < mac.length() ? cav.length() : mac.length();
      if (mac.equals(cav)) {
        tree[tree_idx] = midx;
      }
      for (int tidx = 0; tidx < tree_idx; ++tidx) {
        fail |= (tree[tidx] == tree[tree_idx]) ? 0 : 1; // todo: ord(boolean) -> int ???
      }
    }

    byte[] key = sig;
    if (tree[tree_idx] < MS_sz) {
//        /* zero everything */
      // ...
//        /* extract VID from base64 */

      String vidData = C_vid.value;
      assert vidData.length() == VID_NONCE_KEY_SZ * 4 / 3; // todo: how is this possible????

      b64_dec = Base64.decode(vidData);
      /* fill in the key */
      System.arraycopy(sig, 0, enc_key, 0, MACAROON_HASH_BYTES);
      /* fill in the nonce */
      System.arraycopy(b64_dec, 0, enc_nonce, 0, MACAROON_SECRET_NONCE_BYTES);
      /* fill in the ciphertext */
      System.arraycopy(b64_dec, MACAROON_SECRET_NONCE_BYTES, enc_ciphertext, MACAROON_SECRET_BOX_ZERO_BYTES, VID_NONCE_KEY_SZ - MACAROON_SECRET_NONCE_BYTES);
      /* now get the plaintext */
      fail |= macaroon_secretbox_open(enc_key, enc_nonce, enc_ciphertext, MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES, enc_plaintext);

      key = new byte[MACAROON_HASH_BYTES];
      System.arraycopy(enc_plaintext, MACAROON_SECRET_TEXT_ZERO_BYTES, key, 0, MACAROON_HASH_BYTES);
      /*fail |= */macaroon_verify_inner(MS[tree[tree_idx]], TM, key, MS, tree, tree_idx + 1);
    }

//    assert fail == 0;

    return key;
  }

  private static int macaroon_secretbox_open(byte[] enc_key, byte[] enc_nonce, byte[] ciphertext, int ciphertext_sz, byte[] plaintext) {
    return crypto_secretbox_open(plaintext, ciphertext, ciphertext_sz, enc_nonce, enc_key);
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
}
