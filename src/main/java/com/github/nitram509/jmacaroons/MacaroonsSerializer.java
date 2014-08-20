package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;

import java.nio.charset.Charset;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

public class MacaroonsSerializer {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  static final Charset ISO8859 = Charset.forName("ISO8859-1");

  public static String serialize(Macaroon macaroon) {
    String serializedPackets = serialize_packet(createLocationPacket(macaroon.location))
        + serialize_packet(createIdentifierPacket(macaroon.identifier))
        + serialize_packet(createSignaturePacket(macaroon.signatureBytes));
    byte[] serializePacketBytes = serializedPackets.getBytes(ISO8859);
    return Base64.encodeToString(serializePacketBytes, false);
  }

  private static String serialize_packet(String data) {
    return packet_header(data.length() + PACKET_PREFIX + LINE_SEPARATOR.length()) + data + LINE_SEPARATOR;
  }

  private static String createLocationPacket(String location) {
    return createKeyValuePacket(LOCATION, location);
  }

  private static String createIdentifierPacket(String identifier) {
    return createKeyValuePacket(MacaroonsConstants.IDENTIFIER, identifier);
  }

  private static String createSignaturePacket(byte[] signature) {
    return createKeyValuePacket(SIGNATURE, new String(signature, ISO8859));
  }

  private static String createKeyValuePacket(String key, String value) {
    return key + KEY_VALUE_SEPARATOR + value;
  }

  private static String packet_header(int size) {
    assert (size < 65536);
    size = (size & 0xffff);
    assert PACKET_PREFIX == 4; /* modify this method on failure */
    return "" + HEX[(size >> 12) & 15]
        + HEX[(size >> 8) & 15]
        + HEX[(size >> 4) & 15]
        + HEX[(size) & 15];
  }

}
