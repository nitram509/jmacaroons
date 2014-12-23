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
    Macaroon m = new MacaroonsBuilder(location, secret, publicIdentifier)
        .add_first_party_caveat("account = 3735928559")
        .add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(publicIdentifier);
    assertThat(m.location).isEqualTo(location);
    assertThat(m.caveatPackets).isEqualTo(new CaveatPacket[]{
        new CaveatPacket(Type.cid, "account = 3735928559"),
        new CaveatPacket(Type.cid, identifier),
        new CaveatPacket(Type.vid, getVidFromBase64()),
        new CaveatPacket(Type.cl, "http://auth.mybank/")
    });
    assertThat(m.signature).isEqualTo("d27db2fd1f22760e4c3dae8137e2d8fc1df6c0741c18aed4b97256bf78d1f55c");
    assertThat(m.serialize()).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMmNpZGVudGlmaWVyIHdlIHVzZWQgb3VyIG90aGVyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDMwY2lkIHRoaXMgd2FzIGhvdyB3ZSByZW1pbmQgYXV0aCBvZiBrZXkvcHJlZAowMDUxdmlkIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAANNuxQLgWIbR8CefBV-lJVTRbRbBsUB0u7g_8P3XncL-CY8O1KKwkRMOa120aiCoawowMDFiY2wgaHR0cDovL2F1dGgubXliYW5rLwowMDJmc2lnbmF0dXJlINJ9sv0fInYOTD2ugTfi2Pwd9sB0HBiu1LlyVr940fVcCg");
  }

  private byte[] getVidFromBase64() {
    return Base64.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA027FAuBYhtHwJ58FX6UlVNFtFsGxQHS7uD_w_dedwv4Jjw7UorCREw5rXbRqIKhr");
  }

}