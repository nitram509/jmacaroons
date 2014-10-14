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
    verifier = new AuthoritiesCaveatVerifier(null);
  }

  @DataProvider(name = "SingleAuthorityCombinations_for_ADMIN")
  public static Object[][] SingleAuthorityCombinations_for_ADMIN() {
    return new Object[][]{
            {"time < 2014-10-10", false},
            {"authorities = ABC", false},
            {"authorities = ADMIN", true},
            {"authorities = NOADMIN", false}
    };
  }

  @Test(dataProvider = "SingleAuthorityCombinations_for_ADMIN")
  public void verify_a_single_authority_by_name(String sampleCaveat, Boolean isValid) throws Exception {
    verifier = new AuthoritiesCaveatVerifier("ADMIN");

    boolean actual = verifier.verifyCaveat(sampleCaveat);
    assertThat(actual).isEqualTo(isValid);
  }
}