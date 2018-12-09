package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.LocationSyncError;

public interface LocationsContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        ArrayList<Location> getNotDeletedLocationsByDateDesc();

        void startCreateActivity();
        void startViewActivity(long locationId);
        void startSettingsActivity();
    }

    interface Observer {
        void onLocationsChanged();
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
    }

}
