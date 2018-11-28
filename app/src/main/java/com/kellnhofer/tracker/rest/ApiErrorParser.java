package com.kellnhofer.tracker.rest;

import com.kellnhofer.tracker.service.LocationError;

public class ApiErrorParser {

    public static LocationError parseError(int code) {
        switch (code) {
            case 400:
                return LocationError.BAD_REQUEST;
            case 401:
                return LocationError.UNAUTHORIZED;
            case 404:
                return LocationError.LOCATION_NOT_FOUND;
            default:
                return LocationError.SERVER_ERROR;
        }
    }

}
