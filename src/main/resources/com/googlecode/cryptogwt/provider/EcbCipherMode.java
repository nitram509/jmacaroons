package com.googlecode.cryptogwt.provider;

import javax.crypto.Cipher;

public class EcbCipherMode implements CipherMode {

    public void setIV(byte[] iv) {
    }
   
    public String getName() {
        return "ECB";
    }

    public void doBlock(int opmode, BlockCipherAlgorithm alg, byte[] input,
            int offset, byte[] output, int outputOffset, int blocksize) {
        if (opmode == Cipher.ENCRYPT_MODE) {
            alg.encryptBlock(input, offset, output, outputOffset);
        } else {
            alg.decryptBlock(input, offset, output, outputOffset);
        }    
    }

}
