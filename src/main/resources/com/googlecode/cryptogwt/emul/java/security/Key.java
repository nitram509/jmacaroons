package java.security;

import java.io.Serializable;

public interface Key extends Serializable {

    public abstract String getAlgorithm();

    public abstract String getFormat();

    public abstract byte[] getEncoded();

}