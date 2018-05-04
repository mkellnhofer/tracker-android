package com.kellnhofer.tracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.data.DbContract.LocationEntry;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationDataSource {

    private final DbHelper mDbHelper;

    public LocationDataSource(Context context) {
        mDbHelper = new DbHelper(context);
    }

    // --- CRUD methods ---

    public List<Location> getLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE, LocationEntry.PROJECTION_ALL, null, null,
                null, null, null);

        List<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
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

        return location;
    }

    public void saveLocation(@NonNull Location location) {
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

        db.replace(LocationEntry.TABLE, null, values);
    }

    public int deleteLocation(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(LocationEntry.TABLE, LocationEntry._ID + " = ?", new String[]{
                Long.toString(id)});
    }

    // --- Query methods ---

    public List<Location> getNotDeletedLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE,
                LocationEntry.PROJECTION_ALL,
                LocationEntry.COLUMN_DELETED + " = 0",
                null, null, null, null);

        List<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return locations;
    }

    public List<Location> getChangedOrDeletedLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocationEntry.TABLE,
                LocationEntry.PROJECTION_ALL,
                LocationEntry.COLUMN_CHANGED + " = 0 OR " + LocationEntry.COLUMN_DELETED + " = 0",
                null, null, null, null);

        List<Location> locations = createLocationsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
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

    private List<Location> createLocationsFromCursor(Cursor cursor) {
        List<Location> locations = new ArrayList<>();
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

}
