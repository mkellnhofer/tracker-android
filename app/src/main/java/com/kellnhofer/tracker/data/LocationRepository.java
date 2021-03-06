package com.kellnhofer.tracker.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.model.Location;

public class LocationRepository {

    public interface LocationRepositoryObserver {
        void onLocationChanged();
    }

    private final LocationDataSource mDataSource;

    private List<LocationRepositoryObserver> mObservers = new ArrayList<>();

    public LocationRepository(LocationDataSource dataSource) {
        mDataSource = dataSource;
    }

    public void addContentObserver(LocationRepositoryObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void removeContentObserver(LocationRepositoryObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    // --- CRUD methods ---

    public ArrayList<Location> getLocations() {
        return mDataSource.getLocations();
    }

    @Nullable
    public Location getLocation(long id) {
        return mDataSource.getLocation(id);
    }

    public long saveLocation(@NonNull Location location) {
        long id = mDataSource.saveLocation(location);
        notifyContentObserver();
        return id;
    }

    public int deleteLocation(long id) {
        int count = mDataSource.deleteLocation(id);
        notifyContentObserver();
        return count;
    }

    // --- Query methods ---

    @Nullable
    public Location getLocationByRemoteId(long id) {
        return mDataSource.getLocationByRemoteId(id);
    }

    public ArrayList<Location> getNotDeletedLocationsByDateDesc() {
        return mDataSource.getNotDeletedLocationsOrderByDateDesc();
    }

    public ArrayList<Location> getNotDeletedLocationsByPersonIds(ArrayList<Long> personIds) {
        return mDataSource.getNotDeletedLocationsByPersonIds(personIds);
    }

    public ArrayList<Location> getChangedOrDeletedLocations() {
        return mDataSource.getChangedOrDeletedLocations();
    }

    public ArrayList<Location> findNotDeletedLocationsByName(String name) {
        return mDataSource.findNotDeletedLocationsByName(name);
    }

    // --- Update methods ---

    public void setLocationDeleted(long id) {
        mDataSource.setLocationDeleted(id);
    }

    // --- Helper methods ---

    private void notifyContentObserver() {
        for (LocationRepositoryObserver observer : mObservers) {
            observer.onLocationChanged();
        }
    }

}
