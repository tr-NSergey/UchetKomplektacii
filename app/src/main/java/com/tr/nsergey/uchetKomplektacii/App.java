package com.tr.nsergey.uchetKomplektacii;

import android.app.Application;
import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.tr.nsergey.uchetKomplektacii.Component.AppComponent;
import com.tr.nsergey.uchetKomplektacii.Component.DaggerAppComponent;
import com.tr.nsergey.uchetKomplektacii.Module.AppModule;

import java.util.Calendar;

public class App extends Application {

    public static final String MODE_SKETCH = "Работа с чертежом";
    public static final String MODE_ACCOUNTING = "Работа со складом";
    public static final String MODE_NO = "Режим по умолчанию";
    public static final String I_MODE_MANUAL = "manualMode";
    public static final String I_MODE_QR = "qrMode";

    private static AppComponent APP_COMPONENT;

    public static long START_SECONDS;
    public static boolean IS_EDITABLE_MODE;
    public static String MODE;
    public static String INPUT_MODE;
    public static void resetStartSeconds(){
        App.START_SECONDS = Calendar.getInstance().getTimeInMillis() / 1000;
    }
    private static GoogleAccountCredential CREDENTIAL;
    public static Context APP_CONTEXT;
    public static volatile boolean TOKEN_REFRESH_SERVICE_RUNNING = false;
    public static boolean BACKUP_SERVICE_RUNNING = false;

    @Override
    public void onCreate(){
        super.onCreate();
        App.APP_CONTEXT = getApplicationContext();
        App.APP_COMPONENT = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }
    public static GoogleAccountCredential getCREDENTIAL(){
        return CREDENTIAL;
    }
    public static void setCREDENTIAL(GoogleAccountCredential CREDENTIAL){
        App.CREDENTIAL = CREDENTIAL;
    }
    public static AppComponent getAppComponent(){
        return APP_COMPONENT;
    }
}
