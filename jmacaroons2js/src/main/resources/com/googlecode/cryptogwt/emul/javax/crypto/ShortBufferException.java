package javax.crypto;

import java.security.GeneralSecurityException;

/**
 * This exception is thrown when an output buffer provided by the user is too short to hold the operation result.
 *
 */
public class ShortBufferException extends GeneralSecurityException {

    private static final long serialVersionUID = 6727471961254106805L;

    public ShortBufferException() {
    }

    public ShortBufferException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShortBufferException(String message) {
        super(message);
    }

    public ShortBufferException(Throwable cause) {
        super(cause);
    }

}
