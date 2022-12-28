package com.kellnhofer.tracker.presenter;

import java.util.List;

import com.kellnhofer.tracker.data.AsyncResult;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;

public interface CreateEditContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        AsyncResult<Location> getLocation(long locationId);
        AsyncResult<List<Person>> getLocationPersons(long locationId);
        AsyncResult<List<Person>> getPersons();

        void createLocation(Location location, List<Person> locationPersons);
        void updateLocation(Location location, List<Person> locationPersons);

        void requestGpsLocationUpdates();
        void removeGpsLocationUpdates();
        LatLng getGpsLocation();
    }

    interface Observer {
        void onGpsLocationChanged(LatLng latLng);
    }

}
