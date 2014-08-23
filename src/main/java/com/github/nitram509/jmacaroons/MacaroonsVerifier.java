package com.github.nitram509.jmacaroons;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.github.nitram509.jmacaroons.CryptoTools.generate_derived_key;
import static com.github.nitram509.jmacaroons.CryptoTools.macaroon_hmac;

public class MacaroonsVerifier {

  public boolean verify(Macaroon macaroon, String secret) {
    try {
      byte[] key = generate_derived_key(secret);
      byte[] hmac = macaroon_hmac(key, macaroon.identifier);
      for (String caveat : macaroon.caveats) {
        hmac = macaroon_hmac(hmac, caveat);
      }
      return Arrays.equals(hmac, macaroon.signatureBytes);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
