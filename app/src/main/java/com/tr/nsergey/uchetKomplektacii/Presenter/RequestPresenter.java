package com.tr.nsergey.uchetKomplektacii.Presenter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.MakeRequestTask;
import com.tr.nsergey.uchetKomplektacii.Model.QueueResolver;
import com.tr.nsergey.uchetKomplektacii.R;
import com.tr.nsergey.uchetKomplektacii.View.MainActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;

public class RequestPresenter {

    private static final String[] SCOPES = {"https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/spreadsheets"};

    private List<ArtObject> requestQue = new ArrayList<>();
    private String uName = "";
    private String functionName;

    private List<ArtObject> requestObjects = new ArrayList<>();
    private Map<String, String> parametersMap = new HashMap<>();
    private WeakReference<MainActivity> mainActivityWeakReference;
    @Inject
    protected Context context;
    @Inject
    QueuePresenter queuePresenter;
    @Inject
    HistoryPresenter historyPresenter;
    @Inject
    BackupPresenter backupPresenter;
    @Inject
    NbAccountsManager nbAccountsManager;

    public RequestPresenter() {
    }

    public void onCreate(MainActivity mainActivity) {
        this.mainActivityWeakReference = new WeakReference<>(mainActivity);
        App.getAppComponent().inject(this);
        // Initialize credentials and service object.
        App.setCREDENTIAL(GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff()));
        queuePresenter.subscribeForUpdates(requestQue);
    }

    public void onResume() {
        startQueueResolver();
    }

    public void onRefresh() {
        functionName = context.getString(R.string.getAccountsDataFunc);
        parametersMap.clear();
        parametersMap.put("appName", "Учет комплектации");
        parametersMap.put("deviceId", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        getResultsFromApi();
    }

    public void onLogout() {
        uName = "";
    }

    public void setUserName(String uName) {
        this.uName = uName;
    }

    public void setFunctionName(String fName) {
        this.functionName = fName;
    }

    public String getQueueMessage() {
        String message = "";
        if (requestQue.size() == 0) {
            message = context.getString(R.string.queueIsEmpty);
        } else {
            for (ArtObject artObject : requestQue) {
                message += artObject.getUserName() + ": ";
                message += artObject.getFunction() + " ";
                message += artObject.getName() + " ";
                message += artObject.getQuantity() + "\n";
            }
        }
        return message;
    }

    public void addToQue(List<ArtObject> requestObjects, boolean showMessage) {
        queuePresenter.addEntries(requestObjects);
        requestObjects.clear();
        parametersMap.clear();
        functionName = "";
        if (showMessage) {
            mainActivityWeakReference.get().onAddToQue();
        }
    }

    /**
     * After the QR code has been received, the request to check available modifications
     * and quantities is being sent, thus quantity is always "" at this point.
     */
    public void onQrReceived(String scanResult) {
        App.resetStartSeconds();
        parametersMap.clear();
        //first parameter(art or sketchname)
        if (functionName.equals(context.getString(R.string.checkSketchFunc))) {
            parametersMap.put("sketchName", scanResult);
        } else {
            int spaceIndex = scanResult.indexOf(" ");
            parametersMap.put("artNum0", spaceIndex == -1 ? scanResult : scanResult.substring(0, scanResult.indexOf(" ")));
        }
        //modification && quantity parameters must be null for the following in order to get treated as a quantity check
        if (functionName.equals("add") ||
                functionName.equals("remove") ||
                functionName.equals("replace")) {
            App.IS_EDITABLE_MODE = true;
            parametersMap.put("modification0", "");
            parametersMap.put("quantity0", "-1");
        } else {
            App.IS_EDITABLE_MODE = false;
        }
        parametersMap.put("userName0", uName);
        parametersMap.put("deviceId0", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        getResultsFromApi();
    }

    public void onSendRequest(List<ArtObject> artObjects) {
        parametersMap.clear();
        if (artObjects.size() > 0) {
            requestObjects.clear();
            requestObjects.addAll(artObjects);
            Observable.range(0, requestObjects.size())
                    .forEach(integer -> {
                        ArtObject requestObject = requestObjects.get(integer);
                        parametersMap.put("artNum" + integer.toString(), requestObject.getArt());
                        parametersMap.put("modification" + integer.toString(), requestObject.getModification());
                        parametersMap.put("quantity" + integer.toString(), requestObject.getQuantity());
                        parametersMap.put("userName" + integer.toString(), uName);
                        parametersMap.put("deviceId" + integer.toString(), Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                        requestObject.setUserName(uName);
                        requestObject.setFunction(functionName);
                        requestObject.setOldQuantity((int) (Calendar.getInstance().getTimeInMillis() / 1000));
                    });
            historyPresenter.addEntries(requestObjects);
        }
        App.IS_EDITABLE_MODE = false;
        getResultsFromApi();
    }

    public void onGoogleServicesObtained() {
        getResultsFromApi();
    }

    public void onAccountObtained() {
        getResultsFromApi();
    }

    public void onAuthOkay() {
        getResultsFromApi();
    }

    public void startQueueResolver() {
        if (!isServiceRunning("com.tr.nsergey.uchetKomplektacii.Model.QueueResolver") &&
                requestQue.size() != 0) {
            if (requestQue.size() != 0 && App.getCREDENTIAL().getSelectedAccountName() != null) {
                for (ArtObject artObject : requestQue) {
                    QueueResolver.startResolveQueue(context, artObject);
                }
            }
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isServiceRunning(String clazz) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (clazz.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            //dont need
            //showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        MainActivity mainActivity = mainActivityWeakReference.get();
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (App.getCREDENTIAL().getSelectedAccountName() == null) {
            if (mainActivity != null) {
                mainActivity.chooseAccount();
            }
        } else if (!isDeviceOnline()) {
            if (functionName.equals(context.getString(R.string.checkQuantityFunc)) ||
                    functionName.equals(context.getString(R.string.checkSketchFunc)) ||
                    functionName.equals(context.getString(R.string.getAccountsDataFunc))) {
                Toast.makeText(context, context.getString(R.string.noInternetError),
                        Toast.LENGTH_LONG).show();
            } else {
                //now we can load from the database
                if (App.IS_EDITABLE_MODE) {
                    List<ArtObject> artObjects = backupPresenter.getEntries(parametersMap.get("artNum0"));
                    for (ArtObject artObject : artObjects) {
                        artObject.set_id(null);
                        artObject.setFunction(functionName);
                    }
                    mainActivity.showRequestResult(artObjects);
                } else {
                    this.addToQue(requestObjects, true);
                    Toast.makeText(context, context.getString(R.string.redirectedToQueue),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            //Dirty fix for the first installation force-update of user list
            if (functionName.equals(context.getString(R.string.getAccountsDataFunc))) {
                parametersMap.put("accountName", App.getCREDENTIAL().getSelectedAccountName());
            }
            MakeRequestTask.createFgMakeRequestTask(functionName,
                    parametersMap, requestObjects, mainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public String getFunctionName() {
        return functionName;
    }
}
