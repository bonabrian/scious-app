package id.bonabrian.scious.util;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArrayUtils {
    public static boolean equals(byte[] first, byte[] second, int startIndex) {
        if (first == null) {
            throw new IllegalArgumentException("first must not be null");
        }
        if (second == null) {
            throw new IllegalArgumentException("second must not be null");
        }
        if (startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be >= 0");
        }

        if (second.length + startIndex > first.length) {
            return false;
        }
        for (int i = 0; i < second.length; i++) {
            if (first[startIndex + i] != second[i]) {
                return false;
            }
        }
        return true;
    }
}
