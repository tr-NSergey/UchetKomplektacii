package com.tr.nsergey.uchetKomplektacii.Model;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;
import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.Presenter.QueuePresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.NbAccountsManager;
import com.tr.nsergey.uchetKomplektacii.R;
import com.tr.nsergey.uchetKomplektacii.Services.BackupService;
import com.tr.nsergey.uchetKomplektacii.Services.TokenRefreshService;
import com.tr.nsergey.uchetKomplektacii.View.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * An asynchronous task that handles the Google Apps Script Execution API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class MakeRequestTask extends AsyncTask<Void, Void, JSONObject> {
    private List<ArtObject> requestObjects;
    private com.google.api.services.script.Script mService = null;
    private Exception mLastError = null;
    private boolean isBackgroundRequest = false;
    private String functionName;
    private Integer requestId;
    private Map<String, String> parametersMap;
    //only needed in non-bg requests
    private WeakReference<MainActivity> mainActivityWeakReference;
    @Inject
    NbAccountsManager nbAccountsManager;
    @Inject
    protected QueuePresenter queuePresenter;

    static MakeRequestTask createBgMakeRequestTask(String functionName, Map<String, String> parametersMap,
                                                   List<ArtObject> requestObjects, Integer requestId) {
        MakeRequestTask makeRequestTask = new MakeRequestTask(functionName, requestObjects, parametersMap);
        makeRequestTask.isBackgroundRequest = true;
        makeRequestTask.requestId = requestId;
        return makeRequestTask;
    }

    public static MakeRequestTask createFgMakeRequestTask(String functionName, Map<String, String> parametersMap,
                                                          List<ArtObject> requestObjects, MainActivity mainActivity) {
        MakeRequestTask makeRequestTask = new MakeRequestTask(functionName, requestObjects, parametersMap);
        makeRequestTask.isBackgroundRequest = false;
        makeRequestTask.mainActivityWeakReference = new WeakReference<>(mainActivity);
        return makeRequestTask;
    }

    private MakeRequestTask(String functionName, List<ArtObject> requestObjects, Map<String, String> parametersMap) {
        this.functionName = functionName;
        this.requestObjects = new ArrayList<>();
        this.requestObjects.addAll(requestObjects);
        this.parametersMap = new HashMap<>();
        this.parametersMap.putAll(parametersMap);
        App.getAppComponent().inject(this);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.script.Script.Builder(
                transport, jsonFactory, setHttpTimeout(App.getCREDENTIAL()))
                .setApplicationName("Учет комплектации")
                .build();
    }

    /**
     * Background task to call Google Apps Script Execution API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected JSONObject doInBackground(Void... params) {
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
    private JSONObject getDataFromApi()
            throws IOException, GoogleAuthException, JSONException {
        // ID of the script to call. Acquire this from the Apps Script editor,
        // under Publish > Deploy as API executable.
        //String scriptId = "MVVZiSOOUWILAZ4OVDSfZmqlftPelU-yI";
        String scriptId = "MqNqphcRHgmupV7ODRojuJ6lftPelU-yI";

        // Create an execution request object.
        ExecutionRequest request = new ExecutionRequest()
                .setFunction(functionName);
        List<Object> list = new ArrayList<>();
        list.add(new JSONObject(parametersMap).toString());
        request.setParameters(list);
        // TODO: 02.11.16 change to false on release
        request.setDevMode(true);
        if (!App.TOKEN_REFRESH_SERVICE_RUNNING) {
            new TokenRefreshService().start();
        }
        if (!App.BACKUP_SERVICE_RUNNING){
            App.APP_CONTEXT.startService(new Intent(App.APP_CONTEXT, BackupService.class));
        }
        // Make the request.
        Operation op =
                mService.scripts().run(scriptId, request).execute();
        if (op.getError() != null) {
            throw new IOException(getScriptError(op));
        }
        if (op.getResponse() != null &&
                op.getResponse().get("result") != null) {
            return new JSONObject((String) op.getResponse().get("result"));
        }
        throw new IOException("Incorrect or null response from the target script");
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
            if (mainActivityWeakReference.get() != null) {
                mainActivityWeakReference.get().showProgress(isBackgroundRequest);
            }
        }
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        if (isBackgroundRequest) {
            queuePresenter.deleteEntry(requestId);
            return;
        }
        MainActivity mainActivity = mainActivityWeakReference.get();
        switch (functionName) {
            case "getAccountsData":
                Map<String, String> newCreds = new HashMap<>();
                try {
                    JSONArray jsonArray = response.getJSONArray("accounts");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        newCreds.put(jsonObject.getString("name"), jsonObject.getString("pass"));
                    }
                } catch (JSONException e) {

                }
                nbAccountsManager.updateUserCreds(newCreds);
                nbAccountsManager.writeUserCreds(newCreds);
                if(mainActivity != null) {
                    mainActivity.hideProgress();
                }
                break;
            default:
                try {
                    String resultCode = response.getString("code");
                    switch (resultCode) {
                        case "arts":
                            List<ArtObject> artObjects = ArtObject.fromJsonArray((JSONArray) response.get(resultCode));
                            if(mainActivity != null) {
                                mainActivity.hideProgress();
                                mainActivity.showRequestResult(artObjects);
                            }
                            break;
                        case "sketchVersion":
                            if(mainActivity != null){
                                mainActivity.showRequestResult(response.getString(resultCode));
                            }
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onCancelled() {
        if (isBackgroundRequest) {
            return;
        }
        MainActivity mainActivity = mainActivityWeakReference.get();
        if (mainActivity == null) {
            return;
        }
        mainActivity.hideProgress();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                mainActivity.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                mainActivity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            } else if (mLastError instanceof UnknownHostException) {
                queuePresenter.addEntries(requestObjects);
                Toast.makeText(mainActivity, "Активное WiFi подключение не имеет доступа к сети интернет! \n" +
                                "Запрос передан в очередь запросов",
                        Toast.LENGTH_LONG).show();
            } else {
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                String errorMessage = "";
                try {
                    JsonParser jsonParser = jsonFactory.createJsonParser(mLastError.getMessage());
                    jsonParser.skipToKey("message");
                    errorMessage = jsonParser.getText();
                    if (errorMessage.equals("404")) {
                        Snackbar.make(mainActivity.findViewById(R.id.a_mCoordinatorLayout),
                                "Аккаунт не подходит!", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Выбрать", v -> {
                                    mainActivity.removePreferredAccount();
                                    mainActivity.refreshUserData();
                                })
                                .setActionTextColor(Color.CYAN)
                                .show();
//                        Toast.makeText(mainActivity, "У выбранного аккаунта нет прав доступа к скрипту!",
//                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                } catch (IOException e) {
                    Toast.makeText(mainActivity, "У выбранного аккаунта нет прав доступа к скрипту!" + errorMessage,
                            Toast.LENGTH_SHORT).show();
//                    Toast.makeText(mainActivity, "The following error occurred:\n"
//                            + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            mainActivity.onRequestCancelled();
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
            // shortening this to 40 sec
            httpRequest.setReadTimeout(40000);
        };
    }
}