package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class MacSpi {
    protected abstract int engineGetMacLength();
    protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
         throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract void engineUpdate(byte input);
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    protected abstract byte[] engineDoFinal();
    protected abstract void engineReset();
}
