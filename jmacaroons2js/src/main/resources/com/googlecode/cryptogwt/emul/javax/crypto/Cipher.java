package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Provider.Service;
import java.security.spec.AlgorithmParameterSpec;

public class Cipher {

    private static final byte[] EMPTY = new byte[] {};

    private final CipherSpi cipherSpi;

    private final Provider provider;

    private final String transformation;

    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    public static final int WRAP_MODE = 3;
    public static final int UNWRAP_MODE = 4;
    public static final int PUBLIC_KEY = 1;
    public static final int PRIVATE_KEY = 2;
    public static final int SECRET_KEY = 3;

    public Cipher(CipherSpi cipherSpi, Provider provider, String transformation) {
        this.cipherSpi = cipherSpi;
        this.provider = provider;
        this.transformation = transformation;
    }

    public static Cipher getInstance(String transformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        for (Provider provider : Security.getProviders()) {
            try {
                return getInstance(transformation, provider);
            } catch (NoSuchAlgorithmException e) {
                // Squash
            }
        }
        throw new NoSuchAlgorithmException("Cipher Algorithm " + transformation
                + " not supported");
    }

    public Cipher getInstance(String transformation, String provider)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            NoSuchPaddingException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null)
            throw new NoSuchProviderException("Invalid provider: " + provider);
        return getInstance(transformation, providerInstance);
    }

    public static Cipher getInstance(String transformation, Provider provider)
            throws NoSuchAlgorithmException, NoSuchPaddingException {        
        if (provider == null)
            throw new IllegalArgumentException();
        Provider.Service service = provider
                .getService("Cipher", transformation);
        if (service != null) {
            return new Cipher((CipherSpi) service.newInstance(transformation),
                    provider, transformation);
        }

        if (transformation.contains("/")) {
            return getInstanceWithModeAndPadding(provider, transformation);
        }

        throw new NoSuchAlgorithmException("Cipher Algorithm " + transformation
                + " not supported");

    }

    private static Cipher getInstanceWithModeAndPadding(Provider provider,
            String transformation) throws NoSuchAlgorithmException,
            NoSuchPaddingException {        
        CipherSpi spi;

        String[] components = getTransformationComponents(transformation);

        String alg = components[0];
        String mode = components[1];
        String padding = components[2];

        if (mode != null) {
            Service service = provider.getService("Cipher", alg + "/" + mode);
            if (service != null) {
                spi = (CipherSpi) service.newInstance(null);
                if (padding != null)
                    spi.engineSetPadding(padding);
                return new Cipher(spi, provider, transformation);
            }
        }

        if (padding != null) {
            Service service = provider.getService("Cipher", alg + "//"
                    + padding);
            if (service != null) {
                spi = (CipherSpi) service.newInstance(null);
                if (mode != null)
                    spi.engineSetMode(mode);
                return new Cipher(spi, provider, transformation);
            }
        }

        Service service = provider.getService("Cipher", alg);
        if (service != null) {
            spi = (CipherSpi) service.newInstance(null);
            if (padding != null)
                spi.engineSetPadding(padding);
            if (mode != null)
                spi.engineSetMode(mode);
            return new Cipher(spi, provider, transformation);
        }

        throw new NoSuchAlgorithmException(transformation);

    }

    private static String[] getTransformationComponents(String algorithm) {
        String[] parsed = algorithm.split("/", 3);
        String[] result = new String[] { null, null, null };
        assert result.length > 0;
        switch (parsed.length) {
        case 3:
            result[2] = parsed[2];
            //$FALL-THROUGH$
        case 2:
            result[1] = parsed[1];
            //$FALL-THROUGH$
        case 1:
            result[0] = parsed[0];
        }

        trimComponentsAndReplaceEmptyStringsWithNull(result);
        return result;
    }

    private static void trimComponentsAndReplaceEmptyStringsWithNull(
            String[] result) {
        for (int i = 0; i < result.length; i++) {
            if (result[i] != null) {
                result[i] = result[i].trim();
                if (result[i].isEmpty())
                    result[i] = null;
            }
        }
    }

    public void init(int opmode, Key key) throws InvalidKeyException {
        cipherSpi.engineInit(opmode, key, null);
    }

    public void init(int opmode, Key key, AlgorithmParameterSpec parameterSpec)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        cipherSpi.engineInit(opmode, key, parameterSpec, (SecureRandom) null);
    }

    public byte[] doFinal() throws IllegalStateException,
            IllegalBlockSizeException, BadPaddingException {
        return cipherSpi.engineDoFinal(null, 0, 0);
    }

    public byte[] doFinal(byte[] input) throws IllegalStateException,
            IllegalBlockSizeException, BadPaddingException {
        return cipherSpi.engineDoFinal(input, 0, input.length);
    }

    public byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalStateException,
        IllegalBlockSizeException, BadPaddingException {
        return cipherSpi.engineDoFinal(input, inputOffset, inputLen);
    }
    
    public int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
            throws IllegalStateException, IllegalBlockSizeException,
            BadPaddingException, ShortBufferException {
        return cipherSpi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
    }

    
    public byte[] update(byte[] input) {
        return cipherSpi.engineUpdate(input, 0, input.length);
    }

    public byte[] update(byte[] input, int inputOffset, int inputLen) {
        return cipherSpi.engineUpdate(input, inputOffset, inputLen);
    }

    public int update(byte[] input, int inputOffset, int inputLen,
            byte[] output, int outputOffset) throws ShortBufferException {
        return cipherSpi.engineUpdate(input, inputOffset, inputLen, output,
                outputOffset);
    }

    public int getBlockSize() {
        return cipherSpi.engineGetBlockSize();
    }

}
