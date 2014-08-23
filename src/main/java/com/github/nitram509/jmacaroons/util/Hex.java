package com.github.nitram509.jmacaroons.util;

public class Hex {

  private static final char[] ALPHABET = "0123456789abcdef".toCharArray();

  public static String toHex(byte... bytes) {
    if (bytes == null) return null;
    char[] hex = new char[bytes.length * 2];
    int counter = 0;
    for (byte b : bytes) {
      hex[counter++] = ALPHABET[(b & 0xff) >> 4];
      hex[counter++] = ALPHABET[(b & 0xff) & 0xf];
    }
    return new String(hex);
  }

}
