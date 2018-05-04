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

    public List<Location> getLocations() {
        return mDataSource.getLocations();
    }

    @Nullable
    public Location getLocation(long id) {
        return mDataSource.getLocation(id);
    }

    public void saveLocation(@NonNull Location location) {
        mDataSource.saveLocation(location);
        notifyContentObserver();
    }

    public int deleteLocation(long id) {
        int count = mDataSource.deleteLocation(id);
        notifyContentObserver();
        return count;
    }

    // --- Query methods ---

    public List<Location> getNotDeletedLocations() {
        return mDataSource.getNotDeletedLocations();
    }

    public List<Location> getChangedOrDeletedLocations() {
        return mDataSource.getChangedOrDeletedLocations();
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
