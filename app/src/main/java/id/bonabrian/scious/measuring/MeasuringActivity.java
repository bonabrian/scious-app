package id.bonabrian.scious.measuring;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.measuringresult.MeasuringResultActivity;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MeasuringActivity extends AppCompatActivity {
    public static int STATE_OFF = 0;
    public static int STATE_ON = 1;

    private int state;
    private SciousDevice mSciousDevice;
    @BindView(R.id.monitor_container)
    View overlay;
    @BindView(R.id.start_measuring)
    Button startMeasuring;
    @BindView(R.id.stop_measuring)
    Button stopMeasuring;
    @BindView(R.id.heart_rate_value)
    TextView heartRateValue;
    @BindView(R.id.rr_value)
    TextView rrValue;
//    @BindView(R.id.rmssd_value)
//    TextView rmssdValue;
    @BindView(R.id.countdown)
    TextView countDown;
    @BindView(R.id.recyclerview_result)
    RecyclerView recyclerView;

    //MeasuringAdapter adapter;

    private CountDownTimer countDownTimer;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;
    boolean isListeningHeartRate = false;
    private int heartRateBpm;
    private double rr = 0;

    private List<Double> rrList = new ArrayList<>();
    private List<Integer> heartList = new ArrayList<>();

    ScheduledExecutorService pulseScheduler;

    private static final String TAG = MeasuringActivity.class.getSimpleName();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case SciousDevice.ACTION_DEVICE_CHANGED:
                    SciousDevice device = intent.getParcelableExtra(SciousDevice.EXTRA_DEVICE);
                    break;
            }
        }
    };

