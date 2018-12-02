package com.kellnhofer.tracker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerSettings;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;

public class LocationService extends Service implements LocationSyncThread.Callback {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    public static final String ACTION_REFRESH = BuildConfig.APPLICATION_ID + ".action.LOCATIONS_REFRESH";
    public static final String ACTION_CREATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_CREATE";
    public static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_UPDATE";
    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".action.LOCATION_DELETE";

    public static final String ACTION_START_SYNC = BuildConfig.APPLICATION_ID + ".action.START_SYNC";
    public static final String ACTION_STOP_SYNC = BuildConfig.APPLICATION_ID + ".action.STOP_SYNC";

    public static final String EVENT_SUCCESS = BuildConfig.APPLICATION_ID + ".event.LOCATIONS_SUCCESS";
    public static final String EVENT_ERROR = BuildConfig.APPLICATION_ID + ".event.LOCATIONS_ERROR";

    public static final String EXTRA_ID = "ID";
    public static final String EXTRA_LOCATION = "LOCATION";
    public static final String EXTRA_PERSONS = "PERSONS";

    public static final String EXTRA_ACTION = "ACTION";
    public static final String EXTRA_ERROR = "ERROR";

    private TrackerApplication mApplication;
    private TrackerSettings mSettings;

    private LocationRepository mLocationRepository;
    private PersonRepository mPersonRepository;

    private LocationSyncThread mSyncThread = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = (TrackerApplication) this.getApplication();
        mSettings = mApplication.getSettings();

        mLocationRepository = Injector.getLocationRepository(this);
        mPersonRepository = Injector.getPersonRepository(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            throw new IllegalStateException("Action missing!");
        }

        switch(action) {
            case ACTION_REFRESH:
                syncLocations();
                break;
            case ACTION_CREATE:
                createLocation(getLocationFromIntent(intent), getPersonsFromIntent(intent));
                break;
            case ACTION_UPDATE:
                updateLocation(getLocationFromIntent(intent), getPersonsFromIntent(intent));
                break;
            case ACTION_DELETE:
                deleteLocation(getLocationIdFromIntent(intent));
                break;
            case ACTION_START_SYNC:
                startSync(true);
                break;
            case ACTION_STOP_SYNC:
                stopSync();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported action '" + action + "'!");
        }

        return START_NOT_STICKY;
    }

    private void syncLocations() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                startSync(false);
            }
        };

        new Thread(r).start();
    }

    private void createLocation(final Location location, final ArrayList<Person> persons) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                location.setChanged(true);

                ArrayList<Long> pIds = getPersonIds(persons);
                location.setPersonIds(pIds);

                mLocationRepository.saveLocation(location);

                broadcastSuccess(ACTION_CREATE);
                startSync(false);
            }
        };

        new Thread(r).start();
    }

    private void updateLocation(final Location location, final ArrayList<Person> persons) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Location existingLocation = mLocationRepository.getLocation(location.getId());
                if (existingLocation != null) {
                    location.setRemoteId(existingLocation.getRemoteId());
                }

                location.setChanged(true);

                ArrayList<Long> pIds = getPersonIds(persons);
                location.setPersonIds(pIds);

                mLocationRepository.saveLocation(location);
                mPersonRepository.deleteUnusedPersons();

                broadcastSuccess(ACTION_UPDATE);
                startSync(false);
            }
        };

        new Thread(r).start();
    }

    private ArrayList<Long> getPersonIds(ArrayList<Person> persons) {
        ArrayList<Long> personIds = new ArrayList<>();
        for (Person person : persons) {
            Person existingPerson = mPersonRepository.getPersonByFirstNameAndLastName(
                    person.getFirstName(), person.getLastName());
            if (existingPerson != null) {
                personIds.add(existingPerson.getId());
            } else {
                long newPersonId = mPersonRepository.savePerson(person);
                personIds.add(newPersonId);
            }
        }
        return personIds;
    }

    private void deleteLocation(final long locationId) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mLocationRepository.setLocationDeleted(locationId);
                broadcastSuccess(ACTION_DELETE);
                startSync(false);
            }
        };

        new Thread(r).start();
    }

    private void startSync(boolean restart) {
        if (!mSettings.isSyncEnabled()) {
            return;
        }

        if (restart) {
            stopSync();
        }

        if (mSyncThread != null) {
            Log.d(LOG_TAG, "Location sync is already running. No need to start sync.");
            return;
        }

        Log.i(LOG_TAG, "Starting location sync ...");

        mSyncThread = new LocationSyncThread(mApplication);
        mSyncThread.setCallback(this);
        mSyncThread.start();
    }

    private void stopSync() {
        if (mSyncThread == null) {
            return;
        }

        Log.i(LOG_TAG, "Stopping location sync ...");

        if (mSyncThread.isAlive()) {
            mSyncThread.interrupt();
        }

        mSyncThread = null;
    }

    // --- Sync callbacks ---

    @Override
    public void onSyncSuccess() {
        Log.i(LOG_TAG, "Location sync finished.");
        mSyncThread = null;
    }

    @Override
    public void onSyncCancel() {
        Log.i(LOG_TAG, "Location sync canceled.");
    }

    @Override
    public void onSyncError(LocationError error) {
        Log.i(LOG_TAG, "Location sync failed.");
        mSyncThread = null;
    }

    // --- Helper methods ---

    private long getLocationIdFromIntent(Intent intent) {
        return intent.getLongExtra(EXTRA_ID, 0L);
    }

    private Location getLocationFromIntent(Intent intent) {
        return intent.getParcelableExtra(EXTRA_LOCATION);
    }

    private ArrayList<Person> getPersonsFromIntent(Intent intent) {
        return intent.getParcelableArrayListExtra(EXTRA_PERSONS);
    }

    private void broadcastSuccess(String action) {
        Intent i = new Intent(EVENT_SUCCESS);
        i.putExtra(EXTRA_ACTION, action);
        sendBroadcast(i);
    }

    private void broadcastError(LocationError error) {
        Intent i = new Intent(EVENT_ERROR);
        i.putExtra(EXTRA_ERROR, error.ordinal());
        sendBroadcast(i);
    }

}
