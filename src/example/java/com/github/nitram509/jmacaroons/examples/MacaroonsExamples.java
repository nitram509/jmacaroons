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

import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsBuilder;
import com.github.nitram509.jmacaroons.MacaroonsVerifier;
import com.github.nitram509.jmacaroons.verifier.TimestampCaveatVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.verifier.AuthoritiesCaveatVerifier.hasAuthority;

/**
 * These are examples, for copy&paste into README.md.
 * They should be correct and compilable.
 */
public class MacaroonsExamples {

  private Macaroon create() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
    System.out.println(macaroon.inspect());

    return macaroon;
  }

  private void serialize() {
    Macaroon macaroon = create();

    String serialized = macaroon.serialize();
    System.out.println("Serialized: " + serialized);
  }

  private void deserialize() {
    String serialized = create().serialize();

    Macaroon macaroon = MacaroonsBuilder.deserialize(serialized);
    System.out.println(macaroon.inspect());
  }

  private void verify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();

    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    String secret = "this is our super secret key; only we should know it";
    boolean valid = verifier.isValid(secret);

    // > True
  }

  private void addCaveat() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();
    System.out.println(macaroon.inspect());
  }

  private void addCaveat_modify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();
    macaroon = MacaroonsBuilder.modify(macaroon)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();
    System.out.println(macaroon.inspect());
  }

  private void verify_required_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();
    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    verifier.isValid(secretKey);
    // > False

    verifier.satisfyExcact("account = 3735928559");
    verifier.isValid(secretKey);
    // > True

    verifier.satisfyExcact("IP = 127.0.0.1')");
    verifier.satisfyExcact("browser = Chrome')");
    verifier.satisfyExcact("action = deposit");
    verifier.isValid(secretKey);
    // > True
  }

  private void verify_general_caveats() throws InvalidKeyException, NoSuchAlgorithmException {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
        .add_first_party_caveat("time < 2042-01-01T00:00")
        .getMacaroon();
    MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
    verifier.isValid(secretKey);
    // > False

    verifier.satisfyGeneral(new TimestampCaveatVerifier());
    verifier.isValid(secretKey);
    // > True
  }

  private void with_3rd_party_caveats() {
    // create a simple macaroon first
    String location = "http://mybank/";
    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    MacaroonsBuilder mb = new MacaroonsBuilder(location, secret, publicIdentifier)
        .add_first_party_caveat("account = 3735928559");

    // add a 3rd party caveat
    // you'll likely want to use a higher entropy source to generate this key
    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    // send_to_3rd_party_location_and_do_auth(caveat_key, predicate);
    // identifier = recv_from_auth();
    String identifier = "this was how we remind auth of key/pred";
    Macaroon m = mb.add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
        .getMacaroon();

    System.out.println(m.inspect());

    Macaroon d = new MacaroonsBuilder("http://auth.mybank/", caveat_key, identifier)
        .add_first_party_caveat("time < 2015-01-01T00:00")
        .getMacaroon();

    Macaroon dp = MacaroonsBuilder.modify(m)
        .prepare_for_request(d)
        .getMacaroon();

    System.out.println("d.signature = " + d.signature);
    System.out.println("dp.signature = " + dp.signature);

    new MacaroonsVerifier(m)
        .satisfyExcact("account = 3735928559")
        .satisfyGeneral(new TimestampCaveatVerifier())
        .satisfy3rdParty(dp)
        .assertIsValid(secret);
  }

  private void timestamp_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
        .add_first_party_caveat("time < 2015-01-01T00:00")
        .getMacaroon();

    new MacaroonsVerifier(macaroon)
        .satisfyGeneral(new TimestampCaveatVerifier())
        .isValid(secretKey);
    // > True
  }

  private void authorities_verifier() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";

    Macaroon macaroon = new MacaroonsBuilder(location, secretKey, identifier)
        .add_first_party_caveat("authorities = ROLE_USER, DEV_TOOLS_AVAILABLE")
        .getMacaroon();

    new MacaroonsVerifier(macaroon)
        .satisfyGeneral(hasAuthority("DEV_TOOLS_AVAILABLE"))
        .isValid(secretKey);
    // > True
  }

  public static void main(String[] args) {
    MacaroonsExamples examples = new MacaroonsExamples();
    try {
      examples.create();
      examples.serialize();
      examples.deserialize();
      examples.verify();
      examples.addCaveat();
      examples.addCaveat_modify();
      examples.verify_required_caveats();
      examples.verify_general_caveats();
      examples.with_3rd_party_caveats();
      examples.timestamp_verifier();
      examples.authorities_verifier();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
