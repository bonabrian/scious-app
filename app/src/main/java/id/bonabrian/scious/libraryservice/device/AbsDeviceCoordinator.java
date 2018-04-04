package id.bonabrian.scious.libraryservice.device;

import android.bluetooth.le.ScanFilter;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.database.DBHandler;
import id.bonabrian.scious.database.DBHelper;
import id.bonabrian.scious.entities.DaoSession;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.source.dao.Device;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsDeviceCoordinator implements IDeviceCoordinator {
    private static final String TAG = AbsDeviceCoordinator.class.getSimpleName();

    @Override
    public boolean supports(SciousDeviceCandidate candidate) {
        return getSupportedType(candidate).isSupported();
    }

    @Override
    public boolean supports(SciousDevice device) {
        return getDeviceType().equals(device.getType());
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        return Collections.emptyList();
    }

    @Override
    public SciousDevice createDevice(SciousDeviceCandidate candidate) {
        return new SciousDevice(candidate.getDevice().getAddress(), candidate.getName(), getDeviceType());
    }

    @Override
    public void deleteDevice(SciousDevice sciousDevice) throws Exception {
        Log.i(TAG, "Will try to delete device: " + sciousDevice.getName());
        if (sciousDevice.isConnected() || sciousDevice.isConnecting()) {
            SciousApplication.deviceService().disconnect();
        }
        try (DBHandler dbHandler = SciousApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            Device device = DBHelper.findDevice(sciousDevice, session);
            if (device != null) {
                deleteDevice(sciousDevice, device, session);
                session.getDeviceDao().delete(device);
            } else {
                Log.i(TAG, "Device to delete not found in database");
            }
        } catch (Exception e) {
            throw new Exception("Error deleting device: " + e.getMessage(), e);
        }
    }

    protected abstract void deleteDevice(@NonNull SciousDevice sciousDevice, @NonNull Device device, @NonNull DaoSession session) throws Exception;

    @Override
    public boolean allowFetchActivityData(SciousDevice device) {
        return device.isInitialized() && !device.isBusy() && supportsActivityDataFetching();
    }

    @Override
    public int getBondingStyle(SciousDevice device) {
        return BONDING_STYLE_ASK;
    }
}
