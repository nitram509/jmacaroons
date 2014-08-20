package com.github.nitram509.jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderTest {

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
  public void create_a_Macaroon_and_verify_signature_location_and_identfier() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroons = MacaroonsBuilder.create(location, secret, identifier);

    assertThat(macaroons.location).isEqualTo(location);
    assertThat(macaroons.identifier).isEqualTo(identifier);
    assertThat(macaroons.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void create_a_Macaroon_and_inspect() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroons = MacaroonsBuilder.create(location, secret, identifier);

    String inspect = macaroons.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f\n"
    );
  }

}