package com.kellnhofer.tracker.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.kellnhofer.tracker.data.AsyncResult;
import com.kellnhofer.tracker.data.DbContract.LocationPersonTbl;
import com.kellnhofer.tracker.data.DbContract.LocationTbl;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.LocationPersonRef;
import com.kellnhofer.tracker.model.LocationWithPersonRefs;

@Dao
public abstract class LocationDao extends BaseDao {

    @Query("SELECT COUNT(*) FROM " + LocationTbl.NAME)
    public abstract LiveData<Integer> getLocationsCntLiveData();

    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_CHANGED + " = 0" +
            " OR " + LocationTbl.COLUMN_DELETED + " = 0")
    public abstract List<Location> getChangedOrDeletedLocations();

    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_DELETED + " = 0" +
            " ORDER BY " + LocationTbl.COLUMN_DATE + " DESC")
    public abstract List<Location> getNotDeletedLocationsOrderedByDateDesc();

    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_DELETED + " = 0" +
            " ORDER BY " + LocationTbl.COLUMN_DATE + " DESC")
    public abstract LiveData<List<Location>> getNotDeletedLocationsOrderedByDateDescLiveData();

    public List<Location> findNotDeletedLocationsByNameWithWildcards(String name) {
        if (name == null || name.isEmpty()) {
            return new ArrayList<>();
        }

        String escapedName  = escapeSearchString(name);

        return findNotDeletedLocationsByName(escapedName);
    }

    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_DELETED + " = 0" +
            " AND " + LocationTbl.COLUMN_NAME + " LIKE :name")
    protected abstract List<Location> findNotDeletedLocationsByName(String name);

    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_DELETED + " = 0" +
            " AND " + LocationTbl._ID + " IN (" +
            "SELECT " + LocationPersonTbl.COLUMN_LOCATION_ID + " FROM " + LocationPersonTbl.NAME +
            " WHERE " + LocationPersonTbl.COLUMN_PERSON_ID + " IN (:personIds)" +
            ")")
    public abstract List<Location> getNotDeletedLocationsByPersonIds(List<Long> personIds);

    public AsyncResult<Location> getLocationAsync(long id) {
        return AsyncResult.createAsyncResult(() -> getLocation(id));
    }

    @Nullable
    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl._ID + " = :id")
    public abstract Location getLocation(long id);

    @Nullable
    @Query("SELECT * FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl.COLUMN_REMOTE_ID + " = :remoteId")
    public abstract Location getLocationByRemoteId(long remoteId);

    @Transaction
    public long saveLocation(Location location) {
        long id = insertLocation(location);
        if (id > 0L) {
            return id;
        }
        updateLocation(location);
        return location.getId();
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long insertLocation(Location location);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void updateLocation(Location location);

    @Query("UPDATE " + LocationTbl.NAME +
            " SET " + LocationTbl.COLUMN_DELETED + " = 1" +
            " WHERE " + LocationTbl._ID + " = :id")
    public abstract void setLocationDeleted(long id);

    @Query("DELETE FROM " + LocationTbl.NAME +
            " WHERE " + LocationTbl._ID + " = :id")
    public abstract void deleteLocation(long id);

    @Transaction
    public long saveLocationWithPersonRefs(LocationWithPersonRefs locationWithPersonRefs) {
        long locationId = saveLocation(locationWithPersonRefs.location);
        List<LocationPersonRef> locationPersonRefs = locationWithPersonRefs.personIds.stream()
                .map((personId) -> new LocationPersonRef(locationId, personId))
                .collect(Collectors.toList());
        deleteLocationPersonRefs(locationId);
        insertLocationPersonRef(locationPersonRefs);
        return locationId;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insertLocationPersonRef(List<LocationPersonRef> locationPersonRef);

    @Query("DELETE FROM " + LocationPersonTbl.NAME +
            " WHERE " + LocationPersonTbl.COLUMN_LOCATION_ID + " = :locationId")
    protected abstract void deleteLocationPersonRefs(long locationId);

}
