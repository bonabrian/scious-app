package id.bonabrian.scious.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.main.MainActivity;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Scious {
    public static final int NOTIFICATION_ID = 1;
    public static String formatRssi(short rssi) {
        return String.valueOf(rssi);
    }

    public static Notification createNotification(String text, boolean connected, Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name))
                .setTicker(text)
                .setContentText(text)
                .setSmallIcon(connected ? R.drawable.ic_notification : R.drawable.ic_notification_disconnected)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        if (SciousApplication.isRunningLollipopOrLater()) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        if (SciousApplication.minimizeNotification()) {
            builder.setPriority(Notification.PRIORITY_MIN);
        }
        return builder.build();
    }

    public static boolean supportsBTLE() {
        return SciousApplication.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public static void updateNotification(String text, boolean connected, Context context) {
        Notification notification = createNotification(text, connected, context);
        // TODO
        updateNotification(notification, NOTIFICATION_ID, context);
    }

    private static void updateNotification(@Nullable Notification notification, int id, Context context) {
        if (notification == null) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }
}
