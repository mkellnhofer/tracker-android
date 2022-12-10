package com.kellnhofer.tracker;

import android.content.Context;
import android.content.SharedPreferences;

public class TrackerStates {

    // Preferences file name
    public static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + "_states";
    // Keys
    private static final String STATE_KEY_LAST_SYNC_VERSION = "state_last_sync_version";
    // Default values
    private static final long STATE_DEFAULT_LAST_SYNC_VERSION = -1L;

    private final SharedPreferences mPreferences;

    public TrackerStates(TrackerApplication application) {
        mPreferences = application.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public long getLastSyncVersion() {
        return mPreferences.getLong(STATE_KEY_LAST_SYNC_VERSION, STATE_DEFAULT_LAST_SYNC_VERSION);
    }

    public void setLastSyncVersion(long lastSyncVersion) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(STATE_KEY_LAST_SYNC_VERSION, lastSyncVersion);
        editor.apply();
    }


}
