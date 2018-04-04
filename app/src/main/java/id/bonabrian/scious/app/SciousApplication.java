package id.bonabrian.scious.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import id.bonabrian.scious.LockHandler;
import id.bonabrian.scious.SciousEnvironment;
import id.bonabrian.scious.database.DBHandler;
import id.bonabrian.scious.database.DBHelper;
import id.bonabrian.scious.database.DBOpenHelper;
import id.bonabrian.scious.entities.DaoMaster;
import id.bonabrian.scious.libraryservice.device.DeviceManager;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceService;
import id.bonabrian.scious.libraryservice.model.IDeviceService;
import id.bonabrian.scious.util.Prefs;
import id.bonabrian.scious.util.SciousPrefs;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousApplication extends Application {
    private static final String TAG = "SciousApplication";
    public static final String DATABASE_NAME = "Scious";

    private static SciousApplication context;
    private static final Lock dbLock = new ReentrantLock();
    private static IDeviceService deviceService;
    private static SharedPreferences sharedPreferences;
    private static Prefs prefs;
    private static SciousPrefs sciousPrefs;
    private static LockHandler lockHandler;

    private static NotificationManager notificationManager;

    public static final String ACTION_QUIT = "id.bonabrian.scious.application.action.quit";

    public static SciousApplication application;
    private DeviceManager deviceManager;

    public static void quit() {
        Log.i(TAG, "Application quitting...");
        Intent intent = new Intent(SciousApplication.ACTION_QUIT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        SciousApplication.deviceService().quit();
    }

    public SciousApplication() {
        context = this;
    }

    protected IDeviceService createDeviceService() {
        return new SciousDeviceService(this);
    }

    @Override
    public void onCreate() {
        application = this;
        super.onCreate();

        if (lockHandler != null) {
            return;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = new Prefs(sharedPreferences);
        sciousPrefs = new SciousPrefs(prefs);

        if (!SciousEnvironment.isEnvironmentSetup()) {
            SciousEnvironment.setupEnvironment(SciousEnvironment.createDeviceEnvironment());
            setupDatabase();
        }

        deviceManager = new DeviceManager(this);
        deviceService = createDeviceService();

        if (isRunningMarshmallowOrLater()) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void setupDatabase() {
        DaoMaster.OpenHelper helper;
        SciousEnvironment env = SciousEnvironment.env();
        if (env.isTest()) {
            helper = new DaoMaster.DevOpenHelper(this, null, null);
        } else {
            helper = new DBOpenHelper(this, DATABASE_NAME, null);
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        if (lockHandler == null) {
            lockHandler = new LockHandler();
        }
        lockHandler.init(daoMaster, helper);
    }

    public static DBHandler acquireDB() throws Exception {
        try {
            if (dbLock.tryLock(30, TimeUnit.SECONDS)) {
                return lockHandler;
            }
        } catch (InterruptedException ex) {
            Log.i(TAG, "Interrupted while waiting for DB lock");
        }
        throw new Exception("Unable to access the database");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_BACKGROUND) {
            if (!hasBusyDevice()) {
                DBHelper.clearSession();
            }
        }
    }

    private boolean hasBusyDevice() {
        List<SciousDevice> devices = getDeviceManager().getDevices();
        for (SciousDevice device : devices) {
            if (device.isBusy()) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static int getGrantedInterruptionFilter() {
        if (prefs.getBoolean("notification_filter", false) && SciousApplication.isRunningMarshmallowOrLater()) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                return notificationManager.getCurrentInterruptionFilter();
            }
        }
        return NotificationManager.INTERRUPTION_FILTER_ALL;
    }

    public static void releaseDB() {
        dbLock.unlock();
    }

    public static Context getContext() {
        return context;
    }

    public static IDeviceService deviceService() {
        return deviceService;
    }

    public static boolean isRunningLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isRunningMarshmallowOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean minimizeNotification() {
        return prefs.getBoolean("minimize_priority", false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static Prefs getPrefs() {
        return prefs;
    }

    public static SciousPrefs getSciousPrefs() {
        return sciousPrefs;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public static SciousApplication application() {
        return application;
    }
}
