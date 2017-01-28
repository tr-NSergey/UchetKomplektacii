package com.tr.nsergey.uchetKomplektacii.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BackupOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    private static final String CREATE_TABLE = ""
            + "CREATE TABLE " + "NO_ART_CODE" + "("
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

    public static String getCreateTable(String tableName) {
        return "CREATE TABLE " + tableName + "("
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
    }

    public BackupOpenHelper(Context context) {
        super(context, ArtObject.BACKUP_DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BackupOpenHelper.getCreateTable("NO_ART_CODE"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + "NO_ART_CODE");
        onCreate(db);
    }
}
