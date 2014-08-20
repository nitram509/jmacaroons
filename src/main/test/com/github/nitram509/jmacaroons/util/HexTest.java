package com.github.nitram509.jmacaroons.util;

import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class HexTest {

  @Test
  public void happy_path() {
    String hexstr = Hex.toHex((byte) 1, (byte) 128, (byte) 255);

    assertThat(hexstr).isEqualTo("0180ff");
  }

  @Test
  public void null_safe() {
    String hexstr = Hex.toHex(null);

    assertThat(hexstr).isNull();
  }

  @Test
  public void empty_string() {
    String hexstr = Hex.toHex();

    assertThat(hexstr).isEqualTo("");
  }

}