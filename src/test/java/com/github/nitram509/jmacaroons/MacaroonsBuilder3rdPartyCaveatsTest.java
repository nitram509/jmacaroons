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

import org.testng.annotations.Test;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilder3rdPartyCaveatsTest {

  @Test
  public void add_third_party_caveat() {

    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    String location = "http://mybank/";

    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    String identifier = "this was how we remind auth of key/pred";
    Macaroon m = Macaroon.builder(location, secret, publicIdentifier)
        .addCaveat("account = 3735928559")
        .addCaveat("http://auth.mybank/", caveat_key, identifier)
        .build();

    assertThat(m.identifier).isEqualTo(publicIdentifier);
    assertThat(m.location).isEqualTo(location);
    assertThat(m.caveatPackets[0]).isEqualTo(new CaveatPacket(Type.cid, "account = 3735928559"));
    assertThat(m.caveatPackets[1]).isEqualTo(new CaveatPacket(Type.cid, identifier));
    // packet with type VID can't be asserted to be equal to a constant, because random nonce influences signature
    assertThat(m.caveatPackets[3]).isEqualTo(new CaveatPacket(Type.cl, "http://auth.mybank/"));
    assertThat(m.caveatPackets).hasSize(4);
    // signature can't be asserted to be equal to a constant, because random nonce influences signature
    assertThat(m.serialize()).startsWith("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMmNpZGVudGlmaWVyIHdlIHVzZWQgb3VyIG90aGVyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDMwY2lkIHRoaXMgd2FzIGhvdyB3ZSByZW1pbmQgYXV0aCBvZiBrZXkvcHJlZAowMDUxdmlkI");
  }

}
