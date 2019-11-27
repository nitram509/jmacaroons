/*
 * Copyright (c) 2019 Neil Madden.
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

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.github.nitram509.jmacaroons.CaveatPacket.Type;

final class SerializationFormatV2 implements MacaroonSerializationFormat {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

    private static final byte LOCATION = 1;
    private static final byte IDENTIFIER = 2;
    private static final byte VERIFIER_ID = 4;
    private static final byte SIGNATURE = 6;
    private static final byte END_OF_SECTION = 0;

    @Override
    public String serialize(Macaroon macaroon) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream out = BASE64_ENCODER.wrap(baos)) {
            out.write(2); // Version
            writeOptionalField(out, LOCATION, macaroon.location);
            writeField(out, IDENTIFIER, macaroon.identifier.getBytes(MacaroonsConstants.IDENTIFIER_CHARSET));
            out.write(END_OF_SECTION);

            Caveat caveat = new Caveat();
            for (CaveatPacket packet : macaroon.caveatPackets) {
                if (packet.type == Type.cid) {
                    writeCaveat(out, caveat);
                    caveat = new Caveat();
                }
                switch (packet.type) {
                case cid:
                    caveat.id = packet.rawValue;
                    break;
                case cl:
                    caveat.loc = packet.rawValue;
                    break;
                case vid:
                    caveat.vid = packet.rawValue;
                    break;
                default:
                    throw new IllegalArgumentException("unexpected packet type: " + packet.type);
                }
            }
            writeCaveat(out, caveat);

            out.write(END_OF_SECTION);
            writeField(out, SIGNATURE, macaroon.signatureBytes);

        } catch (IOException e) {
            throw new RuntimeException("Unable to serialize macaroon", e);
        }

        return new String(baos.toByteArray(), US_ASCII);
    }

    @Override
    public Macaroon deserialize(String data) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(
                BASE64_DECODER.wrap(new ByteArrayInputStream(data.getBytes(US_ASCII)))))) {

            rejectIfFalse(in.readUnsignedByte() == 2, "Incorrect version");

            String location = readOptionalString(in, LOCATION).orElse(null);
            String identifier = readStringField(in, IDENTIFIER);
            rejectIfFalse(in.readUnsignedByte() == END_OF_SECTION, "Invalid macaroon");

            List<CaveatPacket> caveats = new ArrayList<>();
            while (peek(in) != END_OF_SECTION) {
                String caveatLocation = readOptionalString(in, LOCATION).orElse(null);
                byte[] caveatIdentifier = readField(in, IDENTIFIER);
                byte[] verifierId = readOptionalField(in, VERIFIER_ID).orElse(null);

                caveats.add(new CaveatPacket(Type.cid, caveatIdentifier));
                if (verifierId != null) {
                    caveats.add(new CaveatPacket(Type.vid, verifierId));
                }
                if (caveatLocation != null) {
                    caveats.add(new CaveatPacket(Type.cl, caveatLocation));
                }

                rejectIfFalse(in.readUnsignedByte() == END_OF_SECTION, "Invalid macaroon");
            }

            rejectIfFalse(in.readUnsignedByte() == END_OF_SECTION, "Invalid macaroon");
            byte[] tag = readField(in, SIGNATURE);
            rejectIfFalse(tag.length == 32, "Invalid authentication tag");

            return new Macaroon(location, identifier, tag, caveats.toArray(new CaveatPacket[0]));
        } catch (IOException e) {
            throw new NotDeSerializableException("Unable to decode macaroon", e);
        } catch (IllegalArgumentException e) {
            throw new NotDeSerializableException(e);
        }
    }

    private static void writeCaveat(OutputStream out, Caveat caveat) throws IOException {
        if (caveat.id != null) {
            writeOptionalField(out, LOCATION, caveat.loc);
            writeField(out, IDENTIFIER, caveat.id);
            writeOptionalField(out, VERIFIER_ID, caveat.vid);
            out.write(END_OF_SECTION);
        }
    }

    private static void rejectIfFalse(boolean condition, String message) {
        if (!condition) {
            throw new NotDeSerializableException(message);
        }
    }

    private static void writeField(OutputStream out, byte type, byte[] data) throws IOException {
        out.write(type);
        writeVarInt(out, data.length);
        out.write(data);
    }

    private static void writeOptionalField(OutputStream out, byte type, byte[] data) throws IOException {
        if (data != null) {
            writeField(out, type, data);
        }
    }

    private static void writeOptionalField(OutputStream out, byte type, String data) throws IOException {
        if (data != null) {
            writeField(out, type, data.getBytes(MacaroonsConstants.IDENTIFIER_CHARSET));
        }
    }

    private static byte[] readField(DataInputStream in, byte type) throws IOException {
        rejectIfFalse(in.readUnsignedByte() == type, "Unexpected packet - expected type " + type);
        long length = readVarInt(in);
        rejectIfFalse(length >= 0 && length < 65536, "Packet too large");
        byte[] data = new byte[(int) length];
        in.readFully(data);
        return data;
    }

    private static Optional<byte[]> readOptionalField(DataInputStream in, byte type) throws IOException {
        if (peek(in) == type) {
            return Optional.of(readField(in, type));
        }
        return Optional.empty();
    }

    private static String readStringField(DataInputStream in, byte type) throws IOException {
        return new String(readField(in, type), MacaroonsConstants.IDENTIFIER_CHARSET);
    }

    private static Optional<String> readOptionalString(DataInputStream in, byte type) throws IOException {
        return readOptionalField(in, type).map(bytes -> new String(bytes, MacaroonsConstants.IDENTIFIER_CHARSET));
    }

    private static int peek(DataInputStream in) throws IOException {
        in.mark(1);
        int result = in.readUnsignedByte();
        in.reset();
        return result;
    }

    static void writeVarInt(OutputStream out, long value) throws IOException {
        while (Long.compareUnsigned(value, 128L) >= 0) {
            out.write((int) (value & 127) | 128);
            value >>>= 7;
        }
        out.write((int) (value & 127));
    }

    static long readVarInt(DataInputStream in) throws IOException {
        long result = 0L;
        int shift = 0;
        long b = in.readUnsignedByte();
        while ((b & 128) != 0 && shift <= 64) {
            result |= ((b & 127L) << shift);
            shift += 7;
            b = in.readUnsignedByte();
        }
        result |= (b << shift);
        return result;
    }

    private static class Caveat {
        byte[] id;
        byte[] loc;
        byte[] vid;
    }
}
