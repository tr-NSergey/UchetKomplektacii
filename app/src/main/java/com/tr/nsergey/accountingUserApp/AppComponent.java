package com.tr.nsergey.accountingUserApp;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Provides;

@Singleton
@Component(modules = {AppModule.class, PersistenceModule.class, ThreadsModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(LocalDbModel localDbModel);
    void inject(PersistenceModel persistenceModel);
    void inject(PersistencePresenter persistencePresenter);
    void inject(RequestTask requestTask);
}
