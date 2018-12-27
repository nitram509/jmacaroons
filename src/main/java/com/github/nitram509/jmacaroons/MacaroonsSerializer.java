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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nitram509.jmacaroons.util.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;
import static com.github.nitram509.jmacaroons.util.ArrayTools.flattenByteArray;

class MacaroonsSerializer {

  private static final byte[] HEX = new byte[]{
          '0', '1', '2', '3',
          '4', '5', '6', '7',
          '8', '9', 'a', 'b',
          'c', 'd', 'e', 'f'};

  private static ObjectMapper mapper = new ObjectMapper();

  /**
   * Serialize a given {@link Macaroon} in the {@link MacaroonVersion#V1_BINARY} format.
   * This is the default method from the original jmacaroons implementation.
   * Users will most likely want to call {@link MacaroonsSerializer#serialize(Macaroon, MacaroonVersion)} with the desired format.
   *
   * @param macaroon - {@link Macaroon} to serialize.
   * @return - Base64 encoded {@link String} representation of the given {@link Macaroon}.
   */
  public static String serialize(Macaroon macaroon) {
    return serialize(macaroon, MacaroonVersion.V1_BINARY);
  }

  /**
   * Serialize a given {@link Macaroon} in the specified {@link MacaroonVersion#V1_BINARY} format.
   *
   * @param macaroon - {@link Macaroon} to serialize.
   * @param version - {@link MacaroonVersion} to use for serializing.
   * @return - Base64 encoded {@link String} representation of the given {@link Macaroon}.
   */
  public static String serialize(Macaroon macaroon, MacaroonVersion version) {
    switch (version) {
      case V1_BINARY:
        return serializeV1Binary(macaroon);
      case V2_JSON:
        return serializeV2JSON(macaroon);
      default:
        throw new IllegalArgumentException(String.format("Cannot serialize to version: %s", version));
    }
  }

