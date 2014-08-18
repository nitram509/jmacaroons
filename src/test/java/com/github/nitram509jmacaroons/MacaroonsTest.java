package com.github.nitram509jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsTest {

  private String publicKey;
  private String secret;
  private String location;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank";
    secret = "this is our super secret key; only we should know it";
    publicKey = "we used out secret key";
  }

  @Test
  public void create_a_Macaroon() {
    Macaroons macaroons = Macaroons.create(location, secret, publicKey);

    assertThat(macaroons.getLocation()).isEqualTo("location");
    assertThat(macaroons.getSignature()).isEqualTo("270f5305f178ae1451f253416ade32c6774297059a6af3882d2cf4859b702e2e");
  }

}