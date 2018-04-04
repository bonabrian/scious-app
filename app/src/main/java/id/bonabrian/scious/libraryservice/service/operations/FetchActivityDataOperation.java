package id.bonabrian.scious.libraryservice.service.operations;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Service;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Support;
import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.actions.WaitAction;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class FetchActivityDataOperation extends AbsFetchOperation {

    public FetchActivityDataOperation(MiBand2Support support) {
        super(support);
    }

    @Override
    protected void startFetching(TransactionBuilder builder) {
        GregorianCalendar sinceWhen = getLastSuccessfulSyncTime();
        builder.write(characteristicFetch, BLETypeConversions.join(new byte[] { MiBand2Service.COMMAND_ACTIVITY_DATA_START_DATE, MiBand2Service.COMMAND_ACTIVITY_DATA_TYPE_ACTIVTY }, getSupport().getTimeBytes(sinceWhen, TimeUnit.MINUTES)));
        builder.add(new WaitAction(1000)); // TODO: actually wait for the success-reply
        builder.notify(characteristicActivityData, true);
        builder.write(characteristicFetch, new byte[] { MiBand2Service.COMMAND_FETCH_ACTIVITY_DATA });
    }

    protected void handleActivityFetchFinish() {
//        GregorianCalendar lastSyncTimestamp = saveSamples();
//        if (lastSyncTimestamp != null && needsAnotherFetch(lastSyncTimestamp)) {
//            try {
//                startFetching();
//                return;
//            } catch (IOException ex) {
//                Log.e("FetchActivityDataOperation", "Error starting another round of fetching activity data", ex);
//            }
//        }

        super.handleActivityFetchFinish();
    }

    @Override
    protected String getLastSyncTimeKey() {
        return null;
    }

    @Override
    protected void handleActivityNotif(byte[] value) {

    }

    @Override
    protected void bufferActivityData(byte[] value) {

    }
}
