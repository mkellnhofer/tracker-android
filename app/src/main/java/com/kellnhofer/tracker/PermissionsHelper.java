package com.kellnhofer.tracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

public final class PermissionsHelper {

    public static final String[] GPS_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private PermissionsHelper() {

    }

    public static boolean hasGpsPermissions(Context context) {
        for (String permission : GPS_PERMISSIONS) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
