package com.kellnhofer.tracker;

import android.content.Context;

import com.kellnhofer.tracker.data.LocationDataSource;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonDataSource;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.service.ExportServiceAdapter;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class Injector {

    private static LocationDataSource LOCATION_DS_INSTANCE;
    private static LocationRepository LOCATION_REP_INSTANCE;
    private static PersonDataSource PERSON_DS_INSTANCE;
    private static PersonRepository PERSON_REP_INSTANCE;

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

    public static PersonDataSource getPersonDataSource(Context context) {
        if (PERSON_DS_INSTANCE == null) {
            PERSON_DS_INSTANCE = new PersonDataSource(context);
        }
        return PERSON_DS_INSTANCE;
    }

    public static PersonRepository getPersonRepository(Context context) {
        if (PERSON_REP_INSTANCE == null) {
            PERSON_REP_INSTANCE = new PersonRepository(getPersonDataSource(context));
        }
        return PERSON_REP_INSTANCE;
    }

    public static LocationServiceAdapter getLocationService(Context context) {
        return new LocationServiceAdapter(context);
    }

    public static ExportServiceAdapter getExportService(Context context) {
        return new ExportServiceAdapter(context);
    }

}
