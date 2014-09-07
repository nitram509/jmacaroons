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

import static com.github.nitram509.jmacaroons.CryptoTools.*;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.ArrayTools.appendToArray;

/**
 * Used to build Macaroons
 * <pre>
 * String location = "http://www.example.org";
 * String secretKey = "this is our super secret key; only we should know it";
 * String identifier = "we used our secret key";
 * Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
 * </pre>
 */
public class MacaroonsBuilder {

  private String location;
  private String secretKey;
  private String identifier;
  private String[] caveats = new String[0];

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   */
  public MacaroonsBuilder(String location, String secretKey, String identifier) {
    this.location = location;
    this.secretKey = secretKey;
    this.identifier = identifier;
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, String secretKey, String identifier) {
    return new MacaroonsBuilder(location, secretKey, identifier).getMacaroon();
  }

  /**
   * @param serializedMacaroon serializedMacaroon
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid base64, length is to short or contains invalid packet data
   */
  public static Macaroon deserialize(String serializedMacaroon) throws IllegalArgumentException {
    return MacaroonsDeSerializer.deserialize(serializedMacaroon);
  }

  /**
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public Macaroon getMacaroon() throws GeneralSecurityRuntimeException {
    assert this.location.length() < MACAROON_MAX_STRLEN;
    assert this.identifier.length() < MACAROON_MAX_STRLEN;
    try {
      byte[] key = generate_derived_key(this.secretKey);
      byte[] hmac = macaroon_hmac(key, this.identifier);
      for (String caveat : this.caveats) {
        hmac = macaroon_hmac(hmac, caveat);
      }
      return new Macaroon(location, identifier, caveats, hmac);
    } catch (InvalidKeyException e) {
      throw new GeneralSecurityRuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @param macaroon  macaroon
   * @param secretKey secretKey
   * @return {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   */
  public static MacaroonsBuilder modify(Macaroon macaroon, String secretKey) {
    return new MacaroonsBuilder(macaroon.location, secretKey, macaroon.identifier);
  }

  /**
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   */
  public MacaroonsBuilder add_first_party_caveat(String caveat) {
    if (caveat != null) {
      assert caveat.length() < MACAROON_MAX_STRLEN;
      if (this.caveats.length + 1 > MACAROON_MAX_CAVEATS) {
        throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
      }
      this.caveats = appendToArray(this.caveats, caveat);
    }
    return this;
  }

  /**
   * @param location   location
   * @param secret     secret
   * @param identifier identifier
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder add_third_party_caveat(String location, String secret, String identifier) throws GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;

    this.add_first_party_caveat(identifier);

    try {
      byte[] enc_key = generate_derived_key(secret);


      macaroon_add_third_party_caveat_raw(location, enc_key, identifier);

    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  void macaroon_add_third_party_caveat_raw(String location, byte[] key, String identifier) {
    // return macaroon_add_third_party_caveat_raw(N, location, location_sz, derived_key, MACAROON_HASH_BYTES, id, id_sz, err)

    byte[] enc_plaintext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] enc_key = new byte[MACAROON_SECRET_KEY_BYTES];
    byte[] enc_nonce = new byte[MACAROON_SECRET_NONCE_BYTES];
    byte[] enc_ciphertext = new byte[MACAROON_SECRET_TEXT_ZERO_BYTES + MACAROON_HASH_BYTES];
    byte[] vid = new byte[VID_NONCE_KEY_SZ * 3];

    byte[] old_key = getMacaroon().signatureBytes;
    System.arraycopy(key, 0, enc_plaintext, MACAROON_SECRET_TEXT_ZERO_BYTES, MACAROON_HASH_BYTES);

    macaroon_secretbox(enc_key, enc_nonce, enc_plaintext, enc_ciphertext);

    System.arraycopy(enc_nonce, 0, vid, 0, MACAROON_SECRET_NONCE_BYTES);
    System.arraycopy(enc_ciphertext, MACAROON_SECRET_BOX_ZERO_BYTES, vid, MACAROON_SECRET_NONCE_BYTES, VID_NONCE_KEY_SZ - MACAROON_SECRET_NONCE_BYTES);
    byte[] vidAsBase64 = Base64.encodeToByte(vid, 0, VID_NONCE_KEY_SZ, false);
    System.arraycopy(vidAsBase64, 0, vid, VID_NONCE_KEY_SZ, 2 * VID_NONCE_KEY_SZ);

  }

}
