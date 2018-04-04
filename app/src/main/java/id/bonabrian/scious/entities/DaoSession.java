package id.bonabrian.scious.entities;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;
import id.bonabrian.scious.source.dao.Device;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DaoSession extends AbstractDaoSession {
    private final DaoConfig deviceDaoConfig;

    private final DeviceDao deviceDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
        super(db);

        deviceDaoConfig = daoConfigMap.get(DeviceDao.class).clone();
        deviceDaoConfig.initIdentityScope(type);

        deviceDao = new DeviceDao(deviceDaoConfig, this);

        registerDao(Device.class, deviceDao);
    }

    public void clear() {
        deviceDaoConfig.getIdentityScope().clear();
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }
}
