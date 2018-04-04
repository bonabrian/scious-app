package id.bonabrian.scious.pairing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import id.bonabrian.scious.AbsSciousActivity;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.discovery.DiscoveryActivity;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Coordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.util.AndroidUtils;
import id.bonabrian.scious.util.DeviceHelper;
import id.bonabrian.scious.util.Prefs;

import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.PREF_MIBAND2_DISPLAY_ITEMS;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DevicePairingActivity extends AbsSciousActivity {
    private static final String TAG = DevicePairingActivity.class.getSimpleName();

    private static final int REQ_CODE_USER_SETTINGS = 52;
    private static final String STATE_DEVICE_CANDIDATE = "stateDeviceCandidate";
    private static final long DELAY_AFTER_BONDING = 1000; // 1s
    private boolean isPairing;
    private SciousDeviceCandidate deviceCandidate;
    private String bondingMacAddress;

    private final BroadcastReceiver mPairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SciousDevice.ACTION_DEVICE_CHANGED.equals(intent.getAction())) {
                SciousDevice device = intent.getParcelableExtra(SciousDevice.EXTRA_DEVICE);
                Log.d(TAG, "pairing activity: device changed: " + device);
                if (deviceCandidate.getMacAddress().equals(device.getAddress())) {
                    if (device.isInitialized()) {
                        pairingFinished(true, deviceCandidate);
                    } else if (device.isConnecting() || device.isInitializing()) {
                        Log.i(TAG, "still connecting/initializing device...");
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mBondingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "Bond state changed: " + device + ", state: " + device.getBondState() + ", expected address: " + bondingMacAddress);
                if (bondingMacAddress != null && bondingMacAddress.equals(device.getAddress())) {
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "Bonded with " + device.getAddress());
                        bondingMacAddress = null;
                        attemptToConnect();
                    } else if (bondState == BluetoothDevice.BOND_BONDING) {
                        Log.i(TAG, "Bonding in progress with " + device.getAddress());
                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        Log.i(TAG, "Not bonded with " + device.getAddress() + ", attempting to connect anyway.");
                        bondingMacAddress = null;
                        attemptToConnect();
                    } else {
                        Log.w(TAG, "Unknown bond state for device " + device.getAddress() + ": " + bondState);
                        pairingFinished(false, deviceCandidate);
                    }
                }
            }
        }
    };

    private void attemptToConnect() {
        Looper mainLooper = Looper.getMainLooper();
        new Handler(mainLooper).postDelayed(new Runnable() {
            @Override
            public void run() {
                performApplicationLevelPair();
            }
        }, DELAY_AFTER_BONDING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pairing);

        Intent intent = getIntent();
        deviceCandidate = intent.getParcelableExtra(IDeviceCoordinator.EXTRA_DEVICE_CANDIDATE);
        if (deviceCandidate == null && savedInstanceState != null) {
            deviceCandidate = savedInstanceState.getParcelable(STATE_DEVICE_CANDIDATE);
        }
        if (deviceCandidate == null) {
            Toast.makeText(this, getString(R.string.cannot_pair_no_mac), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DiscoveryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            return;
        }

        if (MiBand2Coordinator.getValidateUserInfo(deviceCandidate.getMacAddress())) {
            Intent userIntent = new Intent(this, MiBand2PreferencesActivity.class);
            startActivityForResult(userIntent, REQ_CODE_USER_SETTINGS, null);
            return;
        }
        startPairing();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DEVICE_CANDIDATE, deviceCandidate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        deviceCandidate = savedInstanceState.getParcelable(STATE_DEVICE_CANDIDATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_USER_SETTINGS) {
//            if (!MiBand2Coordinator.getValidateUserInfo(deviceCandidate.getMacAddress())) {
//                Toast.makeText(this, "Was saved with dummy user data", Toast.LENGTH_LONG).show();
//            }
            startPairing();
        }
    }

    @Override
    protected void onDestroy() {
        AndroidUtils.safeUnregisterBroadcastReceiver(LocalBroadcastManager.getInstance(this), mPairingReceiver);
        AndroidUtils.safeUnregisterBroadcastReceiver(this, mBondingReceiver);
        if (isPairing) {
            stopPairing();
        }
        super.onDestroy();
    }

    private void startPairing() {
        isPairing = true;

        IntentFilter filter = new IntentFilter(SciousDevice.ACTION_DEVICE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPairingReceiver, filter);
        if (!shouldSetupBTLevelPairing()) {
            attemptToConnect();
            return;
        }
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondingReceiver, filter);

        performBluetoothPair(deviceCandidate);
    }

    private boolean shouldSetupBTLevelPairing() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getPreferences().getBoolean(MiBand2Const.PREF_MIBAND2_SETUP_BT_PAIRING, true);
    }

    private void pairingFinished(boolean pairedSuccessfully, SciousDeviceCandidate candidate) {
        Log.d(TAG, "pairingFinished: " + pairedSuccessfully);
        if (!isPairing) {
            return;
        }

        isPairing = false;
        AndroidUtils.safeUnregisterBroadcastReceiver(LocalBroadcastManager.getInstance(this), mPairingReceiver);
        AndroidUtils.safeUnregisterBroadcastReceiver(this, mBondingReceiver);

        if (pairedSuccessfully) {
            String macAddress = deviceCandidate.getMacAddress();
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
            if (device != null && device.getBondState() == BluetoothDevice.BOND_NONE) {
                Prefs prefs = SciousApplication.getPrefs();
                prefs.getPreferences().edit().putString(MiBand2Const.PREF_MIBAND2_ADDRESS, macAddress).apply();
            }
            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void stopPairing() {
        isPairing = false;
    }

    protected void performBluetoothPair(SciousDeviceCandidate deviceCandidate) {
        BluetoothDevice device = deviceCandidate.getDevice();

        int bondState = device.getBondState();
        if (bondState == BluetoothDevice.BOND_BONDED) {
            Toast.makeText(this, getString(R.string.pairing_already_bonded, device.getName()), Toast.LENGTH_SHORT).show();
            performApplicationLevelPair();
            return;
        }

        bondingMacAddress = device.getAddress();
        if (bondState == BluetoothDevice.BOND_BONDING) {
            Toast.makeText(this, getString(R.string.pairing_in_progress, device.getName()), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.pairing_creating_bond_with, device.getName(), bondingMacAddress), Toast.LENGTH_LONG).show();
        if (!device.createBond()) {
            Toast.makeText(this, getString(R.string.pairing_unable_to_pair, device.getName()), Toast.LENGTH_LONG).show();
        }
    }

    private void performApplicationLevelPair() {
        SciousApplication.deviceService().disconnect();
        SciousDevice device = DeviceHelper.getInstance().toSupportedDevice(deviceCandidate);
        if (device != null) {
            SciousApplication.deviceService().connect(device, true);
        } else {
            Toast.makeText(this, "Unable to connect, can't recognize the device type: " + deviceCandidate, Toast.LENGTH_LONG).show();
        }
    }
}
