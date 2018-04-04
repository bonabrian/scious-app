package id.bonabrian.miservice.util;

import android.os.ParcelUuid;
import android.os.Parcelable;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AndroidUtils {
    public static ParcelUuid[] toParcelUUids(Parcelable[] uuids) {
        if (uuids == null) {
            return null;
        }
        ParcelUuid[] uuids2 = new ParcelUuid[uuids.length];
        System.arraycopy(uuids, 0, uuids2, 0, uuids.length);
        return uuids2;
    }
}
