package com.kellnhofer.tracker.presenter;

public interface BaseContract {

    interface Presenter {
        void onResume();
        void onPause();
    }

    interface Observer {

    }

}
