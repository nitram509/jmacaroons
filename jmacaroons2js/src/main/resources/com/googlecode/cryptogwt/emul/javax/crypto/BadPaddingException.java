package javax.crypto;

import java.security.GeneralSecurityException;

public class BadPaddingException extends GeneralSecurityException {

    private static final long serialVersionUID = -921257190066033921L;

    public BadPaddingException() {
    }

    public BadPaddingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadPaddingException(String message) {
        super(message);
    }

    public BadPaddingException(Throwable cause) {
        super(cause);
    }

}
