package id.bonabrian.scious.util;

import id.bonabrian.scious.libraryservice.model.ActivityUser;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class UserInfo {
    private final String btAddress;
    private final String alias;
    private final String email;
    private final int gender;
    private final int age;
    private final int height;
    private final int weight;

    private UserInfo(String address, String alias, String email, int gender, int age, int height, int weight) {
        this.btAddress = address;
        this.alias = alias;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
    }

    private byte[] data = new byte[20];

    public static UserInfo getDefault(String btAddress) {
        return new UserInfo(btAddress, "1550050550", ActivityUser.defaultEmail, ActivityUser.defaultUserGender, ActivityUser.defaultUserAge, ActivityUser.defaultUserHeightCm, ActivityUser.defaultUserWeightKg);
    }

    public static UserInfo create(String address, String alias, String email, int gender, int age, int height, int weight) {
        if (address == null || address.length() == 0 || alias == null || alias.length() == 0 || email == null || gender < 0 || age <= 0 || weight <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        try {
            return new UserInfo(address, alias, email, gender, age, height, weight);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Illegal user info data", ex);
        }
    }
}
