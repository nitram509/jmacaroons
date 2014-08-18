package com.github.nitram509jmacaroons;

public class Macaroons {

  private String location;
  private String secretKey;
  private String publicKey;

  public static Macaroons create(String location, String secretKey, String publicKey) {
    Macaroons m = new Macaroons();
    m.location = location;
    m.secretKey = secretKey;
    m.publicKey = publicKey;
    return m;
  }

  public String getLocation() {
    return location;
  }

  public String getSignature() {
    return null;
  }
}
