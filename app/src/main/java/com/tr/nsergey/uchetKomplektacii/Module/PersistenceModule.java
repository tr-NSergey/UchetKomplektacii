package com.tr.nsergey.uchetKomplektacii.Module;

import android.content.Context;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.tr.nsergey.uchetKomplektacii.Model.BackupOpenHelper;
import com.tr.nsergey.uchetKomplektacii.Model.HistoryOpenHelper;
import com.tr.nsergey.uchetKomplektacii.Model.PersistenceModel;
import com.tr.nsergey.uchetKomplektacii.Model.QueueOpenHelper;
import com.tr.nsergey.uchetKomplektacii.Presenter.BackupPresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.HistoryPresenter;
import com.tr.nsergey.uchetKomplektacii.Presenter.QueuePresenter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;

@Module
public class PersistenceModule {
    public PersistenceModule(){}
    @Provides
    @Singleton
    public SqlBrite provideSqlBrite(){
        return SqlBrite.create();
    }
    @Provides
    @Singleton
    @Named("QUEUE_DB")
    public BriteDatabase provideQueueBriteDatabase(SqlBrite sqlBrite, QueueOpenHelper queueOpenHelper){
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(queueOpenHelper, Schedulers.io());
        db.setLoggingEnabled(true);
        return db;
    }
    @Provides
    @Singleton
    @Named("HISTORY_DB")
    public BriteDatabase provideHistoryBriteDatabase(SqlBrite sqlBrite, HistoryOpenHelper historyOpenHelper){
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(historyOpenHelper, Schedulers.io());
        db.setLoggingEnabled(true);
        return db;
    }
    @Provides
    @Singleton
    @Named("BACKUP_DB")
    public BriteDatabase provideBackupBriteDatabase(SqlBrite sqlBrite, BackupOpenHelper backupOpenHelper){
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(backupOpenHelper, Schedulers.io());
        db.setLoggingEnabled(true);
        return db;
    }
    @Provides
    @Singleton
    public PersistenceModel providePersistenceModel(){
        return new PersistenceModel();
    }
    @Provides
    @Singleton
    public HistoryPresenter provideHistoryPresenter(){return new HistoryPresenter();}
    @Provides
    @Singleton
    public QueuePresenter providePersistencePresenter(){
        return new QueuePresenter();
    }
    @Provides
    @Singleton
    public BackupPresenter provideArtPresenter(){
        return new BackupPresenter();
    }
    @Provides
    @Singleton
    public QueueOpenHelper provideQueueOpenHelper(Context context){
        return new QueueOpenHelper(context);
    }
    @Provides
    @Singleton
    public HistoryOpenHelper provideHistoryOpenHelper(Context context){
        return new HistoryOpenHelper(context);
    }
    @Provides
    @Singleton
    public BackupOpenHelper provideArtOpenHelper(Context context){
        return new BackupOpenHelper(context);
    }
}
