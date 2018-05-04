package com.kellnhofer.tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationServiceAdapter {

    public interface Listener {
        void onServiceSuccess();
        void onServiceError();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case LocationService.EVENT_SUCCESS:
                    mListener.onServiceSuccess();
                    break;
                case LocationService.EVENT_ERROR:
                    mListener.onServiceError();
                    break;
                default:
            }
        }
    };

    private Context mContext;

    private boolean mIsReceiverRegistered = false;
    private Listener mListener;

    public LocationServiceAdapter(Context context) {
        mContext = context;
    }

    public void addListener(Listener listener) {
        mListener = listener;

        IntentFilter f = new IntentFilter();
        f.addAction(LocationService.EVENT_SUCCESS);
        f.addAction(LocationService.EVENT_ERROR);
        mContext.registerReceiver(mReceiver, f);
        mIsReceiverRegistered = true;
    }

    public void removeListener() {
        if (mIsReceiverRegistered) {
            mIsReceiverRegistered = false;
            mContext.unregisterReceiver(mReceiver);
        }

        mListener = null;
    }

    public void fetchLocations() {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_REFRESH);
        mContext.startService(intent);
    }

    public void createLocation(Location location) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_CREATE);
        intent.putExtra(LocationService.EXTRA_ID, location.getId());
        intent.putExtra(LocationService.EXTRA_REMOTE_ID, location.getRemoteId());
        intent.putExtra(LocationService.EXTRA_NAME, location.getName());
        intent.putExtra(LocationService.EXTRA_DATE, DateUtils.toServiceFormat(location.getDate()));
        intent.putExtra(LocationService.EXTRA_LATITUDE, location.getLatitude());
        intent.putExtra(LocationService.EXTRA_LONGITUDE, location.getLongitude());
        mContext.startService(intent);
    }

    public void updateLocation(Location location) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_UPDATE);
        intent.putExtra(LocationService.EXTRA_ID, location.getId());
        intent.putExtra(LocationService.EXTRA_REMOTE_ID, location.getRemoteId());
        intent.putExtra(LocationService.EXTRA_NAME, location.getName());
        intent.putExtra(LocationService.EXTRA_DATE, DateUtils.toServiceFormat(location.getDate()));
        intent.putExtra(LocationService.EXTRA_LATITUDE, location.getLatitude());
        intent.putExtra(LocationService.EXTRA_LONGITUDE, location.getLongitude());
        mContext.startService(intent);
    }

    public void deleteLocation(long id) {
        Intent intent = new Intent(mContext, LocationService.class);
        intent.setAction(LocationService.ACTION_DELETE);
        intent.putExtra(LocationService.EXTRA_ID, id);
        mContext.startService(intent);
    }

}
