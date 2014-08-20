package com.github.nitram509.jmacaroons;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MacaroonsBuilder {

  private static final String MAGIC_KEY = "macaroons-key-generator";

  private static final Charset ASCII = Charset.forName("ASCII");
  private static final Charset UTF8 = Charset.forName("UTf-8");

  private String location;
  private String secretKey;
  private String identifier;

  public MacaroonsBuilder(String location, String secretKey, String identifier) {
    this.location = location;
    this.secretKey = secretKey;
    this.identifier = identifier;
  }

  public static Macaroon create(String location, String secretKey, String publicKey) {
    try {
      return new MacaroonsBuilder(location, secretKey, publicKey).getM();
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }


  private Macaroon getM() throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] key = generate_derived_key(this.secretKey);
    return macaroon_create_raw(this.location, key, this.identifier);
  }

  private byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MAGIC_KEY.getBytes(ASCII), variableKey);
  }

  private Macaroon macaroon_create_raw(String location, byte[] key, String id) throws InvalidKeyException, NoSuchAlgorithmException {
    assert location.length() < MacaroonsConstants.MACAROON_MAX_STRLEN;
    assert id.length() < MacaroonsConstants.MACAROON_MAX_STRLEN;
    assert key.length == MacaroonsConstants.MACAROON_SUGGESTED_SECRET_LENGTH;

    byte[] hash = macaroon_hmac(key, id);

    return new Macaroon(location, id, hash);
  }

  private byte[] macaroon_hmac(byte[] key, String text) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(text.getBytes(UTF8));
  }

}
