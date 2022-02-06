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

import com.github.nitram509.jmacaroons.util.ArrayTools;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.CryptoTools.*;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_CAVEATS;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_STRLEN;

/**
 * Used to build and modify Macaroons, example:
 * <pre>{@code
 * String location = "http://www.example.org";
 * String secretKey = "this is our super secret key; only we should know it";
 * String identifier = "we used our secret key";
 * Macaroon macaroon = Macaroon.create(location, secretKey, identifier);
 * }</pre>
 */
public class MacaroonsBuilder {

  private Macaroon macaroon;

  /**
   * @deprecated use {@link Macaroon#builder(String, String, String)}
   * @param location   location
   * @param secretKey  secretKey this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  @Deprecated
  public MacaroonsBuilder(String location, String secretKey, String identifier) throws GeneralSecurityRuntimeException {
    try {
      this.macaroon = new Macaroon(location, generate_derived_key(string_to_bytes(secretKey)), identifier);
    }
    catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @deprecated use {@link Macaroon#builder(String, byte[], String)}
   * @param location   location
   * @param secretKey  secretKey this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  @Deprecated
  public MacaroonsBuilder(String location, byte[] secretKey, String identifier) throws GeneralSecurityRuntimeException {
    try {
      this.macaroon = new Macaroon(location, generate_derived_key(secretKey), identifier);
    }
    catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @deprecated use {@link Macaroon#builder(Macaroon)}
   * @param macaroon macaroon to modify
   */
  @Deprecated
  public MacaroonsBuilder(Macaroon macaroon) {
    assert macaroon != null;
    this.macaroon = macaroon;
  }

  /**
   * @deprecated use {@link Macaroon#create(String, String, String)} )}
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  @Deprecated
  public static Macaroon create(String location, String secretKey, String identifier) {
    return Macaroon.create(location, secretKey, identifier);
  }

  /**
   * @deprecated use {@link Macaroon#create(String, byte[], String)} )}
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  @Deprecated
  public static Macaroon create(String location, byte[] secretKey, String identifier) {
    return Macaroon.create(location, secretKey, identifier);
  }

  /**
   * @deprecated use {@link Macaroon#builder(Macaroon)}
   * @param macaroon macaroon
   * @return {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   */
  @Deprecated
  public static MacaroonsBuilder modify(Macaroon macaroon) {
    return Macaroon.builder(macaroon);
  }

  /**
   * Deserializes a macaroon using the {@link MacaroonsSerializer#V1} format.
   *
   * @deprecated use {@link Macaroon#deserialize(String)}
   * @param serializedMacaroon serializedMacaroon
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid base64, length is to short or contains invalid packet data
   * @see #deserialize(String, MacaroonsSerializer)
   */
  @Deprecated
  public static Macaroon deserialize(String serializedMacaroon) throws IllegalArgumentException {
    return Macaroon.deserialize(serializedMacaroon);
  }

  /**
   * Deserializes a macaroon using the given format.
   *
   * @deprecated use {@link Macaroon#deserialize(String, MacaroonsSerializer)}
   * @param serializedMacaroon serializedMacaroon
   * @param format the serialization format.
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid, length is to short or contains invalid packet data
   * @see #deserialize(String, MacaroonsSerializer)
   */
  @Deprecated
  public static Macaroon deserialize(String serializedMacaroon, MacaroonsSerializer format) throws IllegalArgumentException {
    return Macaroon.deserialize(serializedMacaroon, format);
  }

  /**
   * @deprecated use {@link #build()}
   * @return a {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  @Deprecated
  public Macaroon getMacaroon() {
    return build();
  }

  /**
   * @return a {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public Macaroon build() {
    return macaroon;
  }

  /**
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder addCaveat(String caveat) throws IllegalStateException, GeneralSecurityRuntimeException {
    if (caveat != null) {
      byte[] caveatBytes = caveat.getBytes(MacaroonsConstants.IDENTIFIER_CHARSET);
      assert caveatBytes.length < MACAROON_MAX_STRLEN;
      if (this.macaroon.caveatPackets.length + 1 > MACAROON_MAX_CAVEATS) {
        throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
      }
      try {
        byte[] signature = macaroon_hmac(macaroon.signatureBytes, caveatBytes);
        CaveatPacket[] caveatsAppended = ArrayTools.appendToArray(macaroon.caveatPackets, new CaveatPacket(CaveatPacket.Type.cid, caveatBytes));
        this.macaroon = new Macaroon(macaroon.location, macaroon.identifier, signature, caveatsAppended);
      } catch (InvalidKeyException | NoSuchAlgorithmException e) {
        throw new GeneralSecurityRuntimeException(e);
      }
    }
    return this;
  }

  /**
   * @deprecated use {@link #addCaveat(String)}
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  @Deprecated
  public MacaroonsBuilder add_first_party_caveat(String caveat) throws IllegalStateException, GeneralSecurityRuntimeException {
    return addCaveat(caveat);
  }

  /**
   * @param location   location
   * @param secret     secret
   * @param identifier identifier
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder addCaveat(String location, String secret, String identifier) throws IllegalStateException, GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;

    if (this.macaroon.caveatPackets.length + 1 > MACAROON_MAX_CAVEATS) {
      throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
    }
    try {
      byte[] derived_key = generate_derived_key(string_to_bytes(secret));
      ThirdPartyPacket thirdPartyPacket = macaroon_add_third_party_caveat_raw(macaroon.signatureBytes, derived_key, identifier);
      byte[] hash = thirdPartyPacket.signature;
      CaveatPacket[] caveatsExtended = ArrayTools.appendToArray(macaroon.caveatPackets,
          new CaveatPacket(CaveatPacket.Type.cid, identifier),
          new CaveatPacket(CaveatPacket.Type.vid, thirdPartyPacket.vid_data),
          new CaveatPacket(CaveatPacket.Type.cl, location)
      );
      this.macaroon = new Macaroon(macaroon.location, macaroon.identifier, hash, caveatsExtended);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
    return this;
  }

  /**
   * @deprecated use {@link #addCaveat(String, String, String)}
   * @param location   location
   * @param secret     secret
   * @param identifier identifier
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  @Deprecated
  public MacaroonsBuilder add_third_party_caveat(String location, String secret, String identifier) throws IllegalStateException, GeneralSecurityRuntimeException {
    return addCaveat(location, secret, identifier);
  }

  /**
   * @param other macaroon used for preparing a request
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder prepareForRequest(Macaroon other) throws GeneralSecurityRuntimeException {
    assert other.signatureBytes.length > 0;
    assert macaroon.signatureBytes.length > 0;
    try {
      byte[] hash = macaroon_bind(macaroon.signatureBytes, other.signatureBytes);
      this.macaroon = new Macaroon(other.location, other.identifier, hash, other.caveatPackets);
      return this;
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @deprecated use {@link #prepareForRequest(Macaroon)}
   * @param macaroon macaroon used for preparing a request
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  @Deprecated
  public MacaroonsBuilder prepare_for_request(Macaroon macaroon) throws GeneralSecurityRuntimeException {
    return prepareForRequest(macaroon);
  }

}
