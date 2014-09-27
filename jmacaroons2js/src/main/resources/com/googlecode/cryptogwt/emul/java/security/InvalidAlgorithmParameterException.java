package java.security;

public class InvalidAlgorithmParameterException extends
        GeneralSecurityException {

    private static final long serialVersionUID = 2772607662424458745L;

    public InvalidAlgorithmParameterException() {
    }

    public InvalidAlgorithmParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAlgorithmParameterException(String message) {
        super(message);
    }

    public InvalidAlgorithmParameterException(Throwable cause) {
        super(cause);
    }

}
