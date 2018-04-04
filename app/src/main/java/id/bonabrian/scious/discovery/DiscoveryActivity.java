package id.bonabrian.scious.discovery;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jpardogo.android.googleprogressbar.library.ChromeFloatingCirclesDrawable;
import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.AbsSciousActivity;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.libraryservice.model.DeviceType;
import id.bonabrian.scious.util.AndroidUtils;
import id.bonabrian.scious.util.DeviceHelper;
import id.bonabrian.scious.util.Scious;

import static android.bluetooth.le.ScanSettings.MATCH_MODE_STICKY;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DiscoveryActivity extends AbsSciousActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = DiscoveryActivity.class.getSimpleName();

    private static final long SCAN_DURATION = 60000; // 60s

    private ScanCallback newLeScanCallback = null;

    private final Handler handler = new Handler();

    private BluetoothAdapter adapter;
    private final ArrayList<SciousDeviceCandidate> deviceCandidates = new ArrayList<>();
    private DeviceCandidateAdapter candidateListAdapter;
    @BindView(R.id.btn_discovery)
    Button startButton;
    @BindView(R.id.discovery_progressbar)
    GoogleProgressBar progressBar;
    private Scanning isScanning = Scanning.SCANNING_OFF;
    private SciousDeviceCandidate bondingDevice;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    if (isScanning != Scanning.SCANNING_BTLE && isScanning != Scanning.SCANNING_NEW_BTLE) {
                        discoveryStarted(Scanning.SCANNING_BT);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isScanning == Scanning.SCANNING_BT) {
                                checkAndRequestLocationPermission();
                                if (SciousApplication.isRunningLollipopOrLater()) {
                                    startDiscovery(Scanning.SCANNING_NEW_BTLE);
                                } else {
                                    startDiscovery(Scanning.SCANNING_BTLE);
                                }
                            } else {
                                discoveryFinished();
                            }
                        }
                    });
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int oldState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);
                    int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    bluetoothStateChanged(oldState, newState);
                    break;
                case BluetoothDevice.ACTION_FOUND: {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, SciousDevice.RSSI_UNKNOWN);
                    handleDeviceFound(device, rssi);
                    break;
                }
                case BluetoothDevice.ACTION_UUID: {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, SciousDevice.RSSI_UNKNOWN);
                    Parcelable[] uuids1 = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    ParcelUuid[] uuids2 = AndroidUtils.toParcelUUids(uuids1);
                    handleDeviceFound(device, rssi, uuids2);
                    break;
                }
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && bondingDevice != null && device.getAddress().equals(bondingDevice.getMacAddress())) {
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                        if (bondState == BluetoothDevice.BOND_BONDED) {
                            handleDeviceBonded();
                        }
                    }
                }
            }
        }
    };

    private enum Scanning {
        SCANNING_BT,
        SCANNING_BTLE,
        SCANNING_NEW_BTLE,
        SCANNING_OFF
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_discovery);
        ButterKnife.bind(this);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartButtonClick(startButton);
            }
        });

        progressBar.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(this).build());
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        ListView deviceCandidatesView = (ListView) findViewById(R.id.discovery_device_candidates);

        candidateListAdapter = new DeviceCandidateAdapter(this, deviceCandidates);
        deviceCandidatesView.setAdapter(candidateListAdapter);
        deviceCandidatesView.setOnItemClickListener(this);

        IntentFilter bluetoothIntent = new IntentFilter();
        bluetoothIntent.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothIntent.addAction(BluetoothDevice.ACTION_UUID);
        bluetoothIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(bluetoothReceiver, bluetoothIntent);

        startDiscovery();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("deviceCandidates", deviceCandidates);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Parcelable> restoredCandidates = savedInstanceState.getParcelableArrayList("deviceCandidates");
        if (restoredCandidates != null) {
            deviceCandidates.clear();
            for (Parcelable p : restoredCandidates) {
                deviceCandidates.add((SciousDeviceCandidate) p);
            }
        }
    }

    public void onStartButtonClick(View button) {
        Log.d(TAG, "Discovery Button clicked");
        if (isScanning()) {
            stopDiscovery();
        } else {
            startDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Tried to unregister Bluetooth Receiver that wasn't registered");
        }
        super.onDestroy();
    }

    private void handleDeviceFound(BluetoothDevice device, short rssi) {
        ParcelUuid[] uuids = device.getUuids();
        if (uuids == null) {
            if (device.fetchUuidsWithSdp()) {
                return;
            }
        }
        handleDeviceFound(device, rssi, uuids);
    }

    private void handleDeviceFound(BluetoothDevice device, short rssi, ParcelUuid[] uuids) {
        Log.d(TAG, "Found device: " + device.getName() + ", " + device.getAddress());
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            return; // already bonded
        }
        SciousDeviceCandidate candidate = new SciousDeviceCandidate(device, rssi, uuids);
        DeviceType deviceType = DeviceHelper.getInstance().getSupportedType(candidate);
        if (deviceType.isSupported()) {
            candidate.setDeviceType(deviceType);
            Log.i(TAG, "Recognized supported device: " + candidate);
            int index = deviceCandidates.indexOf(candidate);
            if (index >= 0) {
                deviceCandidates.set(index, candidate);
            } else {
                deviceCandidates.add(candidate);
            }
            candidateListAdapter.notifyDataSetChanged();
        }
    }

    private void startDiscovery() {
        if (isScanning()) {
            Log.w(TAG, "Already scanning");
            return;
        }
        startDiscovery(Scanning.SCANNING_BT);
    }

    private void startDiscovery(Scanning what) {
        Log.i(TAG, "Starting discovery: " + what);
        discoveryStarted(what);
        if (ensureBluetoothReady()) {
            if (what == Scanning.SCANNING_BT) {
                startBTDiscovery();
            } else if (what == Scanning.SCANNING_BTLE) {
                if (Scious.supportsBTLE()) {
                    startBLTEDiscovery();
                } else {
                    discoveryFinished();
                }
            } else if (what == Scanning.SCANNING_NEW_BTLE) {
                if (Scious.supportsBTLE()) {
                    startNewBTLEDiscovery();
                } else {
                    discoveryFinished();
                }
            }
        } else {
            discoveryFinished();
            Toast.makeText(DiscoveryActivity.this, getString(R.string.bluetooth_is_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isScanning() {
        return isScanning != Scanning.SCANNING_OFF;
    }

    private void stopDiscovery() {
        Log.i(TAG, "Stopping discovery");
        if (isScanning()) {
            Scanning wasScaning = isScanning;
            discoveryFinished();

            if (wasScaning == Scanning.SCANNING_BT) {
                stopBTDiscovery();
            } else if (wasScaning == Scanning.SCANNING_BTLE) {
                stopBTLEDiscovery();
            } else if (wasScaning == Scanning.SCANNING_NEW_BTLE) {
                stopNewBTLEDiscovery();
            }
            handler.removeMessages(0, stopRunnable);
        }
    }

    private void stopBTLEDiscovery() {
        adapter.stopLeScan(leScanCallback);
    }

    private void stopBTDiscovery() {
        adapter.cancelDiscovery();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopNewBTLEDiscovery() {
        adapter.getBluetoothLeScanner().stopScan(newLeScanCallback);
    }

    private void bluetoothStateChanged(int oldState, int newState) {
        discoveryFinished();
        if (newState == BluetoothAdapter.STATE_ON) {
            this.adapter = BluetoothAdapter.getDefaultAdapter();
            startButton.setEnabled(true);
        } else {
            this.adapter = null;
            startButton.setEnabled(false);
        }
    }

    private void discoveryFinished() {
        isScanning = Scanning.SCANNING_OFF;
        progressBar.setVisibility(View.GONE);
        startButton.setText(getString(R.string.start_scanning));
    }

    private void discoveryStarted(Scanning what) {
        isScanning = what;
        progressBar.setVisibility(View.VISIBLE);
        startButton.setText(getString(R.string.stop_scanning));
    }

    private boolean ensureBluetoothReady() {
        boolean available = checkBluetoothAvailable();
        startButton.setEnabled(available);
        if (available) {
            adapter.cancelDiscovery();
            return true;
        }
        return false;
    }

    private boolean checkBluetoothAvailable() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.w(TAG, "No bluetooth available");
            this.adapter = null;
            return false;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            Log.w(TAG, "No bluetooth available");
            this.adapter = null;
            return false;
        }
        if (!adapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is disabled");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
            this.adapter = null;
            return false;
        }
        this.adapter = adapter;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startNewBTLEDiscovery() {
        Log.i(TAG, "Start New BTLE Discovery");
        handler.removeMessages(0, stopRunnable);
        handler.sendMessageDelayed(getPostMessage(stopRunnable), SCAN_DURATION);
        adapter.getBluetoothLeScanner().startScan(getScanFilters(), getScanSettings(), getScanCallback());
    }

    private List<ScanFilter> getScanFilters() {
        List<ScanFilter> allFilters = new ArrayList<>();
        for (IDeviceCoordinator coordinator : DeviceHelper.getInstance().getAllCoordinators()) {
            allFilters.addAll(coordinator.createBLEScanFilters());
        }
        return allFilters;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanSettings getScanSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ScanSettings.Builder()
                    .setScanMode(SCAN_MODE_LOW_LATENCY)
                    .setMatchMode(MATCH_MODE_STICKY)
                    .build();
        } else {
            return new ScanSettings.Builder()
                    .setScanMode(SCAN_MODE_LOW_LATENCY)
                    .build();
        }
    }

    private void startBLTEDiscovery() {
        Log.i(TAG, "Starting BTLE Discovery");
        handler.removeMessages(0, stopRunnable);
        handler.sendMessageDelayed(getPostMessage(stopRunnable), SCAN_DURATION);
        adapter.startLeScan(leScanCallback);
    }

    private void startBTDiscovery() {
        Log.i(TAG, "Starting BT Discovery");
        handler.removeMessages(0, stopRunnable);
        handler.sendMessageDelayed(getPostMessage(stopRunnable), SCAN_DURATION);
        adapter.startDiscovery();
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private Message getPostMessage(Runnable runnable) {
        Message m = Message.obtain(handler, runnable);
        m.obj = runnable;
        return m;
    }

    private void connectAndFinish(SciousDevice device) {
        Toast.makeText(DiscoveryActivity.this, getString(R.string.discovery_trying_to_connect_to, device.getName()), Toast.LENGTH_SHORT).show();
        SciousApplication.deviceService().connect(device, true);
        finish();
    }

    private void createBond(final SciousDeviceCandidate deviceCandidate, int bondingStyle) {
        if (bondingStyle == IDeviceCoordinator.BONDING_STYLE_NONE) {
            return;
        }
        if (bondingStyle == IDeviceCoordinator.BONDING_STYLE_ASK) {
//            new AlertDialog.Builder(this)
//                    .setCancelable(true)
//                    .setTitle(DiscoveryActivity.this.getString(R.string.discovery_pair_title, deviceCandidate.getName()))
//                    .setMessage(DiscoveryActivity.this.getString(R.string.discovery_pair_question))
//                    .setPositiveButton(DiscoveryActivity.this.getString(R.string.discovery_yes_pair), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            doCreatePair(deviceCandidate);
//                        }
//                    })
//                    .setNegativeButton(R.string.discovery_dont_pair, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            SciousDevice device = DeviceHelper.getInstance().toSupportedDevice(deviceCandidate);
//                            connectAndFinish(device);
//                        }
//                    })
//                    .show();
        } else {
            doCreatePair(deviceCandidate);
        }
    }

    private void doCreatePair(SciousDeviceCandidate deviceCandidate) {
        Toast.makeText(DiscoveryActivity.this, getString(R.string.discovery_attempting_to_pair, deviceCandidate.getName()), Toast.LENGTH_SHORT).show();
        if (deviceCandidate.getDevice().createBond()) {
            Log.i(TAG, "Bonding in progress...");
            bondingDevice = deviceCandidate;
        } else {
            Toast.makeText(DiscoveryActivity.this, getString(R.string.discovery_bonding_failed_immediately, deviceCandidate.getName()), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDeviceBonded() {
        Toast.makeText(DiscoveryActivity.this, getString(R.string.discovery_successfully_bonded, bondingDevice.getName()), Toast.LENGTH_SHORT).show();
        SciousDevice device = DeviceHelper.getInstance().toSupportedDevice(bondingDevice);
        connectAndFinish(device);
    }

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.w(TAG, device.getName() + ": " + ((scanRecord != null) ? scanRecord.length : -1));
            logMessageContent(scanRecord);
            handleDeviceFound(device, (short) rssi);
        }
    };

    private ScanCallback getScanCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            newLeScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    try {
                        ScanRecord scanRecord = result.getScanRecord();
                        ParcelUuid[] uuids = null;
                        if (scanRecord != null) {
                            List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();
                            if (serviceUuids != null) {
                                uuids = serviceUuids.toArray(new ParcelUuid[0]);
                            }
                        }
                        Log.w(TAG, result.getDevice().getName() + ": " + ((scanRecord != null) ? scanRecord.getBytes().length : -1));
                        handleDeviceFound(result.getDevice(), (short) result.getRssi(), uuids);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Error handling scan result", e);
                    }
                }
            };
        }
        return newLeScanCallback;
    }

    public void logMessageContent(byte[] value) {
        if (value != null) {
            for (byte b : value) {
                Log.w(TAG, "DATA: " + String.format("0x%2x", b) + " - " + (char) (b & 0xff));
            }
        }
    }

    private final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopDiscovery();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        SciousDeviceCandidate deviceCandidate = deviceCandidates.get(position);
        if (deviceCandidate == null) {
            Log.e(TAG, "Device candidate clicked, but item not found");
            return;
        }

        stopDiscovery();
        IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(deviceCandidate);
        Log.i(TAG, "Using device candidate " + deviceCandidate + " with coordinator: " + coordinator.getClass());
        Class<? extends Activity> pairingActivity = coordinator.getPairingActivity();
        if (pairingActivity != null) {
            Intent intent = new Intent(this, pairingActivity);
            intent.putExtra(IDeviceCoordinator.EXTRA_DEVICE_CANDIDATE, deviceCandidate);
            startActivity(intent);
        } else {
            SciousDevice device = DeviceHelper.getInstance().toSupportedDevice(deviceCandidate);
            int bondingStyle = coordinator.getBondingStyle(device);
            if (bondingStyle == IDeviceCoordinator.BONDING_STYLE_NONE) {
                Log.i(TAG, "No bonding needed, connecting right away");
                connectAndFinish(device);
                return;
            }
            try {
                BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(deviceCandidate.getMacAddress());
                switch (bluetoothDevice.getBondState()) {
                    case BluetoothDevice.BOND_NONE: {
                        createBond(deviceCandidate, bondingStyle);
                        break;
                    }
                    case BluetoothDevice.BOND_BONDING:
                        bondingDevice = deviceCandidate;
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        handleDeviceBonded();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error pairing device " + deviceCandidate.getMacAddress());
            }
        }
    }
}
