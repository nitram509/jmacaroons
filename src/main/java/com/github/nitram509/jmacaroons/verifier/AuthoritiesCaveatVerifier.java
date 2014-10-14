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

import com.github.nitram509.jmacaroons.GeneralCaveatVerifier;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

public class AuthoritiesCaveatVerifier implements GeneralCaveatVerifier {

  private String[] authorities;

  public AuthoritiesCaveatVerifier(String authority) {
    this.authorities = new String[]{authority};
  }

  @Override
  public boolean verifyCaveat(String caveat) {
    if (caveat.startsWith("authorities = ")) {
      return caveat.contains(authorities[0]);
    }
    return false;

  }

}
