package com.tr.nsergey.accountingUserApp;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.functions.Func1;

public class RequestElem {
    public static final String DATABASE_NAME = "queue$";
    public static final String ALL_VALUES_QUERY = "SELECT * FROM " + DATABASE_NAME;
    public static final String ID_FIELD = "_id";
    public static final String PARAMS_FIELD = "params";
    public static final String USERNAME_FIELD = "userName";
    public static final String OPERATION_FIELD = "operationName";
    public static final String FUNCTION_FIELD = "functionName";
    public final Integer _id;
    public final List<Object> params;
    public final String paramsString;
    public final String userName;
    public final String operationName;
    public final String functionName;
    protected static Func1<Cursor, RequestElem> MAP = cursor -> {
        Integer _id = Db.getInt(cursor, ID_FIELD);
        String paramsString = Db.getString(cursor, PARAMS_FIELD);
        String userName = Db.getString(cursor, USERNAME_FIELD);
        String operationName = Db.getString(cursor, OPERATION_FIELD);
        String functionName = Db.getString(cursor, FUNCTION_FIELD);
        return new RequestElem(_id, paramsString, userName, operationName, functionName);
    };
    public RequestElem(Integer _id, String paramsString, String userName, String operationName, String functionName){
        this._id = _id;
        this.paramsString = paramsString;
        this.params = RequestElem.paramsToList(paramsString);
        this.userName = userName;
        this.operationName = operationName;
        this.functionName = functionName;
    }
    public RequestElem(List<Object> params, String userName, String operationName, String functionName){
        this._id = null;
        this.params = new ArrayList<>();
        this.params.addAll(params);
        this.paramsString = RequestElem.paramsToString(params);
        this.userName = userName;
        this.operationName = operationName;
        this.functionName = functionName;
    }
    protected ContentValues toContentValues(){
        ContentValues contentValues = new ContentValues();
        if(this._id != null){
            contentValues.put(ID_FIELD, this._id);
        }
        contentValues.put(PARAMS_FIELD, this.paramsString);
        contentValues.put(USERNAME_FIELD, this.userName);
        contentValues.put(OPERATION_FIELD, this.operationName);
        contentValues.put(FUNCTION_FIELD, this.functionName);
        return contentValues;
    }
    private static List<Object> paramsToList(String paramsString){
        List<Object> p = new ArrayList<>();
        p.addAll(Arrays.asList(paramsString.split(", ")));
        return p;
    }
    private static String paramsToString(List<Object> params){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < params.size(); i++){
            sb.append((String)params.get(i));
            if(i != params.size() - 1){
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}

