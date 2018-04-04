package id.bonabrian.scious.util;

import android.content.Context;
import android.support.annotation.NonNull;

import id.bonabrian.scious.libraryservice.model.NotificationSpec;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class NotificationUtils {
    @NonNull
    public static String getPreferredTextFor(NotificationSpec notificationSpec, int lengthBody, int lengthSubject, Context context) {
        switch (notificationSpec.type) {
            case RIOT:
            case SIGNAL:
            case TELEGRAM:
            case TWITTER:
            case WHATSAPP:
            case CONVERSATIONS:
            case FACEBOOK:
        }
        return "";
    }
}
