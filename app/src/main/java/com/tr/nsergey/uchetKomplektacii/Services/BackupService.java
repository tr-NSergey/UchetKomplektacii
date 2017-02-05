package com.tr.nsergey.uchetKomplektacii.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.Model.AppsScriptRequest;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by sergey on 23.01.17.
 */

public class BackupService extends Service {

    private String functionName = App.APP_CONTEXT.getString(R.string.refreshDatabase);
    private com.google.api.services.script.Script mService = null;
    private List<ArtObject> requestObjects;
    private Map<String, String> parametersMap;
    private AppsScriptRequest appsScriptRequest;
    SharedPreferences sharedPref;

    public BackupService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("tr.n.sergey", "BackupService onCreate, thread " + Thread.currentThread().getName());
        requestObjects = new ArrayList<>();
        parametersMap = new HashMap<>();
        parametersMap.put("deviceId", Settings.Secure.getString(App.APP_CONTEXT.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        appsScriptRequest = new AppsScriptRequest(functionName, parametersMap, requestObjects);
        sharedPref = App.APP_CONTEXT.getSharedPreferences(App.APP_CONTEXT
                .getString(R.string.databaseUpdateTimestamp), Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("tr.n.sergey", "BackupService onStartCommand, thread " + Thread.currentThread().getName());
        // TODO: 28.01.17 change to a decent interval, 1 minute is just for testing
        Observable.interval(0, 2, TimeUnit.HOURS, Schedulers.io())
                .forEach((l) -> {
                    Map<String, String> prefMap;
                    try {
                        prefMap = (Map<String, String>) sharedPref.getAll();
                    } catch (Exception e){
                        prefMap = new HashMap<>();
                    }
                    for(Map.Entry<String, String> pref: prefMap.entrySet()){
                        parametersMap.put(pref.getKey(), pref.getValue());
                    }
                    appsScriptRequest.setParametersMap(parametersMap);
                    Log.d("tr.n.sergey", "BackupService before runRequest, thread " + Thread.currentThread().getName());
                    appsScriptRequest.runRequest();
                });
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("tr.n.sergey", "BackupService before onDestroy, thread " + Thread.currentThread().getName());
        super.onDestroy();
        Log.d("tr.n.sergey", "BackupService onDestroy, thread " + Thread.currentThread().getName());
        App.BACKUP_SERVICE_RUNNING = false;
    }
}
