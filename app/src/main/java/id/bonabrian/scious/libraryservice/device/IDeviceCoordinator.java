package id.bonabrian.scious.libraryservice.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.libraryservice.model.DeviceType;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IDeviceCoordinator {
    String EXTRA_DEVICE_CANDIDATE = "id.bonabrian.scious.impl.SciousDeviceCandidate.EXTRA_DEVICE_CANDIDATE";
    int BONDING_STYLE_NONE = 0;
    int BONDING_STYLE_BOND = 1;
    int BONDING_STYLE_ASK = 2;

    @NonNull
    DeviceType getSupportedType(SciousDeviceCandidate candidate);

    boolean supports(SciousDeviceCandidate candidate);

    boolean supports(SciousDevice device);

    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    Collection<? extends ScanFilter> createBLEScanFilters();

    SciousDevice createDevice(SciousDeviceCandidate candidate);

    void deleteDevice(SciousDevice device) throws Exception;

    DeviceType getDeviceType();

    @Nullable
    Class<? extends Activity> getPairingActivity();

    @Nullable
    Class<? extends Activity> getPrimaryActivity();

    boolean supportsActivityDataFetching();

    boolean supportsActivityTracking();

    boolean allowFetchActivityData(SciousDevice device);

    // TODO get samples
    //SampleProvider<? extends ActivitySample> getSampleProvider(SciousDevice device, DaoSession session);

    boolean supportsHeartRateMeasurement(SciousDevice device);

    String getManufacturer();

    int getBondingStyle(SciousDevice device);

    boolean supportsRealtimeData();
}
