package com.googlecode.future;

/**
 * Result of an operation that is constant but needs to be accessed as a FutureResult.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public class ConstantResult<T> extends FutureResult<T> implements AutoFuture<T> {
     
    /**
     * Create a {@link ConstantResult} with the given value.
     * 
     * @param value value to set the result to.
     */
    public ConstantResult(String name, T value) {
        super(name);
        setResult(value);
    }
    
    /**
     * Create a {@link ConstantResult} with the given value.
     * 
     * @param value value to set the result to.
     */
    public ConstantResult(T value) {
        setResult(value);
    }       
    
    /**
     * Convenience factory method that can be used to create a {@link ConstantResult}
     * 
     * @param <T> Type of result
     * @param value value to set the result to
     * @return a ConstantResult containing the specified value.
     */
    public static <T> ConstantResult<T> constant(T value) {
        return new ConstantResult<T>(value); 
    }  
    
    /**
     * Convenience factory method that can be used to create a {@link ConstantResult}
     * 
     * @param <T> Type of result
     * @param value value to set the result to
     * @return a ConstantResult containing the specified value.
     */
    public static <T> ConstantResult<T> constant(String name, T value) {
        return new ConstantResult<T>(name, value); 
    }

    @Override
    protected String getFutureType() {
        return "ConstantResult";        
    }
    
    

}
