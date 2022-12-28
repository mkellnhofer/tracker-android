package com.kellnhofer.tracker;

import android.content.Context;

import com.kellnhofer.tracker.data.TrackerDatabase;
import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.service.ExportServiceAdapter;
import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class Injector {

    private static LocationDao sLocationDaoInstance;
    private static PersonDao sPersonDaoInstance;

    public static LocationDao getLocationDao(Context context) {
        if (sLocationDaoInstance == null) {
            sLocationDaoInstance = TrackerDatabase.getDatabase(context).locationDao();
        }
        return sLocationDaoInstance;
    }

    public static PersonDao getPersonDao(Context context) {
        if (sPersonDaoInstance == null) {
            sPersonDaoInstance = TrackerDatabase.getDatabase(context).personDao();
        }
        return sPersonDaoInstance;
    }

    public static LocationServiceAdapter getLocationService(Context context) {
        return new LocationServiceAdapter(context);
    }

    public static ExportServiceAdapter getExportService(Context context) {
        return new ExportServiceAdapter(context);
    }

}
