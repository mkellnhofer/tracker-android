package com.kellnhofer.tracker.model;

import java.util.ArrayList;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.kellnhofer.tracker.data.DbContract.LocationPersonTbl;
import com.kellnhofer.tracker.data.DbContract.LocationTbl;

public class LocationWithPersonRefs {

    @Embedded
    public Location location;

    @Relation(
            entity = LocationPersonRef.class,
            parentColumn = LocationTbl._ID,
            entityColumn = LocationPersonTbl.COLUMN_LOCATION_ID,
            projection = {LocationPersonTbl.COLUMN_PERSON_ID})
    public ArrayList<Long> personIds;

    public LocationWithPersonRefs() {

    }

    @Ignore
    public LocationWithPersonRefs(Location location, ArrayList<Long> personIds) {
        this.location = location;
        this.personIds = personIds;
    }

}
