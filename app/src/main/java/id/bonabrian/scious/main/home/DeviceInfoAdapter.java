package id.bonabrian.scious.main.home;

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
import id.bonabrian.scious.libraryservice.model.IDeviceInfo;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceInfoAdapter extends ArrayAdapter<IDeviceInfo> {

    private final Context context;
    private boolean horizontalAlignment;

    @BindView(R.id.item_info_name)
    TextView nameView;
    @BindView(R.id.item_info_details)
    TextView detailsView;

    public DeviceInfoAdapter(Context context, List<IDeviceInfo> items) {
        super(context, 0, items);

        this.context = context;
    }

    public void setHorizontalAlignment(boolean horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @NonNull
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        IDeviceInfo item = getItem(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (horizontalAlignment) {
                view = inflater.inflate(R.layout.adapter_device_info, parent, false);
            }
            ButterKnife.bind(this, view);
        }

        nameView.setText(item.getName());
        detailsView.setText(item.getDetails());

        return view;
    }
}
