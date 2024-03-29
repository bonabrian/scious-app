package id.bonabrian.scious.entities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import id.bonabrian.scious.source.dao.Device;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceDao extends AbstractDao<Device, Long> {

    public static final String TABLENAME = "DEVICE";

    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Manufacturer = new Property(2, String.class, "manufacturer", false, "MANUFACTURER");
        public final static Property Identifier = new Property(3, String.class, "identifier", false, "IDENTIFIER");
        public final static Property Type = new Property(4, int.class, "type", false, "TYPE");
        public final static Property Model = new Property(5, String.class, "model", false, "MODEL");
    };

    private DaoSession daoSession;

    public DeviceDao(DaoConfig config) {
        super(config);
    }

    public DeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEVICE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"NAME\" TEXT NOT NULL ," + // 1: name
                "\"MANUFACTURER\" TEXT NOT NULL ," + // 2: manufacturer
                "\"IDENTIFIER\" TEXT NOT NULL UNIQUE ," + // 3: identifier
                "\"TYPE\" INTEGER NOT NULL ," + // 4: type
                "\"MODEL\" TEXT);"); // 5: model
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, Device entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getName());
        stmt.bindString(3, entity.getManufacturer());
        stmt.bindString(4, entity.getIdentifier());
        stmt.bindLong(5, entity.getType());

        String model = entity.getModel();
        if (model != null) {
            stmt.bindString(6, model);
        }
    }

    @Override
    protected void attachEntity(Device entity) {
        super.attachEntity(entity);
        entity.setDaoSession(daoSession);
    }

    @Override
    protected Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    protected Device readEntity(Cursor cursor, int offset) {
        Device entity = new Device( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getString(offset + 1), // name
                cursor.getString(offset + 2), // manufacturer
                cursor.getString(offset + 3), // identifier
                cursor.getInt(offset + 4), // type
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // model
        );
        return entity;
    }

    @Override
    protected void readEntity(Cursor cursor, Device entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.getString(offset + 1));
        entity.setManufacturer(cursor.getString(offset + 2));
        entity.setIdentifier(cursor.getString(offset + 3));
        entity.setType(cursor.getInt(offset + 4));
        entity.setModel(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
    }

    @Override
    protected Long updateKeyAfterInsert(Device entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    protected Long getKey(Device entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }

}
