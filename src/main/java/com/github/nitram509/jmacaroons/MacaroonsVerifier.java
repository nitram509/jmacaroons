package com.github.nitram509.jmacaroons;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MacaroonsVerifier {

  public boolean verify(Macaroon macaroon, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] key = CryptoTools.generate_derived_key(secret);
    return CryptoTools.macaroon_verify_raw(macaroon, key);
  }
}
