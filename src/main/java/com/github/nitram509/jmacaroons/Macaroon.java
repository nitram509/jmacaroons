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

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;
import static com.github.nitram509.jmacaroons.CryptoTools.string_to_bytes;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.KEY_VALUE_SEPARATOR;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.LINE_SEPARATOR;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_STRLEN;
import static com.github.nitram509.jmacaroons.util.BinHex.bin2hex;

/**
 * <p>
 * Macaroons: Cookies with Contextual Caveats for Decentralized Authorization in the Cloud
 * </p>
 * This is an immutable and serializable object.
 * Use {@link com.github.nitram509.jmacaroons.MacaroonsBuilder} to modify it.
 * Use {@link com.github.nitram509.jmacaroons.MacaroonsVerifier} to verify it.
 *
 * @see <a href="http://research.google.com/pubs/pub41892.html">http://research.google.com/pubs/pub41892.html</a>
 */
public class Macaroon implements Serializable {

  public final String location;
  public final String identifier;
  public final String signature;
  public final CaveatPacket[] caveatPackets;

  final byte[] signatureBytes;

  Macaroon(String location, String identifier, byte[] signature) {
    this(location, identifier, signature, new CaveatPacket[0]);
  }

  Macaroon(String location, String identifier, byte[] signature, CaveatPacket[] caveats) {
    this.location = location;
    this.identifier = identifier;
    this.caveatPackets = caveats;
    this.signature = bin2hex(signature);
    this.signatureBytes = signature;
  }

  Macaroon(String location, byte[] secretKey, String identifier) throws GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;
    this.location = location;
    this.identifier = identifier;
    this.caveatPackets = new CaveatPacket[0];
    try {
      this.signatureBytes = macaroon_hmac(secretKey, identifier);
      this.signature = bin2hex(this.signatureBytes);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  public String inspect() {
    return createKeyValuePacket(Type.location, location)
        + createKeyValuePacket(Type.identifier, identifier)
        + createCaveatsPackets(this.caveatPackets)
        + createKeyValuePacket(Type.signature, signature);
  }

  private String createCaveatsPackets(CaveatPacket[] caveats) {
    if (caveats == null) return "";
    StringBuilder sb = new StringBuilder();
    for (CaveatPacket packet : caveats) {
      sb.append(createKeyValuePacket(packet.type, packet.getValueAsText()));
    }
    return sb.toString();
  }

  private String createKeyValuePacket(Type type, String value) {
    return value != null ? type.name() + KEY_VALUE_SEPARATOR + value + LINE_SEPARATOR : "";
  }

  /**
   * Serializes the macaroon to a string using {@link MacaroonsSerializer#V1}.
   *
   * @return the serialized macaroon.
   * @see #serialize(MacaroonsSerializer)
   */
  public String serialize() {
    return serialize(MacaroonsSerializer.V1);
  }

  /**
   * Serializes the macaroon using the given format.
   *
   * @param format the serialization format.
   * @return the serialized macaroon.
   */
  public String serialize(MacaroonsSerializer format) {
    return format.serialize(this);
  }

  /**
   * Deserializes a macaroon using the {@link MacaroonsSerializer#V1} format.
   *
   * @param serializedMacaroon serializedMacaroon
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid base64, length is to short or contains invalid packet data
   * @see #deserialize(String, MacaroonsSerializer)
   */
  public static Macaroon deserialize(String serializedMacaroon) throws IllegalArgumentException {
    return deserialize(serializedMacaroon, MacaroonsSerializer.V1);
  }

  /**
   * Deserializes a macaroon using the given format.
   *
   * @param serializedMacaroon serializedMacaroon
   * @param format the serialization format.
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid, length is to short or contains invalid packet data
   * @see #deserialize(String, MacaroonsSerializer)
   */
  public static Macaroon deserialize(String serializedMacaroon, MacaroonsSerializer format) throws IllegalArgumentException {
    return format.deserialize(serializedMacaroon);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, String secretKey, String identifier) {
    return create(location, string_to_bytes(secretKey), identifier);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, byte[] secretKey, String identifier) {
    try {
      return new Macaroon(location, generate_derived_key(secretKey), identifier);
    }
    catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  /**
   * @param macaroon macaroon
   * @return {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   */
  public static MacaroonsBuilder builder(Macaroon macaroon) {
    return new MacaroonsBuilder(macaroon);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  public static MacaroonsBuilder builder(String location, byte[] secretKey, String identifier) throws GeneralSecurityRuntimeException {
    return new MacaroonsBuilder(location, secretKey, identifier);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException GeneralSecurityRuntimeException
   */
  public static MacaroonsBuilder builder(String location, String secretKey, String identifier) throws GeneralSecurityRuntimeException {
    return new MacaroonsBuilder(location, secretKey, identifier);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Macaroon macaroon = (Macaroon) o;

    if (!Arrays.equals(caveatPackets, macaroon.caveatPackets)) return false;
    if (!Objects.equals(identifier, macaroon.identifier)) return false;
    if (!Objects.equals(location, macaroon.location)) return false;
    if (!Objects.equals(signature, macaroon.signature)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = location != null ? location.hashCode() : 0;
    result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
    result = 31 * result + (signature != null ? signature.hashCode() : 0);
    result = 31 * result + (caveatPackets != null ? Arrays.hashCode(caveatPackets) : 0);
    return result;
  }
}
