package com.tr.nsergey.uchetKomplektacii.Presenter;

import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;

public class QueuePresenter extends BasePresenter{

    public QueuePresenter() {
        super();
        dbName = PersistenceModel.QUEUE;
    }
}
