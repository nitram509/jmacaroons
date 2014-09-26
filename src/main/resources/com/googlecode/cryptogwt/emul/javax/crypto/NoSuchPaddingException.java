package javax.crypto;

import java.security.GeneralSecurityException;

/**
 * 
 * This exception is thrown when a particular padding mechanism is requested but
 * is not available in the environment.
 * 
 */
public class NoSuchPaddingException extends GeneralSecurityException {

    private static final long serialVersionUID = -2290918942476369781L;

    public NoSuchPaddingException() {
    }

    public NoSuchPaddingException(String message, Throwable cause) {
        super(message, cause);        
    }

    public NoSuchPaddingException(String message) {
        super(message);        
    }

    public NoSuchPaddingException(Throwable cause) {
        super(cause);        
    }

}
