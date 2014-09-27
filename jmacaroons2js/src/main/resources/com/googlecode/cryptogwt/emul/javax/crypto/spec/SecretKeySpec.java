package javax.crypto.spec;

import java.util.Arrays;

import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

/**
 * This class specifies a secret key in a provider-independent fashion.
 * 
 * <p>
 * It can be used to construct a SecretKey from a byte array, without having to
 * go through a (provider-based) SecretKeyFactory.
 * 
 * <p>
 * This class is only useful for raw secret keys that can be represented as a
 * byte array and have no key parameters associated with them.
 * 
 */
public class SecretKeySpec implements KeySpec, SecretKey {
    
    private static final long serialVersionUID = -3508463012414183843L;

    private final byte[] key;
    
    private final String algorithm;
    
    /**
     * Constructs a secret key from the given byte array. This constructor does
     * not check if the given bytes indeed specify a secret key of the specified
     * algorithm.
     * 
     * @param key the key material of the secret key
     * @param algorithm the name of the secret-key algorithm to be associated with the
     * given key material
     */
    public SecretKeySpec(byte[] key, String algorithm) {
        this.key = key;
        this.algorithm = algorithm;
    }

    /**
     * Returns the name of the algorithm associated with this secret key.
     * 
     * @return the secret key algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the key material of this secret key.
     * 
     * @return the key material
     */
    public byte[] getEncoded() {
        return ByteArrayUtils.copyOfRange(key, 0, key.length);
    }

    /**
     * Returns the name of the encoding format for this secret key.
     * 
     * @return the string "RAW".
     */
    public String getFormat() {
        return "RAW";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + Arrays.hashCode(key);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecretKeySpec other = (SecretKeySpec) obj;
        if (algorithm == null) {
            if (other.algorithm != null)
                return false;
        } else if (!algorithm.equals(other.algorithm))
            return false;
        if (!Arrays.equals(key, other.key))
            return false;
        return true;
    }
    
}
