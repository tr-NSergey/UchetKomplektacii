package com.tr.nsergey.uchetKomplektacii.Presenter;

import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;

import java.util.List;

public class BackupPresenter extends BasePresenter {

    public BackupPresenter() {
        super();
        dbName = PersistenceModel.BACKUP;
    }

}
