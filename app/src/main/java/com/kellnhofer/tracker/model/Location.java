package com.kellnhofer.tracker.model;

import java.util.Date;

public class Location {

    private long mId;
    private long mRemoteId;
    private boolean mChanged;
    private boolean mDeleted;
    private String mName;
    private Date mDate;
    private Double mLatitude;
    private Double mLongitude;

    public Location() {

    }

    public Location(long id, long remoteId, boolean changed, boolean deleted, String name, Date date,
            Double latitude, Double longitude) {
        mId = id;
        mRemoteId = remoteId;
        mDeleted = deleted;
        mChanged = changed;
        mName = name;
        mDate = date;
        mLatitude = latitude;
        mLongitude = longitude;
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

}
