package id.bonabrian.scious.libraryservice.events;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class CalendarEvents {
    private static final String TAG = CalendarEvents.class.getSimpleName();

    private static final String[] EVENT_INSTANCE_PROJECTION = new String[]{
            Instances._ID,

            Instances.BEGIN,
            Instances.END,
            Instances.DURATION,
            Instances.TITLE,
            Instances.DESCRIPTION,
            Instances.EVENT_LOCATION,
            Instances.CALENDAR_DISPLAY_NAME,
            Instances.ALL_DAY
    };

    private static final int lookahead_days = 7;

    private List<CalendarEvent> calendarEventList = new ArrayList<CalendarEvent>();

    public List<CalendarEvent> getCalendarEventList(Context mContext) {
        fetchSystemEvents(mContext);
        return calendarEventList;
    }

    private boolean fetchSystemEvents(Context mContext) {

        Calendar cal = GregorianCalendar.getInstance();
        Long dtStart = cal.getTimeInMillis();
        cal.add(Calendar.DATE, lookahead_days);
        Long dtEnd = cal.getTimeInMillis();

        Uri.Builder eventsUriBuilder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, dtStart);
        ContentUris.appendId(eventsUriBuilder, dtEnd);
        Uri eventsUri = eventsUriBuilder.build();

        try (Cursor evtCursor = mContext.getContentResolver().query(eventsUri, EVENT_INSTANCE_PROJECTION, null, null, Instances.BEGIN + " ASC")) {
            if (evtCursor == null || evtCursor.getCount() == 0) {
                return false;
            }
            while (evtCursor.moveToNext()) {
                long start = evtCursor.getLong(1);
                long end = evtCursor.getLong(2);
                if (end == 0) {
                    Log.i(TAG, "No end time, will parse duration string");
                    Time time = new Time(); //FIXME: deprecated FTW
                    time.parse(evtCursor.getString(3));
                    end = start + time.toMillis(false);
                }
                CalendarEvent calEvent = new CalendarEvent(
                        start,
                        end,
                        evtCursor.getLong(0),
                        evtCursor.getString(4),
                        evtCursor.getString(5),
                        evtCursor.getString(6),
                        evtCursor.getString(7),
                        !evtCursor.getString(8).equals("0")
                );
                calendarEventList.add(calEvent);
            }
            return true;
        }
    }

    public static class CalendarEvent {
        private long begin;
        private long end;
        private long id;
        private String title;
        private String description;
        private String location;
        private String calName;
        private boolean allDay;

        public CalendarEvent(long begin, long end, long id, String title, String description, String location, String calName, boolean allDay) {
            this.begin = begin;
            this.end = end;
            this.id = id;
            this.title = title;
            this.description = description;
            this.location = location;
            this.calName = calName;
            this.allDay = allDay;
        }

        public long getBegin() {
            return begin;
        }

        public int getBeginSeconds() {
            return (int) (begin / 1000);
        }

        public long getEnd() {
            return end;
        }

        public long getDuration() {
            return end - begin;
        }

        public int getDurationSeconds() {
            return (int) ((getDuration()) / 1000);
        }

        public short getDurationMinutes() {
            return (short) (getDurationSeconds() / 60);
        }


        public long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getLocation() {
            return location;
        }

        public String getCalName() {
            return calName;
        }

        public boolean isAllDay() {
            return allDay;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof CalendarEvent) {
                CalendarEvent e = (CalendarEvent) other;
                return (this.getId() == e.getId()) &&
                        Objects.equals(this.getTitle(), e.getTitle()) &&
                        (this.getBegin() == e.getBegin()) &&
                        Objects.equals(this.getLocation(), e.getLocation()) &&
                        Objects.equals(this.getDescription(), e.getDescription()) &&
                        (this.getEnd() == e.getEnd()) &&
                        Objects.equals(this.getCalName(), e.getCalName()) &&
                        (this.isAllDay() == e.isAllDay());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int result = (int) id;
            result = 31 * result + Objects.hash(title);
            result = 31 * result + Long.valueOf(begin).hashCode();
            result = 31 * result + Objects.hash(location);
            result = 31 * result + Objects.hash(description);
            result = 31 * result + Long.valueOf(end).hashCode();
            result = 31 * result + Objects.hash(calName);
            result = 31 * result + Boolean.valueOf(allDay).hashCode();
            return result;
        }
    }
}
