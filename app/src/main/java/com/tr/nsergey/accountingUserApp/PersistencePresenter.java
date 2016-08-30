package com.tr.nsergey.accountingUserApp;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class PersistencePresenter extends BasePresenter  implements Action1<List<RequestElem>> {
    @Inject
    protected PersistenceModel persistenceModel;
    private List<RequestElem> queue;

    public PersistencePresenter(){
        super();
        App.getAppComponent().inject(this);
    }
    public void addEntry(List<Object> params, String userName, String operationName, String functionName) {
        persistenceModel.addEntry(params, userName, operationName, functionName);
    }
    public void deleteEntry(Integer id){
        persistenceModel.deleteEntry(id);
    }
    public void onDeleteDb(){
        persistenceModel.emptyDb();
    }
    public void subscribeForUpdates(List<RequestElem> requestElems) {
        this.queue = requestElems;
        Subscription subscription = persistenceModel.getQueue().subscribe(this);
        this.addSubscription(subscription);
    }
    @Override
    public void onStop() {
        super.onStop();
        persistenceModel.close();
    }

    @Override
    public void call(List<RequestElem> requestElems) {
        this.queue.clear();
        this.queue.addAll(requestElems);
    }
}
