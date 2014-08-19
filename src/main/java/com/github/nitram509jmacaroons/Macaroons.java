package com.github.nitram509jmacaroons;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Macaroons {

  private static final String MAGIC_KEY = "macaroons-key-generator";

  private static final Charset ASCII = Charset.forName("ASCII");
  private static final Charset UTF8 = Charset.forName("UTf-8");

  private String location;
  private String secretKey;
  private String publicKey;

  public static Macaroons create(String location, String secretKey, String publicKey) {
    return new Macaroons(location, secretKey, publicKey);
  }

  public Macaroons(String location, String secretKey, String publicKey) {
    this.location = location;
    this.secretKey = secretKey;
    this.publicKey = publicKey;
  }

  public String getLocation() {
    return location;
  }

  public String getSignature() throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] key = generate_derived_key(this.secretKey);
    return macaroon_create_raw(this.location, key, this.publicKey).signature;
  }

  private byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    return macaroon_hmac(MAGIC_KEY.getBytes(ASCII), variableKey);
  }

  private M macaroon_create_raw(String location, byte[] key, String id) throws InvalidKeyException, NoSuchAlgorithmException {
    assert location.length() < MacaroonConstants.MACAROON_MAX_STRLEN;
    assert id.length() < MacaroonConstants.MACAROON_MAX_STRLEN;
    assert key.length == MacaroonConstants.MACAROON_SUGGESTED_SECRET_LENGTH;

    byte[] hash = macaroon_hmac(key, id);

    return new M(location, id, hash);
  }

  private byte[] macaroon_hmac(byte[] key, String text) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(text.getBytes(UTF8));
  }

}
