package java.security;

public class MessageDigest {
           
    private MessageDigestSpi spi;
    
    private final Provider provider;
    
    private final String algorithm;


    protected MessageDigest(MessageDigestSpi spi, Provider provider,
            String algorithm) {
        this.spi = spi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public static MessageDigest getInstance(String algorithm, String provider) throws
        NoSuchAlgorithmException, 
        NoSuchProviderException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null) throw new NoSuchProviderException("Invalid provider: " + provider);
        return getInstance(algorithm, providerInstance);
    }
    
    public static MessageDigest getInstance(String algorithm, Provider provider) throws 
        NoSuchAlgorithmException {
        if (provider == null) throw new IllegalArgumentException();
        Provider.Service service = provider.getService("MessageDigest", algorithm);
        if (service != null) {
            return new MessageDigest(
                    (MessageDigestSpi) service.newInstance(algorithm), 
                    provider, 
                    algorithm);
        }
        throw new NoSuchAlgorithmException("Invalid digest: " + algorithm);
    }
        
    public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
        for (Provider provider : Security.getProviders()) {
            try {
                return getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                // Squash
            }            
        }
        throw new NoSuchAlgorithmException("Invalid digest: " + algorithm);
    }

    public byte[] digest(byte[] input) {
        return digest(input, 0, input.length);
    }

    public byte[] digest(byte[] input, int offset, int len) {
        update(input, offset, len);
        return digest();
    }

    public void update(byte input) {
        spi.engineUpdate(input);
    }

    public void update(byte[] input) {
        update(input, 0, input.length);
    }

    public byte[] digest() {
        return spi.engineDigest();
    }

    public void reset() {
        spi.engineReset();
    }


    public void update(byte[] input, int offset, int len) {
        spi.engineUpdate(input, offset, len);
    }
    
    
    public int getDigestLength() {
        return spi.engineGetDigestLength();
    }

    public Provider getProvider() {
        return provider;
    }

    public String getAlgorithm() {
        return algorithm;
    }
   
}
