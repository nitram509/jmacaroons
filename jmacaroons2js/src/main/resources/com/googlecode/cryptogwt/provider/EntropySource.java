package com.googlecode.cryptogwt.provider;


public abstract class EntropySource {
    protected EntropySink sink;

    public void addEntropy(int seedId, double estimatedEntropy, byte[] seed) {
        sink.addEntropy(seedId, estimatedEntropy, seed);
    }

    public abstract void startCollecting();
    
    public abstract void stopCollecting();
    
    protected EntropySource(EntropySink sink) {
        this.sink = sink;
    }
    
    protected EntropySource() {}

    public EntropySink getSink() {
        return sink;
    }

    public void setSink(EntropySink sink) {
        this.sink = sink;
    }

    public abstract boolean isCollecting();
    
}
