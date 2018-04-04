package id.bonabrian.scious.libraryservice.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import id.bonabrian.scious.libraryservice.events.SciousDeviceEvent;
import id.bonabrian.scious.libraryservice.events.SciousDeviceEventBatteryInfo;
import id.bonabrian.scious.libraryservice.events.SciousDeviceEventVersionInfo;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.model.BatteryState;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsDeviceSupport implements IDeviceSupport {
    private static final String TAG = AbsDeviceSupport.class.getSimpleName();
    protected SciousDevice sciousDevice;
    private BluetoothAdapter btAdapter;
    private Context context;
    private boolean autoReconnect;

    @Override
    public void setContext(SciousDevice sciousDevice, BluetoothAdapter btAdapter, Context context) {
        this.sciousDevice = sciousDevice;
        this.btAdapter = btAdapter;
        this.context = context;
    }

    @Override
    public boolean connectFirstTime() {
        return connect();
    }

    @Override
    public boolean isConnected() {
        return sciousDevice.isConnected();
    }

    protected boolean isInitialized() {
        return sciousDevice.isInitialized();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        autoReconnect = enable;
    }

    @Override
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @Override
    public SciousDevice getDevice() {
        return sciousDevice;
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public void evalueateSciousDeviceEvent(SciousDeviceEvent deviceEvent) {
        if (deviceEvent instanceof SciousDeviceEventBatteryInfo) {
            handleSciousDeviceEvent((SciousDeviceEventBatteryInfo) deviceEvent);
        } else if (deviceEvent instanceof SciousDeviceEventVersionInfo) {
            handleSciousDeviceEvent((SciousDeviceEventVersionInfo) deviceEvent);
        }
    }

    protected void handleSciousDeviceEvent(SciousDeviceEventVersionInfo info) {
        Context context = getContext();
        Log.i(TAG, "Got event for VERSION_INFO");
        if (sciousDevice == null) {
            return;
        }
        sciousDevice.setFirmwareVersion(info.fwVersion);
        sciousDevice.setModel(info.hwVersion);
        sciousDevice.sendDeviceUpdateIntent(context);
    }

    protected void handleSciousDeviceEvent(SciousDeviceEventBatteryInfo batteryInfo) {
        Context context = getContext();
        Log.i(TAG, "Got event for BATTERY_INFO");
        sciousDevice.setBatteryLevel(batteryInfo.level);
        sciousDevice.setBatteryState(batteryInfo.state);

        if (batteryInfo.level <= sciousDevice.getBatteryThresholdPercent() && (BatteryState.BATTERY_LOW.equals(batteryInfo.state) || BatteryState.BATTERY_NORMAL.equals(batteryInfo.state))) {
            sciousDevice.sendDeviceUpdateIntent(context);
        }

        sciousDevice.sendDeviceUpdateIntent(context);
    }
}
