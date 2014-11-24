package com.github.nitram509.jmacaroons.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class Base64Test {

  @DataProvider(name = "URL_safe_base64_strings_and_bytes")
  public static Object[][] URL_safe_base64_strings_and_bytes() {
    return new Object[][]{
        {"-A", (byte) 0xf8},
        {"-Q", (byte) 0xf9},
        {"-g", (byte) 0xfa},
        {"-w", (byte) 0xfb},
        {"_A", (byte) 0xfc},
        {"_Q", (byte) 0xfd},
        {"_g", (byte) 0xfe},
        {"_w", (byte) 0xff},
    };
  }

  @DataProvider(name = "regular_base64_strings_and_bytes")
  public static Object[][] regular_base64_strings_and_bytes() {
    return new Object[][]{
        {"+A", (byte) 0xf8},
        {"+Q", (byte) 0xf9},
        {"+g", (byte) 0xfa},
        {"+w", (byte) 0xfb},
        {"/A", (byte) 0xfc},
        {"/Q", (byte) 0xfd},
        {"/g", (byte) 0xfe},
        {"/w", (byte) 0xff},
    };
  }

  @Test(dataProvider = "URL_safe_base64_strings_and_bytes")
  public void decoder_works_with_URL_safe_alphabet(String base64str, byte expected) throws Exception {
    byte[] actual = Base64.decode(base64str);
    assertThat(actual).isEqualTo(new byte[]{expected});
  }

  @Test(dataProvider = "regular_base64_strings_and_bytes")
  public void decoder_works_with_regular_alphabet(String base64str, byte expected) throws Exception {
    byte[] actual = Base64.decode(base64str);
    assertThat(actual).isEqualTo(new byte[]{expected});
  }

  @Test(dataProvider = "URL_safe_base64_strings_and_bytes")
  public void encoder_produces_URL_safe_bytes_without_padding(String expectedString, byte b) throws Exception {
    String actual = Base64.encodeUrlSafeToString(new byte[]{b});
    assertThat(actual).isEqualTo(expectedString);
  }
}