package com.kellnhofer.tracker.service;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerStates;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.rest.ApiErrorParser;
import com.kellnhofer.tracker.rest.ApiLocation;
import com.kellnhofer.tracker.rest.LocationApi;
import retrofit2.Call;
import retrofit2.Response;

public class LocationSync {

    private static final String LOG_TAG = LocationSync.class.getSimpleName();

    public interface Callback {
        void onSyncSuccess();
        void onSyncError(LocationError error);
    }

    protected static class SyncException extends Exception {

        private final LocationError mError;

        public SyncException(LocationError code) {
            super();
            mError = code;
        }

        public LocationError getError() {
            return mError;
        }

    }

    private TrackerApplication mApplication;

    private LocationRepository mRepository;
    private LocationApi mApi;

    private Callback mCallback;

    public LocationSync(TrackerApplication application) {
        mApplication = application;

        mRepository = Injector.getLocationRepository(mApplication);
        mApi = mApplication.getLocationApi();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void execute() {
        try {
            downSync();
            upSync();
            downSync();
            notifySuccess();
        } catch (SyncException e) {
            notifyError(e.getError());
        }
    }

    private void downSync() throws SyncException {
        TrackerStates states = mApplication.getStates();
        // Get last sync version
        long lastSyncVersion = states.getLastSyncVersion();
        // Execute sync of new locations
        long newSyncVersion = downSyncChanged(lastSyncVersion);
        // Execute sync of deleted locations
        downSyncDeleted(lastSyncVersion);
        // Update last sync version
        states.setLastSyncVersion(newSyncVersion);
    }

    private long downSyncChanged(long lastSyncVersion) throws SyncException {
        // Try to get new/changed locations from remote
        Response<List<ApiLocation>> response;
        try {
            Call<List<ApiLocation>> call = mApi.getLocations(lastSyncVersion+1);
            response = call.execute();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Server communication failed at down sync of changed locations.", e);
            throw new SyncException(LocationError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at down sync of changed locations failed " +
                    "with %d.", response.code()));
            LocationError error = ApiErrorParser.parseError(response.code());
            throw new SyncException(error);
        }

        List<ApiLocation> apiLocations = response.body();

        // If response is null: Abort
        if (apiLocations == null) {
            return lastSyncVersion;
        }

        long syncVersion = lastSyncVersion;

        // Process response
        for (ApiLocation apiLocation : apiLocations) {
            // Convert location from API model
            Location syncedLocation = fromApiLocation(apiLocation);

            // Get existing location
            Location existingLocation = mRepository.getLocationByRemoteId(apiLocation.id);
            // If location already exists: Add its ID
            if (existingLocation != null) {
                syncedLocation.setId(existingLocation.getId());
            }

            // Update location locally
            mRepository.saveLocation(syncedLocation);

            // Increase sync version
            if (apiLocation.changeTime > syncVersion) {
                syncVersion = apiLocation.changeTime;
            }
        }

        // Return latest sync version
        return syncVersion;
    }

    private void downSyncDeleted(long lastSyncVersion) throws SyncException {
        // Try to get deleted locations from remote
        Response<List<Long>> response;
        try {
            Call<List<Long>> call = mApi.getDeletedLocations(lastSyncVersion);
            response = call.execute();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Server communication failed at down sync of deleted locations.", e);
            throw new SyncException(LocationError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at down sync of deleted locations failed " +
                    "with %d.", response.code()));
            LocationError error = ApiErrorParser.parseError(response.code());
            throw new SyncException(error);
        }

        List<Long> apiIds = response.body();

        // If response is null: Abort
        if (apiIds == null) {
            return;
        }

        // Process response
        for (Long apiId : apiIds) {
            // Get existing location
            Location existingLocation = mRepository.getLocationByRemoteId(apiId);

            // Delete location locally
            if (existingLocation != null) {
                mRepository.deleteLocation(existingLocation.getId());
            }
        }
    }

    private void upSync() throws SyncException {
        // Get local changed or deleted locations
        List<Location> locations = mRepository.getChangedOrDeletedLocations();

        // Process locations
        for (Location location : locations) {
            // Sync changed location
            if (location.isChanged()) {
                upSyncChanged(location);
            // Sync deleted location
            } else if (location.isDeleted()) {
                upSyncDeleted(location);
            }
        }
    }

    private void upSyncChanged(Location location) throws SyncException {
        // Convert location to API model
        ApiLocation apiLocation = toApiLocation(location);

        // Try to create/update location on remote
        Response<ApiLocation> response;
        try {
            Call<ApiLocation> call;
            // If location is new: Create location
            if (location.getRemoteId() == 0L) {
                call = mApi.createLocation(apiLocation);
            // If location was changed: Update location
            } else {
                call = mApi.changeLocation(location.getRemoteId(), apiLocation);
            }
            response = call.execute();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Server communication failed at up sync of changed locations.", e);
            throw new SyncException(LocationError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at up sync of changed locations failed " +
                    "with %d.", response.code()));
            LocationError error = ApiErrorParser.parseError(response.code());
            throw new SyncException(error);
        }

        apiLocation = response.body();

        // If response is null: Abort
        if (apiLocation == null) {
            return;
        }

        // Convert location from API model
        Location syncedLocation = fromApiLocation(apiLocation);
        syncedLocation.setId(location.getId());

        // Update location locally
        mRepository.saveLocation(syncedLocation);
    }

    private void upSyncDeleted(Location location) throws SyncException {
        // Try to delete location from remote
        if (location.getRemoteId() != 0L) {
            Response<Void> response;
            try {
                Call<Void> call = mApi.deleteLocation(location.getRemoteId());
                response = call.execute();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Server communication failed at up sync of deleted locations.", e);
                throw new SyncException(LocationError.COMMUNICATION_ERROR);
            }

            // Check response
            if (!response.isSuccessful()) {
                Log.e(LOG_TAG, String.format("Server call at up sync of deleted locations failed " +
                        "with %d.", response.code()));
                LocationError error = ApiErrorParser.parseError(response.code());
                throw new SyncException(error);
            }
        }

        // Delete location locally
        mRepository.deleteLocation(location.getId());
    }

    // --- Helper methods ---

    private void notifySuccess() {
        if (mCallback != null) {
            mCallback.onSyncSuccess();
        }
    }

    private void notifyError(LocationError error) {
        if (mCallback != null) {
            mCallback.onSyncError(error);
        }
    }

    private static ApiLocation toApiLocation(Location location) {
        ApiLocation apiLocation = new ApiLocation();
        apiLocation.name = location.getName();
        apiLocation.time = location.getDate();
        apiLocation.lat = location.getLatitude();
        apiLocation.lng = location.getLongitude();
        return apiLocation;
    }

    private static Location fromApiLocation(ApiLocation apiLocation) {
        Location location = new Location();
        location.setRemoteId(apiLocation.id);
        location.setName(apiLocation.name);
        location.setDate(apiLocation.time);
        location.setLatitude(apiLocation.lat);
        location.setLongitude(apiLocation.lng);
        return location;
    }

}
