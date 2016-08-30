package com.tr.nsergey.accountingUserApp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QueueOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 3;
    private static final String CREATE_TABLE = ""
            + "CREATE TABLE " + RequestElem.DATABASE_NAME + "("
            + RequestElem.ID_FIELD + " INTEGER PRIMARY KEY,"
            + RequestElem.PARAMS_FIELD + " TEXT NOT NULL,"
            + RequestElem.USERNAME_FIELD + " TEXT NOT NULL,"
            + RequestElem.OPERATION_FIELD + " TEXT NOT NULL,"
            + RequestElem.FUNCTION_FIELD + " TEXT NOT NULL"
            + ")";
    public QueueOpenHelper(Context context){
        super(context, RequestElem.DATABASE_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + RequestElem.DATABASE_NAME);
        onCreate(db);
    }
}
