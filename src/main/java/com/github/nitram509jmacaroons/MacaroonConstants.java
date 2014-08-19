package com.github.nitram509jmacaroons;

public interface MacaroonConstants {

  /* public constanst ... copied from libmacaroons */

  public static final int MACAROON_MAX_STRLEN = 32768;
  public static final int MACAROON_MAX_CAVEATS = 65536;
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

  /* more internal use ... */

  static transient final String LOCATION = "location";
  static transient final String IDENTIFIER = "identifier";
  static transient final String SIGNATURE = "signature";
  static transient final String CID = "cid";
  static transient final String VID = "vid";
  static transient final String CL = "cl";

}
