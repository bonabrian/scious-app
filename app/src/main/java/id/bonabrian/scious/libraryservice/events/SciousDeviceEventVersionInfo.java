package id.bonabrian.scious.libraryservice.events;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousDeviceEventVersionInfo extends SciousDeviceEvent {
    public String fwVersion = SciousApplication.getContext().getString(R.string.n_a);
    public String hwVersion = SciousApplication.getContext().getString(R.string.n_a);
}
