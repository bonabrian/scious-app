package id.bonabrian.scious.libraryservice.service.operations;

import android.widget.Toast;

import java.io.IOException;

import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEOperation;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsMiBand2Operation<T extends AbsBTLEDeviceSupport> extends AbsBTLEOperation<T> {

    protected AbsMiBand2Operation(T support) {
        super(support);
    }

    @Override
    protected void prePerform() throws IOException {
        super.prePerform();
        getDevice().setBusyTask("Operation starting...");
        TransactionBuilder builder = performInitialized("Disabling some notifications");
        enableOtherNotifications(builder, false);
        enableNeededNotifications(builder, true);
        builder.queue(getQueue());
    }

    @Override
    protected void operationFinished() {
        operationStatus = OperationStatus.FINISHED;
        if (getDevice() != null && getDevice().isConnected()) {
            unsetBusy();
            try {
                TransactionBuilder builder = performInitialized("reenabling disabled notifications");
                handleFinished(builder);
                builder.setGattCallback(null); // unset ourselves from being the queue's gatt callback
                builder.queue(getQueue());
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error enabling Mi Band 2 notifications, you may need to connect and disconnect", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleFinished(TransactionBuilder builder) {
        enableNeededNotifications(builder, false);
        enableOtherNotifications(builder, true);
    }

    protected abstract void enableNeededNotifications(TransactionBuilder builder, boolean enable);

    protected abstract void enableOtherNotifications(TransactionBuilder builder, boolean enable);
}
