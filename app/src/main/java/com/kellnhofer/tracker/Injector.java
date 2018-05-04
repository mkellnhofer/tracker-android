package com.kellnhofer.tracker;

import android.content.Context;

import com.kellnhofer.tracker.data.LocationDataSource;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class Injector {

    private static LocationDataSource LOCATION_DS_INSTANCE;
    private static LocationRepository LOCATION_REP_INSTANCE;

    public static LocationDataSource getLocationDataSource(Context context) {
        if (LOCATION_DS_INSTANCE == null) {
            LOCATION_DS_INSTANCE = new LocationDataSource(context);
        }
        return LOCATION_DS_INSTANCE;
    }

    public static LocationRepository getLocationRepository(Context context) {
        if (LOCATION_REP_INSTANCE == null) {
            LOCATION_REP_INSTANCE = new LocationRepository(getLocationDataSource(context));
        }
        return LOCATION_REP_INSTANCE;
    }

    public static LocationServiceAdapter getLocationService(Context context) {
        return new LocationServiceAdapter(context);
    }

}
