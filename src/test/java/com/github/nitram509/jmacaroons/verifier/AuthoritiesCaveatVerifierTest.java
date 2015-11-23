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

package com.github.nitram509.jmacaroons.verifier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AuthoritiesCaveatVerifierTest {

  private AuthoritiesCaveatVerifier verifier;

  @BeforeMethod
  public void setUp() throws Exception {
    verifier = new AuthoritiesCaveatVerifier();
  }

  @DataProvider(name = "SingleAuthorityCombinations")
  public static Object[][] SingleAuthorityCombinations_for_ADMIN() {
    return new Object[][]{
        {"time < 2014-10-10", new String[]{"ADMIN"}, false},
        {"authorities = ABC", new String[]{"ADMIN"}, false},
        {"authorities = ADMIN", new String[]{"ADMIN"}, true},
        {"authorities = NOADMIN", new String[]{"ADMIN"}, false},
        {"authorities = FOO, ADMIN", new String[]{"ADMIN"}, true},
        {"authorities =", new String[]{"ADMIN"}, false},
        {"authorities = FOO,BAR,FOO, BAR, ADMIN", new String[]{"ADMIN", "FOO"}, true},
        {"authorities = FOO", new String[]{"ADMIN", "FOO"}, false},
        {"authorities = ,,,FOO,,, ,", new String[]{"FOO"}, true},
        {"authorities = ,,,foo,,, ,", new String[]{"FOO"}, false}
    };
  }

  @Test(dataProvider = "SingleAuthorityCombinations")
  public void verify_a_single_authority_by_name(String sampleCaveat, String[] authorityToHave, Boolean isValid) throws Exception {
    verifier = new AuthoritiesCaveatVerifier(authorityToHave);

    boolean actual = verifier.verifyCaveat(sampleCaveat);
    assertThat(actual).isEqualTo(isValid);
  }
}