package com.kellnhofer.tracker.service;

import com.kellnhofer.tracker.R;

public enum LocationSyncError {

    COMMUNICATION_ERROR(R.string.error_server_communication),
    UNAUTHORIZED(R.string.error_server_unauthorized),
    BAD_REQUEST(R.string.error_server_internal),
    LOCATION_NOT_FOUND(R.string.error_server_internal),
    SERVER_ERROR(R.string.error_server_internal);

    private final int mTextResId;

    LocationSyncError(int textResId) {
        mTextResId = textResId;
    }

    public int getTextResId() {
        return mTextResId;
    }

}
