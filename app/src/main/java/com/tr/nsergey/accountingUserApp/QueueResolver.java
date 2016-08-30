package com.tr.nsergey.accountingUserApp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class QueueResolver extends IntentService {
    private static final String ACTION_RESOLVE_QUEUE = "com.tr.nsergey.accountingUserApp.action.RESOLVE_QUEUE";

    private static final String PARAMS = "com.tr.nsergey.accountingUserApp.extra.PARAMS";
    private static final String OPERATION_NAME = "com.tr.nsergey.accountingUserApp.extra.OPERATION_NAME";
    public static final String REQUEST_ID = "com.tr.nsergey.accontintUserApp.extra.REQUEST_ID";
    private static Context context;

    public QueueResolver() {
        super("QueueResolver");
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void startResolveQueue(Context context, List<Object> params, String operationName, Integer requestId) {
        Intent intent = new Intent(context, QueueResolver.class);
        QueueResolver.context = context;
        intent.setAction(ACTION_RESOLVE_QUEUE);
        String[] paramsString = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            paramsString[i] = (String) params.get(i);
        }
        intent.putExtra(PARAMS, paramsString);
        intent.putExtra(OPERATION_NAME, operationName);
        intent.putExtra(REQUEST_ID, requestId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RESOLVE_QUEUE.equals(action)) {
                final String[] params = intent.getStringArrayExtra(PARAMS);
                final String operationName = intent.getStringExtra(OPERATION_NAME);
                final Integer requestId = intent.getIntExtra(REQUEST_ID, 0);
                handleResolveQueue(params, operationName, requestId);
            }
        }
    }

    synchronized private void handleResolveQueue(String[] params, String operationName, Integer requestId) {
        RequestTask task = new RequestTask(operationName, Arrays.asList(params), requestId, true);
        Log.d(Thread.currentThread().getName(), "entered thread");
        while (!isDeviceOnline()) {
            try {
                Log.d(Thread.currentThread().getName(), "started waiting");
                wait(10 * 1000);
                Log.d(Thread.currentThread().getName(), "finished waiting");
            } catch (InterruptedException e) {
                Log.d(Thread.currentThread().getName(), "got interrupted");
            }
        }
        task.execute();
        try {
            wait(10 * 1000);
        } catch (InterruptedException e){

        }
    }
}
