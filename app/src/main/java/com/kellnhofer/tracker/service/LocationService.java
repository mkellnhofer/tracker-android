package com.kellnhofer.tracker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationService extends Service implements LocationSync.Callback {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    public static final String ACTION_REFRESH = BuildConfig.APPLICATION_ID + ".action.LOCATIONS_REFRESH";
    public static final String ACTION_CREATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_CREATE";
    public static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID + ".action.LOCATION_UPDATE";
    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID + ".action.LOCATION_DELETE";

    public static final String EVENT_SUCCESS = BuildConfig.APPLICATION_ID + ".event.LOCATIONS_SUCCESS";
    public static final String EVENT_ERROR = BuildConfig.APPLICATION_ID + ".event.LOCATIONS_ERROR";

    public static final String EXTRA_ID = "ID";
    public static final String EXTRA_REMOTE_ID = "REMOTE_ID";
    public static final String EXTRA_NAME = "NAME";
    public static final String EXTRA_DATE = "DATE";
    public static final String EXTRA_LATITUDE = "LATITUDE";
    public static final String EXTRA_LONGITUDE = "LONGITUDE";

    public static final String EXTRA_ACTION = "ACTION";
    public static final String EXTRA_ERROR = "ERROR";

    private TrackerApplication mApplication;

    private LocationRepository mRepository;

    private LocationSync mLocationSync = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = (TrackerApplication) this.getApplication();

        mRepository = Injector.getLocationRepository(this);
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
                createLocation(getLocationFromIntent(intent));
                break;
            case ACTION_UPDATE:
                updateLocation(getLocationFromIntent(intent));
                break;
            case ACTION_DELETE:
                deleteLocation(getLocationIdFromIntent(intent));
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
                executeSync();
            }
        };

        new Thread(r).start();
    }

    private void createLocation(final Location location) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                location.setChanged(true);

                mRepository.saveLocation(location);

                broadcastSuccess(ACTION_CREATE);
                executeSync();
            }
        };

        new Thread(r).start();
    }

    private void updateLocation(final Location location) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Location existingLocation = mRepository.getLocation(location.getId());
                if (existingLocation != null) {
                    location.setRemoteId(existingLocation.getRemoteId());
                }

                location.setChanged(true);

                mRepository.saveLocation(location);

                broadcastSuccess(ACTION_UPDATE);
                executeSync();
            }
        };

        new Thread(r).start();
    }

    private void deleteLocation(final long locationId) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mRepository.setLocationDeleted(locationId);

                broadcastSuccess(ACTION_DELETE);
                executeSync();
            }
        };

        new Thread(r).start();
    }

    private void executeSync() {
        if (mLocationSync != null) {
            return;
        }
        mLocationSync = new LocationSync(mApplication);
        mLocationSync.setCallback(this);
        mLocationSync.execute();
    }

    // --- Sync callbacks ---

    @Override
    public void onSyncSuccess() {
        mLocationSync = null;
    }

    @Override
    public void onSyncError(LocationError error) {
        mLocationSync = null;
    }

    // --- Helper methods ---

    private long getLocationIdFromIntent(Intent intent) {
        return intent.getLongExtra(EXTRA_ID, 0L);
    }

    private Location getLocationFromIntent(Intent intent) {
        Location location = new Location();
        location.setId(intent.getLongExtra(EXTRA_ID, 0L));
        location.setRemoteId(intent.getLongExtra(EXTRA_REMOTE_ID, 0L));
        location.setName(intent.getStringExtra(EXTRA_NAME));
        location.setDate(DateUtils.fromServiceFormat(intent.getStringExtra(EXTRA_DATE)));
        location.setLatitude(intent.getDoubleExtra(EXTRA_LATITUDE, 0.0));
        location.setLongitude(intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0));
        return location;
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
