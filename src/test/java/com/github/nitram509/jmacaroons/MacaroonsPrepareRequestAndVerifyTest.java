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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

  @BeforeMethod
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
    assertThat(M.signature).isEqualTo("d27db2fd1f22760e4c3dae8137e2d8fc1df6c0741c18aed4b97256bf78d1f55c");
  }

  private String send_to_auth_and_recv_identifier(String caveat_key, String predicate) {
    return "this was how we remind auth of key/pred";
  }

  @Test
  public void preparing_a_macaroon_for_request() {
    caveat_key = "4; guaranteed random by a fair toss of the dice";
    identifier = "this was how we remind auth of key/pred";
    D = new MacaroonsBuilder("http://auth.mybank/", caveat_key, identifier)
        .add_first_party_caveat("time < 2015-01-01T00:00")
        .getMacaroon();
    assertThat(D.signature).isEqualTo("82a80681f9f32d419af12f6a71787a1bac3ab199df934ed950ddf20c25ac8c65");

    DP = new MacaroonsBuilder(M)
        .prepare_for_request(D)
        .getMacaroon();

    assertThat(DP.signature).isEqualTo("2eb01d0dd2b4475330739140188648cf25dda0425ea9f661f1574ca0a9eac54e");
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
}
