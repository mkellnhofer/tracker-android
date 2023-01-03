package com.kellnhofer.tracker.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import com.kellnhofer.tracker.data.DbContract.LocationTbl;
import com.kellnhofer.tracker.data.DbContract.LocationPersonTbl;
import com.kellnhofer.tracker.data.DbContract.PersonTbl;

@Entity(
    tableName = LocationPersonTbl.NAME,
    primaryKeys = {LocationPersonTbl.COLUMN_LOCATION_ID, LocationPersonTbl.COLUMN_PERSON_ID},
    foreignKeys = {
        @ForeignKey(
            entity = Location.class,
            parentColumns = LocationTbl._ID,
            childColumns = LocationPersonTbl.COLUMN_LOCATION_ID,
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Person.class,
            parentColumns = PersonTbl._ID,
            childColumns = LocationPersonTbl.COLUMN_PERSON_ID,
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {LocationPersonTbl.COLUMN_LOCATION_ID}),
        @Index(value = {LocationPersonTbl.COLUMN_PERSON_ID})
    }
)
public class LocationPersonRef {

    @ColumnInfo(name = LocationPersonTbl.COLUMN_LOCATION_ID)
    private long mLocationId;

    @ColumnInfo(name = LocationPersonTbl.COLUMN_PERSON_ID)
    private long mPersonId;

    public LocationPersonRef() {

    }

    @Ignore
    public LocationPersonRef(long locationId, long personId) {
        mLocationId = locationId;
        mPersonId = personId;
    }

    public long getLocationId() {
        return mLocationId;
    }

    public void setLocationId(long locationId) {
        mLocationId = locationId;
    }

    public long getPersonId() {
        return mPersonId;
    }

    public void setPersonId(long personId) {
        mPersonId = personId;
    }

}
