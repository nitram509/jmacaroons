package com.googlecode.cryptogwt.provider;

import static com.googlecode.cryptogwt.util.ByteArrayUtils.xor;

public class CtrCipherMode implements CipherMode {

    private byte[] counter;

    public void setIV(byte[] iv) {
        assert iv != null;
        counter = new byte[iv.length];
        System.arraycopy(iv, 0, counter, 0, iv.length); 
    }
    
    public String getName() {
        return "CTR";
    }

    // Exported so we can reuse this function in the Fortuna PRNG.
    static void increment_counter(byte[] counter) {        
        for (int i=counter.length-1; i >= 0 && ++counter[i] == 0; i--) {}        
    }
    

    public void doBlock(int opmode, BlockCipherAlgorithm alg, byte[] input,
            int offset, byte[] output, int outputOffset, int blocksize) {                        
        alg.encryptBlock(counter, 0, output, outputOffset);
        increment_counter(counter);
        xor(output, outputOffset, input, offset, blocksize);      
    }


}
