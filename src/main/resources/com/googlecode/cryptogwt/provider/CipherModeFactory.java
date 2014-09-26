package com.googlecode.cryptogwt.provider;

import java.security.NoSuchAlgorithmException;

public class CipherModeFactory {
       
    public static CipherMode getInstance(String mode) throws NoSuchAlgorithmException {
        if ("CBC".equals(mode)) return new CbcCipherMode();
        if ("ECB".equals(mode)) return new EcbCipherMode();
        if ("CTR".equals(mode)) return new CtrCipherMode();        
        throw new NoSuchAlgorithmException(mode);
    }

}
