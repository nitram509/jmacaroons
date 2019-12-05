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

import static org.fest.assertions.Assertions.assertThat;

import java.io.DataInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.github.nitram509.jmacaroons.CaveatPacket.Type;

public class MacaroonsSerializerV2Test {
    private static final String IDENTIFIER = "test macaroon";
    private static final String LOCATION = "https://example.com/macaroon";
    private static final String FIRST_PARTY_CAVEAT = "time < 2020-01-01T00:00:00Z";
    private static final String THIRD_PARTY_LOCATION = "https://auth.example.com/login";
    private static final String THIRD_PARTY_CAVEAT = "group = admin";


    @Test
    public void shouldRoundTripCorrectly() {
        // Given
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 42);
        String caveatKey = "super secret caveat key";
        Macaroon macaroon = MacaroonsBuilder.create(LOCATION, key, IDENTIFIER);
        macaroon = MacaroonsBuilder.modify(macaroon)
                .add_first_party_caveat(FIRST_PARTY_CAVEAT)
                .add_third_party_caveat(THIRD_PARTY_LOCATION, caveatKey, THIRD_PARTY_CAVEAT)
                .getMacaroon();

        // When
        String serialized = macaroon.serialize(MacaroonsSerializer.V2);
        Macaroon deserialized = MacaroonsBuilder.deserialize(serialized, MacaroonsSerializer.V2);

        // Then
        assertThat(deserialized.signature).as("signature").isEqualTo(macaroon.signature);
        assertThat(deserialized.identifier).as("identifier").isEqualTo(IDENTIFIER);
        assertThat(deserialized.location).as("location").isEqualTo(LOCATION);
        assertThat(deserialized.caveatPackets).as("caveats").hasSize(4);

        assertThat(deserialized.caveatPackets[0].type).isEqualTo(Type.cid);
        assertThat(deserialized.caveatPackets[0].getValueAsText()).isEqualTo(FIRST_PARTY_CAVEAT);

        assertThat(deserialized.caveatPackets[1].type).isEqualTo(Type.cid);
        assertThat(deserialized.caveatPackets[1].getValueAsText()).isEqualTo(THIRD_PARTY_CAVEAT);
        assertThat(deserialized.caveatPackets[2].type).isEqualTo(Type.vid);
        assertThat(deserialized.caveatPackets[2].getValueAsText()).isNotEmpty();
        assertThat(deserialized.caveatPackets[3].type).isEqualTo(Type.cl);
        assertThat(deserialized.caveatPackets[3].getValueAsText()).isEqualTo(THIRD_PARTY_LOCATION);
    }


    @Test
    public void shouldHandleVarIntsCorrectly() throws Exception {
        try (PipedOutputStream out = new PipedOutputStream();
             DataInputStream in = new DataInputStream(new PipedInputStream(out))) {

            for (long l = 0L; l < 1000000L; ++l) {
                MacaroonsSerializerV2.writeVarInt(out, l);
                long read = MacaroonsSerializerV2.readVarInt(in);
                assertThat(read).isEqualTo(l);
            }

            MacaroonsSerializerV2.writeVarInt(out, Long.MAX_VALUE);
            assertThat(MacaroonsSerializerV2.readVarInt(in)).isEqualTo(Long.MAX_VALUE);

            MacaroonsSerializerV2.writeVarInt(out, Long.MIN_VALUE);
            assertThat(MacaroonsSerializerV2.readVarInt(in)).isEqualTo(Long.MIN_VALUE);
        }
    }
}