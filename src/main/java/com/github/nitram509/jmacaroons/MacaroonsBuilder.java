package com.github.nitram509.jmacaroons;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_CAVEATS;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_STRLEN;

public class MacaroonsBuilder {

  private String location;
  private String secretKey;
  private String identifier;
  private String[] caveats = new String[0];

  public MacaroonsBuilder(String location, String secretKey, String identifier) {
    this.location = location;
    this.secretKey = secretKey;
    this.identifier = identifier;
  }

  public static Macaroon create(String location, String secretKey, String identifier) {
    return new MacaroonsBuilder(location, secretKey, identifier).getMacaroon();
  }

  public Macaroon getMacaroon() {
    assert this.location.length() < MACAROON_MAX_STRLEN;
    assert this.identifier.length() < MACAROON_MAX_STRLEN;
    try {
      byte[] key = generate_derived_key(this.secretKey);
      byte[] hmac = macaroon_hmac(key, this.identifier);
      for (String caveat : this.caveats) {
        hmac = macaroon_hmac(hmac, caveat);
      }
      return new Macaroon(location, identifier, caveats, hmac);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static MacaroonsBuilder modify(Macaroon m, String secretKey) {
    return new MacaroonsBuilder(m.location, secretKey, m.identifier);
  }

  public MacaroonsBuilder add_first_party_caveat(String predicate) {
    if (predicate == null) return this;
    assert predicate.length() < MACAROON_MAX_STRLEN;
    if (caveats.length + 1 > MACAROON_MAX_CAVEATS) {
      throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
    }
    this.caveats = append(this.caveats, predicate);
    return this;
  }

  private static String[] append(String[] stringArray, String predicate) {
    String[] tmp = new String[stringArray.length + 1];
    System.arraycopy(stringArray, 0, tmp, 0, stringArray.length);
    tmp[stringArray.length] = predicate;
    return tmp;
  }

}
