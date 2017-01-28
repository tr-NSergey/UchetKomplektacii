package com.tr.nsergey.uchetKomplektacii.Model;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class QueueResolver extends IntentService {
    private static final String ACTION_RESOLVE_QUEUE = "com.tr.nsergey.accountingUserApp.action.RESOLVE_QUEUE";


    private static final String ART_NUM = "com.tr.nsergey.accountingUserApp.extra.ART_NUM";
    private static final String NAME = "com.tr.nsergey.accountingUserApp.extra.NAME";
    private static final String USER_NAME = "com.tr.nsergey.accountingUserApp.extra.USER_NAME";
    private static final String MODIFICATION = "com.tr.nsergey.accountingUserApp.extra.MODIFICATION";
    private static final String LOCATION = "com.tr.nsergey.accountingUserApp.extra.LOCATION";
    private static final String QUANTITY = "com.tr.nsergey.accountingUserApp.extra.QUANTITY";
    private static final String FUNCTION_NAME = "com.tr.nsergey.accountingUserApp.extra.FUNCTION_NAME";
    private static final String REQUEST_ID = "com.tr.nsergey.accontintUserApp.extra.REQUEST_ID";
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

    public static void startResolveQueue(Context context, ArtObject artObject) {
        Intent intent = new Intent(context, QueueResolver.class);
        QueueResolver.context = context;
        intent.setAction(ACTION_RESOLVE_QUEUE);
        intent.putExtra(ART_NUM, artObject.getArt());
        intent.putExtra(NAME, artObject.getName());
        intent.putExtra(USER_NAME, artObject.getUserName());
        intent.putExtra(MODIFICATION, artObject.getModification());
        intent.putExtra(LOCATION, artObject.getLocation());
        intent.putExtra(QUANTITY, artObject.getQuantity());
        intent.putExtra(FUNCTION_NAME, artObject.getFunction());
        intent.putExtra(REQUEST_ID, artObject.get_id());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RESOLVE_QUEUE.equals(action)) {
                ArtObject artObject = new ArtObject(intent.getIntExtra(REQUEST_ID, 0),
                        intent.getStringExtra(FUNCTION_NAME),
                        intent.getStringExtra(ART_NUM),
                        intent.getStringExtra(NAME),
                        intent.getStringExtra(USER_NAME),
                        intent.getStringExtra(MODIFICATION),
                        intent.getStringExtra(LOCATION),
                        Integer.valueOf(intent.getStringExtra(QUANTITY)),
                        null);
                handleResolveQueue(artObject);
            }
        }
    }

    synchronized private void handleResolveQueue(ArtObject artObject) {
        List<ArtObject> artObjects = new ArrayList<>();
        artObjects.add(artObject);
        Log.d("handleResolveQueue", "before starting");
        MakeRequestTask task = MakeRequestTask.createBgMakeRequestTask(artObject.getFunction(),
                artObject.toParametersMap(context), artObjects, artObject.get_id());
        while (!isDeviceOnline()) {
            try {
                Log.d("handleResolveQueue", "waiting device to be online");
                wait(10 * 1000);
            } catch (InterruptedException e) {
                Log.d("handleResolveQueue", "waiting device to be online INTERRUPTED");
            }
        }
        task.execute();
        try {
            wait(60 * 1000);
            Log.d("handleResolveQueue", "waiting after execution");
        } catch (InterruptedException e) {
            Log.d("handleResolveQueue", "waiting after execution INTERRUPTED");
        }
    }
}
