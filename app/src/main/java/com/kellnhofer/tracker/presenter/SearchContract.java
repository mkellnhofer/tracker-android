package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;

import com.kellnhofer.tracker.model.Location;

public interface SearchContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        ArrayList<Location> searchLocations(String search);

        void startViewActivity(long locationId);
    }

    interface Observer {
        void onLocationsChanged();
    }

}
