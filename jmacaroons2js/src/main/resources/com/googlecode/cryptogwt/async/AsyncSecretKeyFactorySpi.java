package com.googlecode.cryptogwt.async;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import com.googlecode.future.Future;

public interface AsyncSecretKeyFactorySpi {
    Future<SecretKey> engineGenerateSecretAsync(KeySpec keySpec) throws InvalidKeySpecException;
        
    Future<KeySpec> engineGetKeySpecAsync(SecretKey key, Class keySpec) throws InvalidKeySpecException;
    
    Future<SecretKey> engineTranslateKeyAsync(SecretKey key) throws InvalidKeyException;
}
