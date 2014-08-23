package com.github.nitram509.jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsVerifierTest {

  private String identifier;
  private String secret;
  private String location;
  private Macaroon m;

  @BeforeMethod
  public void setUp() throws Exception {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";
  }

  @Test
  public void verification() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    MacaroonsVerifier verifier = new MacaroonsVerifier();
    assertThat(verifier.verify(m, secret)).isTrue();
  }

  @Test
  public void verification_with_first_party_caveat() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    MacaroonsVerifier verifier = new MacaroonsVerifier();

    assertThat(verifier.verify(m, secret)).isTrue();
  }

}