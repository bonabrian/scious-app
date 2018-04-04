package id.bonabrian.scious.libraryservice.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import java.util.EnumSet;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.model.NotificationSpec;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ServiceDeviceSupport implements IDeviceSupport {

    enum Flags {
        THROTTLING,
        BUSY_CHECKING,
    }

    private static final String TAG = ServiceDeviceSupport.class.getSimpleName();
    private static final long THROTTLING_THRESHOLD = 1000; // throttle multiple events in between one second
    private final IDeviceSupport delegate;

    private long lastNotificationTime = 0;
    private String lastNotificationKind;
    private final EnumSet<Flags> flags;

    public ServiceDeviceSupport(IDeviceSupport delegate, EnumSet<Flags> flags) {
        this.delegate = delegate;
        this.flags = flags;
    }

    @Override
    public void setContext(SciousDevice sciousDevice, BluetoothAdapter btAdapter, Context context) {
        delegate.setContext(sciousDevice, btAdapter, context);
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean connectFirstTime() {
        return delegate.connectFirstTime();
    }

    @Override
    public boolean connect() {
        return delegate.connect();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        delegate.setAutoReconnect(enable);
    }

    @Override
    public boolean getAutoReconnect() {
        return delegate.getAutoReconnect();
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    @Override
    public SciousDevice getDevice() {
        return delegate.getDevice();
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return delegate.getBluetoothAdapter();
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public boolean useAutoConnect() {
        return delegate.useAutoConnect();
    }

    private boolean checkBusy(String notificationKind) {
        if (!flags.contains(Flags.BUSY_CHECKING)) {
            return false;
        }
        if (getDevice().isBusy()) {
            Log.i(TAG, "Ignoring " + notificationKind + " because we're busy with " + getDevice().getBusyTask());
            return true;
        }
        return false;
    }

    private boolean checkThrottle(String notificationKind) {
        if (!flags.contains(Flags.THROTTLING)) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastNotificationTime) < THROTTLING_THRESHOLD) {
            if (notificationKind != null && notificationKind.equals(lastNotificationKind)) {
                Log.i(TAG, "Ignoring " + notificationKind + " because of throttling threshold reached");
                return true;
            }
        }
        lastNotificationTime = currentTime;
        lastNotificationKind = notificationKind;
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        if (checkBusy("generic notification") || checkThrottle("generic notification")) {
            return;
        }
        delegate.onNotification(notificationSpec);
    }

    @Override
    public void onDeleteNotification(int id) {
        delegate.onDeleteNotification(id);
    }

    @Override
    public void onSetTime() {
        if (checkBusy("set time") || checkThrottle("set time")) {
            return;
        }
        delegate.onSetTime();
    }

    @Override
    public void onFetchActivityData() {
        if (checkBusy("fetch activity data")) {
            return;
        }
        delegate.onFetchActivityData();
    }

    @Override
    public void onHeartRateTest() {
        if (checkBusy("heartrate")) {
            return;
        }
        delegate.onHeartRateTest();
    }

    @Override
    public void onFindDevice(boolean start) {
        if (checkBusy("find device")) {
            return;
        }
        delegate.onFindDevice(start);
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        if (checkBusy("enable realtime steps: " + enable)) {
            return;
        }
        delegate.onEnableRealtimeSteps(enable);
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        if (checkBusy("enable heartrate sleep support: " + enable)) {
            return;
        }
        delegate.onEnableHeartRateSleepSupport(enable);
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        if (checkBusy("enable realtime heart rate measurement: " + enable)) {
            return;
        }
        delegate.onEnableRealtimeHeartRateMeasurement(enable);
    }

    @Override
    public void onSendConfiguration(String config) {
        if (checkBusy("send configuration: " + config)) {
            return;
        }
        delegate.onSendConfiguration(config);
    }
}
