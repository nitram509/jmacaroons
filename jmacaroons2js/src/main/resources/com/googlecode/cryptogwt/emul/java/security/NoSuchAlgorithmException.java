package java.security;

/**
 * Exception thrown when algorithm not supported
 * 
 * @author Dean Povey
 *
 */
public class NoSuchAlgorithmException extends GeneralSecurityException {

    private static final long serialVersionUID = -5725417351514175203L;

    public NoSuchAlgorithmException() {
    }

    public NoSuchAlgorithmException(String message, Throwable cause) {
        super(message, cause);        
    }

    public NoSuchAlgorithmException(String message) {
        super(message);        
    }

    public NoSuchAlgorithmException(Throwable cause) {
        super(cause);        
    }

}
