package com.googlecode.future;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A FutureAction represents an action that may be run at some future point
 * (possibly never). It implements {@link Runnable} and the {@link #run()}
 * method is used to specify the action to perform when run. There are some
 * constraints on the run() method, either it must be side-effect free (ie.
 * there must be no method calls which perform an action that cannot be
 * performed multiple times, e.g displaying a result or calling a service), or
 * it must ensure that any references to other {@link Future} instances happen
 * before any side-effects. This quirk comes about because of the way a
 * FutureAction evaluates its run method. If a dependent Future is incomplete,
 * accessing it via {@link #result()} will throw an IncompleteResultException
 * including a reference to the Future which is incomplete. The FutureAction
 * adds a callback so that when this value becomes available then it will
 * re-call the run method. If there are multiple dependencies this continues
 * until all are satisfied and then the side effects can be run. A Future can
 * only be assigned to once so this ensures that once a result is available a
 * call to get() is idempotent, and once the run method() completes successfully
 * (or fails) then it will not be run again.
 * 
 * <p>
 * It is a very important to note that creating a FutureAction does not cause
 * the action to be run. This will only happen if either the {@link #result()},
 * {@link #addCallback(AsyncCallback)} or {@link #start()} is called. The
 * effect of this is that the calling of chained asynchronous actions optimized so
 * that they are only run if they are needed and this approach automatically
 * handles for example boolean shortcutting while resolving dependencies. e.g.
 * 
 * <code><pre>
 * Future<Boolean> succeeds = new FutureAction<Boolean>() {
 *    public void run() {
 *        returnResult(true);
 *   }
 * }
 * 
 * Future<Boolean> neverCalled = new FutureAction<Boolean>() {
 *    public void run() {
 *        throw new AssertionError("Should never be called");
 *    }
 * }
 * 
 * FutureAction<Boolean> result = new FutureAction<Boolean>() {
 *    public void run() {
 *        returnResult(succeeds.result() || fails.result()); // Note: fails will never be run!
 *    }
 * }
 * </pre></code>
 * 
 * <p>
 * In addition, a given action may call a dependent Future as many times as it
 * likes but it will only be evaluated once and the same result returned
 * thereafter.
 * 
 * <p>
 * It should be noted that the FutureAction must either call {@link #returnResult(Object)},
 * {@link #returnEmpty()}, {@link #failWithException(Throwable)}, or {@link #cancel()}; or
 * it must use {@link #callback()} to pass itself to a method that will complete the
 * action via a callback. If this does not happen then the action will never complete.
 * 
 * <p>
 * To return an exception the action can either just throw the exception if it is
 * unchecked or else it can call the {@link #failWithException(Throwable)}
 * method.
 * 
 * @author Dean Povey
 * 
 * @see FutureResult
 * 
 * @param <T> Type of result
 */
public abstract class FutureAction<T> extends FutureResult<T> implements Runnable {
    
    private Set<Future<?>> dependencies = new HashSet<Future<?>>();
    
    private boolean isStarted = false;
    
    private boolean isRunning = false;
 
      
    public FutureAction() {        
    }

    public FutureAction(String name) {
        super(name);
    }

    @Override
    public void addCallback(AsyncCallback<T> callback) {        
        super.addCallback(callback);
        if (!isComplete() && !isStarted()) {
            tryResult();
        }
    }

    @Override
    public T result() {
        if (isComplete()) return super.result();
                
        if (hasUnresolvedDependencies()) {
            throw new IncompleteResultException(this,
                    "Future (" + this + ") has unresolved dependency",
                    new IncompleteResultException(dependencies.iterator().next(), String.valueOf(this)));
        }
        
        if (isStarted() && !recallRunOnResultRequested()) {
            throw new IncompleteResultException(this,
                    "Waiting for result to be set manually or by callback for " + this);
        }
        
        if (isRunning()) {
            throw new IncompleteResultException(this,
                "Still executing run() for " + this);
        }
        
        try {
            setRunning(true);            
            run();            
            setStarted(true);
        } catch(IncompleteResultException e) {
            final Future<?> dependency = e.getFuture();
            addDependency(dependency);
            throw new IncompleteResultException(this, "Found incomplete dependency " + dependency + " for " + this, e);
        } catch(CancelledException e) {
            onCancel();
        } catch(Throwable t) {
            failWithException(t);
        } finally {
            setRunning(false);
        }
        
        return super.result();
    }
    
    /**
     * Indicates whether calling result multiple times should recall the run() method, or
     * whether it should fail with an IncompleteResultException after the first time being run.
     * 
     * @return true if the run method should be recalled.
     */
    protected boolean recallRunOnResultRequested() {        
        return false;
    }

    @Override
    public void setResult(T value) {
        super.setResult(value);
    }
    
    /**
     * Synonym for setResult()
     * 
     * @param value
     */
    public void returnResult(T value) {
        setResult(value);
    }
    
    /**
     * Synonym for setEmpty()
     * 
     * @param value
     */
    public void returnEmpty() {
        setEmpty();
    }
    
    public void onDependencyFailed(Future<?> dependency, Throwable t) {
        Throwable rethrow = catchException(t);
        if (rethrow != null) {            
            super.failWithException(rethrow);
            return;
        }
        dependencies.remove(dependency);
        tryResult();
    }
    
    
    @Override
    public void failWithException(Throwable t) {        
        Throwable rethrow = catchException(t);
        if (rethrow != null) {
            super.failWithException(rethrow);
            return;
        }
        tryResult();
    }
    
    @Override
    public void cancel() {
        onCancel();
    }

    protected void setStarted(boolean b) {
        isStarted = b;
    }

    protected boolean hasUnresolvedDependencies() {
        return dependencies.size() > 0;
    }

    private boolean isStarted() {
        return isStarted && !isComplete();
    }

    @SuppressWarnings("unchecked")
    private void addDependency(final Future<?> dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
            dependency.addCallback(new AsyncCallback() {
                public void onFailure(Throwable t) {
                    if (t instanceof CancelledException) FutureAction.this.cancel();                    
                    else onDependencyFailed(dependency, t);
                }

                public void onSuccess(Object result) {
                    dependencies.remove(dependency);                    
                    tryResult();                    
                }
                
            });
        }
    }
    
    /**
     * Try to evaluate result, but do not propogate exceptions.
     */
    protected void tryResult() {        
        try {
            result();
        } catch(Throwable t) {
            // Squash.  This is a little, dangerous however any exceptions should be
            // caught in the result() method and then set in the result.
        }
    }

    /**
     * Interceptor that allows exceptions to be caught and will not propogate them to other
     * dependent futures.
     * 
     * <p>
     * Supposing you have the following futures:
     * 
     * <code><pre>
     * final FutureAction&lt;Boolean&gt; failure = new FutureAction&lt;Boolean&gt;() {
     *      public void run() {
     *          // Do something... and eventually:
     *          throw SomeException()                
     *      }
     *  };
     *  </pre></code>
     *  
     *  <p>Normally this exception would just be propagated to any dependent futures, however
     *  if we want to catch and process this exception then we can override {@link #catchException(Throwable)}
     *  to do it.  e.g.
     *  
     *  <code><pre>
     *  final FutureAction&lt;Boolean&gt; catcher = new FutureAction&lt;Boolean&gt;() {
     *
     *      public void run() {
     *          boolean result = false;
     *          try {
     *              failure.result();
     *          } catch(SomeException e) {
     *              result = true;
     *          }
     *          returnResult(result);
     *      }
     *      
     *      &#064;Override
     *      public Throwable catchException(Throwable t) {
     *          if (t instanceof SomeException) return null;
     *          return t;
     *      }
     *  };
     *  </code></pre>
     *  
     *  <p>When the future failure throws an exception the catchException method
     *  of catcher will be invoked.  Because catcher returns null this will remove the
     *  dependency but not call failWithException for this future or any dependents.
     *  The run() method will then be recalled.  Because failure has failed a call to
     *  result() will throw an exception which we must catch and then go on processing.  If
     *  this exception was not caught then catchException would be re-called, the exception
     *  would be ignored and catcher would exit without having any result set. 
     *         
     * 
     * @param t The exception to process
     * @return null to ignore the exception, the passed in exception to rethrow it or another
     *    exception to throw a different type
     */
    public Throwable catchException(Throwable t) {
        return t;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    protected String getFutureType() {
        return "FutureAction";
    }

}
