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

package com.github.nitram509.jmacaroons.examples;

import static com.github.nitram509.jmacaroons.MacaroonsSerializer.V2;
import static com.github.nitram509.jmacaroons.verifier.AuthoritiesCaveatVerifier.hasAuthority;

import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsBuilder;
import com.github.nitram509.jmacaroons.MacaroonsVerifier;
import com.github.nitram509.jmacaroons.verifier.TimestampCaveatVerifier;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

/**
 * These are example code snippets, used in documentation.
 * PLEASE, adjust the line numbers in the README.md if you change this file.
 */
public class MacaroonsExamples {

  Macaroon create() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.create(location, secretKey, identifier);
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
    return macaroon;
  }

  void serialize() {
    Macaroon macaroon = create();
    String serialized = macaroon.serialize();
    System.out.println("Serialized: " + serialized);
    // Serialized: MDAyNGxvY2F0aW9uIGh0dHA6Ly93d3cuZXhhbXBsZS5vcmcKMDAyNmlkZW50aWZpZXIgd2UgdXNlZCBvdXIgc2VjcmV0IGtleQowMDJmc2lnbmF0dXJlIOPZ4CkIUmxMADmuFRFBFdl_3Wi_K6N5s0Kq8PYX0FUvCg
  }

  void deserialize() {
    String serialized = create().serialize();
    Macaroon macaroon = Macaroon.deserialize(serialized);
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f
  }

  void verify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();

    MacaroonsVerifier verifier = macaroon.verifier();
    String secret = "this is our super secret key; only we should know it";
    boolean valid = verifier.isValid(secret);
    // > True
  }

  void addCaveat() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("account = 3735928559")
        .build();
    System.out.println(macaroon.inspect());
  }

  void addCaveat_modify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();
    macaroon = Macaroon.builder(macaroon)
        .addCaveat("account = 3735928559")
        .build();
    System.out.println(macaroon.inspect());
    // > location http://www.example.org
    // > identifier we used our secret key
    // > cid account = 3735928559
    // > signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128
  }

  void verify_required_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("account = 3735928559")
        .build();
    MacaroonsVerifier verifier = macaroon.verifier();
    verifier.isValid(secretKey);
    // > False

    verifier.satisfy("account = 3735928559");
    verifier.isValid(secretKey);
    // > True

    verifier.satisfy("IP = 127.0.0.1')");
    verifier.satisfy("browser = Chrome')");
    verifier.satisfy("action = deposit");
    verifier.isValid(secretKey);
    // > True
  }

  void verify_general_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("time < 2042-01-01T00:00")
        .build();
    MacaroonsVerifier verifier = macaroon.verifier();
    verifier.isValid(secretKey);
    // > False

    verifier.satisfy(new TimestampCaveatVerifier());
    verifier.isValid(secretKey);
    // > True
  }

  void with_3rd_party_caveats() {
    // create a simple macaroon first
    String location = "http://mybank/";
    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    MacaroonsBuilder mb = Macaroon.builder(location, secret, publicIdentifier)
        .addCaveat("account = 3735928559");

    // add a 3rd party caveat
    // you'll likely want to use a higher entropy source to generate this key
    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    // send_to_3rd_party_location_and_do_auth(caveat_key, predicate);
    // identifier = recv_from_auth();
    String identifier = "this was how we remind auth of key/pred";
    Macaroon m = mb.addCaveat("http://auth.mybank/", caveat_key, identifier)
        .build();

    System.out.println(m.inspect());
    // > location http://mybank/
    // > identifier we used our other secret key
    // > cid account = 3735928559
    // > cid this was how we remind auth of key/pred
    // > vid AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA027FAuBYhtHwJ58FX6UlVNFtFsGxQHS7uD_w_dedwv4Jjw7UorCREw5rXbRqIKhr
    // > cl http://auth.mybank/
    // > signature d27db2fd1f22760e4c3dae8137e2d8fc1df6c0741c18aed4b97256bf78d1f55c

    final String oneHourFromNow = Instant.now()
        .plus(Duration.ofHours(1))
        .toString();

    Macaroon d = Macaroon.builder("http://auth.mybank/", caveat_key, identifier)
        .addCaveat("time < " + oneHourFromNow)
        .build();

    Macaroon dp = Macaroon.builder(m)
        .prepareForRequest(d)
        .build();

    System.out.println("d.signature = " + d.signature);
    System.out.println("dp.signature = " + dp.signature);
    // > d.signature = 82a80681f9f32d419af12f6a71787a1bac3ab199df934ed950ddf20c25ac8c65
    // > dp.signature = 2eb01d0dd2b4475330739140188648cf25dda0425ea9f661f1574ca0a9eac54e

    m.verifier()
        .satisfy("account = 3735928559")
        .satisfy(new TimestampCaveatVerifier())
        .satisfy(dp)
        .assertIsValid(secret);
    // > ok.
  }

  void timestamp_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("time < 2015-01-01T00:00")
        .build();

    macaroon.verifier()
        .satisfy(new TimestampCaveatVerifier())
        .isValid(secretKey);
    // > True
  }

  void authorities_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = Macaroon.builder(location, secretKey, identifier)
        .addCaveat("authorities = ROLE_USER, DEV_TOOLS_AVAILABLE")
        .build();

    macaroon.verifier()
        .satisfy(hasAuthority("DEV_TOOLS_AVAILABLE"))
        .isValid(secretKey);
    // > True
  }

  void serialize_v2_binary_format() {
    Macaroon macaroon = create();
    String serialized = macaroon.serialize(V2);
    System.out.println("Serialized: " + serialized);
    // Serialized: AgEWaHR0cDovL3d3dy5leGFtcGxlLm9yZwIWd2UgdXNlZCBvdXIgc2VjcmV0IGtleQAABiDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLw
  }

}
