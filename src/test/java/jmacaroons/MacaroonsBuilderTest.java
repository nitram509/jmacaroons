package jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderTest {

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
  public void create_a_Macaroon_and_verify_signature_location_and_identfier() throws InvalidKeyException, NoSuchAlgorithmException {
    m = MacaroonsBuilder.create(location, secret, identifier);

    assertThat(m.location).isEqualTo(location);
    assertThat(m.identifier).isEqualTo(identifier);
    assertThat(m.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void create_a_Macaroon_and_inspect() throws InvalidKeyException, NoSuchAlgorithmException {
    m = MacaroonsBuilder.create(location, secret, identifier);

    String inspect = m.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f\n"
    );
  }

  @Test
  public void different_locations_doesnt_change_the_signatures() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon m1 = MacaroonsBuilder.create("http://location_ONE", secret, identifier);
    Macaroon m2 = MacaroonsBuilder.create("http://location_TWO", secret, identifier);

    assertThat(m1.signature).isEqualTo(m2.signature);
  }

  @Test
  public void Macaroon_can_be_serialized() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    assertThat(m.serialize()).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo=");
  }

}