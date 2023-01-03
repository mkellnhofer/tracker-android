package com.kellnhofer.tracker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerStates;
import com.kellnhofer.tracker.data.dao.LocationDao;
import com.kellnhofer.tracker.data.dao.PersonDao;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.LocationWithPersonRefs;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.remote.ApiErrorParser;
import com.kellnhofer.tracker.remote.ApiLocation;
import com.kellnhofer.tracker.remote.ApiPerson;
import com.kellnhofer.tracker.remote.LocationApi;
import retrofit2.Call;
import retrofit2.Response;

public class LocationSyncThread extends Thread {

    private static final String LOG_TAG = LocationSyncThread.class.getSimpleName();

    public interface Callback {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncCanceled();
        void onSyncFailed(LocationSyncError error);
    }

    protected static class SyncException extends Exception {

        private final LocationSyncError mError;

        public SyncException(LocationSyncError error) {
            super();
            mError = error;
        }

        public LocationSyncError getError() {
            return mError;
        }

    }

    private final TrackerApplication mApplication;

    private final LocationDao mLocationDao;
    private final PersonDao mPersonDao;
    private final LocationApi mApi;

    private Callback mCallback;

    public LocationSyncThread(TrackerApplication application) {
        mApplication = application;

        mLocationDao = Injector.getLocationDao(mApplication);
        mPersonDao = Injector.getPersonDao(mApplication);
        mApi = mApplication.getLocationApi();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            notifyStarted();
            downSync();
            upSync();
            downSync();
            notifyFinished();
        } catch (InterruptedException e) {
            notifyCanceled();
        } catch (SyncException e) {
            notifyFailed(e.getError());
        }
    }

    private void downSync() throws InterruptedException, SyncException {
        TrackerStates states = mApplication.getStates();
        // Get last sync version
        long lastSyncVersion = states.getLastSyncVersion();
        // Execute sync of new locations
        long newSyncVersion = downSyncChanged(lastSyncVersion);
        // Execute sync of deleted locations
        downSyncDeleted(lastSyncVersion);
        // Update last sync version
        states.setLastSyncVersion(newSyncVersion);
        // If canceled: Abort
        if (interrupted()) {
            throw new InterruptedException();
        }
    }

    private long downSyncChanged(long lastSyncVersion) throws SyncException {
        // Try to get new/changed locations from remote
        Response<List<ApiLocation>> response;
        try {
            Call<List<ApiLocation>> call = mApi.getLocations(lastSyncVersion+1);
            response = call.execute();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Server communication failed at down sync of changed locations.", e);
            throw new SyncException(LocationSyncError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at down sync of changed locations failed " +
                    "with %d.", response.code()));
            LocationSyncError error = ApiErrorParser.parseError(response.code());
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
            Location existingLocation = mLocationDao.getLocationByRemoteId(apiLocation.id);
            // If location already exists: Add its ID
            if (existingLocation != null) {
                syncedLocation.setId(existingLocation.getId());
            }

            // Get person IDs
            ArrayList<Person> persons = fromApiPersons(apiLocation.persons);
            ArrayList<Long> personIds = getPersonIds(persons);

            // Update location locally
            mLocationDao.saveLocationWithPersonRefs(new LocationWithPersonRefs(syncedLocation,
                    personIds));

            // Increase sync version
            if (apiLocation.changeTime > syncVersion) {
                syncVersion = apiLocation.changeTime;
            }
        }

        // Return latest sync version
        return syncVersion;
    }

    private ArrayList<Long> getPersonIds(ArrayList<Person> persons) {
        ArrayList<Long> personIds = new ArrayList<>();
        for (Person person : persons) {
            // Get existing person
            Person existingPerson = mPersonDao.getPersonByFirstNameAndLastName(
                    person.getFirstName(), person.getLastName());
            // If person already exists: Add its ID
            if (existingPerson != null) {
                personIds.add(existingPerson.getId());
            // Otherwise: Create new person and add its ID
            } else {
                long newPersonId = mPersonDao.savePerson(person);
                personIds.add(newPersonId);
            }
        }
        return personIds;
    }

    private void downSyncDeleted(long lastSyncVersion) throws SyncException {
        // Try to get deleted locations from remote
        Response<List<Long>> response;
        try {
            Call<List<Long>> call = mApi.getDeletedLocations(lastSyncVersion);
            response = call.execute();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Server communication failed at down sync of deleted locations.", e);
            throw new SyncException(LocationSyncError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at down sync of deleted locations failed " +
                    "with %d.", response.code()));
            LocationSyncError error = ApiErrorParser.parseError(response.code());
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
            Location existingLocation = mLocationDao.getLocationByRemoteId(apiId);

            // Delete location locally
            if (existingLocation != null) {
                mLocationDao.deleteLocation(existingLocation.getId());
            }
        }

        // Delete persons locally
        mPersonDao.deleteUnusedPersons();
    }

    private void upSync() throws InterruptedException, SyncException {
        // Get local changed or deleted locations
        List<Location> locations = mLocationDao.getChangedOrDeletedLocations();

        // Process locations
        for (Location location : locations) {
            // Sync changed location
            if (location.isChanged()) {
                upSyncChanged(location);
            // Sync deleted location
            } else if (location.isDeleted()) {
                upSyncDeleted(location);
            }
            // If canceled: Abort
            if (interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    private void upSyncChanged(Location location) throws SyncException {
        // Convert location to API model
        ApiLocation apiLocation = toApiLocation(location);

        // Get persons
        List<Person> persons = mPersonDao.getPersonsByLocationId(location.getId());
        // Add persons
        apiLocation.persons = toApiPersons(persons);

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
            throw new SyncException(LocationSyncError.COMMUNICATION_ERROR);
        }

        // Check response
        if (!response.isSuccessful()) {
            Log.e(LOG_TAG, String.format("Server call at up sync of changed locations failed " +
                    "with %d.", response.code()));
            LocationSyncError error = ApiErrorParser.parseError(response.code());
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
        mLocationDao.saveLocation(syncedLocation);
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
                throw new SyncException(LocationSyncError.COMMUNICATION_ERROR);
            }

            // Check response
            if (!response.isSuccessful()) {
                Log.e(LOG_TAG, String.format("Server call at up sync of deleted locations failed " +
                        "with %d.", response.code()));
                LocationSyncError error = ApiErrorParser.parseError(response.code());
                throw new SyncException(error);
            }
        }

        // Delete location locally
        mLocationDao.deleteLocation(location.getId());
    }

    // --- Helper methods ---

    private void notifyStarted() {
        if (mCallback != null) {
            mCallback.onSyncStarted();
        }
    }

    private void notifyFinished() {
        if (mCallback != null) {
            mCallback.onSyncFinished();
        }
    }

    private void notifyCanceled() {
        if (mCallback != null) {
            mCallback.onSyncCanceled();
        }
    }

    private void notifyFailed(LocationSyncError error) {
        if (mCallback != null) {
            mCallback.onSyncFailed(error);
        }
    }

    private static ApiLocation toApiLocation(Location location) {
        ApiLocation apiLocation = new ApiLocation();
        apiLocation.name = location.getName();
        apiLocation.time = location.getDate();
        apiLocation.lat = location.getLatitude();
        apiLocation.lng = location.getLongitude();
        apiLocation.description = location.getDescription();
        return apiLocation;
    }

    private static Location fromApiLocation(ApiLocation apiLocation) {
        Location location = new Location();
        location.setRemoteId(apiLocation.id);
        location.setName(apiLocation.name);
        location.setDate(apiLocation.time);
        location.setLatitude(apiLocation.lat);
        location.setLongitude(apiLocation.lng);
        location.setDescription(apiLocation.description);
        return location;
    }

    private static List<ApiPerson> toApiPersons(List<Person> persons) {
        ArrayList<ApiPerson> apiPersons = new ArrayList<>();
        for (Person person : persons) {
            ApiPerson apiPerson = new ApiPerson();
            apiPerson.firstName = person.getFirstName();
            apiPerson.lastName = person.getLastName();
            apiPersons.add(apiPerson);
        }
        return apiPersons;
    }

    private static ArrayList<Person> fromApiPersons(List<ApiPerson> apiPersons) {
        ArrayList<Person> persons = new ArrayList<>();
        for (ApiPerson apiPerson : apiPersons) {
            persons.add(new Person(0L, apiPerson.firstName, apiPerson.lastName));
        }
        return persons;
    }

}
