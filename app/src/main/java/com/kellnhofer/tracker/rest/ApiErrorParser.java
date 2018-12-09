package com.kellnhofer.tracker.rest;

import com.kellnhofer.tracker.service.LocationSyncError;

public class ApiErrorParser {

    public static LocationSyncError parseError(int code) {
        switch (code) {
            case 400:
                return LocationSyncError.BAD_REQUEST;
            case 401:
                return LocationSyncError.UNAUTHORIZED;
            case 404:
                return LocationSyncError.LOCATION_NOT_FOUND;
            default:
                return LocationSyncError.SERVER_ERROR;
        }
    }

}
