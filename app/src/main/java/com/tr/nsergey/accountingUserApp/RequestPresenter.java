package com.tr.nsergey.accountingUserApp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by sergey on 30.08.16.
 */
public class RequestPresenter {

    private static final String[] SCOPES = {"https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/spreadsheets"};
    public static GoogleAccountCredential CREDENTIAL;

    private List<RequestElem> requestQue = new ArrayList<>();
    private String uName = "";
    private String functionName;
    //to call the script
    private List<Object> parametersList = new ArrayList<>();
    private MainActivity mainActivity;
    @Inject
    protected Context context;
    @Inject
    protected PersistencePresenter persistencePresenter;

    public RequestPresenter() {
    }

    void onCreate(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        App.getAppComponent().inject(this);
        // Initialize credentials and service object.
        CREDENTIAL = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        persistencePresenter.subscribeForUpdates(requestQue);
    }
    void onResume(){
        if(!isServiceRunning("com.tr.nsergey.accountingUserApp.QueueResolver") &&
                requestQue.size() != 0){
            startQueueResolver();
        }
    }
    void onRefresh(){
        functionName = "getAccountsData";
        parametersList.clear();
        parametersList.add("Учет комплектации");
        parametersList.add(Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        this.onSendRequest("");
    }
    void onLogout(){
        uName = "";
    }
    void setUserName(String uName){
        this.uName = uName;
    }
    void setFunctionName(String fName){
        this.functionName = fName;
    }
    String getQueueMessage(){
        String message = "";
        if (requestQue.size() == 0) {
            message = "Очередь пуста";
        } else {
            for (RequestElem rElem : requestQue) {
                message += rElem.operationName + " ";
                message += rElem.params.get(rElem.params.size() - 2) + " ";
                message += rElem.userName + "\n";
            }
        }
        return message;
    }
    void addToQue(String operationName){
        persistencePresenter.addEntry(parametersList, uName, operationName, functionName);
        parametersList.clear();
        functionName = "";
    }
    void onSendRequest(String quantity){
        parametersList.add(quantity);
        parametersList.add(uName);
        getResultsFromApi();
    }
    //@returns boolean - true if display result, false if skip
    boolean onQrReceived(String newParameters){
        parametersList.clear();
        parametersList.add(newParameters);
        //skipping the afterQrScreen for the check
        if (functionName.equals("checkQuantity")) {
            onSendRequest("");
            return false;
        } else {
            return true;
        }
    }
    void onGoogleServicesObtained(){
        getResultsFromApi();
    }
    void onAccountObtained(String accountName){
        CREDENTIAL.setSelectedAccountName(accountName);
        getResultsFromApi();
    }
    void onAuthOkay(){
        getResultsFromApi();
    }
    private void startQueueResolver() {
        if (requestQue.size() != 0) {
            for (RequestElem requestElem : requestQue) {
                QueueResolver.startResolveQueue(context, requestElem.params,
                        requestElem.functionName, requestElem._id);
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
     *     /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (CREDENTIAL.getSelectedAccountName() == null) {
            mainActivity.chooseAccount();
        } else if (!isDeviceOnline()) {
            if (functionName.equals("checkQuantity")) {
                Toast.makeText(context, "Интернет-соединение не доступно! \n" +
                                "Попробуйте включить Wi-Fi или Передачу Данных",
                        Toast.LENGTH_LONG).show();
            } else {
                mainActivity.addToQue();
                Toast.makeText(context, "Интернет-соединение не доступно! \n" +
                                "Запрос передан в очередь запросов \n",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            new MakeRequestTask(CREDENTIAL, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    /*
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *                           a credential object.
     * @return an initializer with an extended read timeout.
     */
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            // This allows the API to call (and avoid timing out on)
            // functions that take up to 6 minutes to complete (the maximum
            // allowed script run time), plus a little overhead.
            httpRequest.setReadTimeout(380000);
        };
    }
    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;
        private boolean isBackgroundRequest = false;

        public MakeRequestTask(GoogleAccountCredential credential, boolean isBackgroundRequest) {
            this.isBackgroundRequest = isBackgroundRequest;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, setHttpTimeout(credential))
                    .setApplicationName("Accounting User App")
                    .build();
        }

        /**
         * Background task to call Google Apps Script Execution API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Call the API to run an Apps Script function that returns a list
         * of folders within the user's root directory on Drive.
         *
         * @return list of String folder names and their IDs
         * @throws IOException
         */
        private List<String> getDataFromApi()
                throws IOException, GoogleAuthException {
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
            String scriptId = "MVVZiSOOUWILAZ4OVDSfZmqlftPelU-yI";

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest()
                    .setFunction(functionName);
            request.setParameters(parametersList);
            request.setDevMode(true);
            // Make the request.
            Operation op =
                    mService.scripts().run(scriptId, request).execute();
            List<String> artInfo = new ArrayList<>();
            if (isBackgroundRequest) {
                return artInfo;
            }
            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                Map<String, String> res = (Map<String, String>) (op.getResponse().get("result"));
                if (functionName.equals("getAccountsData")) {
                    mainActivity.updateUserCreds(res);
                    for (String key : res.keySet()) {
                        artInfo.add(key);
                        artInfo.add(res.get(key));
                    }
                } else {
                    for (String key : res.keySet()) {
                        String val;
                        switch (key) {
                            case "result":
                                val = "Результат";
                                break;
                            case "oldQty":
                                val = "Было";
                                break;
                            case "newQty":
                                val = "Стало";
                                break;
                            default:
                                val = "";
                                break;
                        }
                        artInfo.add(String.format("<b>%s</b>: %s", val, res.get(key)));
                    }
                }
            }
            return artInfo;
        }

        /**
         * Interpret an error response returned by the API and return a String
         * summary.
         *
         * @param op the Operation returning an error response
         * @return summary of error response, or null if Operation returned no
         * error
         */
        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            return sb.toString();
        }


        @Override
        protected void onPreExecute() {
            if (!isBackgroundRequest) {
                mainActivity.showProgress(isBackgroundRequest);
            }
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (output.get(0).equals("noRequest")) {
                return;
            }
            mainActivity.hideProgress();
            if (functionName.equals("getAccountsData")) {
                mainActivity.writeUserCreds(output);
            } else {
                String message;
                if (output == null || output.size() == 0) {
                    message = "Получено результатов: 0";
                } else {
                    message = TextUtils.join("<br>", output);
                }
                if (resultScreen == null) {
                    resultScreen = ResultScreen.newInstance(scanResult + "<br>" + "<br>" + message);
                    resultScreen.setHasOptionsMenu(true);
                } else {
                    resultScreen.setMessage(scanResult + "<br>" + "<br>" + message);
                }
                showFragment(resultScreen, RESULT_SCREEN_FRAGMENT_TAG);
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else if (mLastError instanceof UnknownHostException) {
                    addToQue();
                    Toast.makeText(getBaseContext(), "Активное WiFi подключение не имеет доступа к сети интернет! \n" +
                                    "Запрос передан в очередь запросов",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                startScreen.setMessage("Запрос отменен.");
            }
        }
    }
}
