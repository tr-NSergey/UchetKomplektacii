package com.tr.nsergey.accountingUserApp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, StartScreen.OnStartScreenInteractionListener,
        AfterQrScreen.OnAfterQrInteractionListener, ResultScreen.OnResultScreenInteractionListener,
        AuthorizationScreen.OnAuthScreenInteractionListener {
    ProgressDialog mProgress;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_QR_CODE = 1004;
    static final String START_SCREEN_FRAGMENT_TAG = "StartScreenFragmentTag";
    static final String AFTER_QR_SCREEN_FRAGMENT_TAG = "AfterQrScreenFragmentTag";
    static final String RESULT_SCREEN_FRAGMENT_TAG = "ResultScreenFragmentTag";
    static final String AUTHORIZATION_SCREEN_FRAGMENT_TAG = "AuthorisationScreenFragmentTag";

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private String scanResult;
    //for the button name
    private String operationName;

    private Map<String, String> userCreds;
    private AuthorizationScreen authorizationScreen;
    private StartScreen startScreen;
    private AfterQrScreen afterQrScreen;
    private ResultScreen resultScreen;

    @Inject
    protected PersistencePresenter persistencePresenter;
    @Inject
    protected RequestPresenter requestPresenter;
    @Inject
    protected AutologoutModule autologoutModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Запрос выполняется, подождите...");
        if (savedInstanceState == null) {
            App.getAppComponent().inject(this);
            requestPresenter.onCreate(this);
            autologoutModule.onCreate(this);
            loadPreferences();
            authorizationScreen = AuthorizationScreen.newInstance();
            authorizationScreen.setHasOptionsMenu(true);
            showFragment(authorizationScreen, AUTHORIZATION_SCREEN_FRAGMENT_TAG);
        }
    }
    private void loadPreferences(){
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        userCreds = (Map<String, String>) sharedPreferences.getAll();
    }
    protected void updateUserCreds(Map<String, String> uCreds){
        userCreds.clear();
        userCreds.putAll(uCreds);
    }
    protected void writeUserCreds(List<String> output){
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String accountName = sharedPreferences.getString("accountName", "");
        editor.clear();
        editor.putString("accountName", accountName);
        for (int i = 0; i < output.size(); i = i + 2) {
            editor.putString(output.get(i), output.get(i + 1));
        }
        editor.apply();
    }
    protected void showProgress(boolean isBackground){
        if (startScreen != null) {
            startScreen.setMessage("");
        }
        if(!isBackground){
            mProgress.show();
        }
    }
    protected void hideProgress(){
        mProgress.hide();
    }
    @Override
    protected void onPause() {
        super.onPause();
        autologoutModule.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(autologoutModule.onResume()){
            backToLogin();
        }
        requestPresenter.onResume();
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
            case R.id.refresh:
                requestPresenter.onRefresh();
                return true;
            case R.id.logout:
                backToLogin();
                //startQueueResolver();
                return true;
            case R.id.checkQue:
                String message = requestPresenter.getQueueMessage();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Очередь запросов")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
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
            autologoutModule.startCountdown();
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar code
            autologoutModule.setAvoidReset();
            startActivityForResult(intent, REQUEST_QR_CODE);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
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
                    boolean hasVisuals = requestPresenter.onQrReceived(scanResult.substring(0, scanResult.indexOf(" ")));
                    if(hasVisuals){
                        if (afterQrScreen == null) {
                            afterQrScreen = AfterQrScreen.newInstance(scanResult, operationName);
                            afterQrScreen.setHasOptionsMenu(true);
                        } else {
                            afterQrScreen.setMessage(scanResult, operationName);
                        }
                        showFragment(afterQrScreen, AFTER_QR_SCREEN_FRAGMENT_TAG);
                    }
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    startScreen.setMessage(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    requestPresenter.onGoogleServicesObtained();
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
                        requestPresenter.onAccountObtained(accountName);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    requestPresenter.onAuthOkay();
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
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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
    protected void chooseAccount() {
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
    @Override
    public void onAddToWhClick() {
        requestPresenter.setFunctionName("add");
        operationName = getResources().getString(R.string.addToWh);
        getQrCode();
    }

    @Override
    public void onTakeFromWhClick() {
        requestPresenter.setFunctionName("remove");
        operationName = getResources().getString(R.string.takeFromWh);
        getQrCode();
    }

    @Override
    public void onRemainsClick() {
        requestPresenter.setFunctionName("replace");
        operationName = "Ввести остаток";
        getQrCode();
    }

    @Override
    public void onCheckClick() {
        requestPresenter.setFunctionName("checkQuantity");
        operationName = "Проверить количество";
        getQrCode();
    }

    @Override
    public void onSendRequestClick(String quantity) {
        requestPresenter.onSendRequest(quantity);
    }

    @Override
    public void onBackToMainClick() {
        showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
    }

    @Override
    public String checkUser(String userPwd) {
        String name = "";
        for (String userName : userCreds.keySet()) {
            if (userCreds.get(userName).equals(userPwd)) {
                name = userName;
            }
        }
        requestPresenter.setUserName(name);
        return name;
    }

    protected void addToQue(){
        requestPresenter.addToQue(operationName);
        showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
    }

    @Override
    public void onAuthSuccess(String userName) {
        startScreen = StartScreen.newInstance("", userName);
        startScreen.setHasOptionsMenu(true);
        showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
        autologoutModule.startCountdown();
    }

    protected void backToLogin() {
        autologoutModule.logout();
        requestPresenter.onLogout();
        showFragment(authorizationScreen, AUTHORIZATION_SCREEN_FRAGMENT_TAG);
    }
}