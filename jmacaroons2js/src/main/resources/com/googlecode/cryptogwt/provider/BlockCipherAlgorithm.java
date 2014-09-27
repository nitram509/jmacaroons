package com.googlecode.cryptogwt.provider;


/**
 * Interface for a block cipher algorithm.
 * 
 * @author Dean Povey
 *
 */
public interface BlockCipherAlgorithm {
    
    /**
     * Return the block size for this algorithm in bytes.
     * 
     * @return the key size in bytes.
     */
    int getBlockSize();
    
    /**
     * Return the key size for this algorithm in bytes.
     * 
     * @return the key size in bytes.
     */
    int getKeySize();
    
    /**
     * Set the raw key to use.
     * 
     * @param key bytes representing the raw key.
     */
    void setKey(byte[] key);

    /**
     * Encrypt a single block of data
     * 
     * @param input block of data to input
     * @param inputOffset offset within input
     * @param output where to place output bytes
     * @param outputOffset offset within output to put bytes
     */
    public void encryptBlock(byte[] input, int inputOffset, byte[] output,
            int outputOffset);
    /**
     * Decrypt a single block of data
     * 
     * @param input block of data to input
     * @param inputOffset offset within input
     * @param output where to place output bytes
     * @param outputOffset offset within output to put bytes
     */
    public void decryptBlock(byte[] input, int inputOffset, byte[] output,
            int outputOffset);
}
