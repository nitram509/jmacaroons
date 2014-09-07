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

import com.github.nitram509.jmacaroons.GeneralVerifier;
import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsBuilder;
import com.github.nitram509.jmacaroons.MacaroonsVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    String secretKey = "this is our super secret key; only we should know it";
    macaroon = MacaroonsBuilder.modify(macaroon, secretKey)
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

    verifier.satisfyGeneral(new GeneralVerifier() {
      public boolean verify(String caveat) {
        if (caveat.startsWith("time < ")) {
          Date now = new Date();
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
          Date parsedDate = dateFormat.parse(caveat.substring("time < ".length()), new ParsePosition(0));
          return now.compareTo(parsedDate) < 0;
        }
        return false;
      }
    });
    verifier.isValid(secretKey);
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
