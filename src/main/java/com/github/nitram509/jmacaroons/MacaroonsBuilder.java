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
  private Object[] caveats = new Object[0];

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
    List<String> hack = new ArrayList<String>(); // TODO ... better transport caveats
    try {
      byte[] key = generate_derived_key(secretKey);
      byte[] hash = macaroon_hmac(key, identifier);
      for (Object caveat : caveats) {
        if (caveat instanceof Caveat3rdParty) {
          Caveat3rdParty c3 = (Caveat3rdParty) caveat;
          hack.add(c3.identifier);
          hash = macaroon_add_third_party_caveat_raw(hash, c3.secret, c3.identifier);
        } else {
          hash = macaroon_hmac(hash, ((Caveat1stParty) caveat).identifier);
          hack.add(((Caveat1stParty) caveat).identifier);
        }
      }
      return new Macaroon(location, identifier, hack.toArray(new String[0]), hash);
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
    if (macaroon.caveats != null && macaroon.caveats.length > 0) {
      builder.caveats = macaroon.caveats;
    }
    return builder;
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
      this.caveats = appendToArray(this.caveats, new Caveat1stParty(caveat));
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

    if (this.caveats.length + 1 > MACAROON_MAX_CAVEATS) {
      throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
    }
    this.caveats = appendToArray(this.caveats, new Caveat3rdParty(location, secret, identifier));
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
