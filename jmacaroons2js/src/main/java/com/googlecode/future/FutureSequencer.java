package com.googlecode.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A FutureResult that evaluates its dependent results in the order requested and
 * sets true when all are completed.   FutureSequencer differs from {@link FutureSynchronizer}
 * in that a failure of one of the dependent results will cause FutureSequence to also fail.
 * 
 * @author Dean Povey
 *
 */
public class FutureSequencer extends FutureAction<Boolean> {
   
    private List<Future<?>> resultsToSequence;
    
    public FutureSequencer(Future<?>...resultsToSequence) {
        this.resultsToSequence = asList(resultsToSequence);
    }

    public FutureSequencer(
            Collection<? extends Future<?>> resultsToSynchronizeWith) {
        this.resultsToSequence = new ArrayList<Future<?>>(resultsToSynchronizeWith);
    }

    public void run() {
        for (Future<?> result : resultsToSequence) {
            result.result();
        }
        returnResult(true);
    }

}
