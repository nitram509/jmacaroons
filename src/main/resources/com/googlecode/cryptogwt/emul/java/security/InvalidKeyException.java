package java.security;

/**
 * This is the exception for invalid Keys (invalid encoding, wrong length,
 * uninitialized, etc).
 * 
 */
public class InvalidKeyException extends GeneralSecurityException {

    private static final long serialVersionUID = -2688558921393606965L;

    public InvalidKeyException() {
    }

    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidKeyException(String message) {
        super(message);
    }

    public InvalidKeyException(Throwable cause) {
        super(cause);
    }

}
