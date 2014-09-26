package com.googlecode.cryptogwt.async;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKeyFactorySpi;
import com.googlecode.future.ConstantResult;
import com.googlecode.future.Future;

/**
 * An asynchronous version of the SecretKeyFactory.
 * 
 * <p>
 * This class enables access to asynchronous versions of the SecretKeyFactory
 * methods. These may be used for example when an operation is long running and
 * the implementation supports executing in chunks using {@link DeferredCommand}.
 * 
 * <p>
 * The result of these operations is a {@link Future} which can be queried to
 * see if the result is available by using {@link Future#isComplete()} and the
 * result obtained by calling {@link Future#result()}. Alternatively you can
 * register a callback using {@link Future#addCallback(AsyncCallback)}.
 * 
 * <p>
 * Implementations of the SecretKeyFactorySpi that do not support asynchronous
 * operations (do not implement the {@link AsyncSecretKeyFactorySpi} interface)
 * will be automatically adapted.
 * 
 * <p>To obtain an instance of an <code>AsyncSecretKeyFactory</code>, you must call
 * one of the <code>getAsyncInstance</code> static methods.
 * 
 * @see Future
 * @see SecretKeyFactory
 * 
 * @author Dean Povey
 * 
 */
public class AsyncSecretKeyFactory extends SecretKeyFactory {

    protected AsyncSecretKeyFactorySpi asyncSpi;
    
    protected AsyncSecretKeyFactory(final SecretKeyFactorySpi keyFacSpi,
            Provider provider, String algorithm) {
        super(keyFacSpi, provider, algorithm);
        if (keyFacSpi instanceof AsyncSecretKeyFactorySpi) {
            asyncSpi = (AsyncSecretKeyFactorySpi) keyFacSpi;
            return;
        }                 
    }
    
    public static final AsyncSecretKeyFactory getAsyncInstance(String algorithm)
    throws NoSuchAlgorithmException {
        for (Provider provider : Security.getProviders()) {
            try {
                return getAsyncInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                // Squash
            }            
        }
        throw new NoSuchAlgorithmException("Invalid key factory: " + algorithm);

    }

    public static final AsyncSecretKeyFactory getAsyncInstance(String algorithm,
            String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider providerInstance = Security.getProvider(provider);
        if (providerInstance == null) throw new NoSuchProviderException("Invalid provider: " + provider);
        return getAsyncInstance(algorithm, providerInstance);
    }

    public static final AsyncSecretKeyFactory getAsyncInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) throw new IllegalArgumentException();
        Provider.Service service = provider.getService("SecretKeyFactory", algorithm);
        if (service != null) {
            return new AsyncSecretKeyFactory(
                    (SecretKeyFactorySpi) service.newInstance(null), 
                    provider, 
                    algorithm);
        }        
        throw new NoSuchAlgorithmException("Invalid secret key factory: " + algorithm);
    }
    
    public final Future<SecretKey> generateSecretAsync(KeySpec keySpec) throws InvalidKeySpecException {
        if (asyncSpi != null) return asyncSpi.engineGenerateSecretAsync(keySpec);
        return ConstantResult.constant(generateSecret(keySpec));
    }
    
    public final Future<KeySpec> getKeySpecAsync(SecretKey key,
            Class keySpec) throws InvalidKeySpecException {
        if (asyncSpi != null) return asyncSpi.engineGetKeySpecAsync(key, keySpec);
        return ConstantResult.constant(getKeySpec(key, keySpec));
    }
    
    public final Future<SecretKey> translateKeyAsync(SecretKey key) throws InvalidKeyException {
        if (asyncSpi != null) return asyncSpi.engineTranslateKeyAsync(key);
        return ConstantResult.constant(translateKey(key));
    }
    

    

}
