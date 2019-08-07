package com.github.nitram509.jmacaroons.examples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MacaroonsExamplesTest {

  private MacaroonsExamples macaroonsExamples;

  @BeforeMethod
  public void setUp() {
    macaroonsExamples = new MacaroonsExamples();
  }

  @Test
  public void testExamplesRunWithoutErrors() throws NoSuchAlgorithmException, InvalidKeyException {
    macaroonsExamples.create();
    macaroonsExamples.serialize();
    macaroonsExamples.deserialize();
    macaroonsExamples.verify();
    macaroonsExamples.addCaveat();
    macaroonsExamples.addCaveat_modify();
    macaroonsExamples.verify_required_caveats();
    macaroonsExamples.verify_general_caveats();
    macaroonsExamples.with_3rd_party_caveats();
    macaroonsExamples.timestamp_verifier();
    macaroonsExamples.authorities_verifier();
  }

}