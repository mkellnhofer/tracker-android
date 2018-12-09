package com.kellnhofer.tracker.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class CreateEditPresenter implements CreateEditContract.Presenter,
        LocationServiceAdapter.Listener, LocationListener {

    private static final String LOG_TAG = CreateEditPresenter.class.getSimpleName();

    private Context mContext;
    private TrackerApplication mApplication;

    private List<CreateEditContract.Observer> mObservers = new ArrayList<>();

    private LocationRepository mLocationRepository;
    private PersonRepository mPersonRepository;
    private LocationServiceAdapter mService;

    private LocationManager mLocationManager;
    private android.location.Location mGpsLocation;

    public CreateEditPresenter(Context context, LocationRepository locationRepository,
            PersonRepository personRepository, LocationServiceAdapter locationService) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mLocationRepository = locationRepository;
        mPersonRepository = personRepository;
        mService = locationService;

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void addObserver(CreateEditContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(CreateEditContract.Observer observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onResume() {
        mService.setListener(this);
    }

    @Override
    public void onPause() {
        mService.removeListener();

        mLocationManager.removeUpdates(this);
    }

    @Override
    public Location getLocation(long locationId) {
        return mLocationRepository.getLocation(locationId);
    }

    @Override
    public ArrayList<Person> getLocationPersons(long locationId) {
        return mPersonRepository.getPersonsByLocationId(locationId);
    }

    @Override
    public void createLocation(Location location, ArrayList<Person> persons) {
        mService.createLocation(location, persons);
    }

    @Override
    public void updateLocation(Location location, ArrayList<Person> persons) {
        mService.updateLocation(location, persons);
    }

    @Override
    public ArrayList<Person> getPersons() {
        return mPersonRepository.getPersons();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestGpsLocationUpdates() {
        if (mApplication.hasGpsPermissions()) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
        }
    }

    @Override
    public void removeGpsLocationUpdates() {
        mLocationManager.removeUpdates(this);
    }

    @Override
    public LatLng getGpsLocation() {
        return new LatLng(mGpsLocation.getLatitude(), mGpsLocation.getLongitude());
    }

    // --- Service callback methods ---

    @Override
    public void onLocationCreated(long locationId) {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (CreateEditContract.Observer observer : mObservers) {
                    observer.onLocationCreated();
                }
            }
        });
    }

    @Override
    public void onLocationUpdated(long locationId) {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (CreateEditContract.Observer observer : mObservers) {
                    observer.onLocationUpdated();
                }
            }
        });
    }

    @Override
    public void onLocationDeleted(long locationId) {

    }

    @Override
    public void onSyncStarted() {

    }

    @Override
    public void onSyncFinished() {

    }

    @Override
    public void onSyncFailed(LocationSyncError error) {

    }

    // --- GPS location callback methods ---

    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d(LOG_TAG, "Pos: " + location.getLatitude() + "," + location.getLongitude());

        mGpsLocation = location;

        for (CreateEditContract.Observer observer : mObservers) {
            observer.onGpsLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "Status changed.");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "Provider enabled.");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "Provider disabled.");
    }

    // --- Helper methods ---

    private void executeOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(runnable);
    }

}
