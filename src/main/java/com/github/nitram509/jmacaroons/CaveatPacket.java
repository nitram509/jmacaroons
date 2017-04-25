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

import com.github.nitram509.jmacaroons.util.Base64;

import java.io.Serializable;
import java.util.Arrays;

import static com.github.nitram509.jmacaroons.CryptoTools.safeEquals;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.IDENTIFIER_CHARSET;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.KEY_VALUE_SEPARATOR;

public class CaveatPacket implements Serializable {

  public final Type type;
  public final byte[] rawValue;

  private String valueAsText;

  CaveatPacket(Type type, byte[] rawValue) {
    assert type != null;
    assert rawValue != null;
    this.type = type;
    this.rawValue = rawValue;
  }

  CaveatPacket(Type type, String valueAsText) {
    assert type != null;
    assert type != Type.vid : "VIDs should be used as raw bytes, because otherwise UTF8 string encoder would break it";
    assert valueAsText != null;
    this.type = type;
    this.rawValue = valueAsText.getBytes(IDENTIFIER_CHARSET);
  }

  public Type getType() {
    return type;
  }

  public byte[] getRawValue() {
    return rawValue;
  }

  public String getValueAsText() {
    if (valueAsText == null) {
      valueAsText = (type == Type.vid)
              ? Base64.encodeUrlSafeToString(rawValue)
              : new String(rawValue, IDENTIFIER_CHARSET);
    }
    return valueAsText;

  }

  @Override
  public String toString() {
    return type.name() + KEY_VALUE_SEPARATOR + (Arrays.toString(rawValue));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CaveatPacket that = (CaveatPacket) o;

    if (!safeEquals(rawValue, that.rawValue)) return false;
    if (type != that.type) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (rawValue != null ? Arrays.hashCode(rawValue) : 0);
    return result;
  }

  public enum Type {
    location,
    identifier,
    signature,
    cid,
    vid,
    cl
  }
}
