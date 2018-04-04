package id.bonabrian.scious.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import id.bonabrian.scious.entities.DaoSession;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface DBHandler extends AutoCloseable {
    void closeDb();

    void openDb();

    SQLiteOpenHelper getHelper();

    void close();

    SQLiteDatabase getDatabase();

    DaoSession getDaoSession();
}
