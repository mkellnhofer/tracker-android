package com.kellnhofer.tracker;

import android.Manifest;

public interface Constants {

    String DATE_FORMAT_UI = "yyyy-MM-dd - HH:mm";
    String DATE_FORMAT_SERVICE = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    String DATE_FORMAT_DB = "yyyy-MM-dd HH:mm:ss";

    String[] GPS_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

}
