package com.googlecode.cryptogwt.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomSpi;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import com.googlecode.cryptogwt.util.ByteArrayUtils;

import static com.googlecode.cryptogwt.util.ByteArrayUtils.*;

public class FortunaSecureRandom extends SecureRandomSpi implements EntropySink {
        
    private double ENTROPY_HI_WATER_MARK = 1024.0;
    
    private double ENTROPY_LO_WATER_MARK = 256.0;
    
    private double ENTROPY_MINIMUM = 128.0;
    
    private boolean isCollecting = true;
    
    private boolean requireMinimumEntropy = false;
    
    private boolean isEmergency = false;
    
    private static final long serialVersionUID = -6990369253864376314L;

    private static final double DEFAULT_ENTROPY_ESTIMATE_PER_BYTE_BITS = 0.5;
    
    private static final String DIGEST = "SHA-256";
    private static final String BLOCK_CIPHER = "AES";
    private static final int KEYSIZE = 16;
    private static final int MAX_BYTES_BETWEEN_RESEED = 1024;

    public static final int MANUAL_SEED_ID = 0;
    
    public static final int EMERGENCY_SEED_ID = 1;
    
    public Set<EntropySource> sources = new LinkedHashSet<EntropySource>();
    
    private EventEntropySource eventEntropy;
    
    private RunLoopEntropySource runloopSource; 

    private static class Pool {

        MessageDigest digest;

        Pool(MessageDigest digest) {
            this.digest = digest;
        }

        public void add(byte[] seed) {
            digest.update(seed);
        }

        public byte[] hash() {
            return digest.digest();
        }
    }

    enum EntropyCollection { START, STOP }
    
    private final int NR_POOLS = 32;
    private final int NR_POOLS_MASK = 0x1f;
    private Pool pools[] = new Pool[NR_POOLS];
    private int poolIdx = 0;
    private int pool0Cnt = 0;
    private int resetCnt = 0;
    private byte[] counter;
    private byte[] K = null;
    private Cipher cipher;
    private MessageDigest digest;    
    private int bytesProcessed;
    private List<EntropyListener> entropyListeners = new ArrayList<EntropyListener>();
    private static FortunaSecureRandom INSTANCE = new FortunaSecureRandom();
    private boolean initialized = false;

    private int blockSize;

    private double estimatedEntropy = 0;
    
    public static FortunaSecureRandom getInstance() {    
        return INSTANCE;
    }

    private FortunaSecureRandom() {
        // We lazy initialize to make sure that cipher and digests will be available in
        // the provider.
    }

    public void registerDefaultEntropySources() {
        eventEntropy = new EventEntropySource();
        runloopSource = new RunLoopEntropySource();
        registerEntropySource(eventEntropy);
        registerEntropySource(runloopSource);
    }
    
    public Collection<EntropySource> getEntropySources() {
        return sources;
    }

