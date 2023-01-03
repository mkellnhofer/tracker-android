package com.kellnhofer.tracker.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class TrackerDatabaseMigration extends Migration {

    private static final String LOG_TAG = TrackerDatabaseMigration.class.getSimpleName();

    public interface MigrationFunction {
        void migrate(SupportSQLiteDatabase db);
    }

    private final int mStartVersion;
    private final int mEndVersion;

    private final MigrationFunction mMigrationFunction;

    public TrackerDatabaseMigration(int startVersion, int endVersion,
            MigrationFunction migrationFunction) {
        super(startVersion, endVersion);

        mStartVersion = startVersion;
        mEndVersion = endVersion;
        mMigrationFunction = migrationFunction;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        Log.d(LOG_TAG, String.format("Migrate database from version %d to %d.", mStartVersion,
                mEndVersion));
        mMigrationFunction.migrate(database);
    }

}
