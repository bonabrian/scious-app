package id.bonabrian.scious.libraryservice.device.miband2;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AbsInfo {
    protected final byte[] mData;

    public AbsInfo(byte[] data) {
        mData = new byte[data.length];
        System.arraycopy(data, 0, mData, 0, data.length);
    }
}
