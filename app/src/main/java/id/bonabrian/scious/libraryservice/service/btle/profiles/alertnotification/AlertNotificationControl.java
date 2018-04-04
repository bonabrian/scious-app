package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AlertNotificationControl {

    private AlertCategory category;
    private Command command;

    public void setCategory(AlertCategory category) {
        this.category = category;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public AlertCategory getCategory() {
        return category;
    }

    public Command getCommand() {
        return command;
    }

    /**
     * Returns the formatted message to be written to the alert notification control point
     * characteristic
     */
    public byte[] getControlMessage() {
        return new byte[] {
                BLETypeConversions.fromUint8(command.getId()),
                BLETypeConversions.fromUint8(category.getId())
        };
    }
}
