package com.tr.nsergey.uchetKomplektacii.Presenter;

import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;

public class HistoryPresenter extends BasePresenter{
    private List<Observer<Void>> updateObservers;

    public HistoryPresenter(){
        super();
        dbName = PersistenceModel.HISTORY;
        updateObservers = new ArrayList<>();
    }

    @Override
    public void call(List<ArtObject> artObjects) {
        super.call(artObjects);
        for(Observer<Void> obs: updateObservers){
            obs.onNext(null);
        }
    }

    public void addUpdateObserver(Observer<Void> adapter) {
        updateObservers.add(adapter);
    }
}
