package com.tr.nsergey.uchetKomplektacii.Module;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

@Module
public class ThreadsModule {
    public ThreadsModule(){}
    @Provides
    @Singleton
    @Named("UI_THREAD")
    Scheduler provideSchedulerUI(){
        return AndroidSchedulers.mainThread();
    }
    @Provides
    @Singleton
    @Named("IO_THREAD")
    Scheduler provideSchedulerIO(){
        return Schedulers.io();
    }
    @Provides
    @Singleton
    CompositeSubscription provideCompositeSubscription(){
        return new CompositeSubscription();
    }
}
