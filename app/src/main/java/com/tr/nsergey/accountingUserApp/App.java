package com.tr.nsergey.accountingUserApp;

import android.app.Application;

/**
 * Created by sergey on 28.08.16.
 */
public class App extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate(){
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getAppComponent(){
        return appComponent;
    }
}
