package com.googlecode.future;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;

/**
 * Future which wraps GWT's IncrementalCommand to protect against slow script warnings.
 * 
 * @author dpovey
 *
 * @param <T> Type of result to return.
 */
public abstract class FutureIncrementalAction<T> extends FutureAction<T> {
    
    public FutureIncrementalAction() { }

    public FutureIncrementalAction(String name) {
        super(name);        
    }

    @Override    
    public T result() {
        if (GWT.isClient()) {            
            if (isComplete()) return super.result();
            if (keepCallingRun()) {
                // We call run repeatedly until we either encounter an unresolved
                // dependency or we have a result set.
                DeferredCommand.addCommand(new IncrementalCommand() {                    
                    public boolean execute() {                        
                        trySuperResult();                    
                        return keepCallingRun(); 
                    }
                });               
            }            
            setStarted(true);
            throw new IncompleteResultException(this, "Deferred execution for " + this.getName());
        }
        
        // Emulate to allow use in non GWT unit tests.
        do { 
            trySuperResult(); 
        } while(keepCallingRun()) ;
        return super.result();
    }
    
    private boolean keepCallingRun() {
        return !isComplete() && !hasUnresolvedDependencies();
    }

    @Override
    protected boolean recallRunOnResultRequested() {
        return true;
    }
    
    private void trySuperResult() {        
        try {
            super.result();
        } catch (Throwable t) { /* Squash */ }
    }    
}
