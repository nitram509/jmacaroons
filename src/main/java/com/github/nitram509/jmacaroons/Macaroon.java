package com.github.nitram509.jmacaroons;

import java.io.Serializable;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.Hex.toHex;

public class Macaroon implements Serializable {

  public final String location;
  public final String identifier;
  public final String signature;
  public final String[] caveats;

  final byte[] signatureBytes;

  Macaroon(String location, String identifier, byte[] signature) {
    this(location, identifier, new String[0], signature);
  }

  Macaroon(String location, String identifier, String[] caveats, byte[] signature) {
    this.location = location;
    this.identifier = identifier;
    this.caveats = caveats;
    this.signature = toHex(signature);
    this.signatureBytes = signature;
  }

  public String inspect() {
    return createLocationPacket(location)
        + createIdentifierPacket(identifier)
        + createCaveatsPackets(this.caveats)
        + createSignaturePacket(signature);
  }

  private String createLocationPacket(String location) {
    return createKeyValuePacket(LOCATION, location);
  }

  private String createIdentifierPacket(String identifier) {
    return createKeyValuePacket(MacaroonsConstants.IDENTIFIER, identifier);
  }

  private String createCaveatsPackets(String[] caveats) {
    StringBuilder sb = new StringBuilder();
    for (String caveat : caveats) {
      sb.append(createKeyValuePacket(CID, caveat));
    }
    return sb.toString();
  }

  private String createSignaturePacket(String signature) {
    return createKeyValuePacket(SIGNATURE, signature);
  }

  private String createKeyValuePacket(String key, String value) {
    return key + KEY_VALUE_SEPARATOR + value + LINE_SEPARATOR;
  }

  public String serialize() {
    return MacaroonsSerializer.serialize(this);
  }
}
