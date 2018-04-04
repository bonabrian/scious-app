package id.bonabrian.scious.util;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import id.bonabrian.scious.app.SciousApplication;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DateTimeUtils {
    public static String formatDateTime(Date date) {
        return DateUtils.formatDateTime(SciousApplication.getContext(), date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    public static Calendar getCalendarUTC() {
        return GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public static Date parseTimestamp(int timestamp) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000L);
        return calendar.getTime();
    }
}
