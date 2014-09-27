package com.googlecode.cryptogwt.provider;

import com.googlecode.cryptogwt.util.ByteArrayUtils;

public class SHA1MessageDigest extends MessageDigestSupport {
    
    public static final int SHA1_OUTPUT_LEN = 20;
    
    public static final int SHA1_BLOCK_SIZE = 64;
    
    private int h0;
    private int h1;
    private int h2;
    private int h3;
    private int h4;

    public SHA1MessageDigest() {
        super(SHA1_BLOCK_SIZE, SHA1_OUTPUT_LEN);
    }
    
    @Override
    protected void init() {
        h0 = 0x67452301;
        h1 = 0xEFCDAB89;
        h2 = 0x98BADCFE;
        h3 = 0x10325476;
        h4 = 0xC3D2E1F0;
        bufOffset = 0;
        bitsProcessed = 0;
    }
    
    private int rotl(int x, int n) {        
        return (x << n) | (x >>> (32 - n));
    }

    @Override
    protected void block(byte[] input, int offset, int len) {
        int[] w = new int[80]; 
        ByteArrayUtils.toIntegerArray(input, offset, len, w, 0);
        
        for (int t = 16; t < 80; t++) {
            w[t] = rotl(w[t-3] ^ w[t-8] ^ w[t-14] ^ w[t-16], 1);
        }
        
        int a = h0;
        int b = h1;
        int c = h2;
        int d = h3;
        int e = h4;
        
        int f, k;
        for (int t = 0; t < 80; t++) {
            if (t < 20) {
                f = (b & c) ^ ((~b) & d);
                k = 0x5A827999;
            } else if (t < 40) {
                f = b ^ c ^ d;
                k = 0x6ED9EBA1;
            } else if (t < 60) {
                f = (b & c) ^ (b & d) ^ (c & d);
                k = 0x8F1BBCDC;
            } else {
                f = b ^ c ^ d;
                k = 0xCA62C1D6;     
            }
            
            int temp = rotl(a, 5) + f + e + k + w[t];
            e = d;
            d = c;
            c = rotl(b, 30);
            b = a;
            a = temp;                
        }
        
        h0 += a;
        h1 += b;
        h2 += c;
        h3 += d;
        h4 += e;        
    }

    @Override
    protected byte[] doFinal() {        
        padWith1BitZerosThenLengthToBlockSize();
        return ByteArrayUtils.toBytes(h0, h1, h2, h3, h4);
    }

}
