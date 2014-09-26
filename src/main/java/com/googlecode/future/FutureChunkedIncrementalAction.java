package com.googlecode.future;

import java.util.ArrayList;
import java.util.List;


/**
 * Create an incremental future that iterates over a collection of data in chunks.
 * 
 * <p>Subclasses must implement at least the {@link #chunk(List)} method to 
 * process chunks.  They may also override the {@link #before()} and {@link #after} methods,
 * (called before and after any chunks are processed), and the {@link #last(List)} method
 * which is called to process the last chunk.  The default implementation of last(List) will
 * simply call chunk(List).
 * 
 * <p>The default chunk size is 1.
 * 
 * @author Dean Povey
 *
 * @param <DATA_TYPE> type of data to process
 * @param <RESULT_TYPE> result to set the future for on completion
 */
public abstract class FutureChunkedIncrementalAction<RESULT_TYPE, DATA_TYPE> extends 
    FutureIncrementalAction<RESULT_TYPE> implements ChunkProcessor<DATA_TYPE> {
    
    public static int DEFAULT_CHUNK_SIZE = 1;
    
    private final int chunkSize;
    
    private int offset = 0;
    
    private int size;
    
    private boolean isBeforeCompleted = false;
    
    private List<DATA_TYPE> data;
    
    private Future<? extends Iterable<DATA_TYPE>> getData;
    
    public FutureChunkedIncrementalAction(String name, Future<? extends Iterable<DATA_TYPE>> data) {
        this(data, DEFAULT_CHUNK_SIZE);
        setName(name);
    }
    
    public FutureChunkedIncrementalAction(String name, Future<? extends Iterable<DATA_TYPE>> data, int chunkSize) {
        this(data, chunkSize);
        setName(name);
    }
    
    public FutureChunkedIncrementalAction(Future<? extends Iterable<DATA_TYPE>> data) {
        this(data, DEFAULT_CHUNK_SIZE);
    }
    
    public FutureChunkedIncrementalAction(Future<? extends Iterable<DATA_TYPE>> data, int chunkSize) {
        this.chunkSize = chunkSize;
        this.getData = data;        
    }

    public FutureChunkedIncrementalAction(List<DATA_TYPE> data) {
        this(data, DEFAULT_CHUNK_SIZE);
    }
    
    public FutureChunkedIncrementalAction(List<DATA_TYPE> data, int chunkSize) {
        this.chunkSize = chunkSize;
        this.data = data;
        this.size = data.size();
    }

    
    public FutureChunkedIncrementalAction(Iterable<DATA_TYPE> data) {
        this(data, DEFAULT_CHUNK_SIZE);
    }
    
    public FutureChunkedIncrementalAction(Iterable<DATA_TYPE> data, int chunkSize) {
        this(iterableAsList(data), chunkSize);
    }
    
    public FutureChunkedIncrementalAction(String name, Iterable<DATA_TYPE> data) {
        this(data);
        setName(name);        
    }
    
    public FutureChunkedIncrementalAction(String name,
            Iterable<DATA_TYPE> data, int chunkSize) {
        this(data, chunkSize);
        setName(name);        
    }

    private final static <DATA_TYPE> List<DATA_TYPE> iterableAsList(Iterable<DATA_TYPE> data) {
        List<DATA_TYPE> result = new ArrayList<DATA_TYPE>();
        for (DATA_TYPE item : data) {
            result.add(item);
        }
        return result;
    }

    
    public void run() {
        if (isComplete()) return;
        if (!isBeforeCompleted) {
            if (data == null) {
                data = iterableAsList(getData.result());
                size = data.size();
            }
            before();
            isBeforeCompleted = true;            
        }
        if (offset == size) {
            after();            
            return;
        }
        nextChunk();
    }

    private void nextChunk() {
        int endOfChunk = offset + chunkSize;
        if (endOfChunk >= size) {
            endOfChunk = size;
            last(subList(data, offset, endOfChunk));
        } else {
            chunk(subList(data, offset, endOfChunk));
        }
        // Note: If either of last or chunk throw an IncompleteResultException then those
        // methods will be re-called with the current chunk.  Otherwise:
        offset = endOfChunk;
    }

    private static <T> List<T> subList(List<T> data, int offset, int endOfChunk) {
        // Required as List.subList() not supported by GWT.
        List<T> subList = new ArrayList<T>();
        for (int i = offset; i < endOfChunk; i++) {
            subList.add(data.get(i));            
        }
        return subList;
    }
    
    public void before() { }
    
    public void after() { }
    
    public void last(List<DATA_TYPE> data) {
        chunk(data);
    }

}
