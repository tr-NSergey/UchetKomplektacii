package com.tr.nsergey.accountingUserApp;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by sergey on 28.08.16.
 */
public class PersistenceModel extends LocalDbModel{
    public final static String DB_PATH = "/data/data/com.tr.nsergey.accountingUserApp/databases"; //assuming its like this...so far;

    @Inject
    protected BriteDatabase briteDatabase;

    public PersistenceModel() {
        super();
        App.getAppComponent().inject(this);
    }

    public void addEntry(List<Object> params, String userName, String operationName, String functionName) {
        briteDatabase.insert(RequestElem.DATABASE_NAME,
                (new RequestElem(params, userName, operationName, functionName)).toContentValues());
    }

    public void deleteEntry(Integer id) {
        briteDatabase.delete(RequestElem.DATABASE_NAME, RequestElem.ID_FIELD + " = ?", String.valueOf(id));
    }

    public void emptyDb(){
        briteDatabase.execute("DELETE FROM " + RequestElem.DATABASE_NAME);
    }
    public Observable<List<RequestElem>> getQueue() {
        return briteDatabase.createQuery(RequestElem.DATABASE_NAME, RequestElem.ALL_VALUES_QUERY)
                .mapToList(RequestElem.MAP)
                .compose(applySchedulers());
    }
}
