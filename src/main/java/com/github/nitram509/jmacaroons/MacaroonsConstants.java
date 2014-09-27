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

import static com.github.nitram509.jmacaroons.util.StringUtil.getBytes;

public interface MacaroonsConstants {

  /* public constants ... copied from libmacaroons */

  /**
   * All byte strings must be less than this length.
   * Enforced via "assert" internally.
   */
  public static final int MACAROON_MAX_STRLEN = 32768;
  /**
   * Place a sane limit on the number of caveats
   */
  public static final int MACAROON_MAX_CAVEATS = 65536;
  /**
   * Recommended secret length
   */
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

  /* more internal use ... */
  static final int PACKET_PREFIX_LENGTH = 4;
  static final int PACKET_MAX_SIZE = 65535;

  static final int MACAROON_SECRET_KEY_BYTES = 32;
  static final int MACAROON_SECRET_NONCE_BYTES = 24;
  static final int MACAROON_SECRET_TEXT_ZERO_BYTES = 32;
  static final int MACAROON_SECRET_BOX_ZERO_BYTES = 16;

  static final int VID_NONCE_KEY_SZ = MACAROON_SECRET_NONCE_BYTES + MACAROON_HASH_BYTES + MACAROON_SECRET_TEXT_ZERO_BYTES - MACAROON_SECRET_BOX_ZERO_BYTES;

  static final String LOCATION = "location";
  static final byte[] LOCATION_BYTES = getBytes(LOCATION);

  static final String IDENTIFIER = "identifier";
  static final byte[] IDENTIFIER_BYTES = getBytes(IDENTIFIER);

  static final String SIGNATURE = "signature";
  static final byte[] SIGNATURE_BYTES = getBytes(SIGNATURE);

  static final String CID = "cid";
  static final byte[] CID_BYTES = getBytes(CID);

  static final String VID = "vid";
  static final byte[] VID_BYTES = getBytes(VID);

  static final String CL = "cl";
  static final byte[] CL_BYTES = getBytes(CL);

  static final String LINE_SEPARATOR = "\n";
  static final String KEY_VALUE_SEPARATOR = " ";

  static final String IDENTIFIER_CHARSET = "UTF-8";
}
