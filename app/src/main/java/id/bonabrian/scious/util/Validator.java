package id.bonabrian.scious.util;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Validator {
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
