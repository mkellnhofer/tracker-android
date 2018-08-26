package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.view.CreateEditActivity;

public class ViewPresenter implements ViewContract.Presenter {

    private Context mContext;
    private TrackerApplication mApplication;

    private List<ViewContract.Observer> mObservers = new ArrayList<>();

    private LocationRepository mRepository;

    public ViewPresenter(Context context, LocationRepository locationRepository) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mRepository = locationRepository;
    }

    @Override
    public void addObserver(ViewContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(ViewContract.Observer observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public Location getLocation(long locationId) {
        return mRepository.getLocation(locationId);
    }

    @Override
    public void startEditActivity(long locationId) {
        Intent intent = new Intent(mContext, CreateEditActivity.class);
        intent.putExtra(CreateEditActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

}
