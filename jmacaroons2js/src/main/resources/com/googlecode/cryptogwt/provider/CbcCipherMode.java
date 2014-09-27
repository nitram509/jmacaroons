package com.googlecode.cryptogwt.provider;

import static com.googlecode.cryptogwt.util.ByteArrayUtils.*;

import javax.crypto.Cipher;

public class CbcCipherMode implements CipherMode {

    private byte[] iv;
    
    private byte[] nextIv;
                
    public void setIV(byte[] iv) {
        this.iv = iv;
    }

    public String getName() {
        return "CBC";
    }

    public void transformInput(int mode, byte[] bytes, int offset, int blocksize) {        
        if (mode == Cipher.ENCRYPT_MODE) {
            xor(bytes, offset, iv, 0, blocksize);
            return;
        }
        nextIv = copyOfRange(bytes, offset, blocksize);
    }
    
    public void transformOutput(int mode, byte[] bytes, int offset, int blocksize) {        
        if (mode == Cipher.DECRYPT_MODE) {
            xor(bytes, offset, iv, 0, blocksize);
            iv = nextIv;
            return;
        }
        iv = copyOfRange(bytes, offset, blocksize);        
    }

    public void doBlock(int opmode, BlockCipherAlgorithm alg, byte[] input,
            int offset, byte[] output, int outputOffset, int blocksize) {        
        transformInput(opmode, input, offset, blocksize);

        if (opmode == Cipher.ENCRYPT_MODE) {
            alg.encryptBlock(input, offset, output, outputOffset);
        } else {
            alg.decryptBlock(input, offset, output, outputOffset);
        }
        
        transformOutput(opmode, output, outputOffset, blocksize);
        
    }
    
}
