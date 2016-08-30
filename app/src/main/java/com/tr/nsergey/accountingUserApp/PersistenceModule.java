package com.tr.nsergey.accountingUserApp;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

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
        return SqlBrite.create(message -> Log.d("SqlBrite", message));
    }
    @Provides
    @Singleton
    public BriteDatabase provideBriteDatabase(SqlBrite sqlBrite, SQLiteOpenHelper sqLiteOpenHelper){
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(sqLiteOpenHelper, Schedulers.io());
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
    public PersistencePresenter provicePersistencePresenter(){
        return new PersistencePresenter();
    }
    @Provides
    @Singleton
    public SQLiteOpenHelper provideOpenHealper(Context context){
        return new QueueOpenHelper(context);
    }
}
