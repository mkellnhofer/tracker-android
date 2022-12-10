package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.view.CreateEditActivity;

public class ViewPresenter implements ViewContract.Presenter {

    private final Context mContext;

    private final List<ViewContract.Observer> mObservers = new ArrayList<>();

    private final LocationRepository mLocationRepository;
    private final PersonRepository mPersonRepository;
    private final LocationServiceAdapter mService;

    public ViewPresenter(Context context, LocationRepository locationRepository,
            PersonRepository personRepository, LocationServiceAdapter locationService) {
        mContext = context;

        mLocationRepository = locationRepository;
        mPersonRepository = personRepository;
        mService = locationService;
    }

    @Override
    public void addObserver(ViewContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(ViewContract.Observer observer) {
        mObservers.remove(observer);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

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
    public void deleteLocation(long locationId) {
        mService.deleteLocation(locationId);
    }

    @Override
    public void startEditActivity(long locationId) {
        Intent intent = new Intent(mContext, CreateEditActivity.class);
        intent.putExtra(CreateEditActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

}
