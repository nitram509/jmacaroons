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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

class MacaroonsDeSerializer {

  private static final String HEX = "0123456789abcdef";
  private static final Charset UTF8 = Charset.forName("UTF8");

  private String location = null;
  private String identifier = null;
  private List<String> caveats = new ArrayList<String>(3);
  private byte[] signature = null;

  public static Macaroon deserialize(String serializedMacaroon) throws NotDeSerializableException {
    assert serializedMacaroon != null;
    byte[] bytes = Base64.decode(serializedMacaroon);
    if (bytes == null) {
      throw new NotDeSerializableException("Couldn't deserialize macaroon. Invalid base64 string representation.");
    }
    int minLength = MACAROON_HASH_BYTES + KEY_VALUE_SEPARATOR.length() + SIGNATURE.length();
    if (bytes.length < minLength) {
      throw new NotDeSerializableException("Couldn't deserialize macaroon. Not enough bytes for signature found. There have to be at least " + minLength + " bytes");
    }
    InputStream stream = new ByteArrayInputStream(bytes);
    try {
      return new MacaroonsDeSerializer().deserializeStream(stream);
    } catch (IOException e) {
      throw new NotDeSerializableException(e);
    }
  }

  private Macaroon deserializeStream(InputStream stream) throws IOException {
    for (Packet packet; (packet = read_packet(stream)) != null; ) {
      if (bytesStartWith(packet.data, LOCATION_BYTES)) {
        location = parsePacket(packet, LOCATION_BYTES);
      }
      if (bytesStartWith(packet.data, IDENTIFIER_BYTES)) {
        identifier = parsePacket(packet, IDENTIFIER_BYTES);
      }
      if (bytesStartWith(packet.data, CID_BYTES)) {
        caveats.add(parsePacket(packet, CID_BYTES));
      }
      if (bytesStartWith(packet.data, SIGNATURE_BYTES)) {
        signature = parseSignature(packet, SIGNATURE_BYTES);
      }
    }
    return new Macaroon(location, identifier, caveats.toArray(new String[caveats.size()]), signature);
  }

  private static byte[] parseSignature(Packet packet, byte[] signaturePacketData) {
    int headerLen = signaturePacketData.length + KEY_VALUE_SEPARATOR.length();
    int len = Math.min(packet.data.length - headerLen, MacaroonsConstants.MACAROON_HASH_BYTES);
    byte[] signature = new byte[len];
    System.arraycopy(packet.data, headerLen, signature, 0, len);
    return signature;
  }

  private static String parsePacket(Packet packet, byte[] header) {
    int headerLen = header.length + KEY_VALUE_SEPARATOR.length();
    int len = packet.data.length - headerLen;
    String payload = new String(packet.data, headerLen, len, UTF8);
    if (payload.endsWith(LINE_SEPARATOR)) {
      payload = payload.substring(0, len - LINE_SEPARATOR.length());
    }
    return payload;
  }

  private static boolean bytesStartWith(byte[] bytes, byte[] startBytes) {
    if (bytes.length < startBytes.length) return false;
    for (int i = 0, len = startBytes.length; i < len; i++) {
      if (bytes[i] != startBytes[i]) return false;
    }
    return true;
  }

  private static Packet read_packet(InputStream stream) throws IOException {
    int read;

    byte[] lengthHeader = new byte[PACKET_PREFIX_LENGTH];
    read = stream.read(lengthHeader);
    if (read < 0) return null;
    if (read != lengthHeader.length) {
      throw new NotDeSerializableException("Not enough header bytes available. Needed " + lengthHeader.length + " bytes, but was only " + read);
    }
    int size = parse_packet_header(lengthHeader);
    assert size <= PACKET_MAX_SIZE;

    byte[] data = new byte[size - PACKET_PREFIX_LENGTH];
    read = stream.read(data);
    if (read < 0) return null;
    if (read != data.length) {
      throw new NotDeSerializableException("Not enough data bytes available. Needed " + data.length + " bytes, but was only " + read);
    }

    return new Packet(size, data);
  }

  private static int parse_packet_header(byte[] lengthHeader) {
    int size = 0;
    size += (HEX.indexOf(lengthHeader[0]) & 15) << 12;
    size += (HEX.indexOf(lengthHeader[1]) & 15) << 8;
    size += (HEX.indexOf(lengthHeader[2]) & 15) << 4;
    size += (HEX.indexOf(lengthHeader[3]) & 15);
    return size;
  }

  private static class Packet {
    final int size;
    final byte[] data;

    private Packet(int size, byte[] data) {
      this.size = size;
      this.data = data;
    }
  }

}
