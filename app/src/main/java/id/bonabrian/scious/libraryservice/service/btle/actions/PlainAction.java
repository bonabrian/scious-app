package id.bonabrian.scious.libraryservice.service.btle.actions;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class PlainAction extends BTLEAction {

    public PlainAction() {
        super(null);
    }

    @Override
    public boolean expectsResult() {
        return false;
    }

    @Override
    public String toString() {
        return getCreationTime() + ": " + getClass().getSimpleName();
    }
}
