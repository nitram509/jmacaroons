package com.github.nitram509jmacaroons;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static util.Hex.toHex;

public class Macaroons {

  public static final Charset ASCII = Charset.forName("ASCII");
  public static final Charset UTF8 = Charset.forName("UTf-8");

  private final String LOCATION = "location";
  private final String IDENTIFIER = "identifier";
  private final String SIGNATURE = "signature";
  private final String CID = "cid";
  private final String VID = "vid";
  private final String CL = "cl";

  public static final int MACAROON_MAX_STRLEN = 32768;
  public static final int MACAROON_MAX_CAVEATS = 65536;
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

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
    String signature = macaroon_create_raw(this.location, key, this.publicKey).signature;
    return signature.substring(signature.indexOf(' ') + 1).trim(); // TODO: very hacky, create a better internal model !
  }

  private byte[] generate_derived_key(String variableKey) throws InvalidKeyException, NoSuchAlgorithmException {
    String MAGIC_KEY = "macaroons-key-generator";
    return macaroon_hmac(MAGIC_KEY.getBytes(ASCII), variableKey).doFinal();
  }

  private M macaroon_create_raw(String location, byte[] key, String id) throws InvalidKeyException, NoSuchAlgorithmException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert id.length() < MACAROON_MAX_STRLEN;
    assert key.length == MACAROON_SUGGESTED_SECRET_LENGTH;

    Mac mac = macaroon_hmac(key, id);

    M m = new M();
    m.location = createLocationPacket(location);
    m.identifier = createIdentifierPacket(id);
    m.signature = createSignaturePacket(toHex(mac.doFinal()));

    return m;
  }

  private Mac macaroon_hmac(byte[] key, String text) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    sha256_HMAC.update(text.getBytes(UTF8));
    return sha256_HMAC;
  }

  private String createLocationPacket(String location) {
    return createKeyValuePacket(LOCATION, location);
  }

  private String createIdentifierPacket(String identifier) {
    return createKeyValuePacket(IDENTIFIER, identifier);
  }

  private String createSignaturePacket(String signature) {
    return createKeyValuePacket(SIGNATURE, signature);
  }

  private String createKeyValuePacket(String key, String value) {
    return key + " " + value + "\n";
  }

}
