package com.tr.nsergey.uchetKomplektacii.Presenter;

import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public abstract class BasePresenter implements Action1<List<ArtObject>> {
    @Inject
    protected CompositeSubscription compositeSubscription;
    @Inject
    protected PersistenceModel persistenceModel;

    String dbName;
    List<ArtObject> artObjects;

    public BasePresenter(){
        App.getAppComponent().inject(this);
    }
    public void addSubscription(Subscription subscription){
        compositeSubscription.add(subscription);
    }
    public void deleteEntry(Integer id) {
        persistenceModel.deleteEntry(dbName, id);
    }

    public void addEntry(ArtObject artObject){
        persistenceModel.addEntry(dbName, artObject);
    }
    public void addEntries(List<ArtObject> artObjects) {
        for (ArtObject artObject : artObjects) {
            persistenceModel.addEntry(dbName, artObject);
        }
    }
    public void replaceAllEntries(List<ArtObject> artObjects){
        persistenceModel.replaceAllEntries(dbName, artObjects);
    }
    public void subscribeForUpdates(List<ArtObject> artObjects) {
        this.artObjects = artObjects;
        Subscription subscription = persistenceModel.getSubscription(dbName).subscribe(this);
        this.addSubscription(subscription);
    }
    public void onDeleteDb() {
        persistenceModel.emptyDb(dbName);
    }
    public void onStop() {
        persistenceModel.close();
        compositeSubscription.clear();
    }
    @Override
    public void call(List<ArtObject> artObjects) {
        this.artObjects.clear();
        this.artObjects.addAll(artObjects);
    }

    public List<ArtObject> getEntries(String art){
        return persistenceModel.getEntries(dbName, art);
    }
}
