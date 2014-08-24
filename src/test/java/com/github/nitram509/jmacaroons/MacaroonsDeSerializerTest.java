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
    m = MacaroonsBuilder.create(location, secret, identifier);
    String serialized = m.serialize();

    Macaroon deserialized = MacaroonsDeSerializer.deserialize(serialized);

    assertThat(m).isEqualTo(deserialized);
  }

  @Test(expectedExceptions = NotDeSerializableException.class, expectedExceptionsMessageRegExp = ".*Invalid base64 string representation.*")
  public void invalid_base64_throws_NotDeSerializableException() {
    MacaroonsDeSerializer.deserialize("foobar");

    // expected NotDeSerializableException
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
}