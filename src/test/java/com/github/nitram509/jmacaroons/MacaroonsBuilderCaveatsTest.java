package com.github.nitram509.jmacaroons;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderCaveatsTest {

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
  public void add_first_party_caveat() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    Macaroon macaroon = MacaroonsBuilder.modify(m, secret)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    assertThat(macaroon.identifier).isEqualTo(m.identifier);
    assertThat(macaroon.location).isEqualTo(m.location);
    assertThat(macaroon.caveats).isEqualTo(new String[]{"account = 3735928559"});
    assertThat(macaroon.signature).isEqualTo("1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128");
  }

  @Test
  public void add_first_party_caveat_3_times() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    Macaroon macaroon = MacaroonsBuilder.modify(m, secret)
        .add_first_party_caveat("account = 3735928559")
        .add_first_party_caveat("time < 2015-01-01T00:00")
        .add_first_party_caveat("email = alice@example.org")
        .getMacaroon();

    assertThat(macaroon.identifier).isEqualTo(m.identifier);
    assertThat(macaroon.location).isEqualTo(m.location);
    assertThat(macaroon.caveats).isEqualTo(new String[]{"account = 3735928559", "time < 2015-01-01T00:00", "email = alice@example.org"});
    assertThat(macaroon.signature).isEqualTo("882e6d59496ed5245edb7ab5b8839ecd63e5d504e54839804f164070d8eed952");
  }

  @Test
  public void add_first_party_caveat_German_umlauts_using_UTF8_encoding() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    Macaroon macaroon = MacaroonsBuilder.modify(m, secret)
        .add_first_party_caveat("ä")
        .add_first_party_caveat("ü")
        .add_first_party_caveat("ö")
        .getMacaroon();

    assertThat(macaroon.identifier).isEqualTo(m.identifier);
    assertThat(macaroon.location).isEqualTo(m.location);
    assertThat(macaroon.caveats).isEqualTo(new String[]{"ä", "ü", "ö"});
    assertThat(macaroon.signature).isEqualTo("e38cce985a627fbfaea3490ca184fb8c59ec2bd14f0adc3b5035156e94daa111");
  }

  @Test
  public void add_first_party_caveat_null_save() {
    m = MacaroonsBuilder.create(location, secret, identifier);

    Macaroon macaroon = MacaroonsBuilder.modify(m, secret)
        .add_first_party_caveat(null)
        .getMacaroon();

    assertThat(macaroon.identifier).isEqualTo(m.identifier);
    assertThat(macaroon.location).isEqualTo(m.location);
    assertThat(macaroon.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void add_first_party_caveat_inspect() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    String inspect = m.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "cid account = 3735928559\n" +
            "signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128\n"
    );
  }

}