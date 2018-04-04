package id.bonabrian.scious.main.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.DeviceManager;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.model.BatteryState;
import id.bonabrian.scious.measuring.MeasuringActivity;
import id.bonabrian.scious.util.DeviceHelper;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private final Context context;
    private List<SciousDevice> deviceList;
    private int expandedDevicePosition = RecyclerView.NO_POSITION;
    private ViewGroup parent;

    public DeviceListAdapter(Context context, List<SciousDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_device_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SciousDevice device = deviceList.get(position);
        final IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device.isInitialized() || device.isConnected()) {
                    showTransientSnackbar(R.string.long_press_to_disconnect);
                } else {
                    showTransientSnackbar(R.string.connecting);
                    SciousApplication.deviceService().connect(device);
                }
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (device.getState() != SciousDevice.State.NOT_CONNECTED) {
                    showTransientSnackbar(R.string.disconnecting);
                    SciousApplication.deviceService().disconnect();
                }
                return true;
            }
        });

        holder.deviceNameLabel.setText(getUniqueDeviceName(device));

        if (device.isBusy()) {
            holder.deviceStatusLabel.setText(device.getBusyTask());
        } else {
            holder.deviceStatusLabel.setText(device.getStateString());
        }

        // Battery
        holder.batteryStatusBox.setVisibility(View.GONE);
        short batteryLevel = device.getBatteryLevel();

        if (batteryLevel != SciousDevice.BATTERY_UNKNOWN) {
            holder.batteryStatusBox.setVisibility(View.VISIBLE);
            holder.batteryStatusLabel.setText(device.getBatteryLevel() + "%");
            BatteryState batteryState = device.getBatteryState();
            if (BatteryState.BATTERY_CHARGING.equals(batteryState) || BatteryState.BATTERY_CHARGING_FULL.equals(batteryState)) {
                holder.batteryIcon.setImageLevel(device.getBatteryLevel() + 100);
            } else {
                holder.batteryIcon.setImageLevel(device.getBatteryLevel());
            }
        }

        // Show graphs
        holder.startMeasuring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device.isConnected() || device.isInitialized()) {
                    Intent intent;
                    intent = new Intent(context, MeasuringActivity.class);
                    intent.putExtra(SciousDevice.EXTRA_DEVICE, device);
                    context.startActivity(intent);
                } else {
                    showTransientSnackbar(R.string.device_not_connected);
                }
            }
        });

        // Send Vibration
        holder.sendVibrate.setVisibility(device.isInitialized() ? View.VISIBLE : View.GONE);
        holder.vibrateLabel.setVisibility(device.isInitialized() ? View.VISIBLE : View.GONE);
        holder.sendVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SciousApplication.deviceService().onFindDevice(true);
                Snackbar.make(parent, R.string.vibrate, Snackbar.LENGTH_INDEFINITE).setAction("Stop vibration", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SciousApplication.deviceService().onFindDevice(false);
                    }
                }).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        SciousApplication.deviceService().onFindDevice(false);
                        super.onDismissed(snackbar, event);
                    }
                }).show();
            }
        });

        // Remove device
        holder.removeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setTitle(context.getString(R.string.ask_delete_device, device.getName()))
                        .setMessage(R.string.delete_device_dialog_message)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);
                                    if (coordinator != null) {
                                        coordinator.deleteDevice(device);
                                    }
                                    DeviceHelper.getInstance().removeBond(device);
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error deleting device: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                } finally {
                                    Intent intent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        // Remove device
        holder.removeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setTitle(context.getString(R.string.ask_delete_device, device.getName()))
                        .setMessage(R.string.delete_device_dialog_message)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);
                                    if (coordinator != null) {
                                        coordinator.deleteDevice(device);
                                    }
                                    DeviceHelper.getInstance().removeBond(device);
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error deleting device: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                } finally {
                                    Intent intent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        DeviceInfoAdapter infoAdapter = new DeviceInfoAdapter(context, device.getDeviceInfos());
        infoAdapter.setHorizontalAlignment(true);
        holder.deviceInfoList.setAdapter(infoAdapter);
        justifyListViewHeightBasedOnChildren(holder.deviceInfoList);
        holder.deviceInfoList.setFocusable(false);

        final boolean detailsShown = position == expandedDevicePosition;
        boolean showInfoIcon = device.hasDeviceInfos() && !device.isBusy();
        holder.deviceInfoView.setVisibility(showInfoIcon ? View.VISIBLE : View.GONE);
        holder.deviceInfoBox.setActivated(detailsShown);
        holder.deviceInfoBox.setVisibility(detailsShown ? View.VISIBLE : View.GONE);
        holder.deviceInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandedDevicePosition = detailsShown ? -1 : position;
                TransitionManager.beginDelayedTransition(parent);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_device)
        CardView container;

        @BindView(R.id.device_name)
        TextView deviceNameLabel;
        @BindView(R.id.device_status)
        TextView deviceStatusLabel;

        @BindView(R.id.device_battery_status_box)
        LinearLayout batteryStatusBox;
        @BindView(R.id.battery_status)
        TextView batteryStatusLabel;
        @BindView(R.id.device_battery_status)
        ImageView batteryIcon;

        @BindView(R.id.device_measuring_box)
        LinearLayout deviceMeasuringBox;
        @BindView(R.id.device_measuring)
        ImageView startMeasuring;

        @BindView(R.id.device_info_image)
        ImageView deviceInfoView;
        @BindView(R.id.device_item_info_box)
        RelativeLayout deviceInfoBox;
        @BindView(R.id.device_item_info)
        ListView deviceInfoList;

        @BindView(R.id.send_vibration)
        ImageView sendVibrate;
        @BindView(R.id.device_vibrate_label)
        TextView vibrateLabel;
        @BindView(R.id.device_action_remove)
        ImageView removeDevice;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();

        if (adapter == null) {
            return;
        }

        ViewGroup viewGroup = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, viewGroup);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private String getUniqueDeviceName(SciousDevice device) {
        String deviceName = device.getName();
        if (!isUniqueDeviceName(device, deviceName)) {
            if (device.getModel() != null) {
                deviceName = deviceName + " " + device.getModel();
                if (!isUniqueDeviceName(device, deviceName)) {
                    deviceName = deviceName + " " + device.getShortAddress();
                }
            } else {
                deviceName = deviceName + " " + device.getShortAddress();
            }
        }
        return deviceName;
    }

    private boolean isUniqueDeviceName(SciousDevice device, String deviceName) {
        for (int i = 0; i < deviceList.size(); i++) {
            SciousDevice item = deviceList.get(i);
            if (item == device) {
                continue;
            }
            if (deviceName.equals(item.getName())) {
                return false;
            }
        }
        return true;
    }

    private void showTransientSnackbar(int resource) {
        Snackbar snackbar = Snackbar.make(parent, resource, Snackbar.LENGTH_SHORT);

        View snackbarView = snackbar.getView();

        int snackbarTextId = android.support.design.R.id.snackbar_text;
        TextView textView = (TextView) snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(context.getResources().getColor(R.color.white));
        snackbarView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

}
