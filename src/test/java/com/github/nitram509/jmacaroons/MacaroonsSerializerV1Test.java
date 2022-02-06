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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsSerializerV1Test {

  private String identifier;
  private String secret;
  private String location;
  private Macaroon m;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";
  }

  @Test
  public void Macaroon_can_be_serialized() {
    Macaroon m = Macaroon.builder(location, secret, identifier).build();

    assertThat(MacaroonsSerializer.V1.serialize(m)).isEqualTo(
            "MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo");
    assertThat(MacaroonsSerializer.V1.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_with_caveat_can_be_serialized() {
    Macaroon m = Macaroon.builder(location, secret, identifier)
        .addCaveat("account = 3735928559")
        .build();

    assertThat(MacaroonsSerializer.V1.serialize(m)).isEqualTo(
            "MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDJmc2lnbmF0dXJlIB7-R2PykNvODB0IR3Nn4R9O7kVqZJM89mLXl3LbuCEoCg");
    assertThat(MacaroonsSerializer.V1.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_with_3rd_party_caveat_can_be_serialized() {
    Macaroon m = Macaroon.builder(location, secret, identifier)
        .addCaveat("account = 3735928559")
        .addCaveat("http://auth.mybank/", "SECRET for 3rd party caveat", identifier)
        .build();

    assertThat(MacaroonsSerializer.V1.serialize(m)).startsWith(
            "MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDFmY2lkIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDA1MXZpZC");
    assertThat(MacaroonsSerializer.V1.serialize(m)).isEqualTo(m.serialize());
  }


  @Test
  public void Macaroon_can_be_deserialized() {
    m = Macaroon.builder(location, secret, identifier).build();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsSerializer.V1.deserialize(serialized);

    assertThat(m).isEqualTo(deserialized);
  }

  @Test
  public void Macaroon_with_1st_party_caveat_can_be_deserialized() {
    m = Macaroon.builder(location, secret, identifier)
            .addCaveat("account = 3735928559")
            .build();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsSerializer.V1.deserialize(serialized);

    assertThat(m).isEqualTo(deserialized);
  }

  @Test
  public void Macaroon_with_3rd_party_caveat_can_be_deserialized() {
    m = Macaroon.builder(location, secret, identifier)
            .addCaveat("account = 3735928559")
            .addCaveat("http://auth.mybank/", "SECRET for 3rd party caveat", identifier)
            .build();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsSerializer.V1.deserialize(serialized);

    assertThat(deserialized.identifier).isEqualTo(m.identifier);
    assertThat(deserialized.location).isEqualTo(m.location);
    assertThat(deserialized.signature).isEqualTo(m.signature);
    assertThat(deserialized.signatureBytes).isEqualTo(m.signatureBytes);
    assertThat(deserialized.caveatPackets).isEqualTo(m.caveatPackets);
    assertThat(deserialized).isEqualTo(m);
  }

  @Test(expectedExceptions = NotDeSerializableException.class, expectedExceptionsMessageRegExp = ".*Not enough bytes for signature found.*")
  public void to_short_base64_throws_NotDeSerializableException() {
    // packet is: "123"
    MacaroonsSerializer.V1.deserialize("MTIzDQo=");

    // expected NotDeSerializableException
  }

  @Test(expectedExceptions = NotDeSerializableException.class, expectedExceptionsMessageRegExp = ".*Not enough data bytes available.*")
  public void invalid_packet_length_throws_NotDeSerializableException() {
    // packet is: "fffflocation http://mybank12345678901234567890.com"
    MacaroonsSerializer.V1.deserialize("ZmZmZmxvY2F0aW9uIGh0dHA6Ly9teWJhbmsxMjM0NTY3ODkwMTIzNDU2Nzg5MC5jb20=");

    // expected NotDeSerializableException
  }

  @Test
  public void stateful_packet_reader_parses_header_length() throws Exception {
    MacaroonsSerializerV1.StatefulPacketReader packetReader =
            new MacaroonsSerializerV1.StatefulPacketReader(new byte[]{'a', 'b', 'c',
                    'd'});
    assertThat(packetReader.readPacketHeader()).isEqualTo(0xabcd);
  }
}
