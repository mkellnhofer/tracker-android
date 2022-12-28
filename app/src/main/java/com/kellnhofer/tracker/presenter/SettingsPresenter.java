package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class SettingsPresenter implements SettingsContract.Presenter,
        LocationServiceAdapter.Listener {

    private final Context mContext;

    private final List<SettingsContract.Observer> mObservers = new ArrayList<>();

    private final LocationServiceAdapter mService;

    public SettingsPresenter(Context context, LocationServiceAdapter locationService) {
        mContext = context;

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
        mObservers.remove(observer);
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
    public void onSyncStarted() {
        executeOnMainThread(() -> {
            for (SettingsContract.Observer observer : mObservers) {
                observer.onSyncStarted();
            }
        });
    }

    @Override
    public void onSyncFinished() {
        executeOnMainThread(() -> {
            for (SettingsContract.Observer observer : mObservers) {
                observer.onSyncFinished();
            }
        });
    }

    @Override
    public void onSyncFailed(final LocationSyncError error) {
        executeOnMainThread(() -> {
            for (SettingsContract.Observer observer : mObservers) {
                observer.onSyncFailed(error);
            }
        });
    }

    // --- Helper methods ---

    private void executeOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(runnable);
    }

}
