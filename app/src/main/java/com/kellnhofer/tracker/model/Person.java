package com.kellnhofer.tracker.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.kellnhofer.tracker.data.DbContract.PersonTbl;

@Entity(tableName = PersonTbl.NAME)
public class Person implements Parcelable {

    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PersonTbl._ID)
    private long mId;
    @ColumnInfo(name = PersonTbl.COLUMN_FIRST_NAME, collate = ColumnInfo.LOCALIZED)
    private String mFirstName;
    @ColumnInfo(name = PersonTbl.COLUMN_LAST_NAME, collate = ColumnInfo.LOCALIZED)
    private String mLastName;

    public Person() {

    }

    @Ignore
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
