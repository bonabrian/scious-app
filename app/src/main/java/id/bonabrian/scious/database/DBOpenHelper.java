package id.bonabrian.scious.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import id.bonabrian.scious.entities.DaoMaster;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DBOpenHelper extends DaoMaster.OpenHelper {

    private final String updaterClassNamePrefix;
    private final Context context;

    public DBOpenHelper(Context context, String dbName, SQLiteDatabase.CursorFactory factory) {
        super(context, dbName, factory);
        updaterClassNamePrefix = dbName + "update_";
        this.context = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DaoMaster.createAllTables(db, true);
        // TODO upgrade database
        //new SchemaMigration(updaterClassNamePrefix).onUpgrade(db, oldVersion, newVersion);
    }

    public Context getContext() {
        return context;
    }
}
