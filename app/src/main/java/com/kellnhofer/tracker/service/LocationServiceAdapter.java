package com.kellnhofer.tracker.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;

public class LocationServiceAdapter {

    public interface Listener {
        void onLocationCreated(long locationId);
        void onLocationUpdated(long locationId);
        void onLocationDeleted(long locationId);

        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.Binder binder = (LocationService.Binder) service;
            mService = binder.getService();
            mService.setCallback(new LocationService.Callback() {
                @Override
                public void onLocationCreated(long locationId) {
                    mListener.onLocationCreated(locationId);
                }

                @Override
                public void onLocationUpdated(long locationId) {
                    mListener.onLocationUpdated(locationId);
                }

                @Override
                public void onLocationDeleted(long locationId) {
                    mListener.onLocationDeleted(locationId);
                }

                @Override
                public void onSyncStarted() {
                    mListener.onSyncStarted();
                }

                @Override
                public void onSyncFinished() {
                    mListener.onSyncFinished();
                }

                @Override
                public void onSyncFailed(LocationSyncError error) {
                    mListener.onSyncFailed(error);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private final Context mContext;

    private LocationService mService;

    private Listener mListener;

    public LocationServiceAdapter(Context context) {
        mContext = context;
    }

    public void setListener(Listener listener) {
        mListener = listener;

        if (mService == null) {
            mContext.startService(new Intent(mContext, LocationService.class));
            mContext.bindService(new Intent(mContext, LocationService.class),
                    mServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public void removeListener() {
        if (mService != null) {
            mService.setCallback(null);
            mService = null;
            mContext.unbindService(mServiceConnection);
        }

        mListener = null;
    }

    public void createLocation(Location location, ArrayList<Person> persons) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_CREATE);
        intent.putExtra(LocationService.EXTRA_LOCATION, location);
        intent.putParcelableArrayListExtra(LocationService.EXTRA_PERSONS, persons);
        mContext.startService(intent);
    }

    public void updateLocation(Location location, ArrayList<Person> persons) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_UPDATE);
        intent.putExtra(LocationService.EXTRA_LOCATION, location);
        intent.putParcelableArrayListExtra(LocationService.EXTRA_PERSONS, persons);
        mContext.startService(intent);
    }

    public void deleteLocation(long id) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_DELETE);
        intent.putExtra(LocationService.EXTRA_ID, id);
        mContext.startService(intent);
    }

    public void startSync(boolean force) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_START_SYNC);
        intent.putExtra(LocationService.EXTRA_FORCE, force);
        mContext.startService(intent);
    }

    public void stopSync() {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_STOP_SYNC);
        mContext.startService(intent);
    }

}
