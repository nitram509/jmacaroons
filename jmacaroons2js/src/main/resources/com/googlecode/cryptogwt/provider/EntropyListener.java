package com.googlecode.cryptogwt.provider;

public interface EntropyListener {
    public boolean onEntropyUpdate(double availableEntropyEstimate);
}
