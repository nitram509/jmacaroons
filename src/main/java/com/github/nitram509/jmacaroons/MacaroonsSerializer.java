package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;

import java.nio.charset.Charset;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

class MacaroonsSerializer {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  static final Charset ISO8859 = Charset.forName("ISO8859-1");

  /**
   * @param macaroon
   * @return
   */
  public static String serialize(Macaroon macaroon) {
    String serializedPackets = serialize_packet(createKeyValuePacket(LOCATION, macaroon.location))
        + serialize_packet(createKeyValuePacket(MacaroonsConstants.IDENTIFIER, macaroon.identifier))
        + serialize_caveats_packets(macaroon.caveats)
        + serialize_packet(createKeyValuePacket(SIGNATURE, new String(macaroon.signatureBytes, ISO8859)));
    byte[] serializePacketBytes = serializedPackets.getBytes(ISO8859);
    return Base64.encodeToString(serializePacketBytes, false);
  }

  private static String serialize_caveats_packets(String[] caveats) {
    StringBuilder sb = new StringBuilder();
    for (String caveat : caveats) {
      sb.append(serialize_packet(createKeyValuePacket(CID, caveat)));
    }
    return sb.toString();
  }

  private static String serialize_packet(String data) {
    return packet_header(data.length() + PACKET_PREFIX + LINE_SEPARATOR.length()) + data + LINE_SEPARATOR;
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
