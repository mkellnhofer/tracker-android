package com.kellnhofer.tracker.presenter;

import com.kellnhofer.tracker.model.Location;

public interface CreateEditContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        Location getLocation(long locationId);
        void createLocation(Location location);
        void updateLocation(Location location);

        void requestGpsLocationUpdates();
        void removeGpsLocationUpdates();
        LatLng getGpsLocation();
    }

    interface Observer {
        void onGpsLocationChanged(LatLng latLng);
    }

}
