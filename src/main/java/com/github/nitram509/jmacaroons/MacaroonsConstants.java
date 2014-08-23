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

public interface MacaroonsConstants {

  /* public constants ... copied from libmacaroons */

  /**
   *  All byte strings must be less than this length.
   *  Enforced via "assert" internally.
   */
  public static final int MACAROON_MAX_STRLEN = 32768;
  /**
   *  Place a sane limit on the number of caveats
   */
  public static final int MACAROON_MAX_CAVEATS = 65536;
  /**
   *  Recommended secret length
   */
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

  /* more internal use ... */
  static final int PACKET_PREFIX = 4;

  static final String LOCATION = "location";
  static final String IDENTIFIER = "identifier";
  static final String SIGNATURE = "signature";
  static final String CID = "cid";
  static final String VID = "vid";
  static final String CL = "cl";

  static final String LINE_SEPARATOR = "\n";
  static final String KEY_VALUE_SEPARATOR = " ";
}
