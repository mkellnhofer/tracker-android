package com.kellnhofer.tracker.presenter;

import java.util.List;

import com.kellnhofer.tracker.model.Location;

public interface LocationsContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        List<Location> getNotDeletedLocations();
        Location getLocation(long locationId);

        void createLocation(Location location);
        void updateLocation(Location location);
        void deleteLocation(long locationId);

        void startCreateActivity();
        void startViewActivity();
    }

    interface Observer {
        void onLocationsChanged();
    }

}
