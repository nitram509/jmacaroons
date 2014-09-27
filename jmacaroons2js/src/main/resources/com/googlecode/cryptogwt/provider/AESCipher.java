package com.googlecode.cryptogwt.provider;

public class AESCipher implements BlockCipherAlgorithm {

    protected int mode;
        
    protected static final int BLOCKSIZE = 16;
    
    private static final int KEYSIZE = 16;    
    
    JsCryptoNativeAES nativeAes;

    public AESCipher() {        
    }
    
    public int getBlockSize() {
        return BLOCKSIZE;
    }

    public int getKeySize() {
        return KEYSIZE;
    }

    public void setKey(byte[] key) {        
        nativeAes = JsCryptoNativeAES.newInstance(key);
    }
    
    public void encryptBlock(byte[] input, int inputOffset, byte[] output,
            int outputOffset) {
        assert nativeAes != null;        
        JsArrayUtils.toByteArray(nativeAes.encrypt(JsArrayUtils.toJsArrayInteger(input, inputOffset, BLOCKSIZE)),
                output, outputOffset);
    }
    
    public void decryptBlock(byte[] input, int inputOffset, byte[] output,
            int outputOffset) {
        assert nativeAes != null;
        JsArrayUtils.toByteArray(nativeAes.decrypt(JsArrayUtils.toJsArrayInteger(input, inputOffset, BLOCKSIZE)),
                output, outputOffset);
    }
       
 }