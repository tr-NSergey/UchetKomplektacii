package com.tr.nsergey.accountingUserApp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class RequestTask extends AsyncTask<Void, Void, Void> {
    public static GoogleAccountCredential credential;
    private String functionName;
    private List<Object> parametersList;
    private Integer requestId;
    private com.google.api.services.script.Script mService = null;
    private Exception mLastError = null;
    private boolean isBackgroundRequest = false;

    @Inject
    protected PersistencePresenter persistencePresenter;
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            httpRequest.setReadTimeout(380000);
        };
    }

    public RequestTask(String functionName, List<Object> parametersList, Integer requestId, boolean isBackgroundRequest) {
        this.isBackgroundRequest = isBackgroundRequest;
        this.functionName = functionName;
        this.requestId = requestId;
        this.parametersList = new ArrayList<>();
        App.getAppComponent().inject(this);
        this.parametersList.addAll(parametersList);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        if(credential.getSelectedAccountName() == null) {
            credential.setSelectedAccountName("snik@nbdiam.com");
            Log.d(Thread.currentThread().getName(), "used default accountName");
        }
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
    protected Void doInBackground(Void... params) {
        try {
            getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
        return null;
    }

    /**
     * Call the API to run an Apps Script function that returns a list
     * of folders within the user's root directory on Drive.
     *
     * @throws IOException
     */
    private void getDataFromApi()
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
        Log.d(Thread.currentThread().getName(), "sending Request");
        Operation op =
                mService.scripts().run(scriptId, request).execute();
        Log.d(Thread.currentThread().getName(), "receiving Request");
        Log.d(Thread.currentThread().getName(), op.toPrettyString());
        if (op.getError() != null) {
            throw new IOException(getScriptError(op));
        } else {
            persistencePresenter.deleteEntry(requestId);
        }
    }

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
    protected void onPostExecute(Void aVoid) {
    }
}
