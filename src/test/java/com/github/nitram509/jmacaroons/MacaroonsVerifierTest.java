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

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.nitram509.jmacaroons.util.BinHex.hex2bin;
import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsVerifierTest {

  private static SimpleDateFormat ISO_DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  private String identifier;
  private String secret;
  private String location;
  private Macaroon m;
  private byte[] secretBytes;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    secretBytes = hex2bin("a96173391e6bfa0356bbf095621b8af1510968e770e4d27d62109b7dc374814b");
    identifier = "we used our secret key";
  }

  @Test
  public void verification() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secret)).isTrue();
  }

  @Test
  public void verification_with_byteArray() {
    m = new MacaroonsBuilder(location, secretBytes, identifier).getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secretBytes)).isTrue();
  }

  @Test(expectedExceptions = MacaroonValidationException.class)
  public void verification_assertion() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    verifier.assertIsValid("wrong secret");

    // expect MacaroonValidationException
  }

  @Test
  public void verification_satisfy_exact_first_party_caveat() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secret)).isFalse();

    verifier.satisfyExact("account = 3735928559");
    assertThat(verifier.isValid(secret)).isTrue();
  }

  @Test
  public void verification_satisfy_exact_required_first_party_caveat_() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .add_first_party_caveat("credit_allowed = true")
        .getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secret)).isFalse();

    verifier.satisfyExact("account = 3735928559");
    assertThat(verifier.isValid(secret)).isFalse();
  }

  @Test
  public void verification_satisfy_exact_attenuate_with_additional_caveats() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secret)).isFalse();

    verifier.satisfyExact("account = 3735928559");
    verifier.satisfyExact("IP = 127.0.0.1')");
    verifier.satisfyExact("browser = Chrome')");
    verifier.satisfyExact("action = deposit");
    assertThat(verifier.isValid(secret)).isTrue();
  }

  @Test
  public void verification_general() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("time < " + createTimeStamp1WeekInFuture())
        .getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier(m);
    assertThat(verifier.isValid(secret)).isFalse();

    verifier.satisfyGeneral(new TimestampCaveatVerifier());
    assertThat(verifier.isValid(secret)).isTrue();
  }

  private String createTimeStamp1WeekInFuture() {
    return ISO_DateFormat.format(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7)));
  }

}
