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

public class MacaroonsDeSerializerTest {

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
  public void Macaroon_can_be_deserialized() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsDeSerializer.deserialize(serialized);

    assertThat(m).isEqualTo(deserialized);
  }

  @Test
  public void Macaroon_with_1st_party_caveat_can_be_deserialized() {
    m = new MacaroonsBuilder(location, secret, identifier)
            .add_first_party_caveat("account = 3735928559")
            .getMacaroon();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsDeSerializer.deserialize(serialized);

    assertThat(m).isEqualTo(deserialized);
  }

  @Test
  public void Macaroon_with_3rd_party_caveat_can_be_deserialized() {
    m = new MacaroonsBuilder(location, secret, identifier)
            .add_first_party_caveat("account = 3735928559")
            .add_third_party_caveat("http://auth.mybank/", "SECRET for 3rd party caveat", identifier)
            .getMacaroon();
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsDeSerializer.deserialize(serialized);

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
    MacaroonsDeSerializer.deserialize("MTIzDQo=");

    // expected NotDeSerializableException
  }

  @Test(expectedExceptions = NotDeSerializableException.class, expectedExceptionsMessageRegExp = ".*Not enough data bytes available.*")
  public void invalid_packet_length_throws_NotDeSerializableException() {
    // packet is: "fffflocation http://mybank12345678901234567890.com"
    MacaroonsDeSerializer.deserialize("ZmZmZmxvY2F0aW9uIGh0dHA6Ly9teWJhbmsxMjM0NTY3ODkwMTIzNDU2Nzg5MC5jb20=");

    // expected NotDeSerializableException
  }

  @Test
  public void stateful_packet_reader_parses_header_length() throws Exception {
    MacaroonsDeSerializer.StatefulPacketReader packetReader = new MacaroonsDeSerializer.StatefulPacketReader(new byte[]{'a', 'b', 'c', 'd'});
    assertThat(packetReader.readPacketHeader()).isEqualTo(0xabcd);
  }


  @Test
  public void Macaroon_v2_can_be_deserialized() {
    final Macaroon m = new MacaroonsBuilder("http://test.loc", "test-key", "test-id").getMacaroon();
    assertThat(m).isEqualTo(MacaroonsDeSerializer.deserialize(m.serialize(MacaroonVersion.V2_JSON)));

    // Add a third party caveat
    final Macaroon m2 = new MacaroonsBuilder(m)
            .add_third_party_caveat("http://auth.mybank", "SECRET for 3rd party caveat", "test-third-party")
            .getMacaroon();

    assertThat(m2).isEqualTo(MacaroonsDeSerializer.deserialize(MacaroonsSerializer.serialize(m2, MacaroonVersion.V2_JSON)));
  }

  @Test
  public void Macaroon_v2_json_with_third_party_can_be_deserialized() {
    Macaroon m = new MacaroonsBuilder(location, secret, identifier)
            .add_first_party_caveat("account = 3735928559")
            .add_third_party_caveat("http://auth.mybank/", "SECRET for 3rd party caveat", identifier)
            .getMacaroon();

    final Macaroon m2 = MacaroonsDeSerializer.deserialize(m.serialize(MacaroonVersion.V2_JSON));
    assertThat(m).isEqualTo(m2);
  }
}