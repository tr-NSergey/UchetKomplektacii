package com.tr.nsergey.accountingUserApp;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by sergey on 30.08.16.
 */
public class AutologoutModule {
    private CountdownTask countdownTask;
    private boolean avoidReset = true;
    private MainActivity mainActivity;

    public AutologoutModule() {
    }

    void onPause() {
        if (!avoidReset && countdownTask != null) {
            countdownTask.cancel(true);
        }
    }
    void onCreate(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    boolean onResume() {
        if (!avoidReset) {
            return true;
        }
        avoidReset = false;
        return false;
    }

    void logout(){
        if (countdownTask != null) {
            countdownTask.cancel(true);
        }
    }
    void setAvoidReset(){
        avoidReset = true;
    }
    void dropAvoidReset(){
        avoidReset = false;
    }

    protected void startCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel(true);
        }
        countdownTask = new CountdownTask();
        countdownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CountdownTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            (new CountdownRunnable()).run();
            return null;
        }
    }

    private class CountdownRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("CountdownRunnable", Thread.currentThread().getName() + " started!");
                Thread.sleep(60 * 1000);
                Log.d("CountdownRunnable", Thread.currentThread().getName() + " completed!");
                avoidReset = false;
                mainActivity.backToLogin();
            } catch (InterruptedException e) {
                Log.d("CountdownRunnable", Thread.currentThread().getName() + " been interrupted");
            }
        }
    }
}
