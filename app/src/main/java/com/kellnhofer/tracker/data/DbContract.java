package com.kellnhofer.tracker.data;

import android.provider.BaseColumns;

public class DbContract {

    public static class LocationEntry implements BaseColumns {
        // Table name
        public static final String TABLE = "location";

        // Column names
        public static final String COLUMN_REMOTE_ID = "remote_id";
        public static final String COLUMN_CHANGED = "changed";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        // All projection
        public static final String[] PROJECTION_ALL = {
                _ID,              // 0
                COLUMN_REMOTE_ID, // 1
                COLUMN_CHANGED,   // 2
                COLUMN_DELETED,   // 3
                COLUMN_NAME,      // 4
                COLUMN_DATE,      // 5
                COLUMN_LATITUDE,  // 6
                COLUMN_LONGITUDE  // 7
        };

        protected LocationEntry() {}
    }

    private DbContract() {}

}
