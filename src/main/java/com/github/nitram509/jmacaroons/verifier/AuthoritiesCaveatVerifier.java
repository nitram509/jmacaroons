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

import java.util.HashSet;

/**
 * A verifier that is able to verify for given authorities.
 * These authorities are comma separated list of string, which are case sensitive.
 */
public class AuthoritiesCaveatVerifier implements GeneralCaveatVerifier {

  public static final String CAVEAT_PREFIX = "authorities =";

  private String[] requestedAuthorities;

  /**
   * @param requestedAuthorities requestedAuthorities
   */
  public AuthoritiesCaveatVerifier(String... requestedAuthorities) {
    this.requestedAuthorities = requestedAuthorities != null ? requestedAuthorities : new String[0];
  }

  /**
   * A comfort method for better readability - simply returns 'new AuthoritiesCaveatVerifier(...)'
   * @param requestedAuthorities requestedAuthorities
   * @return a new instance of {@link com.github.nitram509.jmacaroons.verifier.AuthoritiesCaveatVerifier}
   */
  public static AuthoritiesCaveatVerifier hasAuthority(String... requestedAuthorities) {
    return new AuthoritiesCaveatVerifier(requestedAuthorities);
  }

  @Override
  public boolean verifyCaveat(String caveat) {
    boolean containsGivenAuthorities = false;
    if (caveat.startsWith(CAVEAT_PREFIX)) {
      HashSet<String> cavaetAuthorities = asTrimmedSet(caveat.substring(CAVEAT_PREFIX.length()).split("[,]"));
      containsGivenAuthorities = requestedAuthorities.length > 0;
      for (String authority : requestedAuthorities) {
        containsGivenAuthorities = containsGivenAuthorities && cavaetAuthorities.contains(authority);
      }
    }
    return containsGivenAuthorities;
  }

  private HashSet<String> asTrimmedSet(String[] cavaetAuthorities) {
    HashSet<String> result = new HashSet<>( cavaetAuthorities.length );
    for (String cavaetAuthority : cavaetAuthorities) {
      result.add(cavaetAuthority.trim());
    }
    return result;
  }

}
