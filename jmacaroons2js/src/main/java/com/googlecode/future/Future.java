package com.googlecode.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *  Represents the future results of some operation which may either succeed, fail or
 *  be cancelled.
 * 
 * <p>A {@link Future} provides an alternative way to process asynchronous operations.  Rather
 * than providing a callback, a Future is either passed in or returned from an asynchronous
 * method.  When a result is available it may be obtained by using the {@link #result()} method.  If 
 * result() is called and no results are available then an {@link IncompleteResultException} should be
 * thrown.  If the operation threw an exception then an {@link ExecutionException} should be thrown
 * with the underlying cause.
 * 
 * @author Dean Povey
 * 
 * @see FutureResult
 * @see FutureAction
 *
 * @param <T> Type of the result.
 */
public interface Future<T> {

    /**
     * Return the result of this future if available.
     * 
     * @return result
     * @throws IncompleteResultException If result is not yet available
     * @throws ExecutionException If operation failed
     * @throws CancelledException if operation was cancelled
     * 
     */
    public abstract T result() throws IncompleteResultException,
            ExecutionException, CancelledException;
    
    /**
     * Return the exception returned for this future, or null if no
     * exception was set.
     * 
     *  @return the exception or null if no exception was set.
     */
    public abstract Throwable exception();

    /**
     * Cancel this future. 
     */
    public abstract void cancel();
    
    /**
     * Evalute the future.
     * 
     * @deprecated use {@link #start()}
     */
    @Deprecated
    public abstract void eval();
    
    
    /**
     * Start evaluating the future but do not register a callback to be notified when complete.
     * This can be used to preemptively evaluate a future without waiting for some
     * dependent result to require it.
     * 
     * @see AutoFuture
     */
    public abstract void start();
    
    /**
     * Add a callback that is invoked when a result becomes available.  This method may be called
     * multiple times with different callbacks.  If the same callback is used multiple times
     * it will only be called once when a result becomes available, otherwise each callback
     * is invoked in the order in which this method was called.
     * 
     * <p>Instances of this interface will also accept a callback of type {@link CancellableAsyncCallback}
     * and may perform additional handling for futures that are cancelled.
     * 
     * @param callback Callback to invoke
     */
    public abstract void addCallback(AsyncCallback<T> callback);

    /**
     * Whether a result is available.
     * 
     * @return true if result is available, was cancelled, or an exception
     *         occurred, false otherwise.
     */
    public abstract boolean isComplete();

    /**
     * Whether the operation was successful.
     * 
     *  @return true if operation succeeded, false if operation failed or is 
     *      incomplete. 
     */
    public abstract boolean isSuccessful();

    /**
     * Whether the operation was successful.
     * 
     * @return true if operation failed with an exception, false if operation succeeded or is 
     *  incomplete. 
     */
    public abstract boolean isFailure();

    /**
     * Whether this future has been cancelled.
     * 
     * @return true if cancelled, false otherwise.
     */
    public abstract boolean isCancelled();
    
    /**
     * Sets the result for this future to the specified value.
     * 
     * @param value value to return.
     */
    public abstract void setResult(T value);
         
    /**
     * Sets the result for this future to an empty (e.g. null) result.
     */
    public abstract void setEmpty();

    /**
     * Indicates that the future failed with the given exception.
     * 
     * @param t Exception to set.
     */
    public abstract void failWithException(Throwable t);
   
    /**
     * Return a callback that can be passed to an asynchronous method.  The
     * {@link AsyncCallback#onFailure(Throwable)}, {@link AsyncCallback#onSuccess(Object)}
     * and {@link CancellableAsyncCallback#onCancel()} methods of this callback will
     * invoke {@link #failWithException(Throwable)}, {@link #returnResult(Object)} and 
     * {@link #cancel()}
     * respectively.
     * 
     * @return a callback to be passed to another method.
     */
    public abstract CancellableAsyncCallback<T> callback();
    
    /**
     * Set the name of this future.  Can be useful for debugging.
     * 
     * @return
     */
    public void setName(String name);
    
    /**
     * Get the name of this future.  Can be useful for debugging.
     * 
     * @return the name of the future
     */
    public String getName();
    
}