package com.tr.nsergey.accountingUserApp;

import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

public class LocalDbModel {
    protected final Observable.Transformer schedulersTransformer;

    protected BriteDatabase briteDatabase;

    @Inject
    @Named("UI_THREAD")
    protected Scheduler uiThread;

    @Inject
    @Named("IO_THREAD")
    protected Scheduler ioThread;

    @SuppressWarnings("unchecked")
    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }

    public LocalDbModel() {
        App.getAppComponent().inject(this);
        schedulersTransformer = observable -> ((Observable) observable)
                .subscribeOn(ioThread)
                .observeOn(uiThread);
    }
    public void close(){
        if(briteDatabase != null) {
            briteDatabase.close();
        }
    }
}
