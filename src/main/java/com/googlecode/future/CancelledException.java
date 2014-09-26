package com.googlecode.future;

/**
 * Exception thrown when a {@link FutureResult} or {@link FutureAction} has been cancelled.
 * 
 * @author Dean Povey
 *
 */
public class CancelledException extends RuntimeException {

    private static final long serialVersionUID = 2791084190296838396L;

    public CancelledException() {
    }

    public CancelledException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelledException(String message) {
        super(message);
    }

    public CancelledException(Throwable cause) {
        super(cause);
    }

}
