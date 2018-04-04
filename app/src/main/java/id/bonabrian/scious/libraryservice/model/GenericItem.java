package id.bonabrian.scious.libraryservice.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.Collator;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class GenericItem implements IDeviceInfo {

    private String name;
    private String details;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<GenericItem>() {
        @Override
        public GenericItem createFromParcel(Parcel source) {
            GenericItem item = new GenericItem();
            item.setName(source.readString());
            item.setDetails(source.readString());
            return item;
        }

        @Override
        public GenericItem[] newArray(int size) {
            return new GenericItem[size];
        }
    };

    public GenericItem(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public GenericItem(String name) {
        this.name = name;
    }

    public GenericItem() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeString(getDetails());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericItem that = (GenericItem) o;

        return !(getName() != null ? !getName().equals(that.getName()) : that.getName() != null);

    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public int compareTo(@NonNull IDeviceInfo another) {
        if (getName() == another.getName()) {
            return 0;
        }
        if (getName() == null) {
            return +1;
        } else if (another.getName() == null) {
            return -1;
        }
        return Collator.getInstance().compare(getName(), another.getName());
    }
}
