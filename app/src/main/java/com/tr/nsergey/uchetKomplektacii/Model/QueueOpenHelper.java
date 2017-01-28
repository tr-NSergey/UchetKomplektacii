package com.tr.nsergey.uchetKomplektacii.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QueueOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 3;
    private static final String CREATE_TABLE = ""
            + "CREATE TABLE " + ArtObject.QUEUE_DATABASE_NAME + "("
            + ArtObject.ID_FIELD + " INTEGER PRIMARY KEY,"
            + ArtObject.FUNCTION_FIELD + " TEXT NOT NULL,"
            + ArtObject.ART_FIELD + " TEXT NOT NULL,"
            + ArtObject.NAME_FIELD + " TEXT NOT NULL,"
            + ArtObject.USER_NAME_FIELD + " TEXT NOT NULL,"
            + ArtObject.MODIFICATION_FIELD + " TEXT,"
            + ArtObject.QUANTITY_FIELD + " INTEGER NOT NULL,"
            + ArtObject.LOCATION_FIELD + " TEXT,"
            + ArtObject.OLD_QUANTITY_FIELD + " INTEGER NOT NULL"
            + ")";
    public QueueOpenHelper(Context context){
        super(context, ArtObject.QUEUE_DATABASE_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + ArtObject.QUEUE_DATABASE_NAME);
        onCreate(db);
    }
}
