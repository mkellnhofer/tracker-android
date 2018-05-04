package com.kellnhofer.tracker;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.kellnhofer.tracker.data.DbHelper;

public class TrackerApplication extends Application {

    private static final String LOG_TAG = TrackerApplication.class.getSimpleName();

    private DbHelper mDbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        initData();
    }

    private void initData() {
        Log.d(LOG_TAG, "Init data.");

        mDbHelper = new DbHelper(this);
    }

    // --- Helper methods ---

    public boolean hasGpsPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : Constants.GPS_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
