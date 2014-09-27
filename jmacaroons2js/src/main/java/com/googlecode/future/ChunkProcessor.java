package com.googlecode.future;

import java.util.List;

/**
 * Interface for a handle that processes a collection of items in chunks.  The 
 * chunk method will be called for each chunk except for the last item.
 *
 * @param <DATA_TYPE>
 */
public interface ChunkProcessor<DATA_TYPE> {
    
    void before();

    void chunk(List<DATA_TYPE> chunk);

    void last(List<DATA_TYPE> chunk);
    
    void after();

}