package javax.crypto;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class SecretKeyFactorySpi {
    
    public SecretKeyFactorySpi() {}
    
    protected abstract SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException;
        
    protected abstract KeySpec engineGetKeySpec(SecretKey key,
            @SuppressWarnings("unchecked") Class keySpec) throws InvalidKeySpecException;
    
    protected abstract SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException;

}
