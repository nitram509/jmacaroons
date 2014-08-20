package com.github.nitram509.jmacaroons;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class CryptoTools {

  private static final String MAGIC_KEY = "macaroons-key-generator";

  private static final Charset ASCII = Charset.forName("ASCII");
  private static final Charset UTF8 = Charset.forName("UTF-8");

  static byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MAGIC_KEY.getBytes(ASCII), variableKey);
  }

  static Macaroon macaroon_create_raw(String location, byte[] key, String identifier) throws InvalidKeyException, NoSuchAlgorithmException {
    assert location.length() < MacaroonsConstants.MACAROON_MAX_STRLEN;
    assert identifier.length() < MacaroonsConstants.MACAROON_MAX_STRLEN;
    assert key.length == MacaroonsConstants.MACAROON_SUGGESTED_SECRET_LENGTH;

    byte[] hash = macaroon_hmac(key, identifier);

    return new Macaroon(location, identifier, hash);
  }

  static boolean macaroon_verify_raw(Macaroon macaroon, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
    assert key.length == MacaroonsConstants.MACAROON_SUGGESTED_SECRET_LENGTH;
    return macaroon_verify_inner(macaroon, macaroon, key);
  }

  private static boolean macaroon_verify_inner(Macaroon m, Macaroon tm, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] csig = macaroon_hmac(key, m.identifier);
    return Arrays.equals(csig, m.signatureBytes);
  }

  private static byte[] macaroon_hmac(byte[] key, String text) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(text.getBytes(UTF8));
  }
}
