package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

public class Mac {
    
    private final MacSpi macSpi;
    
    private final String algorithm;
    
    private final Provider provider;

    protected Mac(MacSpi macSpi, String algorithm, Provider provider) {
        this.macSpi = macSpi;
        this.algorithm = algorithm;
        this.provider = provider;
    }
    
    public static final Mac getInstance(String algorithm) throws NoSuchAlgorithmException {
        for (Provider provider : Security.getProviders()) {
            try {
                return getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                // Squash
            }            
        }
        throw new NoSuchAlgorithmException("Invalid MAC: " + algorithm);
    }
    
    public static final Mac getInstance(String algorithm, String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null) throw new NoSuchProviderException("Invalid provider: " + provider);
        return getInstance(algorithm, providerInstance);
    }
    
    public static final Mac getInstance(String algorithm, Provider provider)
        throws NoSuchAlgorithmException {
        if (provider == null) throw new IllegalArgumentException();
        Provider.Service service = provider.getService("Mac", algorithm);
        if (service != null) {
            return new Mac(
                    (MacSpi) service.newInstance(algorithm),
                    algorithm,
                    provider);
        }
        throw new NoSuchAlgorithmException("Invalid digest: " + algorithm);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Provider getProvider() {
        return provider;
    }
    
    public final int getMacLength() {
        return macSpi.engineGetMacLength();
    }
    
    public final void init(Key key) throws InvalidKeyException {
        try {
            macSpi.engineInit(key, null);
        } catch (InvalidAlgorithmParameterException e) {
            assert false : "Unexpected exception" + e;            
        }
    }

    public final void update(byte input) throws IllegalStateException {
        macSpi.engineUpdate(input);
    }
    
    public final void update(byte[] input) throws IllegalStateException {
        macSpi.engineUpdate(input, 0, input.length);
    }
    
    public final void update(byte[] input, int offset, int len) throws IllegalStateException {
        macSpi.engineUpdate(input, offset, len);
    }
    
    public final byte[] doFinal() throws IllegalStateException {
        return macSpi.engineDoFinal();
    }
    
    public final void doFinal(byte[] output, int outOffset) 
        throws ShortBufferException, IllegalStateException {
        byte[] result = macSpi.engineDoFinal();
        if (result.length > output.length - outOffset) {
            throw new ShortBufferException("Buffer must be at least " +
                    result.length + " bytes long.");
        }
        System.arraycopy(result, 0, output, outOffset, result.length);
    }
    
    public final byte[] doFinal(byte[] input) throws IllegalStateException {
        update(input);
        return doFinal();
    }
    
    public final void reset() {
        macSpi.engineReset();
    }
}
