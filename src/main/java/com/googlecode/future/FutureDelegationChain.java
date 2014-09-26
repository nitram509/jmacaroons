package com.googlecode.future;

import java.util.ArrayList;
import java.util.List;

/**
 * A future that evaluates a series of dependent futures in sequence until one
 * of them satisfies the criteria of the {@link #isResult(Object)} method. If this
 * method is not overridden then the default implementation checks for a non-null
 * value.
 * 
 * <p>
 * If no result satisfies the criteria, the future will complete by calling
 * setEmpty which will set the result to null. Subclasses may override this
 * method to set a specific value.
 * 
 * @author Dean Povey
 * 
 * @param <T>
 *            type to return
 */
public class FutureDelegationChain<T> extends FutureAction<T> {
    private final List<Future<T>> futures = new ArrayList<Future<T>>();
    private int nextFuture = 0;
    
    public static <T> FutureDelegationChain<T> delegationChain(Future<T>...futures) {
        return new FutureDelegationChain<T>(futures);
    }
    
    public static <T> FutureDelegationChain<T> delegationChain(Iterable<Future<T>> futures) {
        return new FutureDelegationChain<T>(futures);
    }
    
    public FutureDelegationChain(Future<T>...futures) {
        for (Future<T> future : futures) {
            this.futures.add(future);
        }
    }
    
    public FutureDelegationChain(Iterable<Future<T>> futures) {
        for (Future<T> future : futures) {
            this.futures.add(future);
        } 
    }
    
    public void run() {
        for (int i=nextFuture; i < futures.size(); i++) {
            T result = futures.get(i).result();
            this.nextFuture++;
            if (isResult(result)) {
                returnResult(result);
                return;
            }
        }
        returnEmpty();
    }

    /**
     * Called to see if the result should be returned or the next future in the chain should
     * be run.  By default this method checks to see if the result is non-null, but this may be
     * overridden by subclasses.
     * 
     * @param result
     * @return true if result should be returned, false otherwise
     */
    public boolean isResult(T result) {
        return result != null;
    }
}
