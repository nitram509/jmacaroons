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
import java.util.List;

import static com.github.nitram509.jmacaroons.CryptoTools.*;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_CAVEATS;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_STRLEN;

/**
 * Used to build Macaroons, example:
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
  private List<Caveat1stParty> caveats = new ArrayList<Caveat1stParty>(8);

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
   * @return a {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public Macaroon getMacaroon() throws GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;
    List<CaveatPacket> packets = new ArrayList<CaveatPacket>(caveats.size() * 3);
    try {
      byte[] key = generate_derived_key(secretKey);
      byte[] hash = macaroon_hmac(key, identifier);
      for (Caveat1stParty caveat : caveats) {
        if (caveat instanceof Caveat3rdParty) {
          Caveat3rdParty c3 = (Caveat3rdParty) caveat;
          ThirdPartyPacket thirdPartyPacket = macaroon_add_third_party_caveat_raw(hash, c3.secret, c3.identifier);
          hash = thirdPartyPacket.hash;
          packets.add(new CaveatPacket(CaveatPacket.Type.cid, c3.identifier));
          packets.add(new CaveatPacket(CaveatPacket.Type.vid, thirdPartyPacket.vid));
          packets.add(new CaveatPacket(CaveatPacket.Type.cl, c3.location));
        } else {
          hash = macaroon_hmac(hash, caveat.identifier);
          packets.add(new CaveatPacket(CaveatPacket.Type.cid, caveat.identifier));
        }
      }
      return new Macaroon(location, identifier, packets.toArray(new CaveatPacket[packets.size()]), hash);
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
    MacaroonsBuilder builder = new MacaroonsBuilder(macaroon.location, secretKey, macaroon.identifier);
    if (macaroon.caveatPackets != null && macaroon.caveatPackets.length > 0) {
      // TODO ... rework the whole builder ...
//      builder.caveats = macaroon.caveats;
    }
    return builder;
  }

  /**
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws IllegalStateException if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder add_first_party_caveat(String caveat) throws IllegalStateException {
    if (caveat != null) {
      assert caveat.length() < MACAROON_MAX_STRLEN;
      if (this.caveats.size() + 1 > MACAROON_MAX_CAVEATS) {
        throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
      }
      this.caveats.add(new Caveat1stParty(caveat));
    }
    return this;
  }

  /**
   * @param location   location
   * @param secret     secret
   * @param identifier identifier
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws IllegalStateException if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder add_third_party_caveat(String location, String secret, String identifier) throws IllegalStateException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;

    if (this.caveats.size() + 1 > MACAROON_MAX_CAVEATS) {
      throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
    }
    this.caveats.add(new Caveat3rdParty(location, secret, identifier));
    return this;
  }

  private static class Caveat1stParty {
    final String identifier;

    public Caveat1stParty(String caveat) {
      this.identifier = caveat;
    }
  }

  private static class Caveat3rdParty extends Caveat1stParty {
    final String location;
    final String secret;

    private Caveat3rdParty(String location, String secret, String identifier) {
      super(identifier);
      this.location = location;
      this.secret = secret;
    }
  }
}
