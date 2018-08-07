package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.view.CreateActivity;
import com.kellnhofer.tracker.view.ViewActivity;

public class LocationsPresenter implements LocationsContract.Presenter,
        LocationServiceAdapter.Listener, LocationRepository.LocationRepositoryObserver {

    private static final String LOG_TAG = LocationsPresenter.class.getSimpleName();

    private Context mContext;
    private TrackerApplication mApplication;

    private List<LocationsContract.Observer> mObservers = new ArrayList<>();

    private LocationRepository mRepository;
    private LocationServiceAdapter mService;

    public LocationsPresenter(Context context, LocationRepository locationRepository,
            LocationServiceAdapter locationService) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mRepository = locationRepository;
        mService = locationService;
    }

    @Override
    public void addObserver(LocationsContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(LocationsContract.Observer observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onResume() {
        mRepository.addContentObserver(this);
        mService.addListener(this);

        mService.fetchLocations();
    }

    @Override
    public void onPause() {
        mRepository.removeContentObserver(this);
        mService.removeListener();
    }

    @Override
    public List<Location> getNotDeletedLocations() {
        return mRepository.getNotDeletedLocations();
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
        mService.updateLocation(location);
    }

    @Override
    public void deleteLocation(long locationId) {
        mService.deleteLocation(locationId);
    }

    @Override
    public void startCreateActivity() {
        Intent intent = new Intent(mContext, CreateActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void startViewActivity(long locationId) {
        Intent intent = new Intent(mContext, ViewActivity.class);
        intent.putExtra(ViewActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
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

    // --- Repository callback methods ---

    @Override
    public void onLocationChanged() {
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                for (LocationsContract.Observer observer : mObservers) {
                    observer.onLocationsChanged();
                }
            }
        };

        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(myRunnable);
    }

}
