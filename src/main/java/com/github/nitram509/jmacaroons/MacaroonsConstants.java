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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface MacaroonsConstants {

  /* public constants ... copied from libmacaroons */

  /**
   * All byte strings must be less than this length.
   * Enforced via "assert" internally.
   */
  int MACAROON_MAX_STRLEN = 32768;
  /**
   * Place a sane limit on the number of caveats
   */
  int MACAROON_MAX_CAVEATS = 65536;
  /**
   * Recommended secret length
   */
  int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  int MACAROON_HASH_BYTES = 32;

  /* ********************************* */
  /* more internal use ... */
  /* ********************************* */

  int PACKET_PREFIX_LENGTH = 4;
  int PACKET_MAX_SIZE = 65535;

  int MACAROON_SECRET_KEY_BYTES = 32;
  int MACAROON_SECRET_NONCE_BYTES = 24;

  /**
   * The number of zero bytes required by crypto_secretbox
   * before the plaintext.
   */
  int MACAROON_SECRET_TEXT_ZERO_BYTES = 32;
  /**
   * The number of zero bytes placed by crypto_secretbox
   * before the ciphertext
   */
  int MACAROON_SECRET_BOX_ZERO_BYTES = 16;

  int SECRET_BOX_OVERHEAD = MACAROON_SECRET_TEXT_ZERO_BYTES - MACAROON_SECRET_BOX_ZERO_BYTES;
  int VID_NONCE_KEY_SZ = MACAROON_SECRET_NONCE_BYTES + MACAROON_HASH_BYTES + SECRET_BOX_OVERHEAD;

  // used to encode identifier with respect to preserve special characters (like umlauts, etc.)
  Charset IDENTIFIER_CHARSET = StandardCharsets.UTF_8;

  // for maximal compatibility between strings and byte arrays for keys, use an encoding that is reversible
  Charset STRING_KEY_CHARSET = StandardCharsets.ISO_8859_1;

  String LOCATION = "location";
  byte[] LOCATION_BYTES = LOCATION.getBytes(STRING_KEY_CHARSET);

  String IDENTIFIER = "identifier";
  byte[] IDENTIFIER_BYTES = IDENTIFIER.getBytes(STRING_KEY_CHARSET);

  String SIGNATURE = "signature";
  byte[] SIGNATURE_BYTES = SIGNATURE.getBytes(STRING_KEY_CHARSET);

  String CID = "cid";
  byte[] CID_BYTES = CID.getBytes(STRING_KEY_CHARSET);

  String VID = "vid";
  byte[] VID_BYTES = VID.getBytes(STRING_KEY_CHARSET);

  String CL = "cl";
  byte[] CL_BYTES = CL.getBytes(STRING_KEY_CHARSET);

  char LINE_SEPARATOR = '\n';
  int LINE_SEPARATOR_LEN = 1;

  char KEY_VALUE_SEPARATOR = ' ';
  int KEY_VALUE_SEPARATOR_LEN = 1;

}
