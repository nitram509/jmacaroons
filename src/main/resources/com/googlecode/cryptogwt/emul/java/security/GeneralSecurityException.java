package java.security;

/**
 * 
 * Superclass for Security Related Exceptions
 * 
 * @author Dean Povey
 *
 */
public class GeneralSecurityException extends Exception {

    private static final long serialVersionUID = 7591142745316452104L;

    public GeneralSecurityException() {
    }

    public GeneralSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralSecurityException(String message) {
        super(message);
    }

    public GeneralSecurityException(Throwable cause) {
        super(cause);
    }

}
