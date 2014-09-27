package com.googlecode.cryptogwt.provider;

public interface EntropySink {
    public void addEntropy(int seedId, double estimatedEntropy, byte[] seed);
    public boolean needsEntropy();
}
