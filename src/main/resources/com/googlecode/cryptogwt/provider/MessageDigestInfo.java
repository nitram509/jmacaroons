package com.googlecode.cryptogwt.provider;

public class MessageDigestInfo {

    public static int getBlockSize(String algorithm) {
        if (("SHA-256").equals(algorithm)) {
            return 64;
        }
        if (("SHA1").equals(algorithm)) {
            return 64;
        }
        throw new IllegalArgumentException("Invalid algorithm: " + algorithm);
    }

}
