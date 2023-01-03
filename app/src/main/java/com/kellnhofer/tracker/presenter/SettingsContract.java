package com.kellnhofer.tracker.presenter;

import com.kellnhofer.tracker.service.LocationSyncError;

public interface SettingsContract {

    interface Presenter extends BaseContract.Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void executeLocationSync();
    }

    interface Observer extends BaseContract.Observer {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(LocationSyncError error);
    }

}
