package com.kellnhofer.tracker.presenter;

import java.util.List;

import android.net.Uri;
import androidx.lifecycle.LiveData;

import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.KmlExportError;
import com.kellnhofer.tracker.service.LocationSyncError;

public interface LocationsContract {

    interface Presenter extends BaseContract.Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        LiveData<List<Location>> getLocations();

        void executeLocationSync();
        void executeKmlExport(Uri fileUri);
        void cancelKmlExport();

        void startCreateActivity();
        void startViewActivity(long locationId);
        void startSearchActivity();
        void startSettingsActivity();
    }

    interface Observer extends BaseContract.Observer {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
        void onKmlExportStarted();
        void onKmlExportProgress(int current, int total);
        void onKmlExportFinished(int total);
        void onKmlExportFailed(KmlExportError error);
    }

}
