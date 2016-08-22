package com.tr.nsergey.accountingUserApp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
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

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, StartScreen.OnStartScreenInteractionListener,
        AfterQrScreen.OnAfterQrInteractionListener, ResultScreen.OnResultScreenInteractionListener {
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_QR_CODE = 1004;
    static final String START_SCREEN_FRAGMENT_TAG = "StartScreenFragmentTag";
    static final String AFTER_QR_SCREEN_FRAGMENT_TAG = "AfterQrScreenFragmentTag";

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/spreadsheets"};

    //to call the script
    private List<Object> parametersList = new ArrayList<>();
    private String functionName;
    private String scanResult;
    //for the button name
    private String operationName;

    private StartScreen startScreen;
    private AfterQrScreen afterQrScreen;
    private ResultScreen resultScreen;

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Запрос выполняется, подождите...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        if (savedInstanceState == null) {
            startScreen = StartScreen.newInstance("");
            startScreen.setHasOptionsMenu(true);
            showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
                showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
                return true;
            case R.id.done:
                showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
                return true;
            default:
                return true;
        }
    }

    private void showFragment(Fragment fragment, String tag) {
        if (!fragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.a_mCoordinatorLayout, fragment, tag);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

    }

    private void getQrCode() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar code
            startActivityForResult(intent, REQUEST_QR_CODE);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *                           a credential object.
     * @return an initializer with an extended read timeout.
     */
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, "Интернет-соединение не найдено! Включите Wi-Fi или Передачу Данных",
                    Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "Этому приложению нужен доступ к вашей учетной записи Google",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_QR_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    scanResult = data.getStringExtra("SCAN_RESULT");
                    parametersList.clear();
                    parametersList.add(scanResult.substring(0, scanResult.indexOf(" ")));
                    if (afterQrScreen == null) {
                        afterQrScreen = AfterQrScreen.newInstance(scanResult, operationName);
                        afterQrScreen.setHasOptionsMenu(true);
                    } else {
                        afterQrScreen.setMessage(scanResult, operationName);
                    }
                    showFragment(afterQrScreen, AFTER_QR_SCREEN_FRAGMENT_TAG);
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    startScreen.setMessage(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
                apiAvailability.isGooglePlayServicesAvailable(this);
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
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onAddToWhClick() {
        functionName = "add";
        operationName = getResources().getString(R.string.addToWh);
        getQrCode();
    }

    @Override
    public void onTakeFromWhClick() {
        functionName = "remove";
        operationName = getResources().getString(R.string.takeFromWh);
        getQrCode();
    }

    @Override
    public void onRemainsClick() {
        functionName = "replace";
        operationName = "Ввести остаток";
        getQrCode();
    }

    @Override
    public void onSendRequestClick(String quantity) {
        //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        parametersList.add(quantity);
        getResultsFromApi();
    }

    @Override
    public void onBackToMainClick() {
        showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
    }

    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
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
            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                Map<String, String> res = (Map<String, String>) (op.getResponse().get("result"));
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
            startScreen.setMessage("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
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
            showFragment(resultScreen, AFTER_QR_SCREEN_FRAGMENT_TAG);
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
                } else if (mLastError instanceof UnknownHostException){
                    Toast.makeText(getBaseContext(), "Активное WiFi подключение не имеет доступа к сети интернет!",
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