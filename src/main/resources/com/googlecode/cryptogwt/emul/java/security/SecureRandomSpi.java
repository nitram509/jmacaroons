package java.security;

import java.io.Serializable;

public abstract class SecureRandomSpi implements Serializable {
    protected abstract void engineSetSeed(byte[] seed);
    
    protected abstract void engineNextBytes(byte[] bytes);
    
    protected abstract byte[] engineGenerateSeed(int numBytes);

}
