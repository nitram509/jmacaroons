package com.github.nitram509.jmacaroons;

public interface MacaroonsConstants {

  /* public constanst ... copied from libmacaroons */

  public static final int MACAROON_MAX_STRLEN = 32768;
  public static final int MACAROON_MAX_CAVEATS = 65536;
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

  /* more internal use ... */

  static final String LOCATION = "location";
  static final String IDENTIFIER = "identifier";
  static final String SIGNATURE = "signature";
  static final String CID = "cid";
  static final String VID = "vid";
  static final String CL = "cl";

}
