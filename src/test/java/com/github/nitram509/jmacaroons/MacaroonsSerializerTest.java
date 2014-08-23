package com.github.nitram509.jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsSerializerTest {

  private String identifier;
  private String secret;
  private String location;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";
  }

  @Test
  public void Macaroon_can_be_serialized() {
    Macaroon m = MacaroonsBuilder.create(location, secret, identifier);

    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo=");
    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_with_caveat_can_be_serialized() {
    Macaroon m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDJmc2lnbmF0dXJlIB7+R2PykNvODB0IR3Nn4R9O7kVqZJM89mLXl3LbuCEoCg==");
    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void convert_bytes_to_String_and_back_to_bytes_will_NOT_change_the_code_points() {
    byte[] bytes = new byte[256];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) i;
    }

    String tmpString = new String(bytes, MacaroonsSerializer.ISO8859);
    byte[] actualBytes = tmpString.getBytes(MacaroonsSerializer.ISO8859);

    assertThat(actualBytes).isEqualTo(bytes);
  }
}