//    private void setRecyclerView() {
//        adapter = new MeasuringAdapter(rrList, MeasuringActivity.this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring);
        ButterKnife.bind(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SciousDevice.ACTION_DEVICE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSciousDevice = extras.getParcelable(SciousDevice.EXTRA_DEVICE);
        } else {
            Log.e(TAG, "Must provide a device when invoking this");
            Toast.makeText(this, "Device not available " + extras.getParcelable(SciousDevice.EXTRA_DEVICE), Toast.LENGTH_LONG).show();
        }

        //setRecyclerView();

        String address = getDevice().getAddress();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        Log.d("Test", "Connecting to: " + address);
        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

        startMeasuring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableRealtimeTracking(true);
            }
        });
        stopMeasuring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivityPulse();
            }
        });
        stopMeasuring.animate().translationX((float) (stopMeasuring.getWidth())).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                stopMeasuring.setVisibility(View.VISIBLE);
            }
        });
    }

    private int getDuration() {
        return 60 * 1000;
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(getDuration(), 60) {
            @Override
            public void onTick(long l) {
                countDown.setText(String.valueOf(l / 1000));
            }

            @Override
            public void onFinish() {
                countDown.setText("60");
                //rrList.clear();
                //adapter.notifyDataSetChanged();
                Intent intent = new Intent(MeasuringActivity.this, MeasuringResultActivity.class);
                intent.putExtra("rmssd-result", countRmssd(rrList));
                intent.putExtra("sdnn-result", countSdnn(rrList));
                intent.putExtra("mean-rr-result", meanRR(rrList));
                intent.putExtra("mean-hr-result", meanHR(heartList));
                intent.putExtra("data-heartrate", (Serializable) heartList);
                intent.putExtra("data-rr", (Serializable) rrList);

                startActivity(intent);
                stopActivityPulse();
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void enableRealtimeTracking(boolean enable) {
        if (enable) {
            state = STATE_ON;
            redraw();
            if (this != null) {
                this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            pulseScheduler = startActivityPulse();
        } else {
            state = STATE_OFF;
            redraw();
            stopActivityPulse();
            if (this != null) {
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (countDownTimer != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure want to stop measurement?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            stopActivityPulse();
                            MeasuringActivity.this.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            super.onBackPressed();
        }
    }

    private ScheduledExecutorService startActivityPulse() {
        startCountdown();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Activity activity = MeasuringActivity.this;
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pulse();
                        }
                    });
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return service;
    }

    private void stopActivityPulse() {
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"));
        characteristic.setValue(new byte[]{21, 0x1, 0});
        bluetoothGatt.writeCharacteristic(characteristic);
        pulseScheduler.shutdownNow();
        pulseScheduler = null;
        if (countDownTimer != null) {
            heartList.clear();
            rrList.clear();
            //adapter.notifyDataSetChanged();
        }
        resetProgress();
        redraw();
    }

    private void resetProgress() {
        heartRateValue.setText("0");
        rrValue.setText("0");
        //rmssdValue.setText("0");
        //sdnnValue.setText("0");
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDown.setText("60");
        }
        state = STATE_OFF;
    }

    private void pulse() {
        startScanHeartRate();
    }

    private void redraw() {
        if (state == STATE_ON) {
            stopMeasuring.animate().translationX(0.0f).alpha(1.0f);
            startMeasuring.animate().translationX((float) startMeasuring.getWidth()).alpha(0.0f);
            return;
        }
        stopMeasuring.animate().translationX((float) (-stopMeasuring.getWidth())).alpha(0.0f);
        startMeasuring.animate().translationX(0.0f).alpha(1.0f);
    }

    private void startScanHeartRate() {
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"));
        characteristic.setValue(new byte[]{21, 0x1, 1});
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void listenHeartRate() {
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    private void stateConnected() {
        bluetoothGatt.discoverServices();
        Log.d(TAG, "Connected");
    }

    private void stateDisconnected() {
        try {
            bluetoothGatt.disconnect();
        } catch (Exception e) {
            Log.d(TAG, "Disconect ignoring: " + e);
        }
        Log.d(TAG, "Disconnected");
    }

    private double meanHR(List<Integer> listHR) {
        double sum = 0;
        for (int i = 0; i < listHR.size(); i++) {
            sum += (listHR.get(i));
        }
        return sum / listHR.size();
    }

    private double meanRR(List<Double> listRr) {
        double sum = 0;
        for (int i = 0; i < listRr.size(); i++) {
            sum += (listRr.get(i));
        }
        return sum / listRr.size();
    }

    private double countSdnn(List<Double> listRr) {
        double sum = 0;
        double mean;
        double sdnn = 0;
        for (int i = 0; i < listRr.size(); i++) {
            sum += (listRr.get(i));
        }
        mean = sum / listRr.size();
        for (int i = 1; i <= listRr.size(); i++) {
            sdnn += Math.pow((listRr.get(i - 1) - mean), 2);
        }
        return Math.sqrt(sdnn / (listRr.size() - 1));
    }

    private double countRmssd(List<Double> listRr) {
        double sum = 0;
        for (int i = 1; i < listRr.size(); i++) {
            sum += (listRr.get(i) - listRr.get(i - 1)) * (listRr.get(i) - listRr.get(i - 1));
        }
        return Math.sqrt(sum / (listRr.size() - 1));
    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                try {
                    gatt.close();
                } catch (Exception e) {
                    Log.d(TAG, "Close ignore: " + e);
                }
                stateDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            final byte[] data = characteristic.getValue();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    heartRateBpm = handleHeartRate(data);
                    heartRateValue.setText(String.valueOf(heartRateBpm));
                    rr = ((double) 60 / handleHeartRate(data));
                    DecimalFormat df = new DecimalFormat("#.####");
                    rrValue.setText(String.valueOf(df.format(rr)));

                    heartList.add(heartRateBpm);
                    rrList.add(rr);

                    for (int i = 0; i < heartList.size(); i++) {
                        Log.e("Test", "Data ke: " + i + ": " + heartList.get(i));
                    }
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data = characteristic.getValue();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    heartRateBpm = handleHeartRate(data);
                    heartRateValue.setText(String.valueOf(heartRateBpm));
                    rr = ((double) 60000 / handleHeartRate(data));
                    DecimalFormat df = new DecimalFormat("#.####");
                    rrValue.setText(String.valueOf(df.format(rr)));

                    heartList.add(heartRateBpm);
                    rrList.add(rr);

                    //sdnnValue.setText(String.valueOf(df.format(countSdnn(rrList))));
                    //rmssdValue.setText(String.valueOf(df.format(countRmssd(rrList))));

                    for (int i = 0; i < heartList.size(); i++) {
                        Log.e(TAG, "Data HR ke: " + i + ": " + heartList.get(i));
                    }
                    for (int i = 0; i < rrList.size(); i++) {
                        Log.e(TAG, "Data RR ke: " + i + ": " + rrList.get(i));
                    }
                    //adapter.notifyItemInserted(rrList.size());
                    Log.e(TAG, "RMSSD: " + countRmssd(rrList));
                    Log.e(TAG, "SDNN: " + countSdnn(rrList));
                    Log.e(TAG, "Mean HR: " + meanHR(heartList));
                }
            });
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private int handleHeartRate(byte[] value) {
        if (value.length == 2) {
            int hrValue = (value[1] & 0xff);
            return hrValue;
        }
        return 0;
    }

    private SciousDevice getDevice() {
        return mSciousDevice;
    }

}
