package com.github.nitram509.jmacaroons.examples;

import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsBuilder;
import com.github.nitram509.jmacaroons.MacaroonsVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * These are examples, for copy&paste into README.md.
 * They should be correct and compilable.
 */
public class MacaroonsExamples {

  private Macaroon create() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
    System.out.println(macaroon.inspect());

    return macaroon;
  }

  private void serialize() {
    Macaroon macaroon = create();

    String serialized = macaroon.serialize();
    System.out.println("Serialized: " + serialized);
  }

  private void verify() throws InvalidKeyException, NoSuchAlgorithmException {
    Macaroon macaroon = create();

    MacaroonsVerifier verifier = new MacaroonsVerifier();
    String secret = "this is our super secret key; only we should know it";
    boolean valid = verifier.verify(macaroon, secret);
    System.out.println("Macaroon is " + (valid ? "Valid" : "Invalid"));
  }

  public static void main(String[] args) {
    MacaroonsExamples examples = new MacaroonsExamples();
    try {
      examples.create();
      examples.serialize();
      examples.verify();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
