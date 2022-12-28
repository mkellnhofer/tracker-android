package com.kellnhofer.tracker.presenter;

import java.util.List;

import com.kellnhofer.tracker.data.AsyncResult;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;

public interface ViewContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        AsyncResult<Location> getLocation(long locationId);
        AsyncResult<List<Person>> getLocationPersons(long locationId);

        void deleteLocation(long locationId);

        void startEditActivity(long locationId);
    }

    interface Observer {

    }

}
