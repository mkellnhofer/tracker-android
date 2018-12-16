package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;
import com.kellnhofer.tracker.view.CreateEditActivity;
import com.kellnhofer.tracker.view.SettingsActivity;
import com.kellnhofer.tracker.view.ViewActivity;

public class LocationsPresenter implements LocationsContract.Presenter,
        LocationServiceAdapter.Listener, LocationRepository.LocationRepositoryObserver {

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
        mService.setListener(this);

        mService.startSync(false);
    }

    @Override
    public void onPause() {
        mRepository.removeContentObserver(this);
        mService.removeListener();
    }

    @Override
    public ArrayList<Location> getNotDeletedLocationsByDateDesc() {
        return mRepository.getNotDeletedLocationsByDateDesc();
    }

    @Override
    public void executeLocationSync() {
        mService.startSync(false);
    }

    @Override
    public void startCreateActivity() {
        Intent intent = new Intent(mContext, CreateEditActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void startViewActivity(long locationId) {
        Intent intent = new Intent(mContext, ViewActivity.class);
        intent.putExtra(ViewActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

    @Override
    public void startSettingsActivity() {
        mContext.startActivity(new Intent(mContext, SettingsActivity.class));
    }

    // --- Service callback methods ---

    @Override
    public void onLocationCreated(long locationId) {

    }

    @Override
    public void onLocationUpdated(long locationId) {

    }

    @Override
    public void onLocationDeleted(long locationId) {

    }

    @Override
    public void onSyncStarted() {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (LocationsContract.Observer observer : mObservers) {
                    observer.onSyncStarted();
                }
            }
        });
    }

    @Override
    public void onSyncFinished() {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (LocationsContract.Observer observer : mObservers) {
                    observer.onSyncFinished();
                }
            }
        });
    }

    @Override
    public void onSyncFailed(final LocationSyncError error) {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (LocationsContract.Observer observer : mObservers) {
                    observer.onSyncFailed(error);
                }
            }
        });
    }

    // --- Repository callback methods ---

    @Override
    public void onLocationChanged() {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (LocationsContract.Observer observer : mObservers) {
                    observer.onLocationsChanged();
                }
            }
        });
    }

    // --- Helper methods ---

    private void executeOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(runnable);
    }

}
