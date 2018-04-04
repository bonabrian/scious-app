package id.bonabrian.scious;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousEnvironment {
    private static SciousEnvironment environment;
    private boolean localTest;
    private boolean deviceTest;

    public static SciousEnvironment createLocalTestEnvironment() {
        SciousEnvironment env = new SciousEnvironment();
        env.localTest = true;
        return env;
    }

    public static SciousEnvironment createDeviceEnvironment() {
        return new SciousEnvironment();
    }

    public final boolean isTest() {
        return localTest || deviceTest;
    }

    public static synchronized SciousEnvironment env() {
        return environment;
    }

    public static synchronized boolean isEnvironmentSetup() {
        return environment != null;
    }

    public synchronized static void setupEnvironment(SciousEnvironment env) {
        environment = env;
    }
}
