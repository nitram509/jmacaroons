package com.googlecode.cryptogwt.provider;

import java.util.Collections;

import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandomSpi;
import javax.crypto.CipherSpi;
import javax.crypto.MacSpi;
import javax.crypto.SecretKeyFactorySpi;
import com.googlecode.cryptogwt.util.SpiFactory;
import com.googlecode.cryptogwt.util.SpiFactoryService;

public class CryptoGwtProvider extends Provider {

    private static final long serialVersionUID = 1949369036679857942L;

    public static Provider INSTANCE = new CryptoGwtProvider();
    
    private CryptoGwtProvider() {
        super("CRYPTOGWT", 1.0, "");
        putService(new SpiFactoryService(
                this,
                "MessageDigest",
                "SHA-256",
                SHA256MessageDigest.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<MessageDigestSpi>() {
                    public MessageDigestSpi create(Object constructorParam) {
                        return new SHA256MessageDigest();
                    }
                }));
        putService(new SpiFactoryService(
                this,
                "MessageDigest",
                "SHA1",
                SHA1MessageDigest.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<MessageDigestSpi>() {
                    public MessageDigestSpi create(Object constructorParam) {
                        return new SHA1MessageDigest();
                    }
                }));
        putService(new SpiFactoryService(
                this,
                "Cipher",
                "AES",
                AESCipher.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<CipherSpi>() {
                    public CipherSpi create(Object constructorParam) {
                        return new BlockCipher(new AESCipher());
                    }
                }));
        putService(new SpiFactoryService(
                this,
                "SecureRandom",
                "FORTUNA",
                FortunaSecureRandom.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<SecureRandomSpi>() {
                    public SecureRandomSpi create(Object constructorParam) {
                        return FortunaSecureRandom.getInstance();
                    }
                }));
        putService(new SpiFactoryService(
                this,
                "Mac",
                "HmacSHA256",
                Hmac.SHA256.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<MacSpi>() {
                    public MacSpi create(Object constructorParam) throws NoSuchAlgorithmException {
                        return new Hmac.SHA256();
                    }
                }));
        
        putService(new SpiFactoryService(
                this,
                "Mac",
                "HmacSHA1",
                Hmac.SHA1.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<MacSpi>() {
                    public MacSpi create(Object constructorParam) throws NoSuchAlgorithmException {
                        return new Hmac.SHA1();
                    }
                }));
        
        putService(new SpiFactoryService(
                this,
                "SecretKeyFactory",
                "PBKDF2WithHmacSHA1",
                Pbkdf2.HmacSHA1.class.getName(),
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap(),
                new SpiFactory<SecretKeyFactorySpi>() {
                    public SecretKeyFactorySpi create(Object constructorParam) throws NoSuchAlgorithmException {
                        return new Pbkdf2.HmacSHA1();
                    }
                }));
        
        
        }

}
