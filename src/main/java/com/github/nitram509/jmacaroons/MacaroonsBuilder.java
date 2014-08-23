package com.github.nitram509.jmacaroons;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MacaroonsBuilder {

  private String location;
  private String secretKey;
  private String identifier;

  public MacaroonsBuilder(String location, String secretKey, String identifier) {
    this.location = location;
    this.secretKey = secretKey;
    this.identifier = identifier;
  }

  public static Macaroon create(String location, String secretKey, String identifier) {
    try {
      return new MacaroonsBuilder(location, secretKey, identifier).getM();
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private Macaroon getM() throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] key = CryptoTools.generate_derived_key(this.secretKey);
    return CryptoTools.macaroon_create_raw(this.location, key, this.identifier);
  }

}