    private static String serializeV2JSON(Macaroon macaroon) {

        final MacaroonJSONV2 serialized = new MacaroonJSONV2();

        // For each element determine whether or not we need to base64 encode the string, before serializing it.
        serialized.setLocation(macaroon.location);

        // Identifier
        if (validUTF8(macaroon.identifier.getBytes())) {
            serialized.setIdentifier(macaroon.identifier);
        } else {
            serialized.setIdentifier64(Base64.encodeUrlSafeToString(macaroon.identifier.getBytes()));
        }

        // Signature
        if (validUTF8(macaroon.signatureBytes)) {
            serialized.setSignature(macaroon.signature);
        } else {
            serialized.setSignature64(Base64.encodeUrlSafeToString(macaroon.signatureBytes));
        }

        // Caveats
        // Initialize the caveat array
//      A caveat can have up to three packets.
//      First-party caveats only have a cid field.
//      Third-party caveats can have an empty location.

        if (macaroon.caveatPackets.length > 0) {
            final List<MacaroonJSONV2.CaveatJSONV2> caveats = serializeCaveatsToJSON(macaroon.caveatPackets);
            serialized.setCaveats(caveats);
        }

        try {
            return Base64.encodeUrlSafeToString(mapper.writeValueAsBytes(serialized));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

  private static String serializeV1Binary(Macaroon macaroon) {
    List<byte[]> packets = new ArrayList<>( 3 + macaroon.caveatPackets.length );
    packets.add(serialize_packet(Type.location, macaroon.location));
    packets.add(serialize_packet(Type.identifier, macaroon.identifier));
    for (CaveatPacket caveatPacket : macaroon.caveatPackets) {
      packets.add(serialize_packet(caveatPacket.type, caveatPacket.rawValue));
    }
    packets.add(serialize_packet(Type.signature, macaroon.signatureBytes));
    return Base64.encodeUrlSafeToString(flattenByteArray(packets));
  }

  private static byte[] serialize_packet(Type type, String data) {
    return serialize_packet(type, data.getBytes(IDENTIFIER_CHARSET));
  }

  private static byte[] serialize_packet(Type type, byte[] data) {
    String typname = type.name();
    int packet_len = PACKET_PREFIX_LENGTH + typname.length() + KEY_VALUE_SEPARATOR_LEN + data.length + LINE_SEPARATOR_LEN;
    byte[] packet = new byte[packet_len];
    int offset = 0;

    System.arraycopy(packet_header(packet_len), 0, packet, offset, PACKET_PREFIX_LENGTH);
    offset += PACKET_PREFIX_LENGTH;

    System.arraycopy(typname.getBytes(), 0, packet, offset, typname.length());
    offset += typname.length();

    packet[offset] = KEY_VALUE_SEPARATOR;
    offset += KEY_VALUE_SEPARATOR_LEN;

    System.arraycopy(data, 0, packet, offset, data.length);
    offset += data.length;

    packet[offset] = LINE_SEPARATOR;
    return packet;
  }

  private static byte[] packet_header(int size) {
    assert (size < 65536);
    size = (size & 0xffff);
    byte[] packet = new byte[PACKET_PREFIX_LENGTH];
    packet[0] = HEX[(size >> 12) & 15];
    packet[1] = HEX[(size >> 8) & 15];
    packet[2] = HEX[(size >> 4) & 15];
    packet[3] = HEX[(size) & 15];
    return packet;
  }

  /**
   * Determines whether or not the given byte array contains only valid UTF-8 characters.
   * Used to determine if an input needs to be base64 encoded, or not.
   * Snagged from this StackOverflow answer: https://stackoverflow.com/questions/887148/how-to-determine-if-a-string-contains-invalid-encoded-characters
   *
   * @param input - {@link byte[]} input string to check
   * @return - {@code true} String is only UTF-8 characters. {@code false} String contains non-UTF8 characters
   */
  private static boolean validUTF8(byte[] input) {
    int i = 0;
    // Check for BOM
    if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
            && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
      i = 3;
    }

    int end;
    for (int j = input.length; i < j; ++i) {
      int octet = input[i];
      if ((octet & 0x80) == 0) {
        continue; // ASCII
      }

      // Check for UTF-8 leading byte
      if ((octet & 0xE0) == 0xC0) {
        end = i + 1;
      } else if ((octet & 0xF0) == 0xE0) {
        end = i + 2;
      } else if ((octet & 0xF8) == 0xF0) {
        end = i + 3;
      } else {
        // Java only supports BMP so 3 is max
        return false;
      }

      while (i < end) {
        i++;
        octet = input[i];
        if ((octet & 0xC0) != 0x80) {
          // Not a valid trailing byte
          return false;
        }
      }
    }
    return true;
  }

  private static List<MacaroonJSONV2.CaveatJSONV2> serializeCaveatsToJSON(CaveatPacket[] packets) {
        boolean seenID = false;
        List<MacaroonJSONV2.CaveatJSONV2> caveats = new ArrayList<>();
        MacaroonJSONV2.CaveatJSONV2 caveat = new MacaroonJSONV2.CaveatJSONV2();

        for (final CaveatPacket packet : packets) {
            switch (packet.type) {
                case cid: {
                    // If we've seen the ID, load the caveat, and start over
                    if (seenID) {
                        caveats.add(caveat);
                        caveat = new MacaroonJSONV2.CaveatJSONV2();
                    }
                    if (validUTF8(packet.rawValue)) {
                        caveat.setCID(packet.getValueAsText());
                    } else {
                        caveat.setCID64(Base64.encodeUrlSafeToString(packet.rawValue));
                    }
                    seenID = true;
                    break;
                }
                case cl: {
                    caveat.setLocation(packet.getValueAsText());
                    break;
                }
                case vid: {
                    if (validUTF8(packet.rawValue)) {
                        caveat.setVID(packet.getValueAsText());
                    } else {
                        caveat.setVID64(Base64.encodeUrlSafeToString(packet.rawValue));
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException(String.format("Caveat cannot have field %s", packet.getType()));
            }
        }
        caveats.add(caveat);
        return caveats;
    }
}
