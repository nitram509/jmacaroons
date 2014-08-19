package com.github.nitram509jmacaroons;

import java.io.Serializable;

import static com.github.nitram509jmacaroons.MacaroonConstants.IDENTIFIER;
import static com.github.nitram509jmacaroons.MacaroonConstants.LOCATION;
import static com.github.nitram509jmacaroons.MacaroonConstants.SIGNATURE;
import static util.Hex.toHex;

public class M implements Serializable {

  public final String location;
  public final String identifier;
  public final String signature;

  public M(String location, String identifier, byte[] signature) {
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
