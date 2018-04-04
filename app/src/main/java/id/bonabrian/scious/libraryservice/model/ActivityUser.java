package id.bonabrian.scious.libraryservice.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.util.DateTimeUtils;
import id.bonabrian.scious.util.Prefs;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ActivityUser {
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    private String activityUserName;
    private String activityEmail;
    private int activityUserGender;
    private int activityUserYearOfBirth;
    private int activityUserHeightCm;
    private int activityUserWeightKg;
    private int activityUserSleepDuration;
    private int activityUserStepsGoal;

    private static final String defaultUserName = "scious-user";
    public static final String defaultEmail = "scious-email";
    public static final int defaultUserGender = GENDER_MALE;
    public static final int defaultUserYearOfBirth = 0;
    public static final int defaultUserAge = 0;
    public static final int defaultUserHeightCm = 175;
    public static final int defaultUserWeightKg = 70;
    public static final int defaultUserSleepDuration = 7;
    public static final int defaultUserStepsGoal = 8000;

    public static final String PREF_USER_NAME = "activity_user_name";
    public static final String PREF_USER_EMAIL = "activity_user_email";
    public static final String PREF_USER_YEAR_OF_BIRTH = "activity_user_age";
    public static final String PREF_USER_GENDER = "activity_user_gender";
    public static final String PREF_USER_HEIGHT_CM = "activity_user_height";
    public static final String PREF_USER_WEIGHT_KG = "activity_user_weight";
    public static final String PREF_USER_SLEEP_DURATION = "activity_user_sleep_duration";
    public static final String PREF_USER_STEPS_GOAL = "mi_fitness_goal";

    public ActivityUser() {
        fetchPreferences();
    }

    public String getName() {
        return activityUserName;
    }

    public String getEmail() {
        return activityEmail;
    }

    public int getWeight() {
        return activityUserWeightKg;
    }

    public int getGender() {
        return activityUserGender;
    }

    public int getYearOfBirth() {
        return activityUserYearOfBirth;
    }

    public int getHeight() {
        return activityUserHeightCm;
    }

    public int getSleepDuration() {
        if (activityUserSleepDuration < 1 || activityUserSleepDuration > 24) {
            activityUserSleepDuration = defaultUserSleepDuration;
        }
        return activityUserSleepDuration;
    }

    public int getStepsGoal() {
        if (activityUserStepsGoal < 0) {
            activityUserStepsGoal = defaultUserStepsGoal;
        }
        return activityUserStepsGoal;
    }

    public int getAge() {
        int userYear = getYearOfBirth();
        int age = 25;
        if (userYear > 1900) {
            age = Calendar.getInstance().get(Calendar.YEAR) - userYear;
            if (age <= 0) {
                age = 25;
            }
        }
        return age;
    }

    private void fetchPreferences() {
        Prefs prefs = SciousApplication.getPrefs();
        activityUserName = prefs.getString(PREF_USER_NAME, defaultUserName);
        activityEmail = prefs.getString(PREF_USER_EMAIL, defaultEmail);
        activityUserGender = prefs.getInt(PREF_USER_GENDER, defaultUserGender);
        activityUserHeightCm = prefs.getInt(PREF_USER_HEIGHT_CM, defaultUserHeightCm);
        activityUserWeightKg = prefs.getInt(PREF_USER_WEIGHT_KG, defaultUserWeightKg);
        activityUserYearOfBirth = prefs.getInt(PREF_USER_YEAR_OF_BIRTH, defaultUserYearOfBirth);
        activityUserSleepDuration = prefs.getInt(PREF_USER_SLEEP_DURATION, defaultUserSleepDuration);
        activityUserStepsGoal = prefs.getInt(PREF_USER_STEPS_GOAL, defaultUserStepsGoal);
    }

    public Date getUserBirthday() {
        Calendar cal = DateTimeUtils.getCalendarUTC();
        cal.set(GregorianCalendar.YEAR, getYearOfBirth());
        return cal.getTime();
    }
}
