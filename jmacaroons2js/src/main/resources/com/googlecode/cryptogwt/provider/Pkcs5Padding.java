package com.googlecode.cryptogwt.provider;

import java.util.Arrays;

import javax.crypto.BadPaddingException;

import javax.crypto.IllegalBlockSizeException;

public class Pkcs5Padding implements Padding {

    public int depad(byte[] input, int offset, int len, byte[] output,
            int outputOffset, int blockSize) throws BadPaddingException {
        // Last byte will indicate pad
        int padLen = input[offset + len - 1];
        if (padLen > blockSize || padLen < 0) throw new BadPaddingException("Invalid padding byte: " + padLen);
        int depaddedLen = len - padLen;
        System.arraycopy(input, offset, output, outputOffset, depaddedLen);
        return depaddedLen;
    }

    public int pad(byte[] input, int offset, int len, byte[] output,
            int outputOffset, int blockSize) throws IllegalBlockSizeException {
        assert len < blockSize;
        System.arraycopy(input, offset, output, outputOffset, len);
        byte remainder = (byte) (blockSize - (len % blockSize));
        outputOffset += len;
        Arrays.fill(output, outputOffset, outputOffset + remainder, remainder);
        return blockSize;
    }

}
