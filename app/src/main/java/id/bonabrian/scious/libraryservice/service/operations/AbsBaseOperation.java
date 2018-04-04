package id.bonabrian.scious.libraryservice.service.operations;

import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Support;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsBaseOperation extends AbsMiBand2Operation<MiBand2Support> {

    protected AbsBaseOperation(MiBand2Support support) {
        super(support);
    }

    @Override
    protected void enableOtherNotifications(TransactionBuilder builder, boolean enable) {
        // TODO: check which notifications we should disable and re-enable here
        //builder.notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_REALTIME_STEPS), enable)
        //.notify(getCharacteristic(MiBandService.UUID_CHARACTERISTIC_SENSOR_DATA), enable);
    }
}
