package id.bonabrian.miservice.device;

import android.os.Parcelable;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IDeviceInfo extends Parcelable, Comparable<IDeviceInfo> {
    String getName();
    String getDetails();
    boolean equals(Object other);
}
