package com.kellnhofer.tracker.service;

public class KmlExportState {

    public static final int STATE_STARTED = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_FAILED = 3;

    private final int mState;
    private final Integer mTotal;
    private final KmlExportError mError;

    private KmlExportState(int state, Integer total, KmlExportError error) {
        mState = state;
        mTotal = total;
        mError = error;
    }

    public int getState() {
        return mState;
    }

    public Integer getTotal() {
        return mTotal;
    }

    public KmlExportError getError() {
        return mError;
    }

    public static KmlExportState createStartedState() {
        return new KmlExportState(STATE_STARTED, null, null);
    }

    public static KmlExportState createFinishedState(int total) {
        return new KmlExportState(STATE_FINISHED, total, null);
    }

    public static KmlExportState createFailedState(KmlExportError error) {
        return new KmlExportState(STATE_FAILED, null, error);
    }

}
