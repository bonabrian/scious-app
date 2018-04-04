package id.bonabrian.scious.libraryservice.service.btle;

import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Transaction {
    private final String mName;
    private final List<BTLEAction> mActions = new ArrayList<>(4);
    private final long creationTimestamp = System.currentTimeMillis();

    private
    @Nullable
    IGattCallback gattCallback;

    public Transaction(String taskName) {
        this.mName = taskName;
    }

    public String getTaskName() {
        return mName;
    }

    public void add(BTLEAction action) {
        mActions.add(action);
    }

    public List<BTLEAction> getActions() {
        return Collections.unmodifiableList(mActions);
    }

    public boolean isEmpty() {
        return mActions.isEmpty();
    }

    protected String getCreationTime() {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date(creationTimestamp));
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s: Transaction task: %s with %d actions", getCreationTime(), getTaskName(), mActions.size());
    }

    public void setGattCallback(@Nullable IGattCallback callback) {
        gattCallback = callback;
    }

    public
    @Nullable
    IGattCallback getGattCallback() {
        return gattCallback;
    }
}
