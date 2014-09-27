package com.github.nitram509.jmacaroons.util;

import java.io.UnsupportedEncodingException;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.IDENTIFIER_CHARSET;

public class StringUtil {

  public static byte[] getBytes(String s) {
    if (s == null) {
      return new byte[0];
    }
    try {
      return s.getBytes(IDENTIFIER_CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String asString(byte[] bytes) {
    try {
      return new String(bytes, IDENTIFIER_CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

}
