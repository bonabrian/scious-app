package id.bonabrian.scious.main.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.discovery.DiscoveryActivity;
import id.bonabrian.scious.libraryservice.device.DeviceManager;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.util.Scious;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    View view;

    private DeviceManager deviceManager;
    private List<SciousDevice> deviceList;
    private DeviceListAdapter deviceListAdapter;
    @BindView(R.id.no_device)
    TextView noDeviceText;
    @BindView(R.id.device_list)
    RecyclerView deviceListView;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DeviceManager.ACTION_DEVICE_CHANGED:
                    refreshPairedDevices();
                    break;
                case SciousApplication.ACTION_QUIT:
                    getActivity().finish();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        deviceManager = ((SciousApplication) getActivity().getApplication()).getDeviceManager();
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        deviceList = deviceManager.getDevices();
        deviceListAdapter = new DeviceListAdapter(getActivity(), deviceList);
        deviceListView.setAdapter(this.deviceListAdapter);

        registerForContextMenu(deviceListView);

        refreshPairedDevices();

        return view;
    }

    private void refreshPairedDevices() {
        List<SciousDevice> deviceList = deviceManager.getDevices();
        if (deviceList.isEmpty()) {
            noDeviceText.setVisibility(View.VISIBLE);
        } else {
            noDeviceText.setVisibility(View.INVISIBLE);
        }
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceManager.ACTION_DEVICE_CHANGED);
        filter.addAction(SciousApplication.ACTION_QUIT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
        SciousApplication.deviceService().start();
        if (deviceList.isEmpty() && Scious.isBluetoothEnabled() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(getActivity(), DiscoveryActivity.class));
        } else {
            SciousApplication.deviceService().requestDeviceInfo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        unregisterForContextMenu(deviceListView);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
