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

import java.util.ArrayList;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

class MacaroonsDeSerializer {

  private static final String HEX_ALPHABET = "0123456789abcdef";

  public static Macaroon deserialize(String serializedMacaroon) throws NotDeSerializableException {
    assert serializedMacaroon != null;
    byte[] bytes = Base64.decode(serializedMacaroon);
    int minLength = MACAROON_HASH_BYTES + KEY_VALUE_SEPARATOR.length() + SIGNATURE.length();
    if (bytes.length < minLength) {
      throw new NotDeSerializableException("Couldn't deserialize macaroon. Not enough bytes for signature found. There have to be at least " + minLength + " bytes");
    }
    return deserializeStream(new StatefulPacketReader(bytes));
  }

  private static Macaroon deserializeStream(StatefulPacketReader packetReader) {
    String location = null;
    String identifier = null;
    List<CaveatPacket> caveats = new ArrayList<CaveatPacket>(3);
    byte[] signature = null;

    for (Packet packet; (packet = readPacket(packetReader)) != null; ) {
      if (bytesStartWith(packet.data, LOCATION_BYTES)) {
        location = parsePacket(packet, LOCATION_BYTES);
      } else if (bytesStartWith(packet.data, IDENTIFIER_BYTES)) {
        identifier = parsePacket(packet, IDENTIFIER_BYTES);
      } else if (bytesStartWith(packet.data, CID_BYTES)) {
        String s = parsePacket(packet, CID_BYTES);
        caveats.add(new CaveatPacket(Type.cid, s));
      } else if (bytesStartWith(packet.data, CL_BYTES)) {
        String s = parsePacket(packet, CL_BYTES);
        caveats.add(new CaveatPacket(Type.cl, s));
      } else if (bytesStartWith(packet.data, VID_BYTES)) {
        byte[] raw = parseRawPacket(packet, VID_BYTES);
        caveats.add(new CaveatPacket(Type.vid, raw));
      } else if (bytesStartWith(packet.data, SIGNATURE_BYTES)) {
        signature = parseSignature(packet, SIGNATURE_BYTES);
      }
    }
    return new Macaroon(location, identifier, caveats.toArray(new CaveatPacket[caveats.size()]), signature);
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
    String payload = new String(packet.data, headerLen, len, IDENTIFIER_CHARSET);
    if (payload.endsWith(LINE_SEPARATOR)) {
      payload = payload.substring(0, len - LINE_SEPARATOR.length());
    }
    return payload;
  }

  private static byte[] parseRawPacket(Packet packet, byte[] header) {
    int headerLen = header.length + KEY_VALUE_SEPARATOR.length();
    int len = packet.data.length - headerLen - LINE_SEPARATOR.length();
    byte[] raw = new byte[len];
    System.arraycopy(packet.data, headerLen, raw, 0, len);
    return raw;
  }

  private static boolean bytesStartWith(byte[] bytes, byte[] startBytes) {
    if (bytes.length < startBytes.length) return false;
    for (int i = 0, len = startBytes.length; i < len; i++) {
      if (bytes[i] != startBytes[i]) return false;
    }
    return true;
  }

  private static Packet readPacket(StatefulPacketReader stream) {
    if (stream.isEOF()) return null;
    if (!stream.isPacketHeaderAvailable()) {
      throw new NotDeSerializableException("Not enough header bytes available. Needed " + PACKET_PREFIX_LENGTH + " bytes.");
    }
    int size = stream.readPacketHeader();
    assert size <= PACKET_MAX_SIZE;

    byte[] data = new byte[size - PACKET_PREFIX_LENGTH];
    int read = stream.read(data);
    if (read < 0) return null;
    if (read != data.length) {
      throw new NotDeSerializableException("Not enough data bytes available. Needed " + data.length + " bytes, but was only " + read);
    }

    return new Packet(size, data);
  }

  private static class Packet {
    final int size;
    final byte[] data;

    private Packet(int size, byte[] data) {
      this.size = size;
      this.data = data;
    }
  }

  private static class StatefulPacketReader {

    private final byte[] buffer;
    private int seekIndex = 0;

    public StatefulPacketReader(byte[] buffer) {
      this.buffer = buffer;
    }

    public int read(byte[] data) {
      int len = Math.min(data.length, buffer.length - seekIndex);
      if (len > 0) {
        System.arraycopy(buffer, seekIndex, data, 0, len);
        seekIndex += len;
        return len;
      }
      return -1;
    }

    public int readPacketHeader() {
      int size = 0;
      size += (HEX_ALPHABET.indexOf(buffer[seekIndex++]) & 15) << 12;
      size += (HEX_ALPHABET.indexOf(buffer[seekIndex++]) & 15) << 8;
      size += (HEX_ALPHABET.indexOf(buffer[seekIndex++]) & 15) << 4;
      size += (HEX_ALPHABET.indexOf(buffer[seekIndex++]) & 15);
      return size;
    }

    public boolean isPacketHeaderAvailable() {
      return seekIndex <= (buffer.length - PACKET_PREFIX_LENGTH);
    }

    public boolean isEOF() {
      return !(seekIndex < buffer.length);
    }
  }

}
