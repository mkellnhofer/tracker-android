package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class SettingsPresenter implements SettingsContract.Presenter,
        LocationServiceAdapter.Listener {

    private Context mContext;
    private TrackerApplication mApplication;

    private List<SettingsContract.Observer> mObservers = new ArrayList<>();

    private LocationServiceAdapter mService;

    public SettingsPresenter(Context context, LocationServiceAdapter locationService) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mService = locationService;
    }

    @Override
    public void addObserver(SettingsContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(SettingsContract.Observer observer) {
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
    }

    @Override
    public void executeLocationSync() {
        mService.startSync(false);
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
                for (SettingsContract.Observer observer : mObservers) {
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
                for (SettingsContract.Observer observer : mObservers) {
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
                for (SettingsContract.Observer observer : mObservers) {
                    observer.onSyncFailed(error);
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
