package com.kellnhofer.tracker.presenter;

import com.kellnhofer.tracker.model.Location;

public interface CreateContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        void createLocation(Location location);

        android.location.Location getGpsLocation();
    }

    interface Observer {
        void onGpsLocationChanged(android.location.Location location);
    }

}
