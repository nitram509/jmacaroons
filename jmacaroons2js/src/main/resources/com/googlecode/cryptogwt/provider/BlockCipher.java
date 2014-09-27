package com.googlecode.cryptogwt.provider;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

public class BlockCipher extends CipherSpi {

    private final BlockCipherAlgorithm alg;
    private final int blocksize;
    private final byte[] buf;
    private int bufLen;
    private CipherMode cipherMode = new EcbCipherMode();
    private byte[] iv = null;
    private int mode;
    private Padding padding = new NoPadding();
    private SecureRandom random = null;

    protected BlockCipher(BlockCipherAlgorithm algorithm) {
        blocksize = algorithm.getBlockSize();
        buf = new byte[blocksize];
        iv = new byte[blocksize];
        bufLen = 0;
        this.alg = algorithm;
    }

    private void blockWithCipherMode(byte[] bytes, int offset, byte[] result,
            int outputOffset) {       
        cipherMode.doBlock(mode, alg, bytes, offset, result, outputOffset, blocksize);
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int offset, int len)
            throws IllegalBlockSizeException, BadPaddingException {
        byte[] result = new byte[engineGetOutputSize(len)];
        int actualLen = 0;
        try {
            actualLen = engineDoFinal(input, offset, len, result, 0);
        } catch (ShortBufferException e) {
            assert false : "Unexpected exception: " + e;
        }
        return ByteArrayUtils.copyOfRange(result, offset, actualLen);
    }

    @Override
    protected int engineDoFinal(byte[] input, int offset, int len,
            byte[] output, int outputOffset) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        int outputLen = engineUpdate(input, offset, len, output, outputOffset);
        outputOffset += outputLen;
        if (this.mode == Cipher.ENCRYPT_MODE) {
            byte[] pad = new byte[blocksize];
            int padSize = padding.pad(this.buf, 0, this.bufLen, pad, 0,
                    this.blocksize);
            if (padSize > 0) {
                assert padSize == this.blocksize;
                blockWithCipherMode(pad, 0, output, outputOffset);
                outputLen += blocksize;
            }
        } else {
            assert this.mode == Cipher.DECRYPT_MODE;
            if (this.bufLen < this.blocksize) {
                // TODO: Is this the right error?
                throw new ShortBufferException(
                        "Insufficent data to decrypt final block");
            }
            byte[] padded = new byte[blocksize];
            blockWithCipherMode(buf, 0, padded, 0);
            outputLen += padding.depad(padded, 0, blocksize, output,
                    outputOffset, blocksize);
        }
        bufLen = 0;
        reset();
        return outputLen;
    }

    @Override
    protected int engineGetBlockSize() {
        return alg.getBlockSize();
    }

    @Override
    protected byte[] engineGetIV() {
        return iv;
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {        
        return inputLen + (blocksize - (inputLen % blocksize)); 
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        this.bufLen = 0;
        this.mode = opmode;
        this.alg.setKey(key.getEncoded());
        // TODO: Handle parameters
        this.random = random;
    }

    @Override
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec parameterSpec, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.bufLen = 0;
        this.mode = opmode;
        this.alg.setKey(key.getEncoded());
        // TODO: Handle parameter spec by creating params?
        if (parameterSpec instanceof IvParameterSpec) {
            IvParameterSpec ivSpec = (IvParameterSpec) parameterSpec;
            assert cipherMode != null;
            this.iv = ivSpec.getIV();
            assert iv != null;
            cipherMode.setIV(iv);
        }
        this.random = random;
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        bufLen = 0;
        this.mode = opmode;
        alg.setKey(key.getEncoded());
        this.random = random;
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        cipherMode = CipherModeFactory.getInstance(mode);
        if (iv != null) cipherMode.setIV(iv);
    }

    @Override
    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
        this.padding = PaddingFactory.getInstance(padding);
    }

    @Override
    protected byte[] engineUpdate(byte[] data, int offset, int len) {
        byte[] result = new byte[engineGetOutputSize(len)];
        int actualLen = 0;
        try {
            actualLen = engineUpdate(data, offset, len, result, 0);
        } catch (ShortBufferException e) {
            assert false : "Unexpected exception: " + e;
        }
        // Lazy: We could probably do a better job of working out the correct
        // length
        // ahead of time and save this copy.
        return ByteArrayUtils.copyOfRange(result, 0, actualLen);
    }

    @Override
    protected int engineUpdate(byte[] bytes, int offset, int len,
            byte[] result, int outputOffset) throws ShortBufferException {
        if (len == 0)
            return 0;
        int bytesToProcess = len - offset + bufLen;
        int nrOutputBlocks = (bytesToProcess / blocksize);

        // If we have a multiple of block size, make sure at least one block
        // remains buffered.
        // This is necessary for padding
        if (mode == Cipher.DECRYPT_MODE) {
            if (bytesToProcess % blocksize == 0)
                nrOutputBlocks -= 1;
        }

        int outputLen = (nrOutputBlocks * blocksize);
        if (outputOffset + result.length < outputLen) {
            throw new ShortBufferException("Output buffer required: "
                    + nrOutputBlocks * blocksize);
        }

        // If buffer is not empty then fill to block size and encrypt/decrypt
        // this first
        if (bufLen != 0) {
            int bytesToBuffer = Math.min(blocksize - bufLen, len);
            System.arraycopy(bytes, offset, buf, bufLen, bytesToBuffer);
            bufLen += bytesToBuffer;
            if (nrOutputBlocks == 0) {
                return 0;
            }
            offset += bytesToBuffer;
            blockWithCipherMode(buf, 0, result, outputOffset);
            outputOffset += blocksize;
        }

        // Encrypt/Decrypt remaining blocks
        for (int i=outputOffset; i < outputOffset + outputLen; i += blocksize) {
            blockWithCipherMode(ByteArrayUtils.copyOfRange(bytes, offset, blocksize), 
                    0, result, i);
            offset += blocksize;
        }

        // Put remainder into buffer
        bufLen = len - offset;
        assert bufLen <= blocksize;
        System.arraycopy(bytes, offset, buf, 0, bufLen);
        return outputLen;
    }
    
    private void reset() {
        // Reset the IV
        cipherMode.setIV(iv);
    }

}
