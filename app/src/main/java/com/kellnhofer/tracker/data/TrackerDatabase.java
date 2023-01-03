package com.kellnhofer.tracker.data;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.LocationPersonRef;
import com.kellnhofer.tracker.model.Person;

@Database(
    entities = {Location.class, LocationPersonRef.class, Person.class},
    version = 4
)
@TypeConverters(DateConverter.class)
public abstract class TrackerDatabase extends RoomDatabase {

    private static final String LOG_TAG = TrackerDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "location.db";

    private static TrackerDatabase sInstance;

    public static TrackerDatabase getDatabase(final Context context) {
        synchronized (TrackerDatabase.class) {
            if (sInstance == null) {
                createDatabase(context);
            }
        }
        return sInstance;
    }

    private static void createDatabase(Context context) {
        sInstance = Room.databaseBuilder(context.getApplicationContext(), TrackerDatabase.class,
                DATABASE_NAME)
                .addMigrations(MIGRATION_V4)
                .fallbackToDestructiveMigration()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        onDatabaseCreate(db);
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        onDatabaseOpen(db);
                    }
                })
                .build();
    }

    private static void onDatabaseCreate(SupportSQLiteDatabase db) {
        Log.d(LOG_TAG, "onDatabaseCreate");
    }

    private static void onDatabaseOpen(SupportSQLiteDatabase db) {
        Log.d(LOG_TAG, "onDatabaseOpen");
    }

    // --- DAO methods ---

    public abstract LocationDao locationDao();
    public abstract PersonDao personDao();

    // --- DB Migrations ---

    private static final Migration MIGRATION_V4 = new TrackerDatabaseMigration(3, 4, db -> {
        db.execSQL("CREATE TABLE location_temp AS SELECT * FROM location;");
        db.execSQL("CREATE TABLE person_temp AS SELECT * FROM person;");
        db.execSQL("CREATE TABLE location_person_temp AS SELECT * FROM location_person;");

        db.execSQL("DROP TABLE location;");
        db.execSQL("DROP TABLE person;");
        db.execSQL("DROP TABLE location_person;");

        db.execSQL("CREATE TABLE location ("
                + "_id"         + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + "remote_id"   + " INTEGER NOT NULL, "
                + "changed"     + " INTEGER NOT NULL DEFAULT 0, "
                + "deleted"     + " INTEGER NOT NULL DEFAULT 0, "
                + "name"        + " TEXT COLLATE LOCALIZED, "
                + "date"        + " TEXT, "
                + "latitude"    + " REAL, "
                + "longitude"   + " REAL, "
                + "description" + " TEXT COLLATE LOCALIZED);");
        db.execSQL("INSERT INTO location "
                + "SELECT _id, remote_id, changed, deleted, name, date, latitude, longitude, "
                + "description FROM location_temp;");

        db.execSQL("CREATE TABLE person ("
                + "_id"        + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + "first_name" + " TEXT COLLATE LOCALIZED, "
                + "last_name"  + " TEXT COLLATE LOCALIZED);");
        db.execSQL("INSERT INTO person "
                + "SELECT _id, first_name, last_name FROM person_temp;");

        db.execSQL("CREATE TABLE location_person ("
                + "location_id" + " INTEGER NOT NULL, "
                + "person_id"   + " INTEGER NOT NULL, "
                + "PRIMARY KEY(location_id, person_id), "
                + "FOREIGN KEY(location_id) REFERENCES location(_id) ON DELETE CASCADE, "
                + "FOREIGN KEY(person_id) REFERENCES person(_id) ON DELETE CASCADE);");
        db.execSQL("CREATE INDEX index_location_person_location_id "
                + "ON location_person (location_id);");
        db.execSQL("CREATE INDEX index_location_person_person_id "
                + "ON location_person (person_id);");
        db.execSQL("INSERT INTO location_person "
                + "SELECT location_id, person_id FROM location_person_temp;");

        db.execSQL("DROP TABLE location_temp;");
        db.execSQL("DROP TABLE person_temp;");
        db.execSQL("DROP TABLE location_person_temp;");
    });

}
