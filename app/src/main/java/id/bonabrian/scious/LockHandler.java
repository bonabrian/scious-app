package id.bonabrian.scious;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.database.DBHandler;
import id.bonabrian.scious.entities.DaoMaster;
import id.bonabrian.scious.entities.DaoSession;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LockHandler implements DBHandler {
    private DaoMaster daoMaster = null;
    private DaoSession session = null;
    private SQLiteOpenHelper helper = null;

    public LockHandler() {

    }

    public void init(DaoMaster daoMaster, DaoMaster.OpenHelper helper) {
        if (isValid()) {
            throw new IllegalStateException("DB must be closed before initializing it again");
        }
        if (daoMaster == null) {
            throw new IllegalArgumentException("daoMaster must not be null");
        }
        if (helper == null) {
            throw new IllegalArgumentException("helper must not be null");
        }
        this.daoMaster = daoMaster;
        this.helper = helper;

        session = daoMaster.newSession();

        if (session == null) {
            throw new RuntimeException("Unable to create database session");
        }
    }

    private boolean isValid() {
        return daoMaster != null;
    }

    private void ensureValid() {
        if (!isValid()) {
            throw new IllegalStateException("LockHandler is not in a valid state");
        }
    }

    @Override
    public void closeDb() {
        if (session == null) {
            throw new IllegalStateException("session must not be null");
        }
        session.clear();
        session.getDatabase().close();
        session = null;
        helper = null;
        daoMaster = null;
    }

    @Override
    public void openDb() {
        if (session != null) {
            throw new IllegalStateException("Session must not be null");
        }
        SciousApplication.application().setupDatabase();
    }

    @Override
    public SQLiteOpenHelper getHelper() {
        ensureValid();
        return helper;
    }

    @Override
    public void close() {
        ensureValid();
        SciousApplication.releaseDB();
    }

    @Override
    public SQLiteDatabase getDatabase() {
        ensureValid();
        return daoMaster.getDatabase();
    }

    @Override
    public DaoSession getDaoSession() {
        ensureValid();
        return session;
    }
}
