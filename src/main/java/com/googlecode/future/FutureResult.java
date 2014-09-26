package com.googlecode.future;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import static com.googlecode.future.ExecutionException.returnIfCheckedThrowIfUnchecked;

/**
 * Represents the future results of some operation which may either succeed or fail.
 * 
 *  <p>FutureResult may be used as a replacement for an {@link AsyncCallback}, for example:
 *  
 *  <code><pre>
 *  RemoteInterfaceAsync remote = GWT.create(RemoteInterface.class);
 *  FutureResult<Boolean> result = new FutureResult<Boolean>();
 *  remote.callRemoteMethod("parameter", result);
 *  ....
 *  // At Some later time
 *  if (result.isDone()) {
 *      boolean success = result.getValue();
 *  } 
 *  </pre></code>
 *  
 *  <p>In general a FutureResult will be used to collect a result from 
 *  one or more {@link FutureAction} instances.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public class FutureResult<T> implements CancellableAsyncCallback<T>, Future<T> {
    
    private String name;
    
    public FutureResult() { }
    
    public FutureResult(String name) {
        this.name = name;
    }

    private T value = null;
    
    private Throwable exception = null;
    
    private LinkedHashSet<AsyncCallback<T>> listeners = new LinkedHashSet<AsyncCallback<T>>();
    
    private enum State { 

        SUCCEEDED, FAILED, INCOMPLETE, CANCELLED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    private State state = State.INCOMPLETE;

    /** {@inheritDoc} */
    public T result() throws IncompleteResultException, ExecutionException,
        CancelledException {
        switch(state) {
        case INCOMPLETE: throw new IncompleteResultException(this, "Future result not yet set for " + this);
        case FAILED: {
            Throwable checkedException = returnIfCheckedThrowIfUnchecked(exception);
            throw new ExecutionException(checkedException);
        }
        case CANCELLED: throw new CancelledException();
        case SUCCEEDED: return value;
         
        }
        throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    public void addCallback(AsyncCallback<T> callback) {
        if (callback == null) return;
        if (isComplete()) {
            if (isSuccessful()) callback.onSuccess(value);
            else callback.onFailure(this.exception);
            return;
        }
       listeners.add(callback);
    }

    /** {@inheritDoc} */
    public boolean isComplete() {
        return state != State.INCOMPLETE;
    }
    
    /** {@inheritDoc} */
    public boolean isSuccessful() {
        return state == State.SUCCEEDED;
    }
    
    /** {@inheritDoc} */
    public boolean isFailure() {
        return state == State.FAILED;
    }
    

    /** {@inheritDoc} */
    public Throwable exception() {
        return this.exception;
    }

    /** {@inheritDoc} */
    public void failWithException(Throwable t) {
        if (isComplete()) {            
            throw new IllegalStateException("Cannot fail when already complete for " + this,
                    t);
        }
        state = State.FAILED;
        this.exception = t;
        onCompleted();        
        notifyListenersOnFailure();
    }

    /** {@inheritDoc} */
    public void setResult(T value) {
        if (isComplete()) {            
            throw new IllegalStateException("Cannot set result when already complete for " + 
                    this);
        }
        state = State.SUCCEEDED;
        this.value = value;
        onCompleted();
        notifyListenersOnSuccess(value);        
    }

    /** {@inheritDoc} */
    public void setEmpty() {
        setResult(null);
    }

    
    private void notifyListenersOnSuccess(T value) {
        for (AsyncCallback<T> callback : copyCallbacksThenClear()) {
            callback.onSuccess(value);
        }
    }
    
    private List<AsyncCallback<T>> copyCallbacksThenClear() {
        List<AsyncCallback<T>> callbacks = new ArrayList<AsyncCallback<T>>(this.listeners);
        this.listeners.clear();
        return callbacks;
    }
    
    /**
     * Method called to indicate a failure.  By default calls {@link #onCancel()} 
     * if exception is {@link CancelledException}, or {@link #failWithException(Throwable)}
     * otherwise.  May be overridden by subclasses.
     */
    public void onFailure(Throwable t) {
        if (t instanceof CancelledException) onCancel();
        else failWithException(t);
    }
    
    /**
     * Method called to indicate success.  By default sets the future to return the
     * result.  May be overridden by subclasses.
     */
    public void onSuccess(T value) {
        setResult(value);
    }
    
    /**
     * Method called to indicate future was cancelled.  By default will call 
     * {@link #setCancelled()} but this behavior may be overriden by subclasses. 
     */
    public void onCancel() {
        setCancelled();
    }

    /**
     * Sets the state of this result to cancelled.  This method is distinct from simply calling
     * cancel which calls the onCancel method which may be overriden by subclasses.  This will
     * set the state of the future to cancelled (so that isCancelled returns true) and set
     * a CancelledException.
     */
    protected void setCancelled() {
        if (isComplete()) return;
        state = State.CANCELLED;
        this.exception = new CancelledException();
        onCompleted();        
        notifyListenersOnCancel();
    }
    
    /**
     * Method called when either a result becomes available or an exception is set. Subclasses
     * may override this to provide custom processing.
     */
    protected void onCompleted() {        
    }

    
    /** {@inheritDoc} */
    public void cancel() {
        onCancel();     
    }

    private void notifyListenersOnFailure() {
        for (AsyncCallback<T> callback : copyCallbacksThenClear()) {
            callback.onFailure(this.exception);
        }
    }
    
    private void notifyListenersOnCancel() {
        for (AsyncCallback<T> callback : copyCallbacksThenClear()) {
            if (callback instanceof CancellableAsyncCallback<?>) {
                ((CancellableAsyncCallback<?>) callback).onCancel();                
            } else callback.onFailure(this.exception);
        }
    }

    /** {@inheritDoc} */
    public boolean isCancelled() {
        return state == State.CANCELLED;
    }
    
    /** {@inheritDoc} */
    public void eval() {
        start();
    }
    
    /** {@inheritDoc} */
    public void start() {
        addCallback(null);
    }


    /** {@inheritDoc} */
    public CancellableAsyncCallback<T> callback() {
        return this;
    }

    /** {@inheritDoc} */
    public String getName() {
        return (name != null && !name.isEmpty()) ? name : synthesizeName();       
    }

    private String synthesizeName() {
        return getFutureType() + "<" +
                ((value != null) ? getSimpleName(value.getClass()) : "?")
                + ">";
    }

    private String getSimpleName(Class<?> type) {
        String className = type.getName();
        int index = className.lastIndexOf('.');
        if (index == -1) return className;
        return className.substring(index + 1);
    }
    
    protected String getFutureType() {
        return "FutureResult";
        
    }

    /** {@inheritDoc} */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        switch(state) {
        case CANCELLED:
            return getName() + " (cancelled)";            
        case FAILED:            
            return getName() + " (failed with cause:\n" + this.exception + ")";            
        case INCOMPLETE:
            return getName() + " (incomplete)";            
        case SUCCEEDED:
            return getName() + " (succeeded with result = " + value + ")";
        }
        throw new IllegalStateException();
    }
    
    

}
