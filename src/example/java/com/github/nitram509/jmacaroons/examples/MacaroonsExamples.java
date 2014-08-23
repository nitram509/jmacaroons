package com.github.nitram509.jmacaroons.examples;

import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsBuilder;

/**
 * These are examples, for copy&paste into README.md.
 * They should be correct and compilable.
 */
public class MacaroonsExamples {

  public void create() {
    String location = "http://www.example.org";
    String secretKey = "this is our super secret key; only we should know it";
    String identifier = "we used our secret key";
    Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
    System.out.println(macaroon.inspect());
  }

  public static void main(String[] args) {
    MacaroonsExamples examples = new MacaroonsExamples();
    examples.create();
  }

}
