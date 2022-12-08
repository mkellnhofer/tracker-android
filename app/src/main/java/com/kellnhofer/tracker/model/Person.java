package com.kellnhofer.tracker.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Person implements Parcelable {

    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    private long mId;
    private String mFirstName;
    private String mLastName;

    public Person() {

    }

    public Person(long id, String firstName, String lastName) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    // --- Parcelable methods ---

    public Person(@NonNull Parcel source) {
        mId = source.readLong();
        mFirstName = source.readString();
        mLastName = source.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

}
