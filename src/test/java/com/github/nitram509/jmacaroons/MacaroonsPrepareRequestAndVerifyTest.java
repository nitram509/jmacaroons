/*
 * Copyright $todaz.zear Martin W. Kirst
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

public class MacaroonsPrepareRequestAndVerifyTest {

  private String identifier;
  private String secret;
  private String location;
  private String caveat_key;
  private String public_;
  private String predicate;
  private Macaroon M;
  private Macaroon DP;
  private Macaroon D;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";


    secret = "this is a different super-secret key; never use the same secret twice";
    public_ = "we used our other secret key";
    location = "http://mybank/";
    M = new MacaroonsBuilder(location, secret, public_)
            .add_first_party_caveat("account = 3735928559")
            .getMacaroon();
    assertThat(M.signature).isEqualTo("1434e674ad84fdfdc9bc1aa00785325c8b6d57341fc7ce200ba4680c80786dda");

    caveat_key = "4; guaranteed random by a fair toss of the dice";
    predicate = "user = Alice";

    identifier = send_to_auth_and_recv_identifier(caveat_key, predicate);

    M = new MacaroonsBuilder(M)
            .add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
            .getMacaroon();
    assertThat(M.signature).isEqualTo("6b99edb2ec6d7a4382071d7d41a0bf7dfa27d87d2f9fea86e330d7850ffda2b2");
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
            .prepare_for_request(D);

    assertThat(DP.signature).isEqualTo("b38b26ab29d3724e728427e758cccc16d9d7f3de46d0d811b70b117b05357b9b");
  }

  @Test(dependsOnMethods = "preparing_a_macaroon_for_request")
  public void verifying_valid() {
    boolean valid = new MacaroonsVerifier(M)
            .bind(DP)
            .isValid(secret);

    assertThat(valid).isTrue();
  }

  @Test(dependsOnMethods = "preparing_a_macaroon_for_request")
  public void verifying_unprepared_macaroon() {
    boolean valid = new MacaroonsVerifier(M)
            .bind(D)
            .isValid(secret);

    assertThat(valid).isFalse();
  }
}