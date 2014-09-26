package com.googlecode.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Converts any future to an AutoFuture, ie. a future that is always evaluated.
 * 
 * @author Dean Povey
 *
 * @param <T>
 */
public class Auto<T> implements AutoFuture<T> {
    
    private Future<T> future;

    public static <T> AutoFuture<T> auto(Future<T> future) {
        return new Auto<T>(future);
    }
    
    private Auto(Future<T> future) {
        this.future = future;
        future.start();
    }

    public void cancel() {
        future.cancel();
    }

    public void start() {
        future.start();
    }
    
    public void eval() {
        start();
    }

    public T result() throws IncompleteResultException, ExecutionException {
        return future.result();
    }

    public void addCallback(AsyncCallback<T> callback) {
        future.addCallback(callback);
    }

    public Throwable exception() {
        return future.exception();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isComplete() {
        return future.isComplete();
    }

    public boolean isFailure() {
        return future.isFailure();
    }

    public boolean isSuccessful() {
        return future.isSuccessful();
    }

    public void setResult(T value) {
        future.setResult(value);
    }

    public void setEmpty() {
        future.setEmpty();
    }
    
    public void failWithException(Throwable t) {
        future.failWithException(t);
    }

    public CancellableAsyncCallback<T> callback() {
        return future.callback();
    }

    public void setName(String name) {
        this.future.setName(name);
    }
    
    @Override
    public String toString() {
        return this.future.toString(); 
    }

    public String getName() {
        return "<Auto>" + this.future.getName();
    }
}