    private void init() {
        if (initialized) return;
        try {
            cipher = getCipher();
            digest = getDigest();
            for (int i = 0; i < pools.length; i++) {
                pools[i] = new Pool(getDigest());
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not initialize PRNG.\n" + e);
        }

        blockSize = cipher.getBlockSize();
        counter = new byte[blockSize];
        if (sources.size() == 0) registerDefaultEntropySources();
        initialized = true;
    }

    private Cipher getCipher() throws NoSuchAlgorithmException,
            NoSuchPaddingException {
        try {
            return Cipher.getInstance(BLOCK_CIPHER);
        } catch(NoSuchAlgorithmException e) {
            return Cipher.getInstance(BLOCK_CIPHER, CryptoGwtProvider.INSTANCE);
        }
    }

    private MessageDigest getDigest() throws NoSuchAlgorithmException {
        try {
            return MessageDigest.getInstance(DIGEST);
        } catch(NoSuchAlgorithmException e) {
            return MessageDigest.getInstance(DIGEST, CryptoGwtProvider.INSTANCE);
        }
    }
    
    public void registerEntropySource(EntropySource source) {
        sources.add(source);
        source.setSink(this);
        if (isCollecting) source.startCollecting();
    }
    
    public void deregisterEntropySource(EntropySource source) {
        source.stopCollecting();
        source.setSink(null);
        sources.remove(source);
    }
    
    /**
     * Register a listener to wait for at least minimum entropy to be available.  If
     * entropy is currently > minimum then then listener is called immediately.
     * 
     * @param listener
     * @see #registerEntropyUpdateListener(EntropyListener)
     */
    public void waitForEntropy(EntropyListener listener) {
        if (estimatedEntropy < ENTROPY_MINIMUM || 
                listener.onEntropyUpdate(estimatedEntropy)) {                
            entropyListeners.add(listener);
        }
    }
    
    /**
     * Register a listener that will be notified when any new entropy is received.
     * 
     * @param listener
     */
    public void registerEntropyUpdateListener(EntropyListener listener) {
        entropyListeners.add(listener);
    }
    
    public double getAvailableEntropyEstimate() {
        return this.estimatedEntropy;
    }
    
    public void removeEntropyListener(AsyncCallback<Double> callback) {
        entropyListeners.remove(callback);
    }
    
    public boolean needsEntropy() {
        return (isCollecting && estimatedEntropy < ENTROPY_HI_WATER_MARK) ||
            estimatedEntropy < ENTROPY_LO_WATER_MARK;
    }
    
    public void reserveEntropy(double bits) {
        if (bits < 0) throw new IllegalStateException("Enropy must be greater than zero, was: " + bits);
        this.estimatedEntropy -= bits;
        if (this.estimatedEntropy < 0) estimatedEntropy = 0;
    }

    public void addEntropy(int seedId, byte[] seed) {
        addEntropy(seedId, DEFAULT_ENTROPY_ESTIMATE_PER_BYTE_BITS * seed.length, seed);
    }
    
    public void addEntropy(int seedId, double entropyEstimate, byte[] seed) {
        assert entropyEstimate >= 0;
        init();        
        this.estimatedEntropy += entropyEstimate;
        assert this.estimatedEntropy >= 0;
              
        pools[poolIdx].add(concatenate(toBytes(seedId), toBytes(seed.length),
                seed));
        if (poolIdx == 0) {
            pool0Cnt += seed.length;
        }
        if (pool0Cnt >= 64) {
            reseed();            
        }
        poolIdx = (poolIdx + 1) & NR_POOLS_MASK;               
        
        // Notify listeners.  Listeners return true if they want to continue being notified,
        // or false if they want more entropy. Acheck is done for minimum entropy before each
        // listener in case they call reserveEntropy.
        for (EntropyListener listener : new ArrayList<EntropyListener>(entropyListeners)) {
            if (this.estimatedEntropy < ENTROPY_MINIMUM) return;
            if (!listener.onEntropyUpdate(this.estimatedEntropy)) {
                entropyListeners.remove(listener);
            }            
        }        
        
        if (!needsEntropy()) {
            stopCollection();
        }
    }
    
    private void stopCollection() {
        for (EntropySource source : sources) {            
            source.stopCollecting();            
        }
    }
    
    private void startCollection() {
        for (EntropySource source : sources) {            
            source.startCollecting();            
        }
    }

    public void reseed() {
        init();
        if (K == null) K = new byte[] {};
        resetCnt++;
        digest.reset();
        digest.update(K);
        for (int j = 0; j < pools.length; j++) {
            if (j == 0 || (((1 << j) & resetCnt) > 0)) {
                digest.update(pools[j].hash());
            }
        }
        K = expandDigest(KEYSIZE);
        reserveEntropy(KEYSIZE);
        pool0Cnt = 0;
        if (!isCollecting && needsEntropy()) {
            startCollection();
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        init();
        // Doesn't really make sense. We'll just generate random output.
        byte[] result = new byte[numBytes];
        engineNextBytes(result);
        return result;
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        init();        
        assert estimatedEntropy >= 0  : "Enropy must be greater than zero, was: " + estimatedEntropy;
        if (this.estimatedEntropy < ENTROPY_MINIMUM) {
            if (requireMinimumEntropy)
               throw new IllegalStateException("Insufficient Entropy (" + estimatedEntropy + " bits) to generate random numbers.");
            addEmergencyEntropy();
        }
        int numBytes = bytes.length;
        try {
            if (K == null || bytesProcessed >= MAX_BYTES_BETWEEN_RESEED) {
                reseed();
                bytesProcessed = 0;
            }
            byte[] result = extractEntropy(numBytes);
            bytesProcessed += numBytes;
            rekey();
            System.arraycopy(result, 0, bytes, 0, numBytes);            
        } catch (GeneralSecurityException e) {
            assert false : "Unexpected exception: " +  e;
        }
    }

    @Override
    protected void engineSetSeed(byte[] seed) {
        init();
        // Assume 8 bits of entropy per byte.  Probably a big overestimate, but it's
        // the callers fault if they get it wrong.
        addEntropy(MANUAL_SEED_ID, 8.0 * seed.length, seed);
    }

    private byte[] extractEntropy(int length) throws GeneralSecurityException {
        assert initialized;
        int remainder = length % blockSize;
        int lengthAsBlockSizeMultiple = length - remainder;
        SecretKeySpec skeySpec = new SecretKeySpec(K, BLOCK_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);        
        byte result[] = new byte[length];
        int outputOffset = 0;
        for (outputOffset = 0; outputOffset < lengthAsBlockSizeMultiple;) {            
            outputOffset = encryptCounterAndUpdate(result, outputOffset);            
        }
        if (length % blockSize > 0) {
            byte[] block = new byte[blockSize];
            encryptCounterAndUpdate(block, 0);            
            System.arraycopy(block, 0, result, outputOffset, remainder);
        }        
        return result;
    }

    private int encryptCounterAndUpdate(byte[] result, int outputOffset)
            throws ShortBufferException {
        outputOffset += cipher.update(counter, 0, blockSize, result, outputOffset);
        CtrCipherMode.increment_counter(counter);
        return outputOffset;
    }

    private void rekey() throws GeneralSecurityException {        
        assert K.length == KEYSIZE;
        assert KEYSIZE % blockSize == 0;        
        for (int offset = 0; offset < KEYSIZE;) {
            offset += encryptCounterAndUpdate(K, offset);            
        }        
    }

    private byte[] expandDigest(int len) {
        if (KEYSIZE == digest.getDigestLength())
            return digest.digest();
        byte[] output = new byte[len];
        for (int offset = 0; offset < len;) {
            byte[] result = digest.digest();
            int copyLen = Math.min(result.length, len - offset);
            System.arraycopy(output, offset, result, 0, copyLen);
            offset += copyLen;
            if (offset < len) {
                digest.update(result);
            }
        }
        digest.reset();
        return output;
    }
    
    private native JsArrayInteger getWindowInfo() /*-{
        var result = [];
        if ($wnd.innerHeight != undefined) result.push($wnd.innerHeight);
        if ($wnd.innerWidth != undefined) result.push($wnd.innerWidth);
        if ($wnd.pageXOffset != undefined) result.push($wnd.pageXOffset);
        if ($wnd.pageYOffset != undefined) result.push($wnd.pageYOffset);
        if ($wnd.screenLeft != undefined) result.push($wnd.screenLeft);
        if ($wnd.screenTop != undefined) result.push($wnd.screenTop);
        if ($wnd.screenX != undefined) result.push($wnd.screenX);
        if ($wnd.screenY != undefined) result.push($wnd.screenY);
        return result;
    }-*/;
      
    
    // TODO: Measure quality.
    private void addEmergencyEntropy() {
        // Iterate for up to a second.  We measure the time between successive generations
        // of a random number of iterations over the the generator and feed this
        // as seed.  In addition, we feed information about the current window.
        // Peforming a random number of iterations also discards a random number of
        // bytes from the generator, thus increasing the work for an attacker to recreate the
        // stream.  This is a pretty low quality source of entropy, and high security 
        // applications should use the reserveEntropy mechanism to avoid fallback to this
        // mechanism if the random output needs to be unguessable.  For applications where
        // the random output goes in the clear (IV, Salt) this should be sufficient.
        if (isEmergency) return;
        isEmergency = true;
        byte[] bytesToDiscard = new byte[(Math.abs(Random.nextInt()) % 256) * blockSize];
        long start = System.currentTimeMillis();
        addEntropy(EMERGENCY_SEED_ID, 0.0, ByteArrayUtils.toBytes(start));
        addEntropy(EMERGENCY_SEED_ID, 0.0, windowInfoAsBytes());
        long curr = start;
        int count = 0;
        do {
            engineNextBytes(bytesToDiscard);            
            curr = System.currentTimeMillis();            
            addEntropy(EMERGENCY_SEED_ID, 0.0, ByteArrayUtils.toBytes(curr));
            byte[] b = new byte[4];
            engineNextBytes(b);
            bytesToDiscard = new byte[(Math.abs(ByteArrayUtils.toInteger(b)) % 256) * blockSize];
            count++;
        } while(curr - start < 1000);
        addEntropy(EMERGENCY_SEED_ID, 0.0, ByteArrayUtils.toBytes(count));
        isEmergency = false;
    }

    private byte[] windowInfoAsBytes() {
        byte[] result = JsArrayUtils.toByteArray(getWindowInfo());
        System.out.println(ByteArrayUtils.toHexString(result));
        return result;
    }
    
    

}
