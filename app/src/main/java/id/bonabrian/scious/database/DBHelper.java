package id.bonabrian.scious.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.greenrobot.dao.query.Query;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.entities.DaoSession;
import id.bonabrian.scious.entities.DeviceDao;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.source.dao.Device;
import id.bonabrian.scious.util.DeviceHelper;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DBHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    private final Context context;

    public DBHelper(Context context) {
        this.context = context;
    }

    private String getClosedDBPath(DBHandler dbHandler) throws IllegalStateException {
        SQLiteDatabase db = dbHandler.getDatabase();
        String path = db.getPath();
        dbHandler.closeDb();
        if (db.isOpen()) {
            throw new IllegalStateException("Database must be closed");
        }
        return path;
    }

    private String getDate() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
    }

    public void validateDB(SQLiteOpenHelper dbHandler) throws IOException {
        try (SQLiteDatabase db = dbHandler.getReadableDatabase()) {
            if (!db.isDatabaseIntegrityOk()) {
                throw new IOException("Database integrity is not OK");
            }
        }
    }

    public static void dropTable(String tableName, SQLiteDatabase db) {
        String statement = "DROP TABLE IF EXISTS '" + tableName + "'";
        db.execSQL(statement);
    }

    public boolean existsDB(String dbName) {
        File path = context.getDatabasePath(dbName);
        return path != null && path.exists();
    }

    public static boolean existsColumn(String tableName, String columnName, SQLiteDatabase db) {
        try (Cursor res = db.rawQuery("PRAGMA table_info('" + tableName + "')", null)) {
            int index = res.getColumnIndex("name");
            if (index < 1) {
                return false;
            }
            while (res.moveToNext()) {
                String cn = res.getString(index);
                if (columnName.equals(cn)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    public static String getWithoutRowId() {
        if (SciousApplication.isRunningLollipopOrLater()) {
            return " WITHOUT ROWID;";
        }
        return "";
    }

    public static Device findDevice(SciousDevice sciousDevice, DaoSession session) {
        DeviceDao deviceDao = session.getDeviceDao();
        Query<Device> query = deviceDao.queryBuilder().where(DeviceDao.Properties.Identifier.eq(sciousDevice.getAddress())).build();
        List<Device> devices = query.list();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }

    public static Device getDevice(SciousDevice sciousDevice, DaoSession session) {
        Device device = findDevice(sciousDevice, session);
        if (device == null) {
            device = createDevice(sciousDevice, session);
        } else {
            ensureDeviceUpToDate(device, sciousDevice, session);
        }
        return device;
    }

    private static void ensureDeviceUpToDate(Device device, SciousDevice sciousDevice, DaoSession session) {
        if (!isDeviceUpToDate(device, sciousDevice)) {
            device.setIdentifier(sciousDevice.getAddress());
            device.setName(sciousDevice.getName());
            IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(sciousDevice);
            device.setManufacturer(coordinator.getManufacturer());
            device.setType(sciousDevice.getType().getKey());
            device.setModel(sciousDevice.getModel());

            if (device.getId() == null) {
                session.getDeviceDao().insert(device);
            } else {
                session.getDeviceDao().update(device);
            }
        }
    }

    private static boolean isDeviceUpToDate(Device device, SciousDevice sciousDevice) {
        if (!Objects.equals(device.getIdentifier(), sciousDevice.getAddress())) {
            return false;
        }
        if (!Objects.equals(device.getName(), sciousDevice.getName())) {
            return false;
        }
        IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(sciousDevice);
        if (!Objects.equals(device.getManufacturer(), coordinator.getManufacturer())) {
            return false;
        }
        if (device.getType() != sciousDevice.getType().getKey()) {
            return false;
        }
        if (!Objects.equals(device.getModel(), sciousDevice.getModel())) {
            return false;
        }
        return true;
    }

    private static Device createDevice(SciousDevice sciousDevice, DaoSession session) {
        Device device = new Device();
        ensureDeviceUpToDate(device, sciousDevice, session);

        return device;
    }

    public static void clearSession() {
        try (DBHandler dbHandler = SciousApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            session.clear();
        } catch (Exception e) {
            Log.w(TAG, "Unable to acquire database to clear the session", e);
        }
    }

    public static List<Device> getActiveDevices(DaoSession daoSession) {
        return daoSession.getDeviceDao().loadAll();
    }
}
