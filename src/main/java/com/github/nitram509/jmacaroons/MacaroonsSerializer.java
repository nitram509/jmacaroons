/*
 * Copyright 2014 Martin W. Kirst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;

import java.nio.charset.Charset;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

class MacaroonsSerializer {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  static final Charset ISO8859 = Charset.forName("ISO8859-1");

  public static String serialize(Macaroon macaroon) {
    String serializedPackets = serialize_packet(createKeyValuePacket(Type.location, macaroon.location))
        + serialize_packet(createKeyValuePacket(Type.identifier, macaroon.identifier))
        + serialize_caveats_packets(macaroon.caveatPackets)
        + serialize_packet(createKeyValuePacket(Type.signature, new String(macaroon.signatureBytes, ISO8859)));
    byte[] serializePacketBytes = serializedPackets.getBytes(ISO8859);
    return Base64.encodeToString(serializePacketBytes, false);
  }

  private static String serialize_caveats_packets(CaveatPacket[] caveats) {
    StringBuilder sb = new StringBuilder();
    for (CaveatPacket caveat : caveats) {
      sb.append(serialize_packet(createKeyValuePacket(Type.cid, caveat.value)));
    }
    return sb.toString();
  }

  private static String serialize_packet(String data) {
    return packet_header(data.length() + PACKET_PREFIX_LENGTH + LINE_SEPARATOR.length()) + data + LINE_SEPARATOR;
  }

  private static String createKeyValuePacket(Type type, String value) {
    return type.name() + KEY_VALUE_SEPARATOR + value;
  }

  private static String packet_header(int size) {
    assert (size < 65536);
    size = (size & 0xffff);
    assert PACKET_PREFIX_LENGTH == 4; /* modify this method on failure */
    return "" + HEX[(size >> 12) & 15]
        + HEX[(size >> 8) & 15]
        + HEX[(size >> 4) & 15]
        + HEX[(size) & 15];
  }

}
