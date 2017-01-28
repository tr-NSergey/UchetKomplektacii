package com.tr.nsergey.uchetKomplektacii.Module;

import android.app.Application;
import android.content.Context;

import com.tr.nsergey.uchetKomplektacii.Presenter.AutologoutPresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.NbAccountsManager;
import com.tr.nsergey.uchetKomplektacii.Presenter.RequestPresenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application;
    }
    @Provides
    @Singleton
    public AutologoutPresenter provideAutologoutModule(){return new AutologoutPresenter();}
    @Provides
    @Singleton
    public RequestPresenter provideRequestPresenter(){return new RequestPresenter();}
    @Provides
    @Singleton
    public NbAccountsManager provideNbAccountsManager(Context context){return new NbAccountsManager(context);}
}
