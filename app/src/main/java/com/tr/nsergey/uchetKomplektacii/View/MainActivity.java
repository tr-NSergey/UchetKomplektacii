package com.tr.nsergey.uchetKomplektacii.View;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.FiveFlingListener;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.OpNames;
import com.tr.nsergey.uchetKomplektacii.Presenter.AutologoutPresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.NbAccountsManager;
import com.tr.nsergey.uchetKomplektacii.Presenter.QueuePresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.RequestPresenter;
import com.tr.nsergey.uchetKomplektacii.R;
import com.tr.nsergey.uchetKomplektacii.View.SketchChecking.SketchCheckingScreen;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observer;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, Observer<Integer>,
        AfterQrScreen.OnAfterQrInteractionListener, ResultScreen.OnResultScreenInteractionListener,
        AuthorizationScreen.OnAuthScreenInteractionListener, FiveFlingListener.FiveFlingObserver {

    ProgressDialog mProgress;
    /**
     * Special impersonal account to check the sketch version.
     * Once a {@link AuthorizationScreen.OnAuthScreenInteractionListener} receives it,
     * app takes a different branch of screens(SketchChecking)
     */
    private static final String SKETCH_CHECKER = "Проверка чертежа";

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_QR_CODE = 1004;
    static final String START_SCREEN_FRAGMENT_TAG = "StartScreenFragmentTag";
    static final String AFTER_QR_SCREEN_FRAGMENT_TAG = "AfterQrScreenFragmentTag";
    static final String RESULT_SCREEN_FRAGMENT_TAG = "ResultScreenFragmentTag";
    static final String AUTHORIZATION_SCREEN_FRAGMENT_TAG = "AuthorisationScreenFragmentTag";
    static final String HISTORY_SCREEN_FRAGMENT_TAG = "HistoryScreenFragmentTag";
    static final String SKETCH_CHECKING_SCREEN_FRAGMENT_TAG = "SketchCheckingScreenFragmentTag";

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private String scanResult;

    private GestureDetectorCompat gestureDetector;

    private AuthorizationScreen authorizationScreen;
    private StartScreen startScreen;
    private AfterQrScreen afterQrScreen;
    private ResultScreen resultScreen;
    private HistoryScreen historyScreen;
    /**
     * Second branch screen
     */
    private SketchCheckingScreen sketchCheckingScreen;

    @Inject
    protected QueuePresenter queuePresenter;
    @Inject
    protected RequestPresenter requestPresenter;
    @Inject
    protected AutologoutPresenter autologoutPresenter;
    @Inject
    protected NbAccountsManager nbAccountsManager;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /**
             * if past registration and NOT at the start screen - go to start screen
             * if past registration and at the start screen - go to login page
             * standard event handling otherwise
             */
            if ((startScreen != null && startScreen.isVisible()) ||
                    (sketchCheckingScreen != null && sketchCheckingScreen.isVisible())) {
                backToLogin();
                return true;
            } else if (authorizationScreen != null && !authorizationScreen.isVisible()) {
                toMainScreen();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FiveFlingListener fiveFlingListener = new FiveFlingListener();
        fiveFlingListener.subscribe(this);
        gestureDetector = new GestureDetectorCompat(this, fiveFlingListener);
        setContentView(R.layout.activity_main);
        mProgress = new ProgressDialog(this);
        App.START_SECONDS = Calendar.getInstance().getTimeInMillis() / 1000;
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.requestInProcess));
        if (savedInstanceState == null) {
            App.getAppComponent().inject(this);
            requestPresenter.onCreate(this);
            fetchPreferredAccount();
            autologoutPresenter.onCreate(this);
            if (!nbAccountsManager.loadPreferences()) {
                refreshUserData();
            }
            authorizationScreen = AuthorizationScreen.newInstance();
            authorizationScreen.setHasOptionsMenu(true);
            showFragment(authorizationScreen, AUTHORIZATION_SCREEN_FRAGMENT_TAG);
        }
    }

    public void showProgress(boolean isBackground) {
        if (!isBackground) {
            mProgress.show();
        }
    }

    public void hideProgress() {
        mProgress.hide();
    }

    public void onRequestCancelled() {
        Toast.makeText(this, getString(R.string.requestCancelled), Toast.LENGTH_LONG).show();
    }
    /**
     * @param artObjects - list of ArtObjects received from a request.
     * */
    public void showRequestResult(List<ArtObject> artObjects){
        for (ArtObject artObject:artObjects){
            artObject.setFunction(requestPresenter.getFunctionName());
        }
        if(App.IS_EDITABLE_MODE){
            if(afterQrScreen == null){
                afterQrScreen = AfterQrScreen.newInstance();
                afterQrScreen.setHasOptionsMenu(true);
            }
            afterQrScreen.setPossibleArts(artObjects);
            showFragment(afterQrScreen, AFTER_QR_SCREEN_FRAGMENT_TAG);
        } else {
            if(resultScreen == null){
                resultScreen = ResultScreen.newInstance();
                resultScreen.setHasOptionsMenu(true);
            }
            resultScreen.setResultArts(artObjects);
            hideProgress();
            showFragment(resultScreen, RESULT_SCREEN_FRAGMENT_TAG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autologoutPresenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autologoutPresenter == null) {
            init(null);
        }
        autologoutPresenter.startCountdown();
        requestPresenter.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
            case R.id.done:
                toMainScreen();
                return true;
            case R.id.refresh:
                refreshUserData();
                return true;
            case R.id.logout:
                backToLogin();
                return true;
            case R.id.checkQue:
                String message = requestPresenter.getQueueMessage();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.requestQueue)
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

    public void refreshUserData() {
        requestPresenter.onRefresh();
    }

    private void showFragment(Fragment fragment, String tag) {
        if (!fragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.a_mCoordinatorLayout, fragment, tag);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commitAllowingStateLoss();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

    }

    private void getQrCode() {
        try {
            autologoutPresenter.startCountdown();
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar code
            startActivityForResult(intent, REQUEST_QR_CODE);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("Для корректной работы приложения необходимо установить Barcode Scanner (это бесплатно).")
                    .setPositiveButton("Скачать", ((dialog, which) -> {
                        Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        startActivity(marketIntent);
                    }))
                    .setNegativeButton("Отмена", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .show();
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
                    processScanResult(scanResult);
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
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
                        savePreferredAccount(accountName);
                        requestPresenter.onAccountObtained();
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

    private void processScanResult(String scanResult) {
        requestPresenter.onQrReceived(scanResult);
        //boolean hasVisuals = requestPresenter.onQrReceived(scanResult);
        /*
        if (hasVisuals) {
            if (afterQrScreen == null) {
                afterQrScreen = AfterQrScreen.newInstance(scanResult, operationName);
                afterQrScreen.setHasOptionsMenu(true);
            } else {
                afterQrScreen.setButtonName(scanResult, operationName);
            }
            showFragment(afterQrScreen, AFTER_QR_SCREEN_FRAGMENT_TAG);
        }
        */
    }

    private void savePreferredAccount(String accountName) {
        SharedPreferences settings =
                getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.apply();
        App.getCREDENTIAL().setSelectedAccountName(accountName);
    }

    public void removePreferredAccount() {
        SharedPreferences settings =
                getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
        App.getCREDENTIAL().setSelectedAccountName(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgress.dismiss();
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
    public void showGooglePlayServicesAvailabilityErrorDialog(
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
    public void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            startActivityForResult(
                    App.getCREDENTIAL().newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.needsGoogleAccount),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onSendRequestClick(List<ArtObject> artObjects) {
        requestPresenter.onSendRequest(artObjects);
    }

    @Override
    public void onToMainScreenClick() {
        toMainScreen();
    }

    private void toMainScreen() {
        switch (App.MODE) {
            case App.MODE_SKETCH:
                showFragment(sketchCheckingScreen, SKETCH_CHECKING_SCREEN_FRAGMENT_TAG);
                break;
            case App.MODE_ACCOUNTING:
                showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
                break;
        }
    }

    @Override
    public String checkUser(String userPwd) {
        String name = nbAccountsManager.getUserForPwd(userPwd);
        requestPresenter.setUserName(name);
        return name;
    }

    public void onAddToQue() {
        toMainScreen();
    }

    @Override
    public void onAuthSuccess(String userName) {
        App.INPUT_MODE = App.I_MODE_QR;
        if (userName.equals(SKETCH_CHECKER)) {
            App.MODE = App.MODE_SKETCH;
            sketchCheckingScreen = SketchCheckingScreen.newInstance(userName);
            sketchCheckingScreen.setHasOptionsMenu(true);
            showFragment(sketchCheckingScreen, SKETCH_CHECKING_SCREEN_FRAGMENT_TAG);
        } else {
            App.MODE = App.MODE_ACCOUNTING;
            startScreen = StartScreen.newInstance(userName);
            startScreen.setHasOptionsMenu(true);
            showFragment(startScreen, START_SCREEN_FRAGMENT_TAG);
        }
        App.resetStartSeconds();
        autologoutPresenter.startCountdown();
    }

    public void backToLogin() {
        App.MODE = App.MODE_NO;
        autologoutPresenter.logout();
        requestPresenter.onLogout();
        showFragment(authorizationScreen, AUTHORIZATION_SCREEN_FRAGMENT_TAG);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    /**
     * Listening to the buttons being pressed
     *
     * @Param buttonName name of the button pressed
     */
    @Override
    public void onNext(Integer buttonId) {
        switch (buttonId) {
            case R.id.addQtyButton:
                requestPresenter.setFunctionName("add");
                break;
            case R.id.removeQtyButton:
                requestPresenter.setFunctionName("remove");
                break;
            case R.id.remainsButton:
                requestPresenter.setFunctionName("replace");
                break;
            case R.id.checkButton:
                requestPresenter.setFunctionName("checkQuantity");
                break;
            case R.id.checkSketch:
                requestPresenter.setFunctionName("checkSketchVersion");
                break;
            default:
                throw new IllegalArgumentException("Unknown button has been pressed! ID:" + buttonId);
        }
        switch (App.INPUT_MODE) {
            case App.I_MODE_QR:
                getQrCode();
                break;
            case App.I_MODE_MANUAL:
                getArtInput();
                break;
        }
    }

    // TODO: 16.11.16 Create a map with function name to operation name
    private void getArtInput() {
        new AlertDialog.Builder(this)
                .setTitle("Введите артикул:")
                .setView(getLayoutInflater().inflate(R.layout.dialog_art_input, null))
                .setPositiveButton("Продолжить", (dialog, which) -> {
                    EditText artInput = (EditText) ((AlertDialog) dialog).findViewById(R.id.artInput);
                    artInput.setActivated(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInputFromWindow(artInput.getWindowToken(), 0,0);
                    scanResult = artInput.getText().toString();
                    processScanResult(scanResult);
                })
                .setNegativeButton("Отмена", ((dialog, which) -> {
                    EditText artInput = (EditText) ((AlertDialog) dialog).findViewById(R.id.artInput);
                    artInput.setActivated(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInputFromWindow(artInput.getWindowToken(), 0,0);
                    dialog.dismiss();
                }))
                .show()
                .findViewById(R.id.artInput)
                .setActivated(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private boolean fetchPreferredAccount() {
        String accountName = getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, "");
        if (!accountName.equals("")) {
            App.getCREDENTIAL().setSelectedAccountName(accountName);
            return true;
        }
        return false;
    }

    @Override
    public void onFiveFlings() {
        if (historyScreen == null) {
            historyScreen = new HistoryScreen();
            historyScreen.setHasOptionsMenu(true);
        }
        if (!historyScreen.isVisible()) {
            showFragment(historyScreen, HISTORY_SCREEN_FRAGMENT_TAG);
        }
    }
}