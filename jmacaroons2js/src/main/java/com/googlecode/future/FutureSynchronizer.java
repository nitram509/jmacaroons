package com.googlecode.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A Future that returns true when one or more other results have values. If one
 * or more of the synchronized results is cancelled then the FutureSynchronizer will be
 * cancelled, however if one or more results fails with an exception then that exception
 * will not be propagated to the FutureSynchronizer instance and it will still succeed.
 * 
 * @author Dean Povey
 *
 */
public class FutureSynchronizer extends FutureAction<Boolean> {
   
    private List<Future<?>> resultsToSynchronizeWith;
    
    public FutureSynchronizer(Future<?>...resultsToSynchronizeWith) {
        this.resultsToSynchronizeWith = asList(resultsToSynchronizeWith);
    }

    public FutureSynchronizer(
            Collection<? extends Future<?>> resultsToSynchronizeWith) {
        this.resultsToSynchronizeWith = new ArrayList<Future<?>>(resultsToSynchronizeWith);
    }

    public void run() {
        for (Future<?> result : resultsToSynchronizeWith) {
            result.start();
        }
        
        for (Future<?> result : resultsToSynchronizeWith) {                
            if (!result.isComplete()) {
                try {
                    result.result();
                } catch(IncompleteResultException e) {
                    throw e;
                } catch(CancelledException e) {
                    throw e;
                } catch(Throwable t) {
                    assert result.isFailure();
                    // Squash
                }
            }
        }
        returnResult(true);
    }

}
