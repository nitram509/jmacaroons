package java.security.spec;

import java.security.GeneralSecurityException;

public class InvalidKeySpecException extends GeneralSecurityException {

    private static final long serialVersionUID = -1183401466209991667L;

    public InvalidKeySpecException() {
    }

    public InvalidKeySpecException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidKeySpecException(String message) {
        super(message);
    }

    public InvalidKeySpecException(Throwable cause) {
        super(cause);
    }

}
