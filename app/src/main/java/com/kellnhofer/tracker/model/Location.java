package com.kellnhofer.tracker.model;

import java.util.ArrayList;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.kellnhofer.tracker.util.TypeUtils;

public class Location implements Parcelable {

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private long mId;
    private long mRemoteId;
    private boolean mChanged;
    private boolean mDeleted;
    private String mName;
    private Date mDate;
    private Double mLatitude;
    private Double mLongitude;
    private String mDescription;
    private ArrayList<Long> mPersonIds;

    public Location() {
        mPersonIds = new ArrayList<>();
    }

    public Location(long id, long remoteId, boolean changed, boolean deleted, String name, Date date,
            Double latitude, Double longitude, String description, ArrayList<Long> personIds) {
        mId = id;
        mRemoteId = remoteId;
        mDeleted = deleted;
        mChanged = changed;
        mName = name;
        mDate = date;
        mLatitude = latitude;
        mLongitude = longitude;
        mDescription = description;
        mPersonIds = personIds != null ? personIds : new ArrayList<>();
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

    public ArrayList<Long> getPersonIds() {
        return mPersonIds;
    }

    public void setPersonIds(ArrayList<Long> personIds) {
        mPersonIds = personIds != null ? personIds : new ArrayList<>();
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
        int personIdsLength = source.readInt();
        long[] personIds = new long[personIdsLength];
        source.readLongArray(personIds);
        mPersonIds = TypeUtils.toLongList(personIds);
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
        int personIdsLength = mPersonIds.size();
        dest.writeInt(personIdsLength);
        long[] personIds = TypeUtils.toLongArray(mPersonIds);
        dest.writeLongArray(personIds);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

}
