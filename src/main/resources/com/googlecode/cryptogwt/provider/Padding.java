package com.googlecode.cryptogwt.provider;

import javax.crypto.BadPaddingException;

import javax.crypto.IllegalBlockSizeException;

public interface Padding {

    public int pad(byte[] input, int offset, int len, byte[] output,
            int outputOffset, int blockSize) throws IllegalBlockSizeException;

    public int depad(byte[] input, int offset, int len, byte[] output,
            int outputOffset, int blockSize) throws BadPaddingException;

}
