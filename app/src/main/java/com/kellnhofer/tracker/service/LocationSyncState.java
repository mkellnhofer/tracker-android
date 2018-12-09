package com.kellnhofer.tracker.service;

public class LocationSyncState {

    public static final int STATE_STARTED = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_FAILED = 3;

    private int mState;
    private LocationSyncError mError;

    private LocationSyncState(int state, LocationSyncError error) {
        mState = state;
        mError = error;
    }

    public int getState() {
        return mState;
    }

    public LocationSyncError getError() {
        return mError;
    }

    public static LocationSyncState createStartedState() {
        return new LocationSyncState(STATE_STARTED, null);
    }

    public static LocationSyncState createFinishedState() {
        return new LocationSyncState(STATE_FINISHED, null);
    }

    public static LocationSyncState createFailedState(LocationSyncError error) {
        return new LocationSyncState(STATE_FAILED, error);
    }

}
