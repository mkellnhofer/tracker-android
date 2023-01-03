package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.PermissionsHelper;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.presenter.CreateEditContract;
import com.kellnhofer.tracker.presenter.CreateEditPresenter;
import com.kellnhofer.tracker.presenter.LatLng;
import com.kellnhofer.tracker.util.DateUtils;

public class CreateEditActivity extends BaseActivity implements CreateEditContract.Observer,
        CreateEditDialogFragment.Listener {

    private static final String FRAGMENT_TAG_CREATE_EDIT = "create_edit_fragment";
    private static final String DIALOG_FRAGMENT_TAG_CREATE_EDIT = "create_edit_dialog_fragment";

    private static final String STATE_LOCATION = "location";
    private static final String STATE_LOCATION_NAME = "location_name";
    private static final String STATE_LOCATION_DATE = "location_date";
    private static final String STATE_LOCATION_LAT = "location_lat";
    private static final String STATE_LOCATION_LNG = "location_lng";
    private static final String STATE_LOCATION_DESCRIPTION = "location_description";
    private static final String STATE_LOCATION_PERSONS = "location_persons";
    private static final String STATE_USE_GPS_LOCATION = "use_gps_location";
    private static final String STATE_REQUESTED_PERMISSIONS = "requested_permissions";

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    public static final String EXTRA_LOCATION_ID = "location_id";

    private CreateEditContract.Presenter mPresenter;

    private FloatingActionButton mGpsFab;
    private FloatingActionButton mOkFab;
    private CreateEditFragment mFragment;

    private long mLocationId;
    private Location mLocation;

    private String mLocationName;
    private String mLocationDate;
    private double mLocationLat;
    private double mLocationLng;
    private String mLocationDescription;
    private ArrayList<String> mLocationPersonNames;

    private boolean mUseGpsLocation = false;
    private boolean mRequestedPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new CreateEditPresenter(this, Injector.getLocationDao(this),
                Injector.getPersonDao(this), Injector.getLocationService(this));

        Intent intent = getIntent();
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        if (savedInstanceState == null && mLocationId == 0L) {
            mLocation = new Location(0L, 0L, false, false, "", new Date(), 0.0, 0.0, "");
            mLocationName = "";
            mLocationDate = DateUtils.toUiFormat(new Date());
            mLocationDescription = "";
            mLocationPersonNames = new ArrayList<>();
        }

        if (savedInstanceState == null && mLocationId == 0L) {
            mUseGpsLocation = true;
        }

        setContentView(R.layout.activity_create_edit);

        AppBarLayout appBarLayout = findViewById(R.id.container_app_bar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));

        MaterialToolbar topAppBar = findViewById(R.id.top_app_bar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        if (savedInstanceState == null) {
            String createTile = getString(R.string.activity_title_create);
            String editTile = getString(R.string.activity_title_edit);
            topAppBar.setTitle(mLocationId == 0L ? createTile : editTile);
        }

        mGpsFab = findViewById(R.id.fab_gps);
        mGpsFab.setOnClickListener(v -> onGpsFabClicked());

        mOkFab = findViewById(R.id.fab_ok);
        mOkFab.setOnClickListener(v -> onOkFabClicked());

        if (savedInstanceState == null && mLocationId == 0L) {
            setOkFabEnabled(false);
        }

        mFragment = (CreateEditFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_CREATE_EDIT);
        if (mFragment == null) {
            Bundle args = new Bundle();
            args.putLong(CreateEditFragment.BUNDLE_KEY_LOCATION_ID, mLocationId);

            mFragment = new CreateEditFragment();
            mFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_CREATE_EDIT)
                    .commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLocation = savedInstanceState.getParcelable(STATE_LOCATION);
        mLocationName = savedInstanceState.getString(STATE_LOCATION_NAME, "");
        mLocationDate = savedInstanceState.getString(STATE_LOCATION_DATE, "");
        mLocationLat = savedInstanceState.getDouble(STATE_LOCATION_LAT, 0.0);
        mLocationLng = savedInstanceState.getDouble(STATE_LOCATION_LNG, 0.0);
        mLocationDescription = savedInstanceState.getString(STATE_LOCATION_DESCRIPTION, "");
        mLocationPersonNames = savedInstanceState.getStringArrayList(STATE_LOCATION_PERSONS);
        mUseGpsLocation = savedInstanceState.getBoolean(STATE_USE_GPS_LOCATION);
        mRequestedPermissions = savedInstanceState.getBoolean(STATE_REQUESTED_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocationId != 0L && mLocation == null) {
            mPresenter.getLocation(mLocationId).observe(this, (location) -> {
                mLocation = location;
                if (location != null) {
                    mLocationName = location.getName();
                    mLocationDate = DateUtils.toUiFormat(location.getDate());
                    mLocationLat = location.getLatitude();
                    mLocationLng = location.getLongitude();
                    mLocationDescription = location.getDescription();
                    mPresenter.getLocationPersons(mLocationId).observe(this, (locationPersons) ->
                            mLocationPersonNames = toPersonNameList(locationPersons));
                    updateFragment();
                }
            });
        }

        mPresenter.addObserver(this);
        mPresenter.onResume();

        if (mUseGpsLocation) {
            mPresenter.requestGpsLocationUpdates();
            requestGpsPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mUseGpsLocation) {
            mPresenter.removeGpsLocationUpdates();
        }

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_LOCATION, mLocation);
        outState.putString(STATE_LOCATION_NAME, mLocationName);
        outState.putString(STATE_LOCATION_DATE, mLocationDate);
        outState.putDouble(STATE_LOCATION_LAT, mLocationLat);
        outState.putDouble(STATE_LOCATION_LNG, mLocationLng);
        outState.putString(STATE_LOCATION_DESCRIPTION, mLocationDescription);
        outState.putStringArrayList(STATE_LOCATION_PERSONS, mLocationPersonNames);
        outState.putBoolean(STATE_USE_GPS_LOCATION, mUseGpsLocation);
        outState.putBoolean(STATE_REQUESTED_PERMISSIONS, mRequestedPermissions);
    }

    private void updateFragment() {
        mFragment.setLatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void onGpsFabClicked() {
        mPresenter.requestGpsLocationUpdates();
        requestGpsPermissions();
    }

    private void setGpsFabActive(boolean active) {
        int resourceId = active ? R.drawable.ic_gps_fixed : R.drawable.ic_gps;
        mGpsFab.setImageResource(resourceId);
    }

    private void onOkFabClicked() {
        if (mUseGpsLocation) {
            LatLng latLng = mPresenter.getGpsLocation();
            saveLocationLatLngChanges(latLng.lat, latLng.lng);
        }
        showCreateEditDialog();
    }

    private void setOkFabEnabled(boolean enabled) {
        mOkFab.setEnabled(enabled);
    }

    // --- Fragment callback methods ---

    public void onMapLocationClicked(double lat, double lng) {
        mPresenter.removeGpsLocationUpdates();
        mUseGpsLocation = false;
        setGpsFabActive(false);
        setOkFabEnabled(true);
        saveLocationLatLngChanges(lat, lng);
    }

    // --- Presenter callback methods ---

    @Override
    public void onGpsLocationChanged(LatLng latLng) {
        mFragment.updateLatLng(latLng.lat, latLng.lng, !mUseGpsLocation);
        setGpsFabActive(true);
        setOkFabEnabled(true);
        mUseGpsLocation = true;
    }

    // --- Dialog methods ---

    private void showCreateEditDialog() {
        mPresenter.getPersons().observe(this, (persons -> {
            List<String> personNames = toPersonNameList(persons);

            CreateEditDialogFragment fragment;
            if (mLocationId == 0L) {
                fragment = CreateEditDialogFragment.newCreateInstance(mLocationName, mLocationDate,
                        mLocationDescription, mLocationPersonNames, personNames);
            } else {
                fragment = CreateEditDialogFragment.newEditInstance(mLocationName, mLocationDate,
                        mLocationDescription, mLocationPersonNames, personNames);
            }
            fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_CREATE_EDIT);
        }));
    }

    @Override
    public void onCreateEditDialogOk(String name, String date, String description,
            ArrayList<String> personNames) {
        saveLocationChanges(name, date, description, personNames);

        Location location = mLocation.copy();
        location.setName(mLocationName);
        location.setDate(DateUtils.fromUiFormat(mLocationDate));
        location.setLatitude(mLocationLat);
        location.setLongitude(mLocationLng);
        location.setDescription(mLocationDescription);
        ArrayList<Person> locationPersons = toPersonList(mLocationPersonNames);

        if (mLocationId == 0L) {
            mPresenter.createLocation(location, locationPersons);
            finish();
        } else {
            mPresenter.updateLocation(location, locationPersons);
            finish();
        }
    }

    @Override
    public void onCreateEditDialogCancel(String name, String date, String description,
            ArrayList<String> personNames) {
        saveLocationChanges(name, date, description, personNames);
    }

    // --- GPS permission methods ---

    private void requestGpsPermissions() {
        if (mRequestedPermissions) {
            return;
        }

        if (!PermissionsHelper.hasGpsPermissions(this)) {
            requestPermissions(PermissionsHelper.GPS_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        mRequestedPermissions = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.message_gps_permissions, Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    // --- Helper methods ---

    private void saveLocationChanges(String name, String date, String description,
            ArrayList<String> personNames) {
        mLocationName = name;
        mLocationDate = date;
        mLocationDescription = description;
        mLocationPersonNames = personNames;
    }

    private void saveLocationLatLngChanges(double lat, double lng) {
        mLocationLat = lat;
        mLocationLng = lng;
    }

    private static ArrayList<String> toPersonNameList(List<Person> persons) {
        ArrayList<String> personNames = new ArrayList<>();
        for (Person person : persons) {
            String personName;
            if (!person.getFirstName().isEmpty() && !person.getLastName().isEmpty()) {
                personName = person.getFirstName() + " " + person.getLastName();
            } else {
                personName = person.getFirstName() + person.getLastName();
            }
            personNames.add(personName);
        }
        return personNames;
    }

    private static ArrayList<Person> toPersonList(List<String> personNames) {
        if (personNames == null) {
            return null;
        }
        ArrayList<Person> persons = new ArrayList<>();
        for (String personName : personNames) {
            String[] names = personName.split(" +", 2);
            String firstName = names.length > 1 ? names[0] : "";
            String lastName = names.length > 1 ? names[1] : names[0];
            if (!firstName.isEmpty() || !lastName.isEmpty()) {
                persons.add(new Person(0, firstName, lastName));
            }
        }
        return persons;
    }

}
