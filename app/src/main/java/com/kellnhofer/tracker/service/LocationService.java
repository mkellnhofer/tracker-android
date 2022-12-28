package com.kellnhofer.tracker.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;

import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerSettings;
import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.LocationWithPersonRefs;
import com.kellnhofer.tracker.model.Person;

public class LocationService extends Service implements LocationSyncThread.Callback {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    public static final String ACTION_CREATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_CREATE";
    public static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_UPDATE";
    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".action.LOCATION_DELETE";

    public static final String ACTION_START_SYNC = BuildConfig.APPLICATION_ID + ".action.START_SYNC";
    public static final String ACTION_STOP_SYNC = BuildConfig.APPLICATION_ID + ".action.STOP_SYNC";

    public static final String EXTRA_ID = "ID";
    public static final String EXTRA_LOCATION = "LOCATION";
    public static final String EXTRA_PERSONS = "PERSONS";
    public static final String EXTRA_FORCE = "FORCE";

    public class Binder extends android.os.Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public interface Callback {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
    }

    private final IBinder mBinder = new Binder();

    private TrackerApplication mApplication;
    private TrackerSettings mSettings;

    private LocationDao mLocationDao;
    private PersonDao mPersonDao;

    private Callback mCallback;

    private LocationSyncThread mSyncThread = null;
    private LocationSyncState mLastSyncState = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = (TrackerApplication) this.getApplication();
        mSettings = mApplication.getSettings();

        mLocationDao = Injector.getLocationDao(this);
        mPersonDao = Injector.getPersonDao(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallback(Callback callback) {
        if (callback != null) {
            notifyLastSyncState(callback);
        }

        mCallback = callback;
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }

        switch(action) {
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
                boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
                startSync(force);
                break;
            case ACTION_STOP_SYNC:
                stopSync();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported action '" + action + "'!");
        }

        return START_NOT_STICKY;
    }

    private void createLocation(final Location location, final ArrayList<Person> persons) {
        executeInThread(() -> {
            location.setChanged(true);
            saveLocationAndPersons(location, persons);
        });
    }

    private void updateLocation(final Location location, final ArrayList<Person> persons) {
        executeInThread(() -> {
            Location existingLocation = mLocationDao.getLocation(location.getId());
            if (existingLocation != null) {
                location.setRemoteId(existingLocation.getRemoteId());
            }

            location.setChanged(true);
            saveLocationAndPersons(location, persons);
            deleteUnusedPersons();
        });
    }

    private void saveLocationAndPersons(Location location, ArrayList<Person> persons) {
        ArrayList<Long> personIds = savePersons(persons);
        mLocationDao.saveLocationWithPersonRefs(new LocationWithPersonRefs(location,
                personIds));
    }

    private ArrayList<Long> savePersons(ArrayList<Person> persons) {
        ArrayList<Long> personIds = new ArrayList<>();
        for (Person person : persons) {
            Person existingPerson = mPersonDao.getPersonByFirstNameAndLastName(
                    person.getFirstName(), person.getLastName());
            if (existingPerson != null) {
                personIds.add(existingPerson.getId());
            } else {
                long newPersonId = mPersonDao.savePerson(person);
                personIds.add(newPersonId);
            }
        }
        return personIds;
    }

    private void deleteUnusedPersons() {
        mPersonDao.deleteUnusedPersons();
    }

    private void deleteLocation(final long locationId) {
        executeInThread(() -> mLocationDao.setLocationDeleted(locationId));
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
    public void onSyncStarted() {
        Log.i(LOG_TAG, "Location sync started.");

        notifySyncStarted();
    }

    @Override
    public void onSyncFinished() {
        Log.i(LOG_TAG, "Location sync finished.");

        mSyncThread = null;

        notifySyncFinished();
    }

    @Override
    public void onSyncCanceled() {
        Log.i(LOG_TAG, "Location sync canceled.");

        mSyncThread = null;
    }

    @Override
    public void onSyncFailed(LocationSyncError error) {
        Log.e(LOG_TAG, "Location sync failed.");

        mSyncThread = null;

        notifySyncFailed(error);
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

    private void notifySyncStarted() {
        if (mCallback != null) {
            mCallback.onSyncStarted();
        }
    }

    private void notifySyncFinished() {
        if (mCallback != null) {
            mCallback.onSyncFinished();
        }
    }

    private void notifySyncFailed(LocationSyncError error) {
        if (mCallback != null) {
            mCallback.onSyncFailed(error);
        } else {
            mLastSyncState = LocationSyncState.createFailedState(error);
        }
    }

    private void notifyLastSyncState(Callback callback) {
        if (mLastSyncState == null) {
            return;
        }
        switch (mLastSyncState.getState()) {
            case LocationSyncState.STATE_STARTED:
                callback.onSyncStarted();
                break;
            case LocationSyncState.STATE_FINISHED:
                callback.onSyncFinished();
                break;
            case LocationSyncState.STATE_FAILED:
                callback.onSyncFailed(mLastSyncState.getError());
                break;
            default:
        }
        mLastSyncState = null;
    }

    private static void executeInThread(Runnable r) {
        new Thread(r).start();
    }

}
