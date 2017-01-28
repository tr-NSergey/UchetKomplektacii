package com.tr.nsergey.uchetKomplektacii.Presenter;

import android.os.AsyncTask;

import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.View.MainActivity;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class AutologoutPresenter {
    private static final int AUTOLOGOUT_INTERVAL = 5*60;
    private CountdownTask countdownTask;
    private WeakReference<MainActivity> mainActivityWeakReference;

    public AutologoutPresenter() {
    }

    public void onPause() {
        if (countdownTask != null) {
            countdownTask.cancel(true);
        }
    }

    public void onCreate(MainActivity mainActivity) {
        this.mainActivityWeakReference = new WeakReference<>(mainActivity);
    }

    public void logout() {
        if (countdownTask != null) {
            countdownTask.cancel(true);
        }
    }

    public void startCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel(true);
        }
        countdownTask = new CountdownTask();
        countdownTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CountdownTask extends AsyncTask<Void, Void, Void> {
        protected CountdownTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            (new CountdownRunnable()).run();
            return null;
        }
    }

    private class CountdownRunnable implements Runnable {
        protected CountdownRunnable() {

        }

        @Override
        public void run() {
            try {
                if(App.START_SECONDS == 0){
                    App.resetStartSeconds();
                }
                long secToSleep = AUTOLOGOUT_INTERVAL - (Calendar.getInstance().getTimeInMillis() / 1000 - App.START_SECONDS);
                if (secToSleep > 0) {
                    Thread.sleep(secToSleep * 1000);
                }
                App.START_SECONDS = 0;
                if(mainActivityWeakReference.get() != null) {
                    mainActivityWeakReference.get().backToLogin();
                }
            } catch (InterruptedException e) {
            }
        }
    }
}