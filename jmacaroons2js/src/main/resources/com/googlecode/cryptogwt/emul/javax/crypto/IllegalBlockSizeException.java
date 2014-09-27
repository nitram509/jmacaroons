package javax.crypto;

import java.security.GeneralSecurityException;

public class IllegalBlockSizeException extends GeneralSecurityException {

    private static final long serialVersionUID = 1166986896887187954L;

    public IllegalBlockSizeException() {
    }

    public IllegalBlockSizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalBlockSizeException(String message) {
        super(message);
    }

    public IllegalBlockSizeException(Throwable cause) {
        super(cause);
    }

}
