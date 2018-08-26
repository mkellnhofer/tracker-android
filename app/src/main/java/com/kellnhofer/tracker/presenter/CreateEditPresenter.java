package com.kellnhofer.tracker.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class CreateEditPresenter implements CreateEditContract.Presenter,
        LocationServiceAdapter.Listener, LocationListener {

    private static final String LOG_TAG = CreateEditPresenter.class.getSimpleName();

    private Context mContext;
    private TrackerApplication mApplication;

    private List<CreateEditContract.Observer> mObservers = new ArrayList<>();

    private LocationRepository mRepository;
    private LocationServiceAdapter mService;

    private LocationManager mLocationManager;
    private android.location.Location mGpsLocation;

    public CreateEditPresenter(Context context, LocationRepository locationRepository,
            LocationServiceAdapter locationService) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mRepository = locationRepository;
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
        mService.addListener(this);
    }

    @Override
    public void onPause() {
        mLocationManager.removeUpdates(this);
        mService.removeListener();
    }

    @Override
    public Location getLocation(long locationId) {
        return mRepository.getLocation(locationId);
    }

    @Override
    public void createLocation(Location location) {
        mService.createLocation(location);
    }

    @Override
    public void updateLocation(Location location) {
        mService.createLocation(location);
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
    public void onServiceSuccess() {
        Log.d(LOG_TAG, "onServiceSuccess");
    }

    @Override
    public void onServiceError() {
        Log.d(LOG_TAG, "onServiceError");
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
