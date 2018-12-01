package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class SettingsPresenter implements SettingsContract.Presenter,
        LocationServiceAdapter.Listener {

    private static final String LOG_TAG = LocationsPresenter.class.getSimpleName();

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
        mService.addListener(this);
    }

    @Override
    public void onPause() {
        mService.removeListener();
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

}
