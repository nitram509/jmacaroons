package com.googlecode.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback that supports cancelling.
 * 
 * @author Dean Povey
 *
 * @param <T>
 */
public interface CancellableAsyncCallback<T> extends AsyncCallback<T> {
    /**
     * Called when an asynchronous call is cancelled.
     */
    void onCancel();
}
