package java.security;

public class DigestException extends GeneralSecurityException {

    private static final long serialVersionUID = -923424751337183219L;

    public DigestException() {
    }

    public DigestException(String message, Throwable cause) {
        super(message, cause);
    }

    public DigestException(String message) {
        super(message);
    }

    public DigestException(Throwable cause) {
        super(cause);
    }

}
