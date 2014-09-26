package com.googlecode.cryptogwt.provider;

import java.security.MessageDigestSpi;

import com.google.gwt.core.client.JsArrayInteger;

import static com.googlecode.cryptogwt.provider.JsArrayUtils.*;

public class SHA256MessageDigest extends MessageDigestSpi {
    
    private static final int DIGEST_LEN = 32;
    
    private JsCryptoSHA256 delegate;
    
    public SHA256MessageDigest() {
        delegate = JsCryptoSHA256.newInstance();
    }
    
    @Override
    protected byte[] engineDigest() {
        JsArrayInteger result = delegate.digest();
        return toByteArray(result);        
    }
    
    @Override
    protected void engineReset() {
        delegate = JsCryptoSHA256.newInstance();
    }

    @Override
    protected void engineUpdate(byte input) {
        delegate.update(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        if (len <= 0) return;        
        for (int i=offset; i < offset + len; i++) {
            delegate.update(input[i]);
        }
    }

    @Override
    protected int engineGetDigestLength() {
        return DIGEST_LEN;
    }

}
