package com.kellnhofer.tracker.presenter;

import com.kellnhofer.tracker.service.LocationSyncError;

public interface SettingsContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();

        void executeLocationSync();
    }

    interface Observer {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
    }

}
