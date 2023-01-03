package com.kellnhofer.tracker.data;

import android.provider.BaseColumns;

public final class DbContract {

    public static class LocationTbl implements BaseColumns {
        // Table name
        public static final String NAME = "location";

        // Column names
        public static final String COLUMN_REMOTE_ID = "remote_id";
        public static final String COLUMN_CHANGED = "changed";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_DESCRIPTION = "description";

        protected LocationTbl() {}
    }

    public static class LocationPersonTbl implements BaseColumns {
        // Table name
        public static final String NAME = "location_person";

        // Column names
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_PERSON_ID = "person_id";

        protected LocationPersonTbl() {}
    }

    public static class PersonTbl implements BaseColumns {
        // Table name
        public static final String NAME = "person";

        // Column names
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";

        protected PersonTbl() {}
    }

    private DbContract() {}

}
