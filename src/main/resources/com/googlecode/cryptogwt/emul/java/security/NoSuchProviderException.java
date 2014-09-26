package java.security;

public class NoSuchProviderException extends GeneralSecurityException {

    private static final long serialVersionUID = 6480637004302499757L;

    public NoSuchProviderException() {
    }

    public NoSuchProviderException(String message, Throwable cause) {
        super(message, cause);        
    }

    public NoSuchProviderException(String message) {
        super(message);        
    }

    public NoSuchProviderException(Throwable cause) {
        super(cause);        
    }

}
