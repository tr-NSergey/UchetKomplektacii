package com.tr.nsergey.uchetKomplektacii.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Func1;
public class ArtObject {
    static final String BACKUP_DATABASE_NAME = "art$";
    static final String HISTORY_DATABASE_NAME = "allRequests$";
    static final String QUEUE_DATABASE_NAME = "queue$";
    static final String ALL_BACKUP_QUERY = "SELECT * FROM " + BACKUP_DATABASE_NAME;
    static final String ALL_HISTORY_QUERY = "SELECT * FROM " + HISTORY_DATABASE_NAME;
    static final String ALL_QUEUE_QUERY = "SELECT * FROM " + QUEUE_DATABASE_NAME;
    static final String ID_FIELD = "_id";
    static final String FUNCTION_FIELD = "function";
    static final String ART_FIELD = "art";
    static final String NAME_FIELD = "name";
    static final String USER_NAME_FIELD = "userName";
    static final String MODIFICATION_FIELD = "modification";
    static final String QUANTITY_FIELD = "quantity";
    static final String OLD_QUANTITY_FIELD = "oldQuantity";
    static final String LOCATION_FIELD = "location";
    private Integer _id;
    private String function;
    private final String art;
    private final String name;
    private String userName;
    private final String modification;
    private final String location;
    private Integer quantity;
    private Integer oldQuantity;
    static Func1<Cursor, ArtObject> MAP = cursor -> {
        Integer _id = Db.getInt(cursor, ID_FIELD);
        String function = Db.getString(cursor, FUNCTION_FIELD);
        String art = Db.getString(cursor, ART_FIELD);
        String name = Db.getString(cursor, NAME_FIELD);
        String userName = Db.getString(cursor, USER_NAME_FIELD);
        String modification = Db.getString(cursor, MODIFICATION_FIELD);
        String location = Db.getString(cursor, LOCATION_FIELD);
        Integer quantity = Db.getInt(cursor, QUANTITY_FIELD);
        Integer oldQuantity = Db.getInt(cursor, OLD_QUANTITY_FIELD);
        return new ArtObject(_id, function, art, name, userName, modification, location, quantity, oldQuantity);
    };

    ArtObject(Integer _id, String function, String art, String name, String userName, String modification,
              String location, Integer quantity, Integer oldQuantity){
        this._id = _id;
        this.function = function != null? function: "";
        this.art = art;
        this.name = name;
        this.userName = userName != null? userName: "";
        this.modification = modification != null? modification: "";
        this.location = location != null? location: "";
        this.quantity = quantity != null? quantity: -1;
        this.oldQuantity = oldQuantity != null? oldQuantity: -1;
    }
    public String getFunction(){
        return function;
    }
    public Integer get_id(){
        return _id;
    }
    public String getArt() {
        return art;
    }

    public String getName(){return name;}

    public String getUserName(){
        return userName;
    }
    public String getModification() {
        return modification;
    }

    public String getLocation() {
        return location;
    }

    public String getQuantity() {
        return Integer.toString(quantity);
    }

    public String getOldQuantity(){
        if(oldQuantity == null){
            return "";
        }
        return Integer.toString(oldQuantity);
    }
    public String getArtCode(){
        char c = this.art.charAt(0);
        if(!(c < '0' || c > '9')) {
            return "";
        }
        return this.art.substring(0, 2);
    }
    public static String getArtCodeFromArt(String art){
        char c = art.charAt(0);
        if(!(c < '0' || c > '9')) {
            return "";
        }
        return art.substring(0, 2);
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public void setOldQuantity(Integer quantity){
        this.oldQuantity = quantity;
    }

    ContentValues toContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, this._id);
        contentValues.put(FUNCTION_FIELD, this.function);
        contentValues.put(ART_FIELD, this.art);
        contentValues.put(NAME_FIELD, this.name);
        contentValues.put(USER_NAME_FIELD, this.userName);
        contentValues.put(MODIFICATION_FIELD, this.modification);
        contentValues.put(LOCATION_FIELD, this.location);
        contentValues.put(QUANTITY_FIELD, this.quantity);
        contentValues.put(OLD_QUANTITY_FIELD, this.oldQuantity);
        return contentValues;
    }
    public Map<String, String> toParametersMap(Context context){
        Map<String, String> map = new HashMap<>();
        map.put("artNum0", this.getArt());
        map.put("modification0", this.getModification());
        map.put("quantity0", this.getQuantity());
        map.put("userName0", this.getUserName());
        map.put("deviceId0", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        return map;
    }
    static ArrayList<ArtObject> fromJsonArray(JSONArray jsonArray) {
        ArrayList<ArtObject> artObjects = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                ArtObject artObject = new ArtObject(null, null, tempObject.getString(ART_FIELD), tempObject.getString(NAME_FIELD),
                        null, tempObject.getString(MODIFICATION_FIELD), tempObject.getString(LOCATION_FIELD),
                        tempObject.getInt(QUANTITY_FIELD), null);
                try{
                    artObject.setOldQuantity(tempObject.getInt(OLD_QUANTITY_FIELD));
                } catch (JSONException e){}
                artObjects.add(artObject);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return artObjects;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }
}

