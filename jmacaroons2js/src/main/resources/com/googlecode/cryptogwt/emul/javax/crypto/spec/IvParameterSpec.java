package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class IvParameterSpec implements AlgorithmParameterSpec {
    
    private final byte[] iv;

    public IvParameterSpec(byte[] iv) {
        this.iv = iv;
    }
    
    public byte[] getIV() {
        return iv;
    }

}
