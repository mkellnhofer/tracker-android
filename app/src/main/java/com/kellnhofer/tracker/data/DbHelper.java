package com.kellnhofer.tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 3;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // --- Public methods ---

    @Override
    public void onCreate(SQLiteDatabase db) {
        update(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        update(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clean(db);
        update(db, 0, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- Internal methods ---

    private void clean(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS location");
        db.execSQL("DROP TABLE IF EXISTS location_person");
        db.execSQL("DROP TABLE IF EXISTS person");
    }

    private void update(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Get all updates
        ArrayList<Method> updateMethods = new ArrayList<>();
        for (Method m : this.getClass().getDeclaredMethods()) {
            // If method is no update method: Continue with next method
            if (!m.isAnnotationPresent(DbUpdate.class)) {
                continue;
            }

            // If method is older/newer than requested: Continue with next method
            int version = m.getAnnotation(DbUpdate.class).newVersion();
            if (version <= oldVersion || version > newVersion) {
                continue;
            }

            updateMethods.add(m);
        }

        // Sort updates by order
        Collections.sort(updateMethods, new Comparator<Method>() {
            @Override
            public int compare(Method m1, Method m2) {
                int m1o = m1.getAnnotation(DbUpdate.class).order();
                int m2o = m2.getAnnotation(DbUpdate.class).order();
                return m1o - m2o;
            }
        });

        Log.i(LOG_TAG, String.format("Executing updates from V%d.", oldVersion));

        // Apply updates
        for (Method m : updateMethods) {
            try {
                DbUpdate update = m.getAnnotation(DbUpdate.class);
                Log.i(LOG_TAG, String.format("Executing update V%d.", update.newVersion()));
                m.invoke(this, db);
                Log.i(LOG_TAG, String.format("Completed update V%d.", update.newVersion()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- Update methods ---

    @DbUpdate(order=0, oldVersion=0, newVersion=1)
    @SuppressWarnings("unused")
    private void updateV1(SQLiteDatabase db) {
        final String sqlCreateLocationTable = "CREATE TABLE location ("
                + "_id"       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "remote_id" + " INTEGER, "
                + "changed"   + " INTEGER NOT NULL DEFAULT 0, "
                + "deleted"   + " INTEGER NOT NULL DEFAULT 0, "
                + "name"      + " TEXT COLLATE LOCALIZED, "
                + "date"      + " TEXT, "
                + "latitude"  + " REAL, "
                + "longitude" + " REAL);";

        db.execSQL(sqlCreateLocationTable);
    }

    @DbUpdate(order=1, oldVersion=1, newVersion=2)
    @SuppressWarnings("unused")
    private void updateV2(SQLiteDatabase db) {
        final String sqlCreatePersonTable = "CREATE TABLE person ("
                + "_id"        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "first_name" + " TEXT COLLATE LOCALIZED, "
                + "last_name"  + " TEXT COLLATE LOCALIZED);";

        final String sqlCreateLocationPersonTable = "CREATE TABLE location_person ("
                + "location_id" + " INTEGER, "
                + "person_id"   + " INTEGER, "
                + "PRIMARY KEY(location_id, person_id), "
                + "FOREIGN KEY(location_id) REFERENCES location(_id) ON DELETE CASCADE, "
                + "FOREIGN KEY(person_id) REFERENCES person(_id) ON DELETE CASCADE);";

        db.execSQL(sqlCreatePersonTable);
        db.execSQL(sqlCreateLocationPersonTable);
    }

    @DbUpdate(order=2, oldVersion=2, newVersion=3)
    @SuppressWarnings("unused")
    private void updateV3(SQLiteDatabase db) {
        final String sqlAlterLocationTable = "ALTER TABLE location "
                + "ADD COLUMN " + "description" + " TEXT COLLATE LOCALIZED;";

        db.execSQL(sqlAlterLocationTable);
    }

}
