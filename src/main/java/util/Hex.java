package util;

public class Hex {

  private static final String ALPHABET = "0123456789abcdef";

  public static String toHex(byte[] bytes) {
    if (bytes == null) return null;
    if (bytes.length == 0) return "";
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(ALPHABET.charAt((b & 0xff) >> 4)).append(ALPHABET.charAt(b & 0xff & 0xf));
    }
    return sb.toString();
  }

}
