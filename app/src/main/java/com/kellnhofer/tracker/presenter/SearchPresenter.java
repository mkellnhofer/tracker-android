package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.view.ViewActivity;

public class SearchPresenter extends BasePresenter implements SearchContract.Presenter {

    private final List<SearchContract.Observer> mObservers = new ArrayList<>();

    private final LocationDao mLocationDao;
    private final PersonDao mPersonDao;

    public SearchPresenter(Context context, LocationDao locationDao, PersonDao personDao) {
        super(context);

        mLocationDao = locationDao;
        mPersonDao = personDao;
    }

    @Override
    public void addObserver(SearchContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(SearchContract.Observer observer) {
        mObservers.remove(observer);
    }

    @Override
    public LiveData<List<Location>> searchLocations(String search) {
        MediatorLiveData<List<Location>> searchLocations = new MediatorLiveData<>();
        searchLocations.addSource(mLocationDao.getLocationsCntLiveData(), o ->
                new Thread(() -> searchLocations.postValue(searchLocationsByName(search))).start());
        return searchLocations;
    }

    private List<Location> searchLocationsByName(String name) {
        List<Location> l1 = searchLocationsByLocationName(name);
        List<Location> l2 = searchLocationsByPersonName(name);

        ArrayList<Location> l = new ArrayList<>();
        l.addAll(l1);
        l.addAll(l2);

        removeLocationsDuplicates(l);
        sortLocationsByDateDesc(l);

        return l;
    }

    private List<Location> searchLocationsByLocationName(String name) {
        return mLocationDao.findNotDeletedLocationsByNameWithWildcards(name);
    }

    private List<Location> searchLocationsByPersonName(String name) {
        List<Person> persons = mPersonDao.findPersonsByNameWithWildcards(name);
        List<Long> personIds = persons.stream()
                .map(Person::getId)
                .collect(Collectors.toList());
        return mLocationDao.getNotDeletedLocationsByPersonIds(personIds);
    }

    @Override
    public void startViewActivity(long locationId) {
        Intent intent = new Intent(mContext, ViewActivity.class);
        intent.putExtra(ViewActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

    // --- Helper methods ---

    private static void removeLocationsDuplicates(ArrayList<Location> locations) {
        HashSet<Long> locationIds = new HashSet<>();
        Iterator<Location> iterator = locations.iterator();
        while (iterator.hasNext()) {
            Location location = iterator.next();
            if (locationIds.contains(location.getId())) {
                iterator.remove();
            }
            locationIds.add(location.getId());
        }
    }

    private static void sortLocationsByDateDesc(ArrayList<Location> locations) {
        locations.sort((l, r) -> {
            long lt = l.getDate().getTime();
            long rt = r.getDate().getTime();
            return Long.compare(rt, lt);
        });
    }

}
