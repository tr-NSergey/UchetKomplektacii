package com.tr.nsergey.uchetKomplektacii.Presenter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;

public class NbAccountsManager {
    public static final String USER_CREDS_PREFERENCE = "UserCredentialsPreference";
    private Context context;
    private Map<String, String> userCreds;

    public NbAccountsManager(Context context) {
        this.context = context;
    }
    public String getUserForPwd(String userPwd){
        String name = "";
        for(Map.Entry<String, String> entry: userCreds.entrySet()){
            if(entry.getValue().equals(userPwd)){
                name = entry.getKey();
            }
        }
        return name;
    }
    public boolean loadPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_CREDS_PREFERENCE, Context.MODE_PRIVATE);
        userCreds = (Map<String, String>) sharedPreferences.getAll();
        return !userCreds.isEmpty();
    }

    public void updateUserCreds(Map<String, String> uCreds) {
        userCreds.clear();
        userCreds.putAll(uCreds);
    }

    public void writeUserCreds(Map<String, String> uCreds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_CREDS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        for(Map.Entry<String, String> entry: uCreds.entrySet()){
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }
}
