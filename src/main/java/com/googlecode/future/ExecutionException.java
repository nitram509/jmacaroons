package com.googlecode.future;

import java.lang.RuntimeException;

/**
 * <p>An {@link ExecutionException} is thrown from {@link FutureResult#result()} or
 * {@link FutureAction#result()} to indicate that an operation failed.  It allows the wrapped
 * exception to be rethrown and provides handling for processing checked exceptions or
 * rethrowing unchecked exceptions.
 * <p>
 * <code><pre>
 * FutureResult<Boolean> result = new FutureResult<Boolean>();
 * ...
 * try {
 *     boolean success = result.get();
 * } catch(ExecutionException e) {
 *     // Rethrow any checked exceptions declared by the calling method or unchecked exceptions
 *     throw (IOException) e.getCheckedCauseOrRethrow(IOException.class);
 * }
 * </pre></code>
 * <p>
 * More than one checked exception can be thrown in this way, however if you want to match
 * the calling method signature you must first cast to the appropriate type:
 * <p>
 * <code><pre>
 * FutureResult<Boolean> result = new FutureResult<Boolean>();
 * ...
 * try {
 *     boolean success = result.get();
 * } catch(ExecutionException e) {
 *     Exception checked = e.getCheckedCauseOrRethrow(IOException.class, CustomException.class);
 *     if (checked instanceof IOException) throw (IOException) e;
 *     if (checked instanceof CustomException) throw (CustomException) e;
 * }
 * </pre></code>
 * <p>
 * Alternatively, the message signature can just be declared to throw Exception.
 * 
 * @author Dean Povey
 *
 */
public class ExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public static Throwable returnIfCheckedThrowIfUnchecked(Throwable t) {
        if (t instanceof RuntimeException) throw (RuntimeException)t;
        if (t instanceof Error) throw (Error) t;
        return t;
    }
    
    public static Throwable wrapIfCheckedReturnIfUnchecked(Throwable t) {
        if (t instanceof RuntimeException) return t;
        if (t instanceof Error) return t;
        return new ExecutionException(t);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }
    
    public Exception getCheckedCauseOrRethrow(Class<? extends Exception>...expectedChecked) {
        Throwable cause = getCause();
        for (Class<? extends Exception> e : expectedChecked) {
            // TODO: Superclasses
            if (cause.getClass() == e) {
                return (Exception)cause;
            }
        }
        rethrowUncheckedCause();
        throw new AssertionError("Should have thrown unchecked cause");
    }
    
    
    public void rethrowUncheckedCause() {
        Throwable cause = getCause();
        if (cause == null) throw new IllegalStateException("No cause set");
        if (cause instanceof RuntimeException) throw (RuntimeException)cause;
        if (cause instanceof Error) throw (Error) cause;
        throw new IllegalStateException("Unexpected checked exception", cause);
    }

}
