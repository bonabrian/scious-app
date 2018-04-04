package id.bonabrian.scious.libraryservice.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.events.BluetoothConnectReceiver;
import id.bonabrian.scious.libraryservice.events.BluetoothPairingRequestReceiver;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.model.NotificationSpec;
import id.bonabrian.scious.libraryservice.model.NotificationType;
import id.bonabrian.scious.util.DeviceHelper;
import id.bonabrian.scious.util.Prefs;
import id.bonabrian.scious.util.Scious;
import id.bonabrian.scious.util.SciousPrefs;

import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_CONNECT;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_DELETE_NOTIFICATION;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_DISCONNECT;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_ENABLE_REALTIME_STEPS;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_FETCH_ACTIVITY_DATA;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_FIND_DEVICE;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_NOTIFICATION;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_REQUEST_DEVICEINFO;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_SEND_CONFIGURATION;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_SET_TIME;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.ACTION_START;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_BOOLEAN_ENABLE;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_CONFIG;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_CONNECT_FIRST_TIME;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_FIND_START;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_NOTIFICATION_FLAGS;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_NOTIFICATION_ID;
import static id.bonabrian.scious.libraryservice.model.IDeviceService.EXTRA_NOTIFICATION_TYPE;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceCommunicationService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = DeviceCommunicationService.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static DeviceSupportFactory DEVICE_SUPPORT_FACTORY = null;

    private boolean mStarted = false;

    private DeviceSupportFactory mFactory;
    private SciousDevice mSciousDevice = null;
    private IDeviceSupport mDeviceSupport;

    private BluetoothConnectReceiver mBlueToothConnectReceiver = null;
    private BluetoothPairingRequestReceiver mBlueToothPairingRequestReceiver = null;

    public static void setDeviceSupportFactory(DeviceSupportFactory factory) {
        DEVICE_SUPPORT_FACTORY = factory;
    }

    public DeviceCommunicationService() {

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SciousDevice.ACTION_DEVICE_CHANGED)) {
                SciousDevice device = intent.getParcelableExtra(SciousDevice.EXTRA_DEVICE);
                if (mSciousDevice != null && mSciousDevice.equals(device)) {
                    mSciousDevice = device;
                    boolean enableReceivers = mDeviceSupport != null && (mDeviceSupport.useAutoConnect() || mSciousDevice.isInitialized());
                    setReceiversEnableState(enableReceivers, mSciousDevice.isInitialized(), DeviceHelper.getInstance().getCoordinator(device));
                    Scious.updateNotification(mSciousDevice.getName() + " " + mSciousDevice.getStateString(), mSciousDevice.isInitialized(), context);
                } else {
                    Log.e(TAG, "Got ACTION_DEVICE_CHANGED from unexpected device: " + device);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "DeviceCommunicationService is being created");
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(SciousDevice.ACTION_DEVICE_CHANGED));
        mFactory = getDeviceSupportFactory();

        if (hasPrefs()) {
            getPrefs().getPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }

    private DeviceSupportFactory getDeviceSupportFactory() {
        if (DEVICE_SUPPORT_FACTORY != null) {
            return DEVICE_SUPPORT_FACTORY;
        }
        return new DeviceSupportFactory(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i(TAG, "No intent");
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        boolean firstTime = intent.getBooleanExtra(EXTRA_CONNECT_FIRST_TIME, false);

        if (action == null) {
            Log.i(TAG, "No action");
            return START_NOT_STICKY;
        }

        Log.d(TAG, "Service startcommand: " + action);
        if (!action.equals(ACTION_START) && !action.equals(ACTION_CONNECT)) {
            if (!mStarted) {
                Log.i(TAG, "Must start service with " + ACTION_START + " or " + ACTION_CONNECT + " before using it: " + action);
                return START_NOT_STICKY;
            }

            if (mDeviceSupport == null || (!isInitialized() && !mDeviceSupport.useAutoConnect())) {
                if (mSciousDevice != null) {
                    mSciousDevice.sendDeviceUpdateIntent(this);
                }
                return START_STICKY;
            }
        }

        Prefs prefs = getPrefs();
        switch (action) {
            case ACTION_START:
                start();
                break;
            case ACTION_CONNECT:
                start();
                SciousDevice sciousDevice = intent.getParcelableExtra(SciousDevice.EXTRA_DEVICE);
                String btDeviceAddress = null;
                if (sciousDevice == null) {
                    if (prefs != null) {
                        btDeviceAddress = prefs.getString("last_device_address", null);
                        if (btDeviceAddress != null) {
                            sciousDevice = DeviceHelper.getInstance().findAvailableDevice(btDeviceAddress, this);
                        }
                    }
                } else {
                    btDeviceAddress = sciousDevice.getAddress();
                }

                boolean autoReconnect = SciousPrefs.AUTO_RECONNECT_DEFAULT;
                if (prefs != null && prefs.getPreferences() != null) {
                    prefs.getPreferences().edit().putString("last_device_address", btDeviceAddress).apply();
                    autoReconnect = getSciousPrefs().getAutoReconnect();
                }

                if (sciousDevice != null && !isConnecting() && !isConnected()) {
                    setDeviceSupport(null);
                    try {
                        IDeviceSupport deviceSupport = mFactory.createDeviceSupport(sciousDevice);
                        if (deviceSupport != null) {
                            setDeviceSupport(deviceSupport);
                            if (firstTime) {
                                deviceSupport.connectFirstTime();
                            } else {
                                deviceSupport.setAutoReconnect(autoReconnect);
                                deviceSupport.connect();
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.cannot_connect, "Can't create device support"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.cannot_connect, e.getMessage()), Toast.LENGTH_SHORT).show();
                        setDeviceSupport(null);
                    }
                } else if (mSciousDevice != null) {
                    mSciousDevice.sendDeviceUpdateIntent(this);
                }
                break;
            case ACTION_REQUEST_DEVICEINFO:
                mSciousDevice.sendDeviceUpdateIntent(this);
                break;
            case ACTION_NOTIFICATION: {
                NotificationSpec notificationSpec = new NotificationSpec();
                notificationSpec.type = (NotificationType) intent.getSerializableExtra(EXTRA_NOTIFICATION_TYPE);
                notificationSpec.id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                notificationSpec.flags = intent.getIntExtra(EXTRA_NOTIFICATION_FLAGS, 0);
                mDeviceSupport.onNotification(notificationSpec);
                break;
            }
            case ACTION_DELETE_NOTIFICATION: {
                mDeviceSupport.onDeleteNotification(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
                break;
            }
            case ACTION_FETCH_ACTIVITY_DATA: {
                mDeviceSupport.onFetchActivityData();
                break;
            }
            case ACTION_DISCONNECT: {
                mDeviceSupport.dispose();
                if (mSciousDevice != null && mSciousDevice.getState() == SciousDevice.State.WAITING_FOR_RECONNECT) {
                    setReceiversEnableState(false, false, null);
                    mSciousDevice.setState(SciousDevice.State.NOT_CONNECTED);
                    mSciousDevice.sendDeviceUpdateIntent(this);
                }
                mDeviceSupport = null;
                break;
            }
            case ACTION_FIND_DEVICE: {
                boolean start = intent.getBooleanExtra(EXTRA_FIND_START, false);
                mDeviceSupport.onFindDevice(start);
                break;
            }
            case ACTION_SET_TIME:
                mDeviceSupport.onSetTime();
                break;
            case ACTION_ENABLE_REALTIME_STEPS: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableRealtimeSteps(enable);
                break;
            }
            case ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableHeartRateSleepSupport(enable);
                break;
            }
            case ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT: {
                boolean enable = intent.getBooleanExtra(EXTRA_BOOLEAN_ENABLE, false);
                mDeviceSupport.onEnableRealtimeHeartRateMeasurement(enable);
                break;
            }
            case ACTION_SEND_CONFIGURATION: {
                String config = intent.getStringExtra(EXTRA_CONFIG);
                mDeviceSupport.onSendConfiguration(config);
                break;
            }
        }
        return START_STICKY;
    }

    private void setDeviceSupport(@Nullable IDeviceSupport deviceSupport) {
        if (deviceSupport != mDeviceSupport && mDeviceSupport != null) {
            mDeviceSupport.dispose();
            mDeviceSupport = null;
            mSciousDevice = null;
        }
        mDeviceSupport = deviceSupport;
        mSciousDevice = mDeviceSupport != null ? mDeviceSupport.getDevice() : null;
    }

    private void start() {
        if (!mStarted) {
            startForeground(Scious.NOTIFICATION_ID, Scious.createNotification(getString(R.string.scious_running), false, this));
            mStarted = true;
        }
    }

    private boolean isConnected() {
        return mSciousDevice != null && mSciousDevice.isConnected();
    }

    private boolean isConnecting() {
        return mSciousDevice != null && mSciousDevice.isConnecting();
    }

    private boolean isInitialized() {
        return mSciousDevice != null && mSciousDevice.isInitialized();
    }

    private void setReceiversEnableState(boolean enable, boolean initialized, IDeviceCoordinator coordinator) {
        Log.i(TAG, "Setting broadcast receivers to: " + enable);

        if (enable) {
            if (mBlueToothConnectReceiver == null) {
                mBlueToothConnectReceiver = new BluetoothConnectReceiver(this);
                registerReceiver(mBlueToothConnectReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            }
            if (mBlueToothPairingRequestReceiver == null) {
                mBlueToothPairingRequestReceiver = new BluetoothPairingRequestReceiver(this);
                registerReceiver(mBlueToothPairingRequestReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
            }
        } else {
            if (mBlueToothConnectReceiver != null) {
                unregisterReceiver(mBlueToothConnectReceiver);
                mBlueToothConnectReceiver = null;
            }
            if (mBlueToothPairingRequestReceiver != null) {
                unregisterReceiver(mBlueToothPairingRequestReceiver);
                mBlueToothPairingRequestReceiver = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (hasPrefs()) {
            getPrefs().getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
        Log.d(TAG, "DeviceCommunicationService is being destroyed");
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        setReceiversEnableState(false, false, null);

        setDeviceSupport(null);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Scious.NOTIFICATION_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SciousPrefs.AUTO_RECONNECT.equals(key)) {
            boolean autoReconnect = getSciousPrefs().getAutoReconnect();
            if (mDeviceSupport != null) {
                mDeviceSupport.setAutoReconnect(autoReconnect);
            }
        }
    }

    protected boolean hasPrefs() {
        return getPrefs().getPreferences() != null;
    }

    public Prefs getPrefs() {
        return SciousApplication.getPrefs();
    }

    public SciousPrefs getSciousPrefs() {
        return SciousApplication.getSciousPrefs();
    }

    public SciousDevice getSciousDevice() {
        return mSciousDevice;
    }
}
