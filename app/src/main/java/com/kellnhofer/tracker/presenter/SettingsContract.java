package com.kellnhofer.tracker.presenter;

public interface SettingsContract {

    interface Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        void onResume();
        void onPause();
    }

    interface Observer {

    }

}
