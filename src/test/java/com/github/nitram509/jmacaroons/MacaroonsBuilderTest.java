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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderTest {

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
  public void create_a_Macaroon_and_verify_signature_location_and_identfier() {
    m = Macaroon.builder(location, secret, identifier).build();

    assertThat(m.location).isEqualTo(location);
    assertThat(m.identifier).isEqualTo(identifier);
    assertThat(m.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void create_a_Macaroon_with_static_helper_method() {
    m = Macaroon.create("http://example.org/", "example secret key", "example entifier");

    assertThat(m.location).isEqualTo("http://example.org/");
    assertThat(m.identifier).isEqualTo("example entifier");
    assertThat(m.signature).isEqualTo("b642ce881b02b1e00b030e039374ece15ea852daa1a42a8dd4e7502977717f8b");
  }

  @Test
  public void create_a_Macaroon_and_inspect() {
    m = Macaroon.builder(location, secret, identifier).build();

    String inspect = m.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f\n"
    );
  }

  @Test
  public void different_locations_doesnt_change_the_signatures() {
    Macaroon m1 = Macaroon.builder("http://location_ONE", secret, identifier).build();
    Macaroon m2 = Macaroon.builder("http://location_TWO", secret, identifier).build();

    assertThat(m1.signature).isEqualTo(m2.signature);
  }

  @Test
  public void Macaroon_can_be_serialized() {
    m = Macaroon.builder(location, secret, identifier).build();

    assertThat(m.serialize()).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo");
  }

  @Test
  public void create_a_Macaroon_from_a_byteArray() throws UnsupportedEncodingException {
    identifier ="we used our secret key";
    byte[] secretBytes = secret.getBytes(StandardCharsets.US_ASCII);
    location ="http://www.example.org";

    m = Macaroon.create(location, secretBytes, identifier);

    assertThat(m.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

}
