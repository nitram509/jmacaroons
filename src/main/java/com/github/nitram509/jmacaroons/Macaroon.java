package com.github.nitram509.jmacaroons;

import java.io.Serializable;

import static com.github.nitram509.jmacaroons.util.Hex.toHex;

public class Macaroon implements Serializable {

  private static final String LINE_SEPARATOR = "\n";
  private static final String KEY_VALUE_SEPARATOR = " ";

  public final String location;
  public final String identifier;
  public final String signature;

  public Macaroon(String location, String identifier, byte[] signature) {
    this.location = location;
    this.identifier = identifier;
    this.signature = toHex(signature);
  }

  public String inspect() {
    return createLocationPacket(location)
        + createIdentifierPacket(identifier)
        + createSignaturePacket(signature);
  }

  private String createLocationPacket(String location) {
    return createKeyValuePacket(MacaroonsConstants.LOCATION, location);
  }

  private String createIdentifierPacket(String identifier) {
    return createKeyValuePacket(MacaroonsConstants.IDENTIFIER, identifier);
  }

  private String createSignaturePacket(String signature) {
    return createKeyValuePacket(MacaroonsConstants.SIGNATURE, signature);
  }

  private String createKeyValuePacket(String key, String value) {
    return key + KEY_VALUE_SEPARATOR + value + LINE_SEPARATOR;
  }
}
