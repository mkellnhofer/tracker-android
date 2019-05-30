package com.kellnhofer.tracker.presenter;

import android.net.Uri;

import java.util.ArrayList;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.KmlExportError;
import com.kellnhofer.tracker.service.LocationSyncError;

public interface LocationsContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        ArrayList<Location> getNotDeletedLocationsByDateDesc();

        void executeLocationSync();
        void executeKmlExport(Uri fileUri);
        void cancelKmlExport();

        void startCreateActivity();
        void startViewActivity(long locationId);
        void startSearchActivity();
        void startSettingsActivity();
    }

    interface Observer {
        void onLocationsChanged();
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
        void onKmlExportStarted();
        void onKmlExportProgress(int current, int total);
        void onKmlExportFinished(int total);
        void onKmlExportFailed(KmlExportError error);
    }

}
