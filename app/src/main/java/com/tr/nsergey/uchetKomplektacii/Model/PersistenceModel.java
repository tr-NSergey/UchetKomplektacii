package com.tr.nsergey.uchetKomplektacii.Model;

import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.tr.nsergey.uchetKomplektacii.App;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

public class PersistenceModel extends LocalDbModel {

    public static final String QUEUE = "queue";
    public static final String HISTORY = "history";
    public static final String BACKUP = "backup";

    @Inject
    @Named("QUEUE_DB")
    protected BriteDatabase queueDb;
    @Inject
    @Named("HISTORY_DB")
    protected BriteDatabase historyDb;
    @Inject
    @Named("BACKUP_DB")
    protected BriteDatabase backupDb;

    public PersistenceModel() {
        super();
        App.getAppComponent().inject(this);
    }

    private void addQueueEntry(ArtObject artObject) {
        queueDb.insert(ArtObject.QUEUE_DATABASE_NAME, artObject.toContentValues());
    }

    private void addHistoryEntry(ArtObject artObject) {
        historyDb.insert(ArtObject.HISTORY_DATABASE_NAME, artObject.toContentValues());
    }

    private void addBackupEntry(ArtObject artObject) {
        backupDb.insert(ArtObject.BACKUP_DATABASE_NAME, artObject.toContentValues());
    }

    private void addBackupEntries(List<ArtObject> artObjects) {

        String dbName = artObjects.get(0).getArtCode();
        //String dbName = "sg";
        backupDb.execute("DROP TABLE IF EXISTS " + dbName);
        backupDb.execute(BackupOpenHelper.getCreateTable(dbName));
        BriteDatabase.Transaction transaction = backupDb.newTransaction();
        try {
            for (ArtObject artObject : artObjects) {
                backupDb.insert(dbName, artObject.toContentValues());
            }
            transaction.markSuccessful();
        } finally {
            transaction.end();
        }
    }

    private void deleteQueueEntry(Integer id) {
        queueDb.delete(ArtObject.QUEUE_DATABASE_NAME, ArtObject.ID_FIELD + " = ?", String.valueOf(id));
    }

    private void deleteHistoryEntry(Integer id) {
        historyDb.delete(ArtObject.HISTORY_DATABASE_NAME, ArtObject.ID_FIELD + " = ?", String.valueOf(id));
    }

    private void deleteBackupEntry(Integer id) {
        backupDb.delete(ArtObject.BACKUP_DATABASE_NAME, ArtObject.ID_FIELD + " = ?", String.valueOf(id));
    }

    private void emptyQueueDb() {
        queueDb.execute("DELETE FROM " + ArtObject.QUEUE_DATABASE_NAME);
    }

    private void emptyHistoryDb() {
        historyDb.execute("DELETE FROM " + ArtObject.HISTORY_DATABASE_NAME);
    }

    private void emptyBackupDb() {
        //backupDb.execute("DELETE FROM " + ArtObject.BACKUP_DATABASE_NAME);
    }

    private Observable<List<ArtObject>> getQueue() {
        return queueDb.createQuery(ArtObject.QUEUE_DATABASE_NAME, ArtObject.ALL_QUEUE_QUERY)
                .mapToList(ArtObject.MAP)
                .compose(applySchedulers());
    }

    private Observable<List<ArtObject>> getHistory() {
        return historyDb.createQuery(ArtObject.HISTORY_DATABASE_NAME, ArtObject.ALL_HISTORY_QUERY)
                .mapToList(ArtObject.MAP)
                .compose(applySchedulers());
    }

    private Observable<List<ArtObject>> getBackup() {
        return backupDb.createQuery(ArtObject.BACKUP_DATABASE_NAME, ArtObject.ALL_BACKUP_QUERY)
                .mapToList(ArtObject.MAP)
                .compose(applySchedulers());
    }

    private Observable<List<ArtObject>> getBackupArt(String art) {
        return backupDb.createQuery(ArtObject.BACKUP_DATABASE_NAME, "SELECT * FROM " + ArtObject.BACKUP_DATABASE_NAME +
                " WHERE " + ArtObject.ART_FIELD + "=" + art)
                .mapToList(ArtObject.MAP)
                .compose(applySchedulers());
    }

    public void addEntry(String dbName, ArtObject artObject) {
        switch (dbName) {
            case HISTORY:
                addHistoryEntry(artObject);
                return;
            case QUEUE:
                addQueueEntry(artObject);
                return;
            case BACKUP:
                addBackupEntry(artObject);
                return;
            default:
                throw new AssertionError();
        }
    }

    public List<ArtObject> getEntries(String dbName, String art) {
        switch (dbName) {
            case BACKUP:
                return getBackupEntries(art);
            default:
                throw new AssertionError();
        }
    }

    private List<ArtObject> getBackupEntries(String art) {
        String tableName = ArtObject.getArtCodeFromArt(art);
        if (tableName.equals("")) {
            tableName = "NO_ART_CODE";
        }
        String query = "SELECT * FROM " + tableName + " WHERE " +
                ArtObject.ART_FIELD + " = '" + art + "'";
        Cursor c = backupDb.query(query);
        List<ArtObject> artObjects = new ArrayList<>();
        c.moveToFirst();
        while(!c.isAfterLast()){
            artObjects.add(ArtObject.MAP.call(c));
            c.moveToNext();
        }
        c.close();
        return artObjects;
        /*
        return backupDb.createQuery(tableName, query)
                .mapToList(ArtObject.MAP)
                .compose(applySchedulers());
                */
    }

    public void replaceAllEntries(String dbName, List<ArtObject> artObjects) {
        switch (dbName) {
            case HISTORY:
                return;
            case QUEUE:
                return;
            case BACKUP:
                addBackupEntries(artObjects);
                return;
            default:
                throw new AssertionError();
        }
    }

    public void deleteEntry(String dbName, Integer id) {
        switch (dbName) {
            case HISTORY:
                deleteHistoryEntry(id);
                return;
            case QUEUE:
                deleteQueueEntry(id);
                return;
            case BACKUP:
                deleteBackupEntry(id);
                return;
            default:
                throw new AssertionError();
        }
    }

    public void emptyDb(String dbName) {
        switch (dbName) {
            case HISTORY:
                emptyHistoryDb();
                return;
            case QUEUE:
                emptyQueueDb();
                return;
            case BACKUP:
                emptyBackupDb();
                return;
            default:
                throw new AssertionError();
        }
    }

    public Observable<List<ArtObject>> getSubscription(String dbName) {
        switch (dbName) {
            case HISTORY:
                return getHistory();
            case QUEUE:
                return getQueue();
            case BACKUP:
                return getBackup();
            default:
                throw new AssertionError();
        }
    }
}
