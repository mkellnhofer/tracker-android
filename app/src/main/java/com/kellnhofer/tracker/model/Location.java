package com.kellnhofer.tracker.model;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.kellnhofer.tracker.data.DbContract.LocationTbl;

@Entity(tableName = LocationTbl.NAME)
public class Location implements Parcelable {

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = LocationTbl._ID)
    private long mId;
    @ColumnInfo(name = LocationTbl.COLUMN_REMOTE_ID)
    private long mRemoteId;
    @ColumnInfo(name = LocationTbl.COLUMN_CHANGED, defaultValue = "0")
    private boolean mChanged;
    @ColumnInfo(name = LocationTbl.COLUMN_DELETED, defaultValue = "0")
    private boolean mDeleted;
    @ColumnInfo(name = LocationTbl.COLUMN_NAME, collate = ColumnInfo.LOCALIZED)
    private String mName;
    @ColumnInfo(name = LocationTbl.COLUMN_DATE)
    private Date mDate;
    @ColumnInfo(name = LocationTbl.COLUMN_LATITUDE)
    private Double mLatitude;
    @ColumnInfo(name = LocationTbl.COLUMN_LONGITUDE)
    private Double mLongitude;
    @ColumnInfo(name = LocationTbl.COLUMN_DESCRIPTION, collate = ColumnInfo.LOCALIZED)
    private String mDescription;

    public Location() {

    }

    @Ignore
    public Location(long id, long remoteId, boolean changed, boolean deleted, String name, Date date,
            Double latitude, Double longitude, String description) {
        mId = id;
        mRemoteId = remoteId;
        mDeleted = deleted;
        mChanged = changed;
        mName = name;
        mDate = date;
        mLatitude = latitude;
        mLongitude = longitude;
        mDescription = description;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(long remoteId) {
        mRemoteId = remoteId;
    }

    public boolean isChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    // --- Parcelable methods ---

    public Location(@NonNull Parcel source) {
        mId = source.readLong();
        mRemoteId = source.readLong();
        mChanged = source.readInt() > 0;
        mDeleted = source.readInt() > 0;
        mName = source.readString();
        long date = source.readLong();
        mDate = date > 0 ? new Date(date) : null;
        mLatitude = source.readDouble();
        mLongitude = source.readDouble();
        mDescription = source.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mRemoteId);
        dest.writeInt(mChanged ? 1 : 0);
        dest.writeInt(mDeleted ? 1 : 0);
        dest.writeString(mName);
        dest.writeLong(mDate != null ? mDate.getTime() : -1);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mDescription);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

}
