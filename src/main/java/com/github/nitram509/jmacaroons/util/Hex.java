package com.github.nitram509.jmacaroons.util;

public class Hex {

  private static final char[] ALPHABET = "0123456789abcdef".toCharArray();

  public static String toHex(byte... bytes) {
    if (bytes == null) return null;
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(ALPHABET[(b & 0xff) >> 4]).append(ALPHABET[(b & 0xff) & 0xf]);
    }
    return sb.toString();
  }

}
