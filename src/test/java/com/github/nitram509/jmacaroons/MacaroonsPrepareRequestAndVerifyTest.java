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

import com.github.nitram509.jmacaroons.verifier.TimestampCaveatVerifier;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsPrepareRequestAndVerifyTest {

  private String identifier;
  private String secret;
  private String location;
  private String caveat_key;
  private String publicIdentifier;
  private String predicate;
  private Macaroon M;
  private Macaroon DP;
  private Macaroon D;

  @BeforeClass
  public void setUp() throws Exception {
    secret = "this is a different super-secret key; never use the same secret twice";
    publicIdentifier = "we used our other secret key";
    location = "http://mybank/";
    M = new MacaroonsBuilder(location, secret, publicIdentifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();
    assertThat(M.signature).isEqualTo("1434e674ad84fdfdc9bc1aa00785325c8b6d57341fc7ce200ba4680c80786dda");

    caveat_key = "4; guaranteed random by a fair toss of the dice";
    predicate = "user = Alice";
    identifier = send_to_auth_and_recv_identifier(caveat_key, predicate);

    M = new MacaroonsBuilder(M)
        .add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
        .getMacaroon();
    // signature can't be asserted to be equal to a constant, because random nonce influences signature
  }

  private String send_to_auth_and_recv_identifier(String caveat_key, String predicate) {
    return "this was how we remind auth of key/pred";
  }

  @Test
  public void preparing_a_macaroon_for_request() {
    caveat_key = "4; guaranteed random by a fair toss of the dice";
    identifier = "this was how we remind auth of key/pred";

    final String oneHourFromNow = Instant.now()
        .plus(Duration.ofHours(1))
        .toString();

    D = new MacaroonsBuilder("http://auth.mybank/", caveat_key, identifier)
        .add_first_party_caveat("time < " + oneHourFromNow)
        .getMacaroon();

    DP = new MacaroonsBuilder(M)
        .prepare_for_request(D)
        .getMacaroon();

    // signature can't be asserted to be equal to a constant, because random nonce influences signature
  }

  @Test(dependsOnMethods = "preparing_a_macaroon_for_request")
  public void verifying_valid() {
    boolean valid = new MacaroonsVerifier(M)
        .satisfyExact("account = 3735928559")
        .satisfyGeneral(new TimestampCaveatVerifier())
        .satisfy3rdParty(DP)
        .isValid(secret);

    assertThat(valid).isTrue();
  }

  @Test(dependsOnMethods = "preparing_a_macaroon_for_request")
  public void verifying_unprepared_macaroon__has_to_fail() {
    boolean valid = new MacaroonsVerifier(M)
        .satisfyExact("account = 3735928559")
        .satisfyGeneral(new TimestampCaveatVerifier())
        .satisfy3rdParty(D)
        .isValid(secret);

    assertThat(valid).isFalse();
  }

  @Test(dependsOnMethods = "preparing_a_macaroon_for_request")
  public void verifying_macaroon_without_satisfying_3rd_party__has_to_fail() {
    boolean valid = new MacaroonsVerifier(M)
        .satisfyExact("account = 3735928559")
        .satisfyGeneral(new TimestampCaveatVerifier())
        .isValid(secret);

    assertThat(valid).isFalse();
  }


  @Test
  public void using_non_string_key_bytes_for_3rd_party__should_succeed() {

    byte[] root_key = keyGen();
    byte[] caveat_key = keyGen();
    String caveat_id = "caveat";
    String macaroon_id = "123456";
    Macaroon M = new MacaroonsBuilder("some-service", root_key, macaroon_id)
        .add_third_party_caveat("authN", new String(caveat_key, Charset.forName("ISO-8859-1")), caveat_id)
        .getMacaroon();

    assertThat(new MacaroonsVerifier(M)
        .isValid(root_key))
        .describedAs("Original should not be valid without discharge macaroon")
        .isFalse();

    // create discharge macaroon using caveat key as bytes
    Macaroon D = new MacaroonsBuilder("authN", caveat_key, caveat_id)
        .getMacaroon();

    assertThat(new MacaroonsVerifier(D)
        .isValid(caveat_key))
        .describedAs("Discharge macaroon should be valid")
        .isTrue();

    // prepare discharge macaroon by binding to the original macaroon
    Macaroon bound = new MacaroonsBuilder(M)
        .prepare_for_request(D)
        .getMacaroon();

    assertThat(new MacaroonsVerifier(bound)
        .isValid(caveat_key))
        .describedAs("Bound discharge macaroon should not be considered valid on its own")
        .isFalse();

    assertThat(new MacaroonsVerifier(M)
        .satisfy3rdParty(bound)
        .isValid(root_key))
        .describedAs("Original should be valid with third party caveat bound")
        .isTrue();
  }

  byte[] keyGen() {
    try {
      byte[] keyBytes = new byte[com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_SUGGESTED_SECRET_LENGTH];
      SecureRandom prng = SecureRandom.getInstanceStrong();
      prng.nextBytes(keyBytes);
      return keyBytes;
    }
    catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }
}
