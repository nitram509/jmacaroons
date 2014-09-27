package javax.crypto.interfaces;

import javax.crypto.SecretKey;

public interface PBEKey extends SecretKey {
    char[] getPassword();
    byte[] getSalt();
    int getIterationCount();
}
