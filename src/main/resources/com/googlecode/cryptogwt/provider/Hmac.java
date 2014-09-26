package com.googlecode.cryptogwt.provider;

import java.util.Arrays;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

public class Hmac extends MacSpi {
    
    public static class SHA256 extends Hmac {
        public SHA256() throws NoSuchAlgorithmException {
            super("SHA-256", 64);            
        }
    }
    
    public static class SHA1 extends Hmac {
        public SHA1() throws NoSuchAlgorithmException {
            super("SHA1", 64);            
        }
    }
    
    private MessageDigest digest;        
    
    private byte[] ipad;
    
    private byte[] opad;
    
    private int digestLen;
    
    private int blockSize;
    
    public Hmac(String algorithm, int blockSize) throws NoSuchAlgorithmException {        
        this.digest = MessageDigest.getInstance(algorithm);        
        this.digestLen = digest.getDigestLength();
        this.blockSize = blockSize;
    }

    @Override
    protected byte[] engineDoFinal() {       
        byte[] inner = digest.digest();
        digest.update(opad);
        byte[] result = digest.digest(inner);
        engineReset();
        return result;
    }

    @Override
    protected int engineGetMacLength() {
        return digestLen;
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        digest.reset();
        this.opad = new byte[blockSize];
        this.ipad = new byte[blockSize];
        Arrays.fill(opad, (byte)0x5c);
        Arrays.fill(ipad, (byte)0x36);
        byte[] blockKey = expandOrReduceKeyToBlocksize(key.getEncoded());
        ByteArrayUtils.xor(opad, blockKey);               
        ByteArrayUtils.xor(ipad, blockKey);
        engineReset();
    }

    private byte[] expandOrReduceKeyToBlocksize(byte[] inputKey) {
        byte[] result = new byte[blockSize];        
        if (inputKey.length > blockSize) {
            inputKey = digest.digest(inputKey);
        }
        System.arraycopy(inputKey, 0, result, 0, Math.min(inputKey.length, result.length));        
        return result;
    }

    @Override
    protected void engineReset() {
        digest.reset();        
        digest.update(ipad);
    }

    @Override
    protected void engineUpdate(byte input) {        
        digest.update(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {       
       digest.update(input, offset, len);
    }
}
