package com.kellnhofer.tracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import com.kellnhofer.tracker.data.DbContract.LocationEntry;
import com.kellnhofer.tracker.data.DbContract.LocationPersonEntry;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationDataSource {

    private final DbHelper mDbHelper;

    public LocationDataSource(Context context) {
        mDbHelper = new DbHelper(context);
    }

    // --- CRUD methods ---

    public ArrayList<Location> getLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE, LocationEntry.PROJECTION_ALL, null, null,
                null, null, null);

        ArrayList<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        for (Location location : locations) {
            ArrayList<Long> personIds = getPersonIds(location.getId());
            location.setPersonIds(personIds);
        }

        return locations;
    }

    @Nullable
    public Location getLocation(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE, LocationEntry.PROJECTION_ALL,
                LocationEntry._ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

        Location location = createLocationFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        ArrayList<Long> personIds = getPersonIds(location.getId());
        location.setPersonIds(personIds);

        return location;
    }

    public long saveLocation(@NonNull Location location) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (location.getId() != 0L) {
            values.put(LocationEntry._ID, location.getId());
        }
        values.put(LocationEntry.COLUMN_REMOTE_ID, location.getRemoteId());
        values.put(LocationEntry.COLUMN_CHANGED, location.isChanged() ? 1 : 0);
        values.put(LocationEntry.COLUMN_DELETED, location.isDeleted() ? 1 : 0);
        values.put(LocationEntry.COLUMN_NAME, location.getName());
        values.put(LocationEntry.COLUMN_DATE, DateUtils.toDbFormat(location.getDate()));
        values.put(LocationEntry.COLUMN_LATITUDE, location.getLatitude());
        values.put(LocationEntry.COLUMN_LONGITUDE, location.getLongitude());

        long id = db.replace(LocationEntry.TABLE, null, values);

        savePersonIds(id, location.getPersonIds());

        return id;
    }

    public int deleteLocation(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(LocationEntry.TABLE, LocationEntry._ID + " = ?", new String[]{
                Long.toString(id)});
    }

    // --- Query methods ---

    @Nullable
    public Location getLocationByRemoteId(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE, LocationEntry.PROJECTION_ALL,
                LocationEntry.COLUMN_REMOTE_ID + " = ?", new String[]{Long.toString(id)}, null,
                null, null);

        Location location = createLocationFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        if (location == null) {
            return null;
        }

        ArrayList<Long> personIds = getPersonIds(location.getId());
        location.setPersonIds(personIds);

        return location;
    }

    public ArrayList<Location> getNotDeletedLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE,
                LocationEntry.PROJECTION_ALL,
                LocationEntry.COLUMN_DELETED + " = 0",
                null, null, null, null);

        ArrayList<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        for (Location location : locations) {
            ArrayList<Long> personIds = getPersonIds(location.getId());
            location.setPersonIds(personIds);
        }

        return locations;
    }

    public ArrayList<Location> getChangedOrDeletedLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE,
                LocationEntry.PROJECTION_ALL,
                LocationEntry.COLUMN_CHANGED + " = 0 OR " + LocationEntry.COLUMN_DELETED + " = 0",
                null, null, null, null);

        ArrayList<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        for (Location location : locations) {
            ArrayList<Long> personIds = getPersonIds(location.getId());
            location.setPersonIds(personIds);
        }

        return locations;
    }

    // --- Update methods ---

    public void setLocationDeleted(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_DELETED, true);

        db.update(LocationEntry.TABLE, values, LocationEntry._ID + " = ?", new String[]{
                Long.toString(id)});
    }

    // --- Helper methods ---

    private ArrayList<Location> createLocationsFromCursor(Cursor cursor) {
        ArrayList<Location> locations = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            locations.add(readLocationFromCursor(cursor));
        }
        return locations;
    }

    private Location createLocationFromCursor(Cursor cursor) {
        Location location = null;
        while (cursor != null && cursor.moveToNext()) {
            location = readLocationFromCursor(cursor);
        }
        return location;
    }

    private Location readLocationFromCursor(Cursor cursor) {
        Location location = new Location();
        location.setId(cursor.getLong(0));
        location.setRemoteId(cursor.getLong(1));
        location.setChanged(cursor.getInt(2) == 1);
        location.setDeleted(cursor.getInt(3) == 1);
        location.setName(cursor.getString(4));
        location.setDate(DateUtils.fromDbFormat(cursor.getString(5)));
        location.setLatitude(cursor.getDouble(6));
        location.setLongitude(cursor.getDouble(7));
        return location;
    }

    private ArrayList<Long> getPersonIds(long locationId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(LocationPersonEntry.TABLE, LocationPersonEntry.PROJECTION_ALL,
                LocationPersonEntry.COLUMN_LOCATION_ID + " = ?",
                new String[]{Long.toString(locationId)}, null, null, null);

        ArrayList<Long> personIds = createPersonIdsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return personIds;
    }

    private void savePersonIds(long locationId, ArrayList<Long> personIds) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(LocationPersonEntry.TABLE, LocationPersonEntry.COLUMN_LOCATION_ID + " = ?",
                new String[]{Long.toString(locationId)});

        for (Long personId : personIds) {
            ContentValues values = new ContentValues();
            values.put(LocationPersonEntry.COLUMN_LOCATION_ID, locationId);
            values.put(LocationPersonEntry.COLUMN_PERSON_ID, personId);
            db.insert(LocationPersonEntry.TABLE, null, values);
        }
    }

    private ArrayList<Long> createPersonIdsFromCursor(Cursor cursor) {
        ArrayList<Long> personIds = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            personIds.add(cursor.getLong(1));
        }
        return personIds;
    }

}
