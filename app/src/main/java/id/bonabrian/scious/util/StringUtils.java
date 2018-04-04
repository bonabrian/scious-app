package id.bonabrian.scious.util;

import android.support.annotation.NonNull;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class StringUtils {
    @NonNull
    public static String truncate(String s, int maxLength){
        if (s == null) {
            return "";
        }

        int length = Math.min(s.length(), maxLength);
        if(length < 0) {
            return "";
        }

        return s.substring(0, length);
    }

    public static String pad(String s, int length){
        return pad(s, length, ' ');
    }

    public static String pad(String s, int length, char padChar){
        while(s.length() < length) {
            s += padChar;
        }
        return s;
    }

    @NonNull
    public static StringBuilder join(String separator, String... elements) {
        StringBuilder builder = new StringBuilder();
        if (elements == null) {
            return builder;
        }
        boolean hasAdded = false;
        for (String element : elements) {
            if (element != null && element.length() > 0) {
                if (hasAdded) {
                    builder.append(separator);
                }
                builder.append(element);
                hasAdded = true;
            }
        }
        return builder;
    }

    @NonNull
    public static String getFirstOf(String first, String second) {
        if (first != null && first.length() > 0) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return "";
    }

    public static boolean isEmpty(String string) {
        return string != null && string.length() == 0;
    }

    public static String ensureNotNull(String message) {
        if (message != null) {
            return message;
        }
        return "";
    }
}
