package com.googlecode.cryptogwt.provider;

public interface CipherMode {
    
    public void setIV(byte[] iv);
    
    public String getName();
    
    void doBlock(int opmode, BlockCipherAlgorithm alg, byte[] input,
            int offset, byte[] output, int outputOffset, int blocksize);

}
