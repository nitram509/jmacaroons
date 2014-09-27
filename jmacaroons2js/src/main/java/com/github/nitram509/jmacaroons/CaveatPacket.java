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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import java.io.Serializable;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.KEY_VALUE_SEPARATOR;

@Export
public class CaveatPacket implements Serializable, Exportable {

  public final Type type;
  public final String value;

  CaveatPacket(Type type, String value) {
    assert type != null;
    assert value != null;
    this.type = type;
    this.value = value;
  }

  public static enum Type {
    location,
    identifier,
    signature,
    cid,
    vid,
    cl
  }

  @Override
  public String toString() {
    return type.name() + KEY_VALUE_SEPARATOR + value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CaveatPacket that = (CaveatPacket) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
