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
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class CreatePresenter implements CreateContract.Presenter,
        LocationServiceAdapter.Listener, LocationListener {

    private static final String LOG_TAG = CreatePresenter.class.getSimpleName();

    private Context mContext;
    private TrackerApplication mApplication;

    private List<CreateContract.Observer> mObservers = new ArrayList<>();

    private LocationServiceAdapter mService;

    private LocationManager mLocationManager;
    private android.location.Location mGpsLocation;

    public CreatePresenter(Context context, LocationServiceAdapter locationService) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mService = locationService;

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void addObserver(CreateContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(CreateContract.Observer observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onResume() {
        mService.addListener(this);

        initGpsLocationManager();
    }

    @Override
    public void onPause() {
        mLocationManager.removeUpdates(this);
        mService.removeListener();
    }

    @Override
    public void createLocation(Location location) {
        mService.createLocation(location);
    }

    @SuppressLint("MissingPermission")
    public void initGpsLocationManager() {
        if (mApplication.hasGpsPermissions()) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
        }
    }

    @Override
    public android.location.Location getGpsLocation() {
        return mGpsLocation;
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

        for (CreateContract.Observer observer : mObservers) {
            observer.onGpsLocationChanged(location);
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
