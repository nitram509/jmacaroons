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

import static com.github.nitram509.jmacaroons.MacaroonsConstants.CID_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.CL_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.IDENTIFIER_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.IDENTIFIER_CHARSET;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.KEY_VALUE_SEPARATOR;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.KEY_VALUE_SEPARATOR_LEN;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.LINE_SEPARATOR;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.LINE_SEPARATOR_LEN;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.LOCATION_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_HASH_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.PACKET_MAX_SIZE;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.PACKET_PREFIX_LENGTH;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.SIGNATURE;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.SIGNATURE_BYTES;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.VID_BYTES;
import static com.github.nitram509.jmacaroons.util.ArrayTools.flattenByteArray;

import java.util.ArrayList;
import java.util.List;

import com.github.nitram509.jmacaroons.CaveatPacket.Type;
import com.github.nitram509.jmacaroons.util.Base64;

final class MacaroonsSerializerV1 implements MacaroonsSerializer {
    private static final byte[] HEX = new byte[]{
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'};
    private static final byte[] HEX_ALPHABET = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0,
            0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    public String serialize(Macaroon macaroon) {
        List<byte[]> packets = new ArrayList<>( 3 + macaroon.caveatPackets.length );
        packets.add(serialize_packet(Type.location, macaroon.location));
        packets.add(serialize_packet(Type.identifier, macaroon.identifier));
        for (CaveatPacket caveatPacket : macaroon.caveatPackets) {
            packets.add(serialize_packet(caveatPacket.type, caveatPacket.rawValue));
        }
        packets.add(serialize_packet(Type.signature, macaroon.signatureBytes));
        return Base64.encodeUrlSafeToString(flattenByteArray(packets));
    }

    @Override
    public Macaroon deserialize(String serializedMacaroon) {
        assert serializedMacaroon != null;
        byte[] bytes = Base64.decode(serializedMacaroon);
        int minLength = MACAROON_HASH_BYTES + KEY_VALUE_SEPARATOR_LEN + SIGNATURE.length();
        if (bytes.length < minLength) {
            throw new NotDeSerializableException("Couldn't deserialize macaroon. Not enough bytes for signature found. There have to be at least " + minLength + " bytes");
        }
        return deserializeStream(new StatefulPacketReader(bytes));
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


    private static Macaroon deserializeStream(StatefulPacketReader packetReader) {
        String location = null;
        String identifier = null;
        List<CaveatPacket> caveats = new ArrayList<>( 3 );
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
        return new Macaroon(location, identifier, signature, caveats.toArray(new CaveatPacket[caveats.size()]));
    }

    private static byte[] parseSignature(Packet packet, byte[] signaturePacketData) {
        int headerLen = signaturePacketData.length + KEY_VALUE_SEPARATOR_LEN;
        int len = Math.min(packet.data.length - headerLen, MacaroonsConstants.MACAROON_HASH_BYTES);
        byte[] signature = new byte[len];
        System.arraycopy(packet.data, headerLen, signature, 0, len);
        return signature;
    }

    private static String parsePacket(Packet packet, byte[] header) {
        int headerLen = header.length + KEY_VALUE_SEPARATOR_LEN;
        int len = packet.data.length - headerLen;
        if (packet.data[headerLen + len - 1] == LINE_SEPARATOR) len--;
        return new String(packet.data, headerLen, len, IDENTIFIER_CHARSET);
    }

    private static byte[] parseRawPacket(Packet packet, byte[] header) {
        int headerLen = header.length + KEY_VALUE_SEPARATOR_LEN;
        int len = packet.data.length - headerLen - LINE_SEPARATOR_LEN;
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

    static class StatefulPacketReader {

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
            return (HEX_ALPHABET[buffer[seekIndex++]] << 12)
                    | (HEX_ALPHABET[buffer[seekIndex++]] << 8)
                    | (HEX_ALPHABET[buffer[seekIndex++]] << 4)
                    | HEX_ALPHABET[buffer[seekIndex++]];
        }

        public boolean isPacketHeaderAvailable() {
            return seekIndex <= (buffer.length - PACKET_PREFIX_LENGTH);
        }

        public boolean isEOF() {
            return !(seekIndex < buffer.length);
        }
    }
}
