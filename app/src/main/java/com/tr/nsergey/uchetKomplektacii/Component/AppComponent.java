package com.tr.nsergey.uchetKomplektacii.Component;

import com.tr.nsergey.uchetKomplektacii.Model.AppsScriptRequest;
import com.tr.nsergey.uchetKomplektacii.Services.BackupService;
import com.tr.nsergey.uchetKomplektacii.Model.LocalDbModel;
import com.tr.nsergey.uchetKomplektacii.Model.MakeRequestTask;
import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;
import com.tr.nsergey.uchetKomplektacii.Module.AppModule;
import com.tr.nsergey.uchetKomplektacii.Module.PersistenceModule;
import com.tr.nsergey.uchetKomplektacii.Module.ThreadsModule;
import com.tr.nsergey.uchetKomplektacii.Presenter.BasePresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.RequestPresenter;
import com.tr.nsergey.uchetKomplektacii.View.HistoryScreen;
import com.tr.nsergey.uchetKomplektacii.View.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, PersistenceModule.class, ThreadsModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(LocalDbModel localDbModel);
    void inject(BasePresenter basePresenter);
    void inject(PersistenceModel persistenceModel);
    void inject(MakeRequestTask makeRequestTask);
    void inject(RequestPresenter requestPresenter);
    void inject(HistoryScreen historyFragment);
    void inject(BackupService backupService);
    void inject(AppsScriptRequest appsScriptRequest);
}
