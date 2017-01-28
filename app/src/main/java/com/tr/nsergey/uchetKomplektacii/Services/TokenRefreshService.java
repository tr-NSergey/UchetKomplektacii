package com.tr.nsergey.uchetKomplektacii.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.tr.nsergey.uchetKomplektacii.App;

import java.io.IOException;

import rx.Observable;

public class TokenRefreshService extends IntentService{
    public TokenRefreshService() {
        super("TokenRefreshService");
    }
    public void start(){
        App.APP_CONTEXT.startService(new Intent(App.APP_CONTEXT, TokenRefreshService.class));
    }
    @Override
    synchronized protected void onHandleIntent(Intent intent) {
        Observable.just(App.APP_CONTEXT)
                .subscribe(context -> {
                    App.TOKEN_REFRESH_SERVICE_RUNNING = true;
                    try {
                        Log.d("tr.n.sergey", "TokenRefreshService before waiting, thread " + Thread.currentThread().getName());
                        wait(10*60*1000);
                        new GoogleAccountManager(context)
                                .invalidateAuthToken(App.getCREDENTIAL().getToken());
                        App.TOKEN_REFRESH_SERVICE_RUNNING = false;
                        Log.d("tr.n.sergey", "TokenRefreshService after refreshing, thread " + Thread.currentThread().getName());
                    } catch (InterruptedException | IOException | GoogleAuthException e){
                        App.TOKEN_REFRESH_SERVICE_RUNNING = false;
                    }
                });
    }
}
