package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.kellnhofer.tracker.PermissionsHelper;
import com.kellnhofer.tracker.data.AsyncResult;
import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class CreateEditPresenter extends BasePresenter implements CreateEditContract.Presenter,
        LocationServiceAdapter.Listener, LocationListener {

    private static final String LOG_TAG = CreateEditPresenter.class.getSimpleName();

    private final List<CreateEditContract.Observer> mObservers = new ArrayList<>();

    private final LocationDao mLocationDao;
    private final PersonDao mPersonDao;
    private final LocationServiceAdapter mService;

    private final LocationManager mLocationManager;
    private android.location.Location mGpsLocation;

    public CreateEditPresenter(Context context, LocationDao locationDao, PersonDao personDao,
            LocationServiceAdapter locationService) {
        super(context);

        mLocationDao = locationDao;
        mPersonDao = personDao;
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
        mObservers.remove(observer);
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
    public AsyncResult<Location> getLocation(long locationId) {
        return mLocationDao.getLocationAsync(locationId);
    }

    @Override
    public AsyncResult<List<Person>> getLocationPersons(long locationId) {
        return mPersonDao.getPersonsByLocationIdAsync(locationId);
    }

    @Override
    public AsyncResult<List<Person>> getPersons() {
        return mPersonDao.getPersonsAsync();
    }

    @Override
    public void createLocation(Location location, List<Person> persons) {
        mService.createLocation(location, persons);
    }

    @Override
    public void updateLocation(Location location, List<Person> persons) {
        mService.updateLocation(location, persons);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestGpsLocationUpdates() {
        if (PermissionsHelper.hasGpsPermissions(mContext)) {
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

}
