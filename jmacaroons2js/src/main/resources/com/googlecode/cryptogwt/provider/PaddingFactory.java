package com.googlecode.cryptogwt.provider;

import javax.crypto.NoSuchPaddingException;

public class PaddingFactory {

    public static Padding getInstance(String padding) throws NoSuchPaddingException {
        if ("NoPadding".equals(padding)) return new NoPadding();
        if ("PKCS5Padding".equals(padding)) return new Pkcs5Padding();
        throw new NoSuchPaddingException(padding);
    }

}
