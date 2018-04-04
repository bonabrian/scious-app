package id.bonabrian.scious.discovery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.util.Scious;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceCandidateAdapter extends ArrayAdapter<SciousDeviceCandidate> {

    @BindView(R.id.item_name)
    TextView deviceNameLabel;
    @BindView(R.id.item_address)
    TextView deviceAddressLabel;
    private final Context context;

    public DeviceCandidateAdapter(Context context, List<SciousDeviceCandidate> deviceCandidates) {
        super(context, 0, deviceCandidates);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        SciousDeviceCandidate device = getItem(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_device_candidate, parent, false);
            ButterKnife.bind(this, view);
        }

        String name = formatDeviceCandidate(device);
        deviceNameLabel.setText(name);
        deviceAddressLabel.setText(device.getMacAddress());

        return view;
    }

    private String formatDeviceCandidate(SciousDeviceCandidate device) {
        if (device.getRssi() > SciousDevice.RSSI_UNKNOWN) {
            return context.getString(R.string.device_with_rssi, device.getName(), Scious.formatRssi(device.getRssi()));
        }
        return device.getName();
    }
}
