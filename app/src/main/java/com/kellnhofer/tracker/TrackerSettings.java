package com.kellnhofer.tracker;

import android.content.Context;
import android.content.SharedPreferences;

import com.kellnhofer.tracker.service.LocationServiceAdapter;

public class TrackerSettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Preferences file name
    public static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + "_preferences";
    // Keys
    public static final String PREF_KEY_SYNC_ENABLED = "pref_sync_enabled";
    public static final String PREF_KEY_SERVER_URL = "pref_server_url";
    public static final String PREF_KEY_SERVER_PASSWORD = "pref_server_password";
    public static final String PREF_KEY_VERSION = "pref_version";

    private TrackerApplication mApplication;
    private SharedPreferences mPreferences;

    private LocationServiceAdapter mService;

    public TrackerSettings(TrackerApplication application, LocationServiceAdapter locationService) {
        mApplication = application;

        mPreferences = mApplication.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mService = locationService;
    }

    public boolean isSyncEnabled() {
        boolean prefDefault = mApplication.getResources().getBoolean(R.bool.pref_enable_sync);
        return mPreferences.getBoolean(PREF_KEY_SYNC_ENABLED, prefDefault);
    }

    public void setSyncEnabled(boolean enableUpload) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_KEY_SYNC_ENABLED, enableUpload);
        editor.apply();
    }

    public String getServerUrl() {
        String prefDefault = mApplication.getResources().getString(R.string.pref_server_url);
        return mPreferences.getString(PREF_KEY_SERVER_URL, prefDefault);
    }

    public void setServerUrl(String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_KEY_SERVER_URL, url);
        editor.apply();
    }

    public String getServerPassword() {
        String prefDefault = mApplication.getResources().getString(R.string.pref_server_password);
        return mPreferences.getString(PREF_KEY_SERVER_PASSWORD, prefDefault);
    }

    public void setServerPassword(String password) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_KEY_SERVER_PASSWORD, password);
        editor.apply();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key) {
            case PREF_KEY_SYNC_ENABLED:
                handleSyncEnabled();
                break;
            case PREF_KEY_SERVER_URL:
                handleServerUrlChanged();
                break;
            case PREF_KEY_SERVER_PASSWORD:
                handleServerPasswordChanged();
                break;
            default:
        }
    }

    private void handleSyncEnabled() {
        if (isSyncEnabled()) {
            mService.startSync(true);
        } else {
            mService.stopSync();
        }
    }

    private void handleServerUrlChanged() {
        mApplication.initRetrofit();
        if (isSyncEnabled()) {
            mService.startSync(true);
        }
    }

    private void handleServerPasswordChanged() {
        mApplication.initOkHttp();
        mApplication.initRetrofit();
        if (isSyncEnabled()) {
            mService.startSync(true);
        }
    }

}
