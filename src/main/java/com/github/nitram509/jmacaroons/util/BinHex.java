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

package com.github.nitram509.jmacaroons.util;

import static java.lang.Character.digit;

public class BinHex {

  private static final char[] ALPHABET = "0123456789abcdef".toCharArray();

  /**
   * @param bytes bytes
   * @return
   */
  public static String bin2hex(byte... bytes) {
    if (bytes == null) return null;
    char[] hex = new char[bytes.length * 2];
    int counter = 0;
    for (byte b : bytes) {
      hex[counter++] = ALPHABET[(b & 0xff) >> 4];
      hex[counter++] = ALPHABET[(b & 0xff) & 0xf];
    }
    return new String(hex);
  }

  /**
   * @param hexString hexString all lower case, 0-9 and a-f only, even length only, null safe
   * @return
   */
  public static byte[] hex2bin(String hexString) {
    if (hexString == null) return null;
    int len = hexString.length();
    byte[] bin = new byte[len >> 1];
    for (int i = 0; i < len; i += 2) {
      bin[i >> 1] = (byte) ((digit((int) hexString.charAt(i), 16) << 4) + digit((int) hexString.charAt(i + 1), 16));
    }
    return bin;
  }
}
