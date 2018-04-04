package id.bonabrian.scious.libraryservice.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import id.bonabrian.scious.libraryservice.device.IEventHandler;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IDeviceSupport extends IEventHandler {

    void setContext(SciousDevice sciousDevice, BluetoothAdapter btAdapter, Context context);

    boolean isConnected();

    boolean connectFirstTime();

    boolean connect();

    void dispose();

    boolean useAutoConnect();

    void setAutoReconnect(boolean enable);

    boolean getAutoReconnect();

    SciousDevice getDevice();

    BluetoothAdapter getBluetoothAdapter();

    Context getContext();
}
