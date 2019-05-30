package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.view.ViewActivity;

public class SearchPresenter implements SearchContract.Presenter,
        LocationRepository.LocationRepositoryObserver {

    private Context mContext;
    private TrackerApplication mApplication;

    private List<SearchContract.Observer> mObservers = new ArrayList<>();

    private LocationRepository mLocationRepository;
    private PersonRepository mPersonRepository;

    public SearchPresenter(Context context, LocationRepository locationRepository,
            PersonRepository personRepository) {
        mContext = context;
        mApplication = (TrackerApplication) context.getApplicationContext();

        mLocationRepository = locationRepository;
        mPersonRepository = personRepository;
    }

    @Override
    public void addObserver(SearchContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(SearchContract.Observer observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onResume() {
        mLocationRepository.addContentObserver(this);
    }

    @Override
    public void onPause() {
        mLocationRepository.removeContentObserver(this);
    }

    @Override
    public ArrayList<Location> searchLocations(String search) {
        ArrayList<Location> l1 = searchLocationsByLocationName(search);
        ArrayList<Location> l2 = searchLocationsByPersonName(search);

        ArrayList<Location> l = new ArrayList<>();
        l.addAll(l1);
        l.addAll(l2);

        removeLocationsDuplicates(l);
        sortLocationsByDateDesc(l);

        return l;
    }

    private ArrayList<Location> searchLocationsByLocationName(String name) {
        return mLocationRepository.findNotDeletedLocationsByName(name);
    }

    private ArrayList<Location> searchLocationsByPersonName(String name) {
        ArrayList<Person> persons = mPersonRepository.findPersonsByName(name);

        ArrayList<Long> personIds = new ArrayList<>();
        for (Person person : persons) {
            personIds.add(person.getId());
        }

        return mLocationRepository.getNotDeletedLocationsByPersonIds(personIds);
    }

    @Override
    public void startViewActivity(long locationId) {
        Intent intent = new Intent(mContext, ViewActivity.class);
        intent.putExtra(ViewActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

    // --- Repository callback methods ---

    @Override
    public void onLocationChanged() {
        executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                for (SearchContract.Observer observer : mObservers) {
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
        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location l, Location r) {
                long lt = l.getDate().getTime();
                long rt = r.getDate().getTime();
                if (lt > rt) {
                    return -1;
                } else if (lt < rt) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

}
