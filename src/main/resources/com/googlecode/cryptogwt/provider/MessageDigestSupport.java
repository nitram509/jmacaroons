package com.googlecode.cryptogwt.provider;

import java.security.MessageDigestSpi;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

public abstract class MessageDigestSupport extends MessageDigestSpi {

    protected final int blockSize;
    protected final int outputLen;
    private final byte[] buffer;    
    protected int bufOffset = 0;
    protected long bitsProcessed = 0;

    public MessageDigestSupport(int blockSize, int outputLen) {
        this.blockSize = blockSize;
        this.outputLen = outputLen;
        this.buffer = new byte[blockSize];
        init();
    }

    @Override
    protected byte[] engineDigest() {        
        byte[] result = doFinal();        
        engineReset();
        return result;
    }
    
    protected abstract byte[] doFinal();
      

    @Override
    protected void engineReset() {
        init();
    }

    @Override
    protected void engineUpdate(byte input) {
        bitsProcessed += 8;
        update(input);
    
    }

    protected void update(byte input) {
        buffer[bufOffset++] = input;
        if (bufOffset == blockSize) {
            block(buffer, 0, blockSize);
            bufOffset = 0;
        }
    }

    protected abstract void block(byte[] input, int offset, int len);

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        bitsProcessed += (len * 8);
        update(input, offset, len);
    }

    protected void update(byte[] input, int offset, int len) {
        if (bufOffset != 0) {
            int lenToCopy = Math.min(len, blockSize - bufOffset);
            System.arraycopy(input, offset, buffer, bufOffset, lenToCopy);
            offset += lenToCopy;
            len -= lenToCopy;
            bufOffset += lenToCopy;
            if (bufOffset == blockSize) {
                block(buffer, 0, blockSize);
                bufOffset = 0;
            }
        }
        if (len == 0) return;
        while (len >= blockSize) {
            block(input, offset, blockSize);
            len -= blockSize;
            offset += blockSize;
        }
        
        System.arraycopy(input, offset, buffer, 0, len);
        bufOffset = len;
    }

    @Override
    protected int engineGetDigestLength() {
        return outputLen;
    }

    protected void padWith1BitZerosThenLengthToBlockSize() {
        update((byte)0x80);
        int zerosToAppend = blockSize - bufOffset - 8;
        if (zerosToAppend < 0) {
            zerosToAppend += blockSize;
        }        
        update(new byte[zerosToAppend], 0, zerosToAppend);
        update(ByteArrayUtils.toBytes(bitsProcessed), 0, 8);
        assert bufOffset == 0 : "Should have consumed all bytes but still has: " + bufOffset;
    }

    protected abstract void init();

}