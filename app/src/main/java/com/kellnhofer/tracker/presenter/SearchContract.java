package com.kellnhofer.tracker.presenter;

import java.util.List;

import androidx.lifecycle.LiveData;

import com.kellnhofer.tracker.model.Location;

public interface SearchContract {

    interface Presenter extends BaseContract.Presenter {
        void addObserver(Observer observer);
        void removeObserver(Observer observer);

        LiveData<List<Location>> searchLocations(String search);

        void startViewActivity(long locationId);
    }

    interface Observer extends BaseContract.Observer {

    }

}
