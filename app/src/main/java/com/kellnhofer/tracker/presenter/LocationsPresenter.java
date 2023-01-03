package com.kellnhofer.tracker.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.LiveData;

import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.service.ExportServiceAdapter;
import com.kellnhofer.tracker.service.KmlExportError;
import com.kellnhofer.tracker.service.LocationServiceAdapter;
import com.kellnhofer.tracker.service.LocationSyncError;
import com.kellnhofer.tracker.view.CreateEditActivity;
import com.kellnhofer.tracker.view.SearchActivity;
import com.kellnhofer.tracker.view.SettingsActivity;
import com.kellnhofer.tracker.view.ViewActivity;

public class LocationsPresenter extends BasePresenter implements LocationsContract.Presenter,
        LocationServiceAdapter.Listener, ExportServiceAdapter.Listener {

    private final List<LocationsContract.Observer> mObservers = new ArrayList<>();

    private final LocationDao mLocationDao;
    private final LocationServiceAdapter mLocationService;
    private final ExportServiceAdapter mExportService;

    public LocationsPresenter(Context context, LocationDao locationDao,
            LocationServiceAdapter locationService, ExportServiceAdapter exportService) {
        super(context);

        mLocationDao = locationDao;
        mLocationService = locationService;
        mExportService = exportService;
    }

    @Override
    public void addObserver(LocationsContract.Observer observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(LocationsContract.Observer observer) {
        mObservers.remove(observer);
    }

    @Override
    public void onResume() {
        mLocationService.setListener(this);
        mLocationService.startSync(false);

        mExportService.setListener(this);
    }

    @Override
    public void onPause() {
        mLocationService.removeListener();

        mExportService.removeListener();
    }

    @Override
    public LiveData<List<Location>> getLocations() {
        return mLocationDao.getNotDeletedLocationsOrderedByDateDescLiveData();
    }

    @Override
    public void executeLocationSync() {
        mLocationService.startSync(false);
    }

    @Override
    public void executeKmlExport(Uri fileUri) {
        mExportService.startKmlExport(fileUri);
    }

    @Override
    public void cancelKmlExport() {
        mExportService.stopKmlExport();
    }

    @Override
    public void startCreateActivity() {
        Intent intent = new Intent(mContext, CreateEditActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void startViewActivity(long locationId) {
        Intent intent = new Intent(mContext, ViewActivity.class);
        intent.putExtra(ViewActivity.EXTRA_LOCATION_ID, locationId);
        mContext.startActivity(intent);
    }

    @Override
    public void startSearchActivity() {
        mContext.startActivity(new Intent(mContext, SearchActivity.class));
    }

    @Override
    public void startSettingsActivity() {
        mContext.startActivity(new Intent(mContext, SettingsActivity.class));
    }

    // --- Service callback methods ---

    @Override
    public void onSyncStarted() {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onSyncStarted();
            }
        });
    }

    @Override
    public void onSyncFinished() {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onSyncFinished();
            }
        });
    }

    @Override
    public void onSyncFailed(final LocationSyncError error) {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onSyncFailed(error);
            }
        });
    }

    @Override
    public void onKmlExportStarted() {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onKmlExportStarted();
            }
        });
    }

    @Override
    public void onKmlExportProgress(final int current, final int total) {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onKmlExportProgress(current, total);
            }
        });
    }

    @Override
    public void onKmlExportFinished(final int total) {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onKmlExportFinished(total);
            }
        });
    }

    @Override
    public void onKmlExportFailed(final KmlExportError error) {
        executeOnMainThread(() -> {
            for (LocationsContract.Observer observer : mObservers) {
                observer.onKmlExportFailed(error);
            }
        });
    }

}
