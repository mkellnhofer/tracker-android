package com.kellnhofer.tracker.presenter;

import com.kellnhofer.tracker.model.Location;

public interface ViewContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        Location getLocation(long locationId);
        void deleteLocation(long locationId);

        void startEditActivity(long locationId);
    }

    interface Observer {

    }

}
