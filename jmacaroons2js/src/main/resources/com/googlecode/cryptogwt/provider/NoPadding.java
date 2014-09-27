package com.googlecode.cryptogwt.provider;

import javax.crypto.IllegalBlockSizeException;

public class NoPadding implements Padding {
    
    public int pad(byte[] input, int offset, int len, byte[] output, int outputOffset, int blocksize) throws IllegalBlockSizeException {
        if (len == 0) return 0;
        if (len < blocksize) throw new IllegalBlockSizeException("Must be a multiple of " + blocksize + "bytes");
        System.arraycopy(input, offset, output, outputOffset, blocksize);
        return blocksize;
    }
    
    public int depad(byte[] input, int offset, int len, byte[] output, int outputOffset, int blocksize) {
        if (len == 0) return 0;
        System.arraycopy(input, offset, output, outputOffset, len);
        return len;
    }

}
