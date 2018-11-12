package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;

public interface CreateEditContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        Location getLocation(long locationId);
        ArrayList<Person> getLocationPersons(long locationId);
        void createLocation(Location location, ArrayList<Person> locationPersons);
        void updateLocation(Location location, ArrayList<Person> locationPersons);

        ArrayList<Person> getPersons();

        void requestGpsLocationUpdates();
        void removeGpsLocationUpdates();
        LatLng getGpsLocation();
    }

    interface Observer {
        void onGpsLocationChanged(LatLng latLng);
    }

}
