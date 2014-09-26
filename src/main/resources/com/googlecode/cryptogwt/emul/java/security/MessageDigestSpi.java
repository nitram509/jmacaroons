package java.security;

public abstract class MessageDigestSpi {

    /**
     * Returns the digest length in bytes.
     *
     * <p>This concrete method has been added to this previously-defined
     * abstract class. (For backwards compatibility, it cannot be abstract.)
     * 
     * <p>The default behavior is to return 0.
     * 
     * <p>This method may be overridden by a provider to return the digest
     * length.
     *
     * @return the digest length in bytes.
     *
     * @since 1.2
     */
    protected int engineGetDigestLength() {
        return 0;
    }

    /**
     * Updates the digest using the specified byte.
     *
     * @param input the byte to use for the update.
     */
    protected abstract void engineUpdate(byte input);

    /**
     * Updates the digest using the specified array of bytes,    
     * starting at the specified offset.
     *
     * @param input the array of bytes to use for the update.
     *
     * @param offset the offset to start from in the array of bytes.
     *
     * @param len the number of bytes to use, starting at 
     * <code>offset</code>.
     */
    protected abstract void engineUpdate(byte[] input, int offset, int len);

    
    /**
     * Completes the hash computation by performing final
     * operations such as padding. Once <code>engineDigest</code> has 
     * been called, the engine should be reset (see 
     * {@link #engineReset() engineReset}).  
     * Resetting is the responsibility of the
     * engine implementor.
     *
     * @return the array of bytes for the resulting hash value.  
     */
    protected abstract byte[] engineDigest();

    /**
     * Completes the hash computation by performing final
     * operations such as padding. Once <code>engineDigest</code> has
     * been called, the engine should be reset (see 
     * {@link #engineReset() engineReset}).  
     * Resetting is the responsibility of the
     * engine implementor.
     *
     *
     * @param buf the output buffer in which to store the digest
     *
     * @param offset offset to start from in the output buffer
     *
     * @param len number of bytes within buf allotted for the digest.
     * Both this default implementation and the SUN provider do not
     * return partial digests.  The presence of this parameter is solely
     * for consistency in our API's.  If the value of this parameter is less
     * than the actual digest length, the method will throw a DigestException.
     * This parameter is ignored if its value is greater than or equal to
     * the actual digest length.
     *
     * @return the length of the digest stored in the output buffer.
     * 
     * @exception DigestException if an error occurs.
     *
     * @since 1.2
     */
    protected void engineDigest(byte[] buf, int offset, int len)
                        throws DigestException {
        byte[] result = engineDigest();
        if (len - offset < result.length) throw new DigestException("Output Buffer to short");
        System.arraycopy(result, 0, buf, offset, result.length);
    }

    /**
     * Resets the digest for further use.
     */
    protected abstract void engineReset();    

}
