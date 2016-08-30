package com.tr.nsergey.accountingUserApp;

import javax.inject.Inject;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sergey on 29.08.16.
 */
public abstract class BasePresenter {
    @Inject
    protected CompositeSubscription compositeSubscription;

    public void addSubscription(Subscription subscription){
        compositeSubscription.add(subscription);
    }

    public void onStop() {
        compositeSubscription.clear();
    }
}
