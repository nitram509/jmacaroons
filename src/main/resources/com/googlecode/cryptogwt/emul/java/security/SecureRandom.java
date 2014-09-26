package java.security;

import java.security.Provider.Service;
import com.googlecode.cryptogwt.util.ByteArrayUtils;


public class SecureRandom extends java.util.Random {

    private static final long serialVersionUID = -7096587537445201422L;

    private final Provider provider;

    private String algorithm;

    private final SecureRandomSpi secureRandomSpi;
    
    public SecureRandom() {
        this(getDefaultSecureRandomService());
    }

    public SecureRandom(byte seed[]) {
        this(getDefaultSecureRandomService());
        setSeed(seed);
    }
    
    private SecureRandom(Service service) {
        this(newSpiFromService(service), service.getProvider(), service.getAlgorithm());
    }
    
    private static SecureRandomSpi newSpiFromService(Service service) {
        try {
            return (SecureRandomSpi) service.newInstance(null);
        } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException(e);
        }
    }
    
    private static Service getDefaultSecureRandomService() {
        for (Provider provider : Security.getProviders()) {
            for (Service service : provider.getServices()) {
                if (service.getType().equals("SecureRandom")) {
                    return service;
                }
            }
        }
        throw new IllegalStateException("No default SecureRandom instance");
    }

    protected SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {        
        this(secureRandomSpi, provider, null);
    }
    
    private SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider, String algorithm) {
        super(0); // Stop currentTimeMillis from being seed
        this.secureRandomSpi = secureRandomSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public static SecureRandom getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        for (Provider provider : Security.getProviders()) {
            try {
                return getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                // Squash
            }            
        }
        throw new NoSuchAlgorithmException("Invalid random number generator: " + algorithm);
    }

    public static SecureRandom getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null) throw new NoSuchProviderException("Invalid provider: " + provider);
        return getInstance(algorithm, providerInstance);
    }

    public static SecureRandom getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) throw new IllegalArgumentException();
        SecureRandomSpi spi = provider.getSpi(algorithm, SecureRandomSpi.class);
        if (spi != null) return new SecureRandom(spi, provider, algorithm);
        throw new NoSuchAlgorithmException("Invalid SecureRandom algorithm: " + algorithm);
    }
    
    public final Provider getProvider() {
        return provider;
    }

    public String getAlgorithm() {
        return (algorithm != null) ? algorithm : "unknown";
    }

    public void setSeed(byte[] seed) {
        secureRandomSpi.engineSetSeed(seed);
    }
    
    @Override
    public void setSeed(long seed) {
        // Ignore call from super constructor
        if (seed != 0) {
            secureRandomSpi.engineSetSeed(ByteArrayUtils.toBytes(seed));
        }
    }

    @Override
    synchronized public void nextBytes(byte[] bytes) {
        secureRandomSpi.engineNextBytes(bytes);
    }

    @Override
    public
    final int next(int numBits) {
        int numBytes = (numBits + 7) / 8;
        byte randomBytes[] = new byte[numBytes];     
        nextBytes(randomBytes);
        int randomInt = 0;
        for (int i = 0; i < numBytes; i++) {
            randomInt = (randomInt << 8) + (randomBytes[i] & 0xff);
        }        
        return randomInt >>> (numBytes * 8 - numBits);
    }

    public static byte[] getSeed(int numBytes) {
        return new SecureRandom().generateSeed(numBytes);
    }

    public byte[] generateSeed(int numBytes) {
        return secureRandomSpi.engineGenerateSeed(numBytes);
    }

}
