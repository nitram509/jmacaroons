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

import com.github.nitram509.jmacaroons.CaveatPacket;
import com.github.nitram509.jmacaroons.GeneralCaveatVerifier;

import java.util.List;

public class ArrayTools {

  public static CaveatPacket[] appendToArray(CaveatPacket[] elements, CaveatPacket... newElements) {
    assert newElements != null;
    CaveatPacket[] tmp = new CaveatPacket[elements.length + newElements.length];
    System.arraycopy(elements, 0, tmp, 0, elements.length);
    System.arraycopy(newElements, 0, tmp, elements.length, newElements.length);
    return tmp;
  }

  public static String[] appendToArray(String[] elements, String newElement) {
    assert newElement != null;
    String[] tmp = new String[elements.length + 1];
    System.arraycopy(elements, 0, tmp, 0, elements.length);
    tmp[elements.length] = newElement;
    return tmp;
  }

  public static GeneralCaveatVerifier[] appendToArray(GeneralCaveatVerifier[] elements, GeneralCaveatVerifier newElement) {
    assert newElement != null;
    GeneralCaveatVerifier[] tmp = new GeneralCaveatVerifier[elements.length + 1];
    System.arraycopy(elements, 0, tmp, 0, elements.length);
    tmp[elements.length] = newElement;
    return tmp;
  }

  public static boolean containsElement(String[] elements, String anElement) {
    if (elements != null) {
      for (String element : elements) {
        if (element.equals(anElement)) return true;
      }
    }
    return false;
  }

  public static byte[] flattenByteArray(List<byte[]> packets) {
    int size = 0;
    for (byte[] packet : packets) {
      size += packet.length;
    }
    byte[] alldata = new byte[size];
    size = 0;
    for (byte[] packet : packets) {
      System.arraycopy(packet, 0, alldata, size, packet.length);
      size += packet.length;
    }
    return alldata;
  }
}
