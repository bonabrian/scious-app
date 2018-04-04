package id.bonabrian.scious.source.dao;

import de.greenrobot.dao.DaoException;
import id.bonabrian.scious.entities.DaoSession;
import id.bonabrian.scious.entities.DeviceDao;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Device {

    private Long id;
    private String name;
    private String manufacturer;
    private String identifier;
    private int type;
    private String model;

    private transient DaoSession daoSession;

    private transient DeviceDao deviceDao;

    public Device() {

    }

    public Device(Long id) {
        this.id = id;
    }

    public Device(Long id, String name, String manufacturer, String identifier, int type, String model) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.identifier = identifier;
        this.type = type;
        this.model = model;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        deviceDao = daoSession != null ? daoSession.getDeviceDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void delete() {
        if (deviceDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        deviceDao.delete(this);
    }

    public void update() {
        if (deviceDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        deviceDao.update(this);
    }

    public void refresh() {
        if (deviceDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        deviceDao.refresh(this);
    }
}
