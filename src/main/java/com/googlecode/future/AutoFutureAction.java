package com.googlecode.future;


public abstract class AutoFutureAction<T> extends FutureAction<T> implements AutoFuture<T> {
    public AutoFutureAction() {
        start();
    }
    
    public AutoFutureAction(String name) {
        super(name);
        start();
    }

    @Override
    protected String getFutureType() {
        return "AutoFutureAction";
    }

}
