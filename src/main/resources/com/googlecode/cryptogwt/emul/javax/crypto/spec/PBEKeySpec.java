package javax.crypto.spec;

import java.util.Arrays;

import java.security.spec.KeySpec;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

public class PBEKeySpec implements KeySpec {
    
    private char[] password;
    
    private byte[] salt;
    
    private int iterationCount;
    
    private int keyLength;
        
    public PBEKeySpec(char[] password) {
        this.password = copyOf(password);        
    }
    
    public PBEKeySpec(char[] password,
            byte[] salt,
            int iterationCount,
            int keyLength) {
        if (salt == null) throw new NullPointerException("salt must not be null");
        if (salt.length == 0) throw new IllegalArgumentException("salt must not be empty");
        if (iterationCount <= 0) throw new IllegalArgumentException("iterationCount must be > 0");
        if (keyLength <= 0) throw new IllegalArgumentException("keyLength must be > 0");
        this.password = copyOf(password);
        this.salt = ByteArrayUtils.copyOf(salt);
        this.iterationCount = iterationCount;
        this.keyLength = keyLength;
    }
    
    public PBEKeySpec(char[] password,
            byte[] salt,
            int iterationCount) {
        if (salt == null) throw new NullPointerException("salt must not be null");
        if (salt.length == 0) throw new IllegalArgumentException("salt must not be empty");
        if (iterationCount <= 0) throw new IllegalArgumentException("iterationCount must be > 0");        
        this.password = copyOf(password);
        this.salt = ByteArrayUtils.copyOf(salt);
        this.iterationCount = iterationCount;        
    }
    
    private char[] copyOf(char[] c) {
        if (c == null) return new char[] {};
        char[] result = new char[c.length];
        System.arraycopy(c, 0, result, 0, c.length);
        return result;
    }
    
    public final void clearPassword() {
        Arrays.fill(password, (char)0);
    }
    
    public final char[] getPassword() {
        return copyOf(this.password);
    }
    
    public final byte[] getSalt() {
        return ByteArrayUtils.copyOf(salt);
    }
    
    public final int getIterationCount() {
        return iterationCount;
    }
    
    public final int getKeyLength() {
        return keyLength;
    }
 
}
