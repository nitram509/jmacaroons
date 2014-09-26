package javax.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class SecretKeyFactory {
    
    
    private Provider provider;
    private SecretKeyFactorySpi keyFacSpi;
    private String algorithm;

    protected SecretKeyFactory(SecretKeyFactorySpi keyFacSpi,
            Provider provider,
            String algorithm) {
        this.keyFacSpi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;        
    }
    
    public static final SecretKeyFactory getInstance(String algorithm)
        throws NoSuchAlgorithmException {
            for (Provider provider : Security.getProviders()) {
                try {
                    return getInstance(algorithm, provider);
                } catch (NoSuchAlgorithmException e) {
                    // Squash
                }            
            }
            throw new NoSuchAlgorithmException("Invalid key factory: " + algorithm);

    }
    
    public static final SecretKeyFactory getInstance(String algorithm,
            String provider)
     throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null) throw new NoSuchProviderException("Invalid provider: " + provider);
        return getInstance(algorithm, providerInstance);
    }
    
    public static final SecretKeyFactory getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) throw new IllegalArgumentException();
        Provider.Service service = provider.getService("SecretKeyFactory", algorithm);
        if (service != null) {
            Object o = service.newInstance(algorithm);
            return new SecretKeyFactory(
                    (SecretKeyFactorySpi) service.newInstance(algorithm), 
                    provider, 
                    algorithm);
        }
        throw new NoSuchAlgorithmException("Invalid secret key factory: " + algorithm);
    }
    
    public final Provider getProvider() {
        return provider;
    }
    
    public final String getAlgorithm() {
        return algorithm;
    }
    
    public final SecretKey generateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        return keyFacSpi.engineGenerateSecret(keySpec);
    }
    
    public final KeySpec getKeySpec(SecretKey key,
            Class keySpec) throws InvalidKeySpecException {
        return keyFacSpi.engineGetKeySpec(key, keySpec);
    }
    
    public final SecretKey translateKey(SecretKey key) throws InvalidKeyException {
        return keyFacSpi.engineTranslateKey(key);
    }
    

}